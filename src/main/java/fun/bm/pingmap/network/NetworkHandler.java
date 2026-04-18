package fun.bm.pingmap.network;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.network.packet.PingC2SPacket;
import fun.bm.pingmap.network.packet.PingS2CPacket;
import fun.bm.pingmap.network.packet.SyncAllPingsS2CPacket;
import fun.bm.pingmap.network.packet.SyncConfigPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Pingmap.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(
                packetId++,
                PingC2SPacket.class,
                PingC2SPacket::encode,
                PingC2SPacket::decode,
                PingC2SPacket::handle
        );

        INSTANCE.registerMessage(
                packetId++,
                PingS2CPacket.class,
                PingS2CPacket::encode,
                PingS2CPacket::decode,
                PingS2CPacket::handle
        );

        INSTANCE.registerMessage(
                packetId++,
                SyncAllPingsS2CPacket.class,
                SyncAllPingsS2CPacket::encode,
                SyncAllPingsS2CPacket::decode,
                SyncAllPingsS2CPacket::handle
        );

        INSTANCE.registerMessage(
                packetId++,
                SyncConfigPacket.class,
                SyncConfigPacket::encode,
                SyncConfigPacket::decode,
                SyncConfigPacket::handle
        );
    }

    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAllPlayers(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
}
