package fun.bm.pingmap.api.data;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record PlayerModVersion(String name, UUID uuid, String version) {
    public PlayerModVersion(Player player, String version) {
        this(player.getDisplayName().getString(), player.getUUID(), version);
    }
}
