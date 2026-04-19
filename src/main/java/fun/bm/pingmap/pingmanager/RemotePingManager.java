package fun.bm.pingmap.pingmanager;

import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.network.MainNetworkHandler;
import fun.bm.pingmap.network.packet.c2s.PingC2SPacket;
import fun.bm.pingmap.pingmanager.ping.EntityPing;
import fun.bm.pingmap.pingmanager.ping.PointPing;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class RemotePingManager {

    public static void sendPing(Ping ping) {
        CompoundTag tag = ping.toNBT();
        int typeOrdinal = ping.getType().ordinal();
        PingC2SPacket packet = new PingC2SPacket(tag, typeOrdinal);
        MainNetworkHandler.sendToServer(packet);
    }

    public static void sendPointPing(double x, double y, double z) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.level == null) {
            return;
        }

        LocalPingManager manager = LocalPingManager.get(minecraft);
        if (manager == null) {
            return;
        }

        String dimension = minecraft.level.dimension().location().toString();
        UUID generatorId = player.getUUID();

        PointPing ping = manager.addPointPing(x, y, z, dimension, generatorId);
        sendPing(ping);
    }

    public static void sendEntityPing(Entity target) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.level == null || target == null) {
            return;
        }

        LocalPingManager manager = LocalPingManager.get(minecraft);
        if (manager == null) {
            return;
        }

        String dimension = minecraft.level.dimension().location().toString();
        UUID generatorId = player.getUUID();

        EntityPing ping = manager.addEntityPing(target, dimension, generatorId, PingType.Enemy);
        sendPing(ping);
    }
}
