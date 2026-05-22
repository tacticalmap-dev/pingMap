package fun.bm.pingmap.network.packet.c2s;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.enums.SyncType;
import fun.bm.pingmap.network.MainNetworkHandler;
import fun.bm.pingmap.network.packet.s2c.SyncSinglePingS2CPacket;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PingC2SPacket {
    private final CompoundTag pingData;

    public PingC2SPacket(CompoundTag pingData) {
        this.pingData = pingData;
    }

    public static void encode(PingC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.pingData);
    }

    public static PingC2SPacket decode(FriendlyByteBuf buf) {
        return new PingC2SPacket(buf.readNbt());
    }

    public static void handle(PingC2SPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null && sender.getServer() != null) {
                ServerPingManager serverManager = ServerPingManager.get(sender.getServer());
                if (serverManager != null && packet.pingData != null) {
                    PingType pingType = PingType.fromOrdinal(packet.pingData.getByte("type"));
                    long currentTimestamp = serverManager.generateUniqueTimestamp();
                    packet.pingData.putLong("timestamp", currentTimestamp);
                    packet.pingData.putInt("expireAfter", CommonConfig.getPingLifetimeSeconds(pingType));
                    serverManager.addPing(packet.pingData, sender.getServer());
                    Pingmap.LOGGER.debug("Received ping data: {}", packet.pingData);
                }

                SyncSinglePingS2CPacket broadcastPacket = new SyncSinglePingS2CPacket(packet.pingData, SyncType.ADD);
                sender.getServer().getPlayerList().getPlayers().forEach(player -> {
                    if (CommonConfig.ONLY_SEND_PINGS_TO_TEAMMATES.get()) {
                        Team team = sender.getTeam();
                        if (team == null || !team.getPlayers().contains(player.getScoreboardName())) {
                            return;
                        }
                    }
                    if (player != sender) {
                        MainNetworkHandler.sendToPlayer(broadcastPacket, player);
                        Pingmap.LOGGER.debug("Sent ping to player: {}", player.getDisplayName().getString());
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}
