package fun.bm.pingmap.api.data;

import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class PlayerModVersion {
    public PlayerModVersion(String name, UUID uuid, String version) {
        this.name = name;
        this.uuid = uuid;
        this.version = version;
    }

    public PlayerModVersion(Player player, String version) {
        this(player.getDisplayName().getString(), player.getUUID(), version);
    }

    public final String name;
    public final UUID uuid;
    public final String version;
}
