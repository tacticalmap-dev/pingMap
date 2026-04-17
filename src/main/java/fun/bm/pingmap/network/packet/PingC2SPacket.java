package fun.bm.pingmap.network.packet;

import fun.bm.pingmap.Pingmap;
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
                if (serverManager != null && packet.pingData != null) {
                    long currentTimestamp = serverManager.generateUniqueTimestamp();
                    packet.pingData.putLong("timestamp", currentTimestamp);
                    serverManager.addPing(packet.pingData, packet.typeOrdinal, sender.getServer());
                }

                PingS2CPacket broadcastPacket = new PingS2CPacket(packet.pingData, packet.typeOrdinal);
                sender.getServer().getPlayerList().getPlayers().forEach(player -> {
                    if (player != sender) {
                        fun.bm.pingmap.network.NetworkHandler.sendToPlayer(broadcastPacket, player);
                        Pingmap.LOGGER.debug("Sent ping to player: {}", player.getDisplayName().getString());
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}
