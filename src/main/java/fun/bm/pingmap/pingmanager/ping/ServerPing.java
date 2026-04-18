package fun.bm.pingmap.pingmanager.ping;

import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class ServerPing implements Ping {
    private String name;
    private final String dimension;
    private int color;
    private final double x;
    private final double y;
    private final double z;
    private final long timestamp;
    private final int expireAfter;
    private boolean showDistance;

    public ServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance) {
        this(name, dimension, x, y, z, color, showDistance, System.currentTimeMillis(), CommonConfig.getPingLifetimeSeconds(PingType.Server));
    }

    public ServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance, long timestamp, int expireAfter) {
        this.name = name;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.showDistance = showDistance;
        this.timestamp = timestamp;
        this.expireAfter = expireAfter;
    }

    public ServerPing() {
        this.dimension = null;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.timestamp = 0;
        this.expireAfter = -1;
    }

    public boolean expired() {
        if (expireAfter < 0) {
            return false;
        }
        return System.currentTimeMillis() - timestamp > expireAfter * 1000L;
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
        tag.putLong("timestamp", timestamp);
        tag.putInt("expireAfter", expireAfter);
        tag.putByte("type", (byte) PingType.Server.ordinal());
        return tag;
    }

    public Ping fromNBT(CompoundTag tag) {
        long readTimestamp = tag.contains("timestamp") ? tag.getLong("timestamp") : System.currentTimeMillis();
        int readExpireAfter = tag.contains("expireAfter")
                ? tag.getInt("expireAfter")
                : CommonConfig.getPingLifetimeSeconds(PingType.Server);
        return new ServerPing(
                tag.getString("name"),
                tag.getString("dimension"),
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getDouble("z"),
                tag.getInt("color"),
                tag.getBoolean("showDistance"),
                readTimestamp,
                readExpireAfter
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

    public void setColor(int color) {
        this.color = color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShowDistance(boolean showDistance) {
        this.showDistance = showDistance;
    }
}
