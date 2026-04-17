package fun.bm.pingmap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PingManager {
    private static final String DATA_FILE = "pingmap_data.dat";
    private static PingManager instance;
    private final Cache<Long, Ping> pings = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS).build();

    public static synchronized PingManager get(Minecraft minecraft) {
        if (instance == null) {
            instance = new PingManager();
            instance.load(minecraft);
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    private void load(Minecraft minecraft) {
        if (minecraft.getSingleplayerServer() == null) {
            return;
        }

        File saveDir = minecraft.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toFile();

        if (saveDir == null || !saveDir.exists()) {
            return;
        }

        File dataFile = new File(saveDir, DATA_FILE);
        if (dataFile.exists()) {
            try {
                CompoundTag tag = NbtIo.read(dataFile);
                if (tag != null) {
                    ListTag listTag = tag.getList("pings", Tag.TAG_COMPOUND);
                    for (int i = 0; i < listTag.size(); i++) {
                        CompoundTag nbt = listTag.getCompound(i);
                        Ping ping = PingType.fromOrdinal(nbt.getByte("type")).newInstance().fromNBT(nbt);
                        pings.put(ping.getTimestamp(), ping);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void save(Minecraft minecraft) {
        if (minecraft.getSingleplayerServer() == null) {
            return;
        }

        File saveDir = minecraft.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toFile();

        if (saveDir == null || !saveDir.exists()) {
            return;
        }

        try {
            File dataFile = new File(saveDir, DATA_FILE);
            CompoundTag tag = new CompoundTag();
            ListTag listTag = new ListTag();
            for (Ping ping : pings.asMap().values()) {
                listTag.add(ping.toNBT());
            }
            tag.put("pings", listTag);
            NbtIo.write(tag, dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPointPing(double x, double y, double z, String dimension, UUID generatorId) {
        Minecraft minecraft = Minecraft.getInstance();
        PingType type = PingType.Point;
        if (type.getMaxPings() > 0) {
            int count = 0;
            for (Ping ping : pings.asMap().values()) {
                if (ping.getDimension().equals(dimension) && ping.getGeneratorId().equals(generatorId) && ping.getType() == type) {
                    count++;
                }
            }
            while (count >= type.getMaxPings()) {
                for (Ping ping : pings.asMap().values()) {
                    if (ping.getDimension().equals(dimension) && ping.getGeneratorId().equals(generatorId) && ping.getType() == type) {
                        pings.invalidate(ping.getTimestamp());
                        count--;
                        break;
                    }
                }
            }
        }
        long timestamp = System.currentTimeMillis();
        pings.put(timestamp, new PointPing(x, y, z, generatorId, dimension, timestamp, 30));
        save(minecraft);
    }

    public void addEntityPing(Entity entity, String dimension, UUID generatorId, PingType type) {
        Minecraft minecraft = Minecraft.getInstance();
        if (type.getMaxPings() > 0) {
            int count = 0;
            for (Ping ping : pings.asMap().values()) {
                if (ping.getDimension().equals(dimension) && ping.getGeneratorId().equals(generatorId) && ping.getType() == type) {
                    count++;
                }
            }
            while (count >= type.getMaxPings()) {
                for (Ping ping : pings.asMap().values()) {
                    if (ping.getDimension().equals(dimension) && ping.getGeneratorId().equals(generatorId) && ping.getType() == type) {
                        pings.invalidate(ping.getTimestamp());
                        count--;
                        break;
                    }
                }
            }
        }
        long timestamp = System.currentTimeMillis();
        pings.put(timestamp, new EntityPing(entity.getUUID(), timestamp, dimension, generatorId, 10));
        save(minecraft);
    }

    public void cancelPing(Ping ping) {
        Minecraft minecraft = Minecraft.getInstance();
        pings.invalidate(ping.getTimestamp());
        save(minecraft);
    }

    public Collection<Ping> getPings() {
        return pings.asMap().values();
    }

    public List<Ping> getPingsForDimension(String dimension) {
        List<Ping> result = new ArrayList<>();
        for (Ping ping : pings.asMap().values()) {
            if (ping.getDimension().equals(dimension)) {
                result.add(ping);
            }
        }
        return result;
    }

    public enum PingType {
        Point(PointPing.class, "●", 0xFFFFFF00, true, Font.DisplayMode.SEE_THROUGH, 1),
        Enemy(EntityPing.class, "●", 0xFFFF0000, true, Font.DisplayMode.SEE_THROUGH, -1),
        Friendly(EntityPing.class, "●", 0x8000FFFF, false, Font.DisplayMode.NORMAL, -1),
        Server(ServerPing.class, null, null, null, Font.DisplayMode.SEE_THROUGH, -1);

        private final Class<? extends Ping> origin;
        private final String icon;
        private final Integer color;
        private final Font.DisplayMode displayMode;
        private final int maxPings;
        private final Boolean showDistance;

        PingType(Class<? extends Ping> clazz, String icon, Integer color, Boolean showDistance, Font.DisplayMode displayMode, int maxPings) {
            this.origin = clazz;
            this.icon = icon;
            this.color = color;
            this.showDistance = showDistance;
            this.displayMode = displayMode;
            this.maxPings = maxPings;
        }

        public static PingType fromOrdinal(int ordinal) {
            for (PingType type : values()) {
                if (type.ordinal() == ordinal) {
                    return type;
                }
            }
            return null;
        }

        @NotNull
        public Ping newInstance() {
            try {
                return this.origin.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getIcon() {
            return icon;
        }

        public int getColor() {
            return color;
        }

        public Font.DisplayMode getDisplayMode() {
            return displayMode;
        }

        public int getMaxPings() {
            return maxPings == -1 ? Integer.MAX_VALUE : maxPings;
        }

        public boolean showDistance() {
            return showDistance;
        }
    }

    public interface Ping {
        boolean expired();

        CompoundTag toNBT();

        Ping fromNBT(CompoundTag tag);

        double getX();

        double getY();

        double getZ();

        long getTimestamp();

        UUID getGeneratorId();

        String getDimension();

        PingType getType();

        default String getIcon() {
            return getType().getIcon();
        }

        default int getColor() {
            return getType().getColor();
        }

        default boolean showDistance() {
            return getType().showDistance();
        }
    }

    public static class PointPing implements Ping {
        private final double x;
        private final double y;
        private final double z;
        private final long timestamp;
        private final UUID generatorId;
        private final String dimension;
        private final int expireAfter;

        public PointPing(double x, double y, double z, UUID generatorId, String dimension, long timestamp, int expireAfter) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.generatorId = generatorId;
            this.dimension = dimension;
            this.timestamp = timestamp;
            this.expireAfter = expireAfter;
        }

        public PointPing() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.timestamp = 0;
            this.generatorId = null;
            this.dimension = null;
            this.expireAfter = 0;
        }

        public boolean expired() {
            return expireAfter == -1 || System.currentTimeMillis() - timestamp > expireAfter * 1000L;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", x);
            tag.putDouble("y", y);
            tag.putDouble("z", z);
            tag.putUUID("generatorId", generatorId);
            tag.putString("dimension", dimension);
            tag.putLong("timestamp", timestamp);
            tag.putInt("expireAfter", expireAfter);
            tag.putByte("type", (byte) PingType.Point.ordinal());
            return tag;
        }

        public PointPing fromNBT(CompoundTag tag) {
            return new PointPing(
                    tag.getDouble("x"),
                    tag.getDouble("y"),
                    tag.getDouble("z"),
                    tag.getUUID("generatorId"),
                    tag.getString("dimension"),
                    tag.getLong("timestamp"),
                    tag.getInt("expireAfter")
            );
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public UUID getGeneratorId() {
            return generatorId;
        }

        public String getDimension() {
            return dimension;
        }

        public PingType getType() {
            return PingType.Point;
        }
    }

    public static class EntityPing implements Ping {
        private final UUID entityId;
        private final long timestamp;
        private final String dimension;
        private final UUID generatorId;
        private final int expireAfter;
        private Entity cachedEntity;
        private long lastCheckTime;

        public EntityPing(UUID entityId, long timestamp, String dimension, UUID generatorId, int expireAfter) {
            this.entityId = entityId;
            this.timestamp = timestamp;
            this.dimension = dimension;
            this.generatorId = generatorId;
            this.cachedEntity = null;
            this.lastCheckTime = 0;
            this.expireAfter = expireAfter;
        }

        public EntityPing() {
            this.entityId = null;
            this.timestamp = 0;
            this.dimension = null;
            this.generatorId = null;
            this.cachedEntity = null;
            this.lastCheckTime = 0;
            this.expireAfter = 0;
        }

        public boolean expired() {
            return expireAfter == -1 || System.currentTimeMillis() - timestamp > expireAfter * 1000L;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("entityId", entityId);
            tag.putLong("timestamp", timestamp);
            tag.putString("dimension", dimension);
            tag.putUUID("generatorId", generatorId);
            tag.putInt("expireAfter", expireAfter);
            tag.putByte("type", (byte) PingType.Enemy.ordinal());
            return tag;
        }

        public EntityPing fromNBT(CompoundTag tag) {
            return new EntityPing(
                    tag.getUUID("entityId"),
                    tag.getLong("timestamp"),
                    tag.getString("dimension"),
                    tag.getUUID("generatorId"),
                    tag.getInt("expireAfter")
            );
        }

        private Entity getEntity() {
            long currentTime = System.currentTimeMillis();
            if (cachedEntity == null || currentTime - lastCheckTime > 1000) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level != null && entityId != null) {
                    for (Entity entity : minecraft.level.entitiesForRendering()) {
                        if (entity.getUUID().equals(entityId)) {
                            cachedEntity = entity;
                            lastCheckTime = currentTime;
                            return entity;
                        }
                    }
                }
                cachedEntity = null;
            }
            return cachedEntity;
        }

        public double getX() {
            Entity entity = getEntity();
            return entity != null ? entity.getX() : Double.MAX_VALUE;
        }

        public double getY() {
            Entity entity = getEntity();
            return entity != null ? entity.getY() : Double.MAX_VALUE;
        }

        public double getZ() {
            Entity entity = getEntity();
            return entity != null ? entity.getZ() : Double.MAX_VALUE;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public UUID getGeneratorId() {
            return generatorId;
        }

        public String getDimension() {
            return dimension;
        }

        public PingType getType() {
            return PingType.Enemy;
        }
    }

    public static class ServerPing implements Ping {
        private String name;
        private final String dimension;
        private int color;
        private final double x;
        private final double y;
        private final double z;
        private boolean showDistance;

        public ServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance) {
            this.name = name;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = color;
            this.showDistance = showDistance;
        }

        public ServerPing() {
            this.dimension = null;
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        public boolean expired() {
            return false;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", name);
            tag.putString("dimension", dimension);
            tag.putDouble("x", x);
            tag.putDouble("y", y);
            tag.putDouble("z", z);
            tag.putInt("color", color);
            tag.putBoolean("showDistance", showDistance);
            tag.putByte("type", (byte) PingType.Server.ordinal());
            return tag;
        }

        public Ping fromNBT(CompoundTag tag) {
            return new ServerPing(
                    tag.getString("name"),
                    tag.getString("dimension"),
                    tag.getDouble("x"),
                    tag.getDouble("y"),
                    tag.getDouble("z"),
                    tag.getInt("color"),
                    tag.getBoolean("showDistance")
            );
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        public long getTimestamp() {
            return -1;
        }

        public UUID getGeneratorId() {
            return null;
        }

        public String getDimension() {
            return dimension;
        }

        public PingType getType() {
            return PingType.Server;
        }

        public String getIcon() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public boolean showDistance() {
            return showDistance;
        }

        public void setColor(int color) { // for server api
            this.color = color;
        }

        public void setName(String name) { // for server api
            this.name = name;
        }

        public void setShowDistance(boolean showDistance) { // for server api
            this.showDistance = showDistance;
        }
    }
}
