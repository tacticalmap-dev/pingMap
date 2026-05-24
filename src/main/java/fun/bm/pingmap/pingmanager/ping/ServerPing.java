package fun.bm.pingmap.pingmanager.ping;

import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ServerPing extends BasePing {
    private String name;
    private ResourceLocation icon;
    private int color;
    private final double x;
    private final double y;
    private final double z;
    private boolean showDistance;

    public ServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance) {
        this(name, dimension, x, y, z, color, showDistance, System.currentTimeMillis(), CommonConfig.getPingLifetimeSeconds(PingType.Server));
    }

    public ServerPing(String name, String dimension, double x, double y, double z, int color, boolean showDistance, long timestamp, int expireAfter) {
        this(name, null, dimension, x, y, z, color, showDistance, timestamp, expireAfter);
    }

    public ServerPing(String name, ResourceLocation icon, String dimension, double x, double y, double z, int color, boolean showDistance, long timestamp, int expireAfter) {
        super(dimension, timestamp, expireAfter);
        this.name = name;
        this.icon = icon;
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.showDistance = showDistance;
    }

    public ServerPing() {
        super();
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = super.toNBT();
        tag.putString("name", name);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putInt("color", color);
        tag.putBoolean("showDistance", showDistance);
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

    public UUID getGeneratorId() {
        return null;
    }

    public PingType getType() {
        return PingType.Server;
    }

    public Object getIcon() {
        return icon == null ? name : icon;
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
