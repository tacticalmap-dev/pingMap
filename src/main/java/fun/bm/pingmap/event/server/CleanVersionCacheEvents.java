package fun.bm.pingmap.event.server;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.data.ServerPlayerModVersionCacheManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Pingmap.MODID)
public class CleanVersionCacheEvents {
    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayerModVersionCacheManager.INSTANCE.removePlayerModVersion(event.getEntity().getUUID());
    }
}
