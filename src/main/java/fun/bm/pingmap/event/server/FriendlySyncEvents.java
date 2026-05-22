package fun.bm.pingmap.event.server;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.enums.SyncType;
import fun.bm.pingmap.network.MainNetworkHandler;
import fun.bm.pingmap.network.packet.s2c.SyncSinglePingS2CPacket;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import fun.bm.pingmap.pingmanager.ping.EntityPing;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Pingmap.MODID)
public class FriendlySyncEvents {
    public static final Set<TeamUpdateHandler> cachedHandlers = new HashSet<>();
    private static final Map<String, EntityPing> cachedPing = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (CommonConfig.AUTO_ADD_FRIENDLY_PING.get()) {
            ServerPingManager manager = ServerPingManager.get(event.getEntity().getServer());
            EntityPing ping = manager.addEntityPing(event.getEntity(),
                    event.getEntity().level().dimension().location().toString(),
                    event.getEntity().getUUID(),
                    PingType.Friendly,
                    event.getEntity().getServer()
            );
            cachedPing.put(event.getEntity().getScoreboardName(), ping);
            manager.save(event.getEntity().getServer());

            for (ServerPlayer player : event.getEntity().getServer().getPlayerList().getPlayers()) {
                if (CommonConfig.ONLY_SEND_PINGS_TO_TEAMMATES.get()) {
                    Team team = player.getTeam();
                    if (team == null) continue;
                    if (!team.getPlayers().contains(event.getEntity().getScoreboardName())) continue;
                }

                Pingmap.LOGGER.debug("Sending friendly ping to player: {} -> {}", event.getEntity().getDisplayName().getString(), player.getDisplayName().getString());
                MainNetworkHandler.sendToPlayer(new SyncSinglePingS2CPacket(ping.toNBT(), SyncType.ADD), player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        Ping ping = cachedPing.get(event.getEntity().getScoreboardName());
        cachedPing.remove(event.getEntity().getScoreboardName());
        Team team = event.getEntity().getTeam();
        for (ServerPlayer p2 : event.getEntity().getServer().getPlayerList().getPlayers()) {
            if (team == null) continue;
            if (team.getPlayers().contains(p2.getScoreboardName())) {
                Pingmap.LOGGER.debug("Sending friendly ping to player: {} -> {}", event.getEntity().getDisplayName().getString(), p2.getDisplayName().getString());
                MainNetworkHandler.sendToPlayer(new SyncSinglePingS2CPacket(ping.toNBT(), SyncType.REMOVE), p2);
            }
        }
        ServerPingManager manager = ServerPingManager.get(event.getEntity().getServer());
        manager.cancelPing(ping);
        manager.save(event.getEntity().getServer());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (cachedHandlers.isEmpty()) return;
        for (TeamUpdateHandler handler : cachedHandlers) {
            Ping ping = cachedPing.get(handler.name);
            for (ServerPlayer p2 : event.getServer().getPlayerList().getPlayers()) {
                Team team = p2.getTeam();
                if (team != null && team.getPlayers().contains(handler.name)) {
                    Pingmap.LOGGER.debug("Sending friendly ping to player: {} -> {}", handler.name, p2.getDisplayName().getString());
                    MainNetworkHandler.sendToPlayer(new SyncSinglePingS2CPacket(ping.toNBT(), handler.getSyncType()), p2);
                    Ping ping2 = cachedPing.get(p2.getScoreboardName());
                    MainNetworkHandler.sendToPlayer(new SyncSinglePingS2CPacket(ping2.toNBT(), handler.getSyncType()), p2);
                }
            }
            cachedHandlers.remove(handler);
        }
    }

    public static class TeamUpdateHandler {
        final String name;
        final boolean add;

        public TeamUpdateHandler(String name, boolean add) {
            this.name = name;
            this.add = add;
        }

        public SyncType getSyncType() {
            return add ? SyncType.ADD : SyncType.REMOVE;
        }
    }
}
