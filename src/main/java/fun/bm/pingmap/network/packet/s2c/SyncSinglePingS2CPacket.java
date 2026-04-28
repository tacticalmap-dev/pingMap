package fun.bm.pingmap.network.packet.s2c;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.enums.PingType;
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
    private final int typeOrdinal;
    private final boolean add;

    public SyncSinglePingS2CPacket(CompoundTag pingData, int typeOrdinal, boolean add) {
        this.pingData = pingData;
        this.typeOrdinal = typeOrdinal;
        this.add = add;
    }

    public static void encode(SyncSinglePingS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.pingData);
        buf.writeInt(packet.typeOrdinal);
        buf.writeBoolean(packet.add);
    }

    public static SyncSinglePingS2CPacket decode(FriendlyByteBuf buf) {
        return new SyncSinglePingS2CPacket(buf.readNbt(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(SyncSinglePingS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPingManager manager = LocalPingManager.get(minecraft);
            if (manager != null && packet.pingData != null) {
                if (packet.add) {
                    manager.addPing(packet.pingData, packet.typeOrdinal);
                    Pingmap.LOGGER.debug("Received ping data: {}", packet.pingData);
                } else {
                    Ping ping = PingType.fromOrdinal(packet.typeOrdinal).newInstance().fromNBT(packet.pingData);
                    manager.cancelPing(ping.getTimestamp());
                    Pingmap.LOGGER.debug("Cancelled ping data: {}", packet.pingData);
                }
            }
        }));
        context.setPacketHandled(true);
    }
}
