package fun.bm.pingmap.pingmanager.ping;

import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class EntityPing extends BasePing {
    private final UUID entityId;
    private final UUID generatorId;
    private final PingType type;
    private Entity cachedEntity;
    private long lastCheckTime;

    public EntityPing(UUID entityId, long timestamp, String dimension, UUID generatorId, int expireAfter) {
        this(entityId, timestamp, dimension, generatorId, expireAfter, PingType.Enemy);
    }

    public EntityPing(UUID entityId, long timestamp, String dimension, UUID generatorId, int expireAfter, PingType type) {
        super(dimension, timestamp, expireAfter);
        this.entityId = entityId;
        this.generatorId = generatorId;
        this.type = type == null ? PingType.Enemy : type;
        this.cachedEntity = null;
        this.lastCheckTime = 0;
    }

    public EntityPing() {
        super();
        this.entityId = null;
        this.generatorId = null;
        this.type = PingType.Enemy;
        this.cachedEntity = null;
        this.lastCheckTime = 0;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = super.toNBT();
        tag.putUUID("entityId", entityId);
        tag.putUUID("generatorId", generatorId);
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

    public UUID getEntityId() {
        return entityId;
    }

    private Entity getEntity() {
        long currentTime = TimeUtil.getServerSideTimeMillis();
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


    public UUID getGeneratorId() {
        return generatorId;
    }

    public PingType getType() {
        return type;
    }
}
