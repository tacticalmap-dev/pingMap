package fun.bm.pingmap.event;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.network.NetworkHandler;
import fun.bm.pingmap.network.packet.SyncAllPingsS2CPacket;
import fun.bm.pingmap.network.packet.SyncConfigPacket;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Pingmap.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
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
                    NetworkHandler.sendToPlayer(syncPacket, player);
                }
            }

            SyncConfigPacket configPacket = new SyncConfigPacket(
                    CommonConfig.POINT_PING_LIFETIME_SECONDS.get(),
                    CommonConfig.ENEMY_PING_LIFETIME_SECONDS.get(),
                    CommonConfig.FRIENDLY_PING_LIFETIME_SECONDS.get()
            );
            NetworkHandler.sendToPlayer(configPacket, player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerPingManager.drop();
    }


}
