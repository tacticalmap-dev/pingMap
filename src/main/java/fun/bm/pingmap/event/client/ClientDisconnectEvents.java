package fun.bm.pingmap.event.client;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.boundary.AreaBoundaryManager;
import fun.bm.pingmap.pingmanager.LocalPingManager;
import fun.bm.pingmap.pingmanager.ServerPingManager;
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
        AreaBoundaryManager.clear();
        Pingmap.LOGGER.debug("Cleared all the server datas on disconnect");
    }
}
