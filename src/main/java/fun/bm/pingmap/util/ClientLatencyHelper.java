package fun.bm.pingmap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraftforge.fml.loading.FMLLoader;

public class ClientLatencyHelper {
    private static int cachedLatency = 0;
    private static long lastUpdateTime = 0;
    private static final long CACHE_DURATION = 5000;

    public static int getMyLatency() {
        if (FMLLoader.getDist().isDedicatedServer()) {
            return 0;
        }

        long currentTime = TimeUtil.getClientSideTimeMillis();

        if (currentTime - lastUpdateTime < CACHE_DURATION) {
            return cachedLatency;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return cachedLatency;
        }

        PlayerInfo info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        if (info != null) {
            cachedLatency = info.getLatency();
            lastUpdateTime = currentTime;
        }

        return cachedLatency;
    }

    public static void forceUpdate() {
        lastUpdateTime = 0;
    }
}
