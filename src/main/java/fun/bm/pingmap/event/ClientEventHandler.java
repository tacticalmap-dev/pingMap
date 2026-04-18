package fun.bm.pingmap.event;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.config.remote.RemoteCommonConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Pingmap.MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        RemoteCommonConfig.clearServerConfig();
        Pingmap.LOGGER.debug("Cleared server config on disconnect");
    }
}
