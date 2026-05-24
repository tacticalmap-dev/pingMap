package fun.bm.pingmap.pingmanager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.bm.pingmap.api.pingmanager.PingManager;
import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.data.ServerDataStoreManager;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.pingmanager.ping.EntityPing;
import fun.bm.pingmap.pingmanager.ping.PointPing;
import fun.bm.pingmap.pingmanager.ping.ServerPing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.util.*;

public class ServerPingManager implements PingManager {
    protected static ServerPingManager instance;
    protected long lastTimestamp = 0;
    protected final Cache<Long, Ping> pings = CacheBuilder.newBuilder().build();

    public static synchronized ServerPingManager get(MinecraftServer server) {
        if (instance == null) {
            instance = new ServerPingManager();
            if (server != null) {
                instance.load(server);
            }
        }
        return instance;
    }

    public static synchronized void drop() {
        if (instance != null) {
            MinecraftServer server = instance.getServer();
            if (server != null) {
                instance.save(server);
            }
        }
        instance = null;
    }

    protected MinecraftServer getServer() {
        return null;
    }

    protected void load(MinecraftServer server) {
        CompoundTag tag = ServerDataStoreManager.getNbt(server);
        if (tag == null) {
            return;
        }
        ListTag listTag = tag.getList("pings", Tag.TAG_COMPOUND);
        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag pingTag = listTag.getCompound(i);
            if (pingTag.getByte("type") == PingType.Friendly.ordinal()) {
                UUID uuid = pingTag.getUUID("entityId");
                boolean flag = false;
                for (UUID uuid1 : uuids) {
                    if (uuid.equals(uuid1)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    continue;
                }
            }
            Ping ping = addPing(pingTag);
            uuids.add(ping.getGeneratorId());
        }
    }

    public void save(MinecraftServer server) {
        if (server == null) {
            return;
        }

        File saveDir = server.getWorldPath(LevelResource.ROOT).toFile();

        if (saveDir == null || !saveDir.exists()) {
            return;
        }

        CompoundTag tag = ServerDataStoreManager.getNbtOrigin(server);

        if (tag == null) return;

        ListTag listTag = new ListTag();
        for (Ping ping : getPings()) {
            listTag.add(ping.toNBT());
        }
        tag.put("pings", listTag);

        ServerDataStoreManager.save();
    }

    public Ping addPing(CompoundTag tag) {
        return addPing(tag, PingType.fromOrdinal(tag.getByte("type")));
    }

    public Ping addPing(CompoundTag tag, MinecraftServer server) {
        return addPing(tag, PingType.fromOrdinal(tag.getByte("type")), server);
    }

    public Ping addPing(CompoundTag tag, PingType type) {
        Ping ping = type.newInstance().fromNBT(tag);
        cleanUpPings(ping.getDimension(), ping.getGeneratorId(), ping.getType());
        pings.put(ping.getTimestamp(), ping);
        return ping;
    }

    public Ping addPing(CompoundTag tag, PingType type, MinecraftServer server) {
        Ping ping = addPing(tag, type);
        save(server);
        return ping;
    }

    public synchronized long generateUniqueTimestamp() {
        long current = System.currentTimeMillis();
        if (current <= lastTimestamp) {
            current = lastTimestamp + 1;
        }
        lastTimestamp = current;
        return current;
    }

    protected void cleanUpPings(String dimension, UUID generatorId, PingType type) {
        if (type == null) {
            return;
        }
        if (type.getMaxPings() > 0) {
            int count = 0;
            for (Ping ping : getPings()) {
                if (Objects.equals(ping.getDimension(), dimension) && Objects.equals(ping.getGeneratorId(), generatorId) && ping.getType() == type) {
                    count++;
                }
            }
            for (Ping ping : getPings()) {
                if (ping.expired() || count >= type.getMaxPings() && Objects.equals(ping.getDimension(), dimension) && Objects.equals(ping.getGeneratorId(), generatorId) && ping.getType() == type) {
                    pings.invalidate(ping.getTimestamp());
                    count--;
                }
            }
        }
    }

    public void cancelPing(Ping ping) {
        MinecraftServer server = getServer();
        pings.invalidate(ping.getTimestamp());
        if (server != null) {
            save(server);
        }
    }

    public void cancelPing(long timestamp) {
        MinecraftServer server = getServer();
        pings.invalidate(timestamp);
        if (server != null) {
            save(server);
        }
    }

    public Collection<Ping> getPings() {
        return pings.asMap().values();
    }

    public List<Ping> getPingsForDimension(String dimension) {
        return getPingsForDimension(dimension, false);
    }

    public List<Ping> getPingsForDimension(String dimension, boolean checkExpired) {
        List<Ping> result = new ArrayList<>();
        for (Ping ping : getPings()) {
            if (checkExpired && ping.expired()) {
                pings.invalidate(ping);
                continue;
            }
            if (ping.getDimension().equals(dimension)) {
                result.add(ping);
            }
        }
        return result;
    }

    public PointPing addPointPing(double x, double y, double z, String dimension, UUID generatorId, MinecraftServer server) {
        PingType type = PingType.Point;
        cleanUpPings(dimension, generatorId, type);
        long timestamp = generateUniqueTimestamp();
        int expireAfter = CommonConfig.getPingLifetimeSeconds(type);
        PointPing ping = new PointPing(x, y, z, generatorId, dimension, timestamp, expireAfter);
        pings.put(timestamp, ping);
        save(server);
        return ping;
    }

    public EntityPing addEntityPing(Entity entity, String dimension, UUID generatorId, PingType type, MinecraftServer server) {
        cleanUpPings(dimension, generatorId, type);
        long timestamp = generateUniqueTimestamp();
        int expireAfter = CommonConfig.getPingLifetimeSeconds(type);
        EntityPing ping = new EntityPing(entity.getUUID(), timestamp, dimension, generatorId, expireAfter, type);
        pings.put(timestamp, ping);
        save(server);
        return ping;
    }

    public ServerPing addServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance, int expireAfter, MinecraftServer server) {
        long timestamp = generateUniqueTimestamp();
        ServerPing ping = new ServerPing(name, dimension, x, y, z, color, showDistance, timestamp, expireAfter);
        pings.put(timestamp, ping);
        save(server);
        return ping;
    }
}
