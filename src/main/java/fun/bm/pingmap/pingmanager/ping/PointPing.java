package fun.bm.pingmap.pingmanager.ping;

import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.enums.PingType;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class PointPing implements Ping {
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
