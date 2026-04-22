package fun.bm.pingmap.event;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import fun.bm.pingmap.pingmanager.ping.EntityPing;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = Pingmap.MODID)
public class ServerEventHandler {

    private static final Map<UUID, Set<String>> PLAYER_TEAM_CACHE = new HashMap<>();
    private static long lastCheckTime = 0;
    private static final long CHECK_INTERVAL = 100;

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerPingManager.drop();
        PLAYER_TEAM_CACHE.clear();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!CommonConfig.AUTO_ADD_FRIENDLY_PING.get()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        updateFriendlyPingsForPlayer(player, server);
        cachePlayerTeam(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!CommonConfig.AUTO_ADD_FRIENDLY_PING.get()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();
        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        updateFriendlyPingsForPlayer(player, server);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PLAYER_TEAM_CACHE.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!CommonConfig.AUTO_ADD_FRIENDLY_PING.get()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCheckTime < CHECK_INTERVAL) {
            return;
        }
        lastCheckTime = currentTime;

        MinecraftServer server = event.getServer();
        if (server == null) {
            return;
        }

        checkAndUpdateTeamChanges(server);
    }

    private static void cachePlayerTeam(ServerPlayer player) {
        Scoreboard scoreboard = player.getServer().getScoreboard();
        PlayerTeam team = scoreboard.getPlayersTeam(player.getName().getString());

        UUID playerUUID = player.getUUID();
        if (team != null) {
            Set<String> teamMembers = new HashSet<>(team.getPlayers());
            PLAYER_TEAM_CACHE.put(playerUUID, teamMembers);
        } else {
            PLAYER_TEAM_CACHE.remove(playerUUID);
        }
    }

    private static void checkAndUpdateTeamChanges(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        List<ServerPlayer> onlinePlayers = server.getPlayerList().getPlayers();

        for (ServerPlayer player : onlinePlayers) {
            UUID playerUUID = player.getUUID();
            PlayerTeam currentTeam = scoreboard.getPlayersTeam(player.getName().getString());

            Set<String> cachedTeam = PLAYER_TEAM_CACHE.getOrDefault(playerUUID, Collections.emptySet());
            Set<String> currentTeamMembers = currentTeam != null ? new HashSet<>(currentTeam.getPlayers()) : Collections.emptySet();

            if (!cachedTeam.equals(currentTeamMembers)) {
                PLAYER_TEAM_CACHE.put(playerUUID, currentTeamMembers);
                updateFriendlyPingsForPlayer(player, server);

                for (String memberName : currentTeamMembers) {
                    ServerPlayer teammate = server.getPlayerList().getPlayerByName(memberName);
                    if (teammate != null && teammate != player) {
                        updateFriendlyPingsForPlayer(teammate, server);
                    }
                }

                Pingmap.LOGGER.debug("Team change detected for player {}, updating friendly pings",
                        player.getName().getString());
            }
        }
    }

    private static void updateFriendlyPingsForPlayer(ServerPlayer player, MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayersTeam(player.getName().getString());

        if (team == null) {
            removeFriendlyPingsForPlayer(player, server);
            return;
        }

        Collection<String> teamMembers = team.getPlayers();
        ServerPingManager pingManager = ServerPingManager.get(server);

        if (pingManager == null) {
            return;
        }

        String playerDimension = player.level().dimension().location().toString();
        UUID playerUUID = player.getUUID();

        Set<UUID> expectedPings = new HashSet<>();

        for (String memberName : teamMembers) {
            if (memberName.equals(player.getName().getString())) {
                continue;
            }

            ServerPlayer teammate = server.getPlayerList().getPlayerByName(memberName);

            if (teammate != null && teammate != player) {
                String teammateDimension = teammate.level().dimension().location().toString();

                if (playerDimension.equals(teammateDimension)) {
                    pingManager.addEntityPing(teammate, playerDimension, playerUUID, PingType.Friendly, server);
                    expectedPings.add(teammate.getUUID());
                    Pingmap.LOGGER.debug("Added/updated friendly ping for teammate {} in dimension {} to player {}",
                            memberName, playerDimension, player.getName().getString());
                }
            }
        }

        cleanUpInvalidFriendlyPings(player, pingManager, playerDimension, expectedPings);
    }

    private static void removeFriendlyPingsForPlayer(ServerPlayer player, MinecraftServer server) {
        ServerPingManager pingManager = ServerPingManager.get(server);
        if (pingManager == null) {
            return;
        }

        String playerDimension = player.level().dimension().location().toString();
        UUID playerUUID = player.getUUID();

        Collection<Ping> allPings = pingManager.getPings();
        List<Ping> toRemove = allPings.stream()
                .filter(ping -> ping.getType() == PingType.Friendly)
                .filter(ping -> ping.getGeneratorId().equals(playerUUID))
                .filter(ping -> ping.getDimension().equals(playerDimension))
                .toList();

        for (Ping ping : toRemove) {
            pingManager.cancelPing(ping);
            Pingmap.LOGGER.debug("Removed friendly ping for player {} (left team)", player.getName().getString());
        }
    }

    private static void cleanUpInvalidFriendlyPings(ServerPlayer player, ServerPingManager pingManager,
                                                    String dimension, Set<UUID> expectedPings) {
        UUID playerUUID = player.getUUID();

        Collection<Ping> allPings = pingManager.getPings();
        List<Ping> toRemove = allPings.stream()
                .filter(ping -> ping.getType() == PingType.Friendly)
                .filter(ping -> ping.getGeneratorId().equals(playerUUID))
                .filter(ping -> ping.getDimension().equals(dimension))
                .filter(ping -> !expectedPings.contains(((EntityPing) ping).getEntityId()))
                .toList();

        for (Ping ping : toRemove) {
            pingManager.cancelPing(ping);
            Pingmap.LOGGER.debug("Cleaned up invalid friendly ping for player {}", player.getName().getString());
        }
    }
}
