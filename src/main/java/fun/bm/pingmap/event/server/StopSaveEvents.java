package fun.bm.pingmap.event.server;

import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.boundary.AreaBoundaryManager;
import fun.bm.pingmap.pingmanager.ServerPingManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Pingmap.MODID)
public class StopSaveEvents {

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MinecraftServer server = event.getServer();
        ServerPingManager.get(server).save(server);
        AreaBoundaryManager.save(server);
    }
}
