package fun.bm.pingmap.pingmanager.ping;

import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.config.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class EntityPing implements Ping {
    private final UUID entityId;
    private final long timestamp;
    private final String dimension;
    private final UUID generatorId;
    private final PingType type;
    private final int expireAfter;
    private Entity cachedEntity;
    private long lastCheckTime;

    public EntityPing(UUID entityId, long timestamp, String dimension, UUID generatorId, int expireAfter) {
        this(entityId, timestamp, dimension, generatorId, expireAfter, PingType.Enemy);
    }

    public EntityPing(UUID entityId, long timestamp, String dimension, UUID generatorId, int expireAfter, PingType type) {
        this.entityId = entityId;
        this.timestamp = timestamp;
        this.dimension = dimension;
        this.generatorId = generatorId;
        this.type = type == null ? PingType.Enemy : type;
        this.cachedEntity = null;
        this.lastCheckTime = 0;
        this.expireAfter = expireAfter;
    }

    public EntityPing() {
        this.entityId = null;
        this.timestamp = 0;
        this.dimension = null;
        this.generatorId = null;
        this.type = PingType.Enemy;
        this.cachedEntity = null;
        this.lastCheckTime = 0;
        this.expireAfter = 0;
    }

    public boolean expired() {
        if (expireAfter < 0) {
            return false;
        }
        return System.currentTimeMillis() - timestamp > expireAfter * 1000L;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("entityId", entityId);
        tag.putLong("timestamp", timestamp);
        tag.putString("dimension", dimension);
        tag.putUUID("generatorId", generatorId);
        tag.putInt("expireAfter", expireAfter);
        tag.putByte("type", (byte) type.ordinal());
        return tag;
    }

    public EntityPing fromNBT(CompoundTag tag) {
        PingType pingType = tag.contains("type") ? PingType.fromOrdinal(tag.getByte("type")) : PingType.Enemy;
        if (pingType == null) {
            pingType = PingType.Enemy;
        }
        int expire = tag.contains("expireAfter")
                ? tag.getInt("expireAfter")
                : CommonConfig.getPingLifetimeSeconds(pingType);
        return new EntityPing(
                tag.getUUID("entityId"),
                tag.getLong("timestamp"),
                tag.getString("dimension"),
                tag.getUUID("generatorId"),
                expire,
                pingType
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
        return type;
    }
}
