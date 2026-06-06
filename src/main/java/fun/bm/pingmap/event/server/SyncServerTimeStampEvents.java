package fun.bm.pingmap.event.server;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.network.MainNetworkHandler;
import fun.bm.pingmap.network.packet.s2c.SyncTimestampS2CPacket;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Pingmap.MODID)
public class SyncServerTimeStampEvents {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.getServer().getTickCount() % CommonConfig.RESYNC_SERVER_TIMESTAMP_INTERVAL.get() == 0) {
            MainNetworkHandler.sendToAllPlayers(new SyncTimestampS2CPacket());
        }
    }
}
