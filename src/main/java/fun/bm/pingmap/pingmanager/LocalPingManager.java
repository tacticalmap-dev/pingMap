package fun.bm.pingmap.pingmanager;

import fun.bm.pingmap.api.pingmanager.PingManager;
import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.pingmanager.ping.EntityPing;
import fun.bm.pingmap.pingmanager.ping.PointPing;
import fun.bm.pingmap.pingmanager.ping.ServerPing;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class LocalPingManager extends ServerPingManager implements PingManager {
    private static LocalPingManager instance;

    public static synchronized LocalPingManager get(Minecraft minecraft) {
        if (instance == null) {
            instance = new LocalPingManager();
            instance.load(minecraft);
        }
        return instance;
    }

    public static synchronized void drop() {
        if (instance != null) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                instance.save(minecraft);
            }
        }
        instance = null;
    }

    @Override
    protected MinecraftServer getServer() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null ? minecraft.getSingleplayerServer() : null;
    }

    private void load(Minecraft minecraft) {
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (server == null) {
            return;
        }
        super.load(server);
    }

    private void save(Minecraft minecraft) {
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (server == null) {
            return;
        }
        super.save(server);
    }

    public PointPing addPointPing(double x, double y, double z, String dimension, UUID generatorId, MinecraftServer server) {
        return addPointPing(x, y, z, dimension, generatorId);
    }

    public EntityPing addEntityPing(Entity entity, String dimension, UUID generatorId, PingType type, MinecraftServer server) {
        return addEntityPing(entity, dimension, generatorId, type);
    }

    public ServerPing addServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance, MinecraftServer server) {
        return addServerPing(name, dimension, x, y, z, color, showDistance);
    }

    public PointPing addPointPing(double x, double y, double z, String dimension, UUID generatorId) {
        Minecraft minecraft = Minecraft.getInstance();
        PingType type = PingType.Point;
        cleanUpPings(dimension, generatorId, type);
        long timestamp = generateUniqueTimestamp();
        PointPing ping = new PointPing(x, y, z, generatorId, dimension, timestamp, 30);
        pings.put(timestamp, ping);
        save(minecraft);
        return ping;
    }

    public EntityPing addEntityPing(Entity entity, String dimension, UUID generatorId, PingType type) {
        Minecraft minecraft = Minecraft.getInstance();
        cleanUpPings(dimension, generatorId, type);
        long timestamp = generateUniqueTimestamp();
        EntityPing ping = new EntityPing(entity.getUUID(), timestamp, dimension, generatorId, 10);
        pings.put(timestamp, ping);
        save(minecraft);
        return ping;
    }

    public ServerPing addServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance) {
        Minecraft minecraft = Minecraft.getInstance();
        ServerPing ping = new ServerPing(name, dimension, x, y, z, color, showDistance);
        long timestamp = generateUniqueTimestamp();
        pings.put(timestamp, ping);
        save(minecraft);
        return ping;
    }

    public Ping addPing(CompoundTag tag) {
        return addPing(tag, tag.getByte("type"));
    }
}
