package fun.bm.pingmap.event.client;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.boundary.AreaBoundaryManager;
import fun.bm.pingmap.data.ServerDataStoreManager;
import fun.bm.pingmap.pingmanager.LocalPingManager;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Pingmap.MODID, value = Dist.CLIENT)
public class ClientDisconnectEvents {
    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        LocalPingManager.drop();
        ServerPingManager.drop();
        Player player = event.getPlayer();
        if (player != null) {
            AreaBoundaryManager.clear(player.getServer());
        }
        ServerDataStoreManager.drop();
        Pingmap.LOGGER.debug("Cleared all the server datas on disconnect");
    }
}
