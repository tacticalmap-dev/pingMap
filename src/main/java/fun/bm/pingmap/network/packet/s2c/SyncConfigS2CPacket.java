package fun.bm.pingmap.network.packet.s2c;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.config.remote.RemoteCommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncConfigS2CPacket {
    private final CompoundTag configData;

    public SyncConfigS2CPacket(int pointPingLifetime, int enemyPingLifetime, int friendlyPingLifetime) {
        this.configData = new CompoundTag();
        this.configData.putInt("pointPingLifetime", pointPingLifetime);
        this.configData.putInt("enemyPingLifetime", enemyPingLifetime);
        this.configData.putInt("friendlyPingLifetime", friendlyPingLifetime);
    }

    public static void encode(SyncConfigS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.configData);
    }

    public static SyncConfigS2CPacket decode(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag == null) {
            tag = new CompoundTag();
        }
        return new SyncConfigS2CPacket(
                tag.getInt("pointPingLifetime"),
                tag.getInt("enemyPingLifetime"),
                tag.getInt("friendlyPingLifetime")
        );
    }

    public static void handle(SyncConfigS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                RemoteCommonConfig.setServerConfig(
                        packet.configData.getInt("pointPingLifetime"),
                        packet.configData.getInt("enemyPingLifetime"),
                        packet.configData.getInt("friendlyPingLifetime")
                );
                Pingmap.LOGGER.debug("Received server config: point={}, enemy={}, friendly={}",
                        packet.configData.getInt("pointPingLifetime"),
                        packet.configData.getInt("enemyPingLifetime"),
                        packet.configData.getInt("friendlyPingLifetime"));
            });
        });
        context.setPacketHandled(true);
    }
}
