package fun.bm.pingmap.network.packet.c2s;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.network.MainNetworkHandler;
import fun.bm.pingmap.network.packet.s2c.PingS2CPacket;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PingC2SPacket {
    private final CompoundTag pingData;
    private final int typeOrdinal;

    public PingC2SPacket(CompoundTag pingData, int typeOrdinal) {
        this.pingData = pingData;
        this.typeOrdinal = typeOrdinal;
    }

    public static void encode(PingC2SPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.pingData);
        buf.writeInt(packet.typeOrdinal);
    }

    public static PingC2SPacket decode(FriendlyByteBuf buf) {
        return new PingC2SPacket(buf.readNbt(), buf.readInt());
    }

    public static void handle(PingC2SPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null && sender.getServer() != null) {
                ServerPingManager serverManager = ServerPingManager.get(sender.getServer());
                PingType pingType = PingType.fromOrdinal(packet.typeOrdinal);
                if (serverManager != null && packet.pingData != null && pingType != null) {
                    long currentTimestamp = serverManager.generateUniqueTimestamp();
                    packet.pingData.putLong("timestamp", currentTimestamp);
                    packet.pingData.putByte("type", (byte) pingType.ordinal());
                    packet.pingData.putInt("expireAfter", CommonConfig.getPingLifetimeSeconds(pingType));
                    serverManager.addPing(packet.pingData, packet.typeOrdinal, sender.getServer());
                    Pingmap.LOGGER.debug("Received ping data: {}", packet.pingData);
                }

                PingS2CPacket broadcastPacket = new PingS2CPacket(packet.pingData, packet.typeOrdinal);
                sender.getServer().getPlayerList().getPlayers().forEach(player -> {
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
