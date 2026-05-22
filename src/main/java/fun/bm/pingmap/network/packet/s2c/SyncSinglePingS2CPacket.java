package fun.bm.pingmap.network.packet.s2c;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.enums.SyncType;
import fun.bm.pingmap.pingmanager.LocalPingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSinglePingS2CPacket {
    private final CompoundTag pingData;
    private final SyncType syncType;

    public SyncSinglePingS2CPacket(CompoundTag pingData, SyncType syncType) {
        this.pingData = pingData;
        this.syncType = syncType;
    }

    public static void encode(SyncSinglePingS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.pingData);
        buf.writeInt(packet.syncType.ordinal());
    }

    public static SyncSinglePingS2CPacket decode(FriendlyByteBuf buf) {
        return new SyncSinglePingS2CPacket(buf.readNbt(), SyncType.fromOrdinal(buf.readInt()));
    }

    public static void handle(SyncSinglePingS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPingManager manager = LocalPingManager.get(minecraft);
            if (manager != null && packet.pingData != null) {
                if (packet.syncType.equals(SyncType.ADD)) {
                    manager.addPing(packet.pingData);
                    Pingmap.LOGGER.debug("Received ping data: {}", packet.pingData);
                } else if (packet.syncType.equals(SyncType.REMOVE)) {
                    Ping ping = PingType.fromOrdinal(packet.pingData.getByte("type")).newInstance().fromNBT(packet.pingData);
                    manager.cancelPing(ping.getTimestamp());
                    Pingmap.LOGGER.debug("Cancelled ping data: {}", packet.pingData);
                } else if (packet.syncType.equals(SyncType.RESYNC)) {
                    manager.cancelPing(packet.pingData.getLong("timestamp"));
                    manager.addPing(packet.pingData);
                }
            }
        }));
        context.setPacketHandled(true);
    }
}
