package fun.bm.pingmap;

import com.mojang.logging.LogUtils;
import fun.bm.pingmap.config.PingmapConfig;
import fun.bm.pingmap.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import org.slf4j.Logger;

@Mod(Pingmap.MODID)
public class Pingmap {

    public static final String MODID = "pingmap";

    public static final Logger LOGGER = LogUtils.getLogger();

    public Pingmap() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PingmapConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }
}
