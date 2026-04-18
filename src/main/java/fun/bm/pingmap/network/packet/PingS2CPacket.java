package fun.bm.pingmap.network.packet;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.pingmanager.LocalPingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PingS2CPacket {
    private final CompoundTag pingData;
    private final int typeOrdinal;

    public PingS2CPacket(CompoundTag pingData, int typeOrdinal) {
        this.pingData = pingData;
        this.typeOrdinal = typeOrdinal;
    }

    public static void encode(PingS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.pingData);
        buf.writeInt(packet.typeOrdinal);
    }

    public static PingS2CPacket decode(FriendlyByteBuf buf) {
        return new PingS2CPacket(buf.readNbt(), buf.readInt());
    }

    public static void handle(PingS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPingManager manager = LocalPingManager.get(minecraft);
            if (manager != null && packet.pingData != null) {
                manager.addPing(packet.pingData, packet.typeOrdinal);
                Pingmap.LOGGER.debug("Received ping data: {}", packet.pingData);
            }
        }));
        context.setPacketHandled(true);
    }
}
