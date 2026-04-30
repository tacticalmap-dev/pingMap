package fun.bm.pingmap.api.data;

import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.UUID;

public interface PlayerModVersionCacheManager {
    void addPlayerModVersion(PlayerModVersion playerModVersion);

    void addPlayerModVersion(String playerName, UUID playerId, String modVersion);

    void addPlayerModVersion(Player player, String modVersion);

    PlayerModVersion getPlayerModVersion(UUID playerId);

    PlayerModVersion getPlayerModVersion(String playerName);

    PlayerModVersion getPlayerModVersion(Player player);

    void removePlayerModVersion(UUID playerId);

    void removePlayerModVersion(String playerName);

    void removePlayerModVersion(Player player);

    boolean containsPlayerModVersion(UUID playerId);

    boolean containsPlayerModVersion(String playerName);

    boolean containsPlayerModVersion(Player player);

    Collection<PlayerModVersion> getAllPlayerModVersions();

    void clear();

    int size();

    boolean isEmpty();
}
