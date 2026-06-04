package fun.bm.pingmap.pingmanager.ping;

import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.util.TimeUtil;
import net.minecraft.nbt.CompoundTag;

public abstract class BasePing implements Ping {
    private final String dimension;
    private final long timestamp;
    private final int expireAfter;

    public BasePing(String dimension, long timestamp, int expireAfter) {
        this.dimension = dimension;
        this.timestamp = timestamp;
        this.expireAfter = expireAfter;
    }

    public BasePing() {
        this(null, 0, 0);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension);
        tag.putLong("timestamp", timestamp);
        tag.putInt("expireAfter", expireAfter);
        return tag;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDimension() {
        return dimension;
    }

    protected int getExpireAfter() {
        return expireAfter;
    }

    public boolean expired() {
        if (this.expireAfter < 0) {
            return false;
        }
        return TimeUtil.getServerSideTimeMillis() - timestamp > expireAfter * 1000L;
    }
}
