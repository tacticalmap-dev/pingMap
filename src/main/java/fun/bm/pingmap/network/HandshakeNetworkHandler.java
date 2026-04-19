package fun.bm.pingmap.network;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.network.packet.c2s.HandshakeC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class HandshakeNetworkHandler {
    private static final String VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(Pingmap.MODID, "handshake"),
            () -> VERSION,
            VERSION::equals,
            VERSION::equals
    );


    public static void register() {
        INSTANCE.registerMessage(
                0,
                HandshakeC2SPacket.class,
                HandshakeC2SPacket::encode,
                HandshakeC2SPacket::decode,
                HandshakeC2SPacket::handle
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
