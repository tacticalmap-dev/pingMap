package fun.bm.pingmap.network.packet;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.pingmanager.LocalPingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncAllPingsS2CPacket {
    private final List<CompoundTag> pingTags;
    private final List<Integer> typeOrdinals;

    public SyncAllPingsS2CPacket(List<CompoundTag> pingTags, List<Integer> typeOrdinals) {
        this.pingTags = pingTags;
        this.typeOrdinals = typeOrdinals;
    }

    public static void encode(SyncAllPingsS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.pingTags.size());
        for (int i = 0; i < packet.pingTags.size(); i++) {
            buf.writeNbt(packet.pingTags.get(i));
            buf.writeInt(packet.typeOrdinals.get(i));
        }
    }

    public static SyncAllPingsS2CPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<CompoundTag> pingTags = new ArrayList<>();
        List<Integer> typeOrdinals = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            pingTags.add(buf.readNbt());
            typeOrdinals.add(buf.readInt());
        }

        return new SyncAllPingsS2CPacket(pingTags, typeOrdinals);
    }

    public static void handle(SyncAllPingsS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft minecraft = Minecraft.getInstance();
                LocalPingManager manager = LocalPingManager.get(minecraft);
                if (manager != null) {
                    for (int i = 0; i < packet.pingTags.size(); i++) {
                        manager.addPing(packet.pingTags.get(i), packet.typeOrdinals.get(i));
                        Pingmap.LOGGER.debug("Received ping: {}", packet.pingTags.get(i));
                    }
                }
            });
        });
        context.setPacketHandled(true);
    }
}
