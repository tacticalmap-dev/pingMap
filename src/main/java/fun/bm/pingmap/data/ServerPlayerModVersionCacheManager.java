package fun.bm.pingmap.data;

import fun.bm.pingmap.api.data.PlayerModVersion;
import fun.bm.pingmap.api.data.PlayerModVersionCacheManager;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServerPlayerModVersionCacheManager implements PlayerModVersionCacheManager {
    public static final ServerPlayerModVersionCacheManager INSTANCE = new ServerPlayerModVersionCacheManager();
    private final Set<PlayerModVersion> playerModVersions = new HashSet<>();

    public void addPlayerModVersion(PlayerModVersion playerModVersion) {
        playerModVersions.add(playerModVersion);
    }

    public void addPlayerModVersion(String playerName, UUID playerId, String modVersion) {
        this.addPlayerModVersion(new PlayerModVersion(playerName, playerId, modVersion));
    }

    public void addPlayerModVersion(Player player, String modVersion) {
        this.addPlayerModVersion(player.getDisplayName().getString(), player.getUUID(), modVersion);
    }

    public PlayerModVersion getPlayerModVersion(UUID playerId) {
        for (PlayerModVersion playerModVersion : playerModVersions) {
            if (playerModVersion.uuid.equals(playerId)) {
                return playerModVersion;
            }
        }
        return null;
    }

    public PlayerModVersion getPlayerModVersion(String playerName) {
        for (PlayerModVersion playerModVersion : playerModVersions) {
            if (playerModVersion.name.equals(playerName)) {
                return playerModVersion;
            }
        }
        return null;
    }

    public PlayerModVersion getPlayerModVersion(Player player) {
        return this.getPlayerModVersion(player.getUUID());
    }

    public void removePlayerModVersion(UUID playerId) {
        for (PlayerModVersion playerModVersion : playerModVersions) {
            if (playerModVersion.uuid.equals(playerId)) {
                playerModVersions.remove(playerModVersion);
                return;
            }
        }
    }

    public void removePlayerModVersion(String playerName) {
        for (PlayerModVersion playerModVersion : playerModVersions) {
            if (playerModVersion.name.equals(playerName)) {
                playerModVersions.remove(playerModVersion);
                return;
            }
        }
    }

    public void removePlayerModVersion(Player player) {
        this.removePlayerModVersion(player.getUUID());
    }

    public boolean containsPlayerModVersion(UUID playerId) {
        for (PlayerModVersion playerModVersion : playerModVersions) {
            if (playerModVersion.uuid.equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPlayerModVersion(String playerName) {
        for (PlayerModVersion playerModVersion : playerModVersions) {
            if (playerModVersion.name.equals(playerName)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPlayerModVersion(Player player) {
        return containsPlayerModVersion(player.getUUID());
    }

    public Collection<PlayerModVersion> getAllPlayerModVersions() {
        return Set.copyOf(playerModVersions);
    }

    public void clear() {
        playerModVersions.clear();
    }

    public int size() {
        return playerModVersions.size();
    }

    public boolean isEmpty() {
        return playerModVersions.isEmpty();
    }
}
