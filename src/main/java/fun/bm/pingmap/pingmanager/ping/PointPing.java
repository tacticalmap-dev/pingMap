package fun.bm.pingmap.pingmanager.ping;

import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class PointPing extends BasePing {
    private final double x;
    private final double y;
    private final double z;
    private final UUID generatorId;

    public PointPing(double x, double y, double z, UUID generatorId, String dimension, long timestamp, int expireAfter) {
        super(dimension, timestamp, expireAfter);
        this.x = x;
        this.y = y;
        this.z = z;
        this.generatorId = generatorId;
    }

    public PointPing() {
        super();
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.generatorId = null;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = super.toNBT();
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putUUID("generatorId", generatorId);
        tag.putByte("type", (byte) PingType.Point.ordinal());
        return tag;
    }

    public PointPing fromNBT(CompoundTag tag) {
        int expire = tag.contains("expireAfter")
                ? tag.getInt("expireAfter")
                : CommonConfig.getPingLifetimeSeconds(PingType.Point);
        return new PointPing(
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.getUUID("generatorId"),
                tag.getString("dimension"),
                tag.getLong("timestamp"),
                expire
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

    public UUID getGeneratorId() {
        return generatorId;
    }

    public PingType getType() {
        return PingType.Point;
    }
}
