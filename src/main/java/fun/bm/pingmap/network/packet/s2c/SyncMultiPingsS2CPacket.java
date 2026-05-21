package fun.bm.pingmap.network.packet.s2c;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.enums.PingType;
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

public class SyncMultiPingsS2CPacket {
    private final List<CompoundTag> pingTags;
    private final List<PingType> typingTypes;

    public SyncMultiPingsS2CPacket(List<CompoundTag> pingTags, List<PingType> typingTypes) {
        this.pingTags = pingTags;
        this.typingTypes = typingTypes;
    }

    public static void encode(SyncMultiPingsS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.pingTags.size());
        for (int i = 0; i < packet.pingTags.size(); i++) {
            buf.writeNbt(packet.pingTags.get(i));
            buf.writeInt(packet.typingTypes.get(i).ordinal());
        }
    }

    public static SyncMultiPingsS2CPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        List<CompoundTag> pingTags = new ArrayList<>();
        List<PingType> pingTypes = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            pingTags.add(buf.readNbt());
            pingTypes.add(PingType.fromOrdinal(buf.readInt()));
        }

        return new SyncMultiPingsS2CPacket(pingTags, pingTypes);
    }

    public static void handle(SyncMultiPingsS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft minecraft = Minecraft.getInstance();
                LocalPingManager manager = LocalPingManager.get(minecraft);
                if (manager != null) {
                    for (int i = 0; i < packet.pingTags.size(); i++) {
                        manager.addPing(packet.pingTags.get(i), packet.typingTypes.get(i));
                        Pingmap.LOGGER.debug("Received ping: {}", packet.pingTags.get(i));
                    }
                }
            });
        });
        context.setPacketHandled(true);
    }
}
