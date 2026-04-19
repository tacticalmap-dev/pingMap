package fun.bm.pingmap.network.packet.c2s;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.network.MainNetworkHandler;
import fun.bm.pingmap.network.packet.s2c.SyncAllPingsS2CPacket;
import fun.bm.pingmap.network.packet.s2c.SyncConfigS2CPacket;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class HandshakeC2SPacket {
    private final String version;

    public HandshakeC2SPacket(String version) {
        this.version = version;
    }

    public static HandshakeC2SPacket decode(FriendlyByteBuf buf) {
        return new HandshakeC2SPacket(buf.readUtf());
    }

    public static void encode(HandshakeC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.version);
    }

    public static void handle(HandshakeC2SPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                Pingmap.LOGGER.warn("Received handshake packet from null player.");
            } else {
                if (!packet.version.equals(MainNetworkHandler.MAIN_PROTOCOL_VERSION)) {
                    try {
                        player.connection.disconnect(Component.translatable("game.connection.disconnect"));
                    } catch (Exception e) {
                        Pingmap.LOGGER.warn("Failed to disconnect client: ", e);
                    }
                } else {
                    Pingmap.LOGGER.debug("Player {} connect server with Ping Map version of {}", player.getDisplayName().getString(), packet.version);
                    ServerPingManager serverManager = ServerPingManager.get(player.getServer());
                    if (serverManager != null) {
                        List<CompoundTag> pingTags = new ArrayList<>();
                        List<Integer> typeOrdinals = new ArrayList<>();

                        serverManager.getPings().forEach(ping -> {
                            pingTags.add(ping.toNBT());
                            typeOrdinals.add(ping.getType().ordinal());
                        });

                        if (!pingTags.isEmpty()) {
                            SyncAllPingsS2CPacket syncPacket = new SyncAllPingsS2CPacket(pingTags, typeOrdinals);
                            MainNetworkHandler.sendToPlayer(syncPacket, player);
                        }
                    }

                    SyncConfigS2CPacket configPacket = new SyncConfigS2CPacket(
                            CommonConfig.POINT_PING_LIFETIME_SECONDS.get(),
                            CommonConfig.ENEMY_PING_LIFETIME_SECONDS.get(),
                            CommonConfig.FRIENDLY_PING_LIFETIME_SECONDS.get()
                    );
                    MainNetworkHandler.sendToPlayer(configPacket, player);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
