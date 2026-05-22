package fun.bm.pingmap.api.pingmanager.ping;

import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.enums.SyncType;
import fun.bm.pingmap.network.MainNetworkHandler;
import fun.bm.pingmap.network.packet.s2c.SyncSinglePingS2CPacket;
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

    default void resentPing() {
        MainNetworkHandler.sendToAllPlayers(new SyncSinglePingS2CPacket(toNBT(), SyncType.RESYNC));
    }
}
