package fun.bm.pingmap.pingmanager.pings;

import fun.bm.pingmap.enums.PingType;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

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
