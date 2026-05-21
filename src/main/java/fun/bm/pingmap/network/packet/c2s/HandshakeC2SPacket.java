package fun.bm.pingmap.network.packet.c2s;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.data.ServerPlayerModVersionCacheManager;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.network.MainNetworkHandler;
import fun.bm.pingmap.network.packet.s2c.SyncConfigS2CPacket;
import fun.bm.pingmap.network.packet.s2c.SyncMultiPingsS2CPacket;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
                ServerPlayerModVersionCacheManager.INSTANCE.addPlayerModVersion(player, packet.version);
                if (packet.version.equals(MainNetworkHandler.MAIN_PROTOCOL_VERSION)) {
                    Pingmap.LOGGER.info("Player {} connect server with Ping Map version of {}", player.getDisplayName().getString(), packet.version);
                } else {
                    Pingmap.LOGGER.warn("Player {} connect server with Ping Map version of {}, but expected version of {}", player.getDisplayName().getString(), packet.version, MainNetworkHandler.MAIN_PROTOCOL_VERSION);
                    player.sendSystemMessage(Component.literal("Ping Map: Mismatch version. Client: " + packet.version + ", Server: " + MainNetworkHandler.MAIN_PROTOCOL_VERSION));
                }
                ServerPingManager serverManager = ServerPingManager.get(player.getServer());
                if (serverManager != null) {
                    List<CompoundTag> pingTags = new ArrayList<>();
                    List<PingType> pingTypes = new ArrayList<>();

                    serverManager.getPings().forEach(ping -> {
                        if (ping.expired()) return;
                        if (CommonConfig.ONLY_SEND_PINGS_TO_TEAMMATES.get()) {
                            UUID generatorId = ping.getGeneratorId();
                            if (generatorId != null) {
                                Team team = player.getTeam();
                                if (team == null) return;
                                ServerPlayer p2 = player.getServer().getPlayerList().getPlayer(generatorId);
                                if (p2 == null) return;
                                if (!team.getPlayers().contains(p2.getScoreboardName())) return;
                            }
                        }
                        pingTags.add(ping.toNBT());
                        pingTypes.add(ping.getType());
                    });

                    if (!pingTags.isEmpty()) {
                        SyncMultiPingsS2CPacket syncPacket = new SyncMultiPingsS2CPacket(pingTags, pingTypes);
                        MainNetworkHandler.sendToPlayer(syncPacket, player);
                    }
                }

                SyncConfigS2CPacket configPacket = new SyncConfigS2CPacket(
                        CommonConfig.getPingLifetimeSeconds(PingType.Point),
                        CommonConfig.getPingLifetimeSeconds(PingType.Enemy),
                        CommonConfig.getPingLifetimeSeconds(PingType.Friendly)
                );
                MainNetworkHandler.sendToPlayer(configPacket, player);
            }
        });
        context.setPacketHandled(true);
    }
}
