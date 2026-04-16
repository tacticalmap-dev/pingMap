package fun.bm.pingmap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
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
        File saveDir = minecraft.getSingleplayerServer() != null
                ? minecraft.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile()
                : null;

        if (saveDir == null && minecraft.level != null) {
            String levelId = minecraft.getSingleplayerServer() != null
                    ? minecraft.getSingleplayerServer().getWorldData().getLevelName()
                    : "default";
            saveDir = minecraft.gameDirectory.toPath().resolve("saves").resolve(levelId).toFile();
        }

        if (saveDir == null || !saveDir.exists()) {
            return;
        }

        File dataFile = new File(saveDir, DATA_FILE);
        if (dataFile.exists()) {
            try {
                CompoundTag tag = net.minecraft.nbt.NbtIo.read(dataFile);
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
        File saveDir = null;

        if (minecraft.getSingleplayerServer() != null) {
            saveDir = minecraft.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
        } else if (minecraft.level != null) {
            String levelId = minecraft.hasSingleplayerServer()
                    ? minecraft.getSingleplayerServer().getWorldData().getLevelName()
                    : "default";
            saveDir = minecraft.gameDirectory.toPath().resolve("saves").resolve(levelId).toFile();
        }

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
            net.minecraft.nbt.NbtIo.write(tag, dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPointPing(double x, double y, double z, String dimension, UUID generatorId) {
        Minecraft minecraft = Minecraft.getInstance();
        for (Ping ping : pings.asMap().values()) {
            if (ping.getDimension().equals(dimension) && ping.getGeneratorId().equals(generatorId)) {
                pings.invalidate(ping.getTimestamp());
            }
        }
        long timestamp = System.currentTimeMillis();
        pings.put(timestamp, new PointPing(x, y, z, generatorId, dimension, timestamp));
        save(minecraft);
    }

    public void addEntityPing(Entity entity, String dimension, UUID generatorId) {
        Minecraft minecraft = Minecraft.getInstance();
        for (Ping ping : pings.asMap().values()) {
            if (ping.getDimension().equals(dimension) && ping.getGeneratorId().equals(generatorId)) {
                pings.invalidate(ping.getTimestamp());
            }
        }
        long timestamp = System.currentTimeMillis();
        pings.put(timestamp, new EntityPing(entity.getUUID(), timestamp, dimension, generatorId));
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
        Point(PointPing.class, "●", 0xFFFFFF00, Font.DisplayMode.SEE_THROUGH),
        Entity(EntityPing.class, "●", 0xFFFF0000, Font.DisplayMode.SEE_THROUGH);

        private final Class<? extends Ping> origin;
        private final String icon;
        private final int color;
        private final Font.DisplayMode displayMode;

        PingType(Class<? extends Ping> clazz, String icon, int color, Font.DisplayMode displayMode) {
            this.origin = clazz;
            this.icon = icon;
            this.color = color;
            this.displayMode = displayMode;
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
    }

    public interface Ping {
        CompoundTag toNBT();

        Ping fromNBT(CompoundTag tag);

        double getX();

        double getY();

        double getZ();

        long getTimestamp();

        UUID getGeneratorId();

        String getDimension();

        PingType getType();
    }

    public static class PointPing implements Ping {
        private final double x;
        private final double y;
        private final double z;
        private final long timestamp;
        private final UUID generatorId;
        private final String dimension;

        public PointPing(double x, double y, double z, UUID generatorId, String dimension, long timestamp) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.generatorId = generatorId;
            this.dimension = dimension;
            this.timestamp = timestamp;
        }

        public PointPing() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.timestamp = 0;
            this.generatorId = null;
            this.dimension = null;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", x);
            tag.putDouble("y", y);
            tag.putDouble("z", z);
            tag.putUUID("generatorId", generatorId);
            tag.putString("dimension", dimension);
            tag.putLong("timestamp", timestamp);
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
                    tag.getLong("timestamp")
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

        @Override
        public PingType getType() {
            return PingType.Point;
        }
    }

    public static class EntityPing implements Ping {
        private final UUID entityId;
        private final long timestamp;
        private final String dimension;
        private final UUID generatorId;
        private Entity cachedEntity;
        private long lastCheckTime;

        public EntityPing(UUID entityId, long timestamp, String dimension, UUID generatorId) {
            this.entityId = entityId;
            this.timestamp = timestamp;
            this.dimension = dimension;
            this.generatorId = generatorId;
            this.cachedEntity = null;
            this.lastCheckTime = 0;
        }

        public EntityPing() {
            this.entityId = null;
            this.timestamp = 0;
            this.dimension = null;
            this.generatorId = null;
            this.cachedEntity = null;
            this.lastCheckTime = 0;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("entityId", entityId);
            tag.putLong("timestamp", timestamp);
            tag.putString("dimension", dimension);
            tag.putUUID("generatorId", generatorId);
            tag.putByte("type", (byte) PingType.Entity.ordinal());
            return tag;
        }

        public EntityPing fromNBT(CompoundTag tag) {
            return new EntityPing(
                    tag.getUUID("entityId"),
                    tag.getLong("timestamp"),
                    tag.getString("dimension"),
                    tag.getUUID("generatorId")
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

        @Override
        public double getX() {
            Entity entity = getEntity();
            return entity != null ? entity.getX() : 0;
        }

        @Override
        public double getY() {
            Entity entity = getEntity();
            return entity != null ? entity.getY() : 0;
        }

        @Override
        public double getZ() {
            Entity entity = getEntity();
            return entity != null ? entity.getZ() : 0;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public UUID getGeneratorId() {
            return generatorId;
        }

        @Override
        public String getDimension() {
            return dimension;
        }

        @Override
        public PingType getType() {
            return PingType.Entity;
        }
    }
}
