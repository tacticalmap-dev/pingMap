package fun.bm.pingmap;

import com.mojang.logging.LogUtils;
import fun.bm.pingmap.config.local.ClientConfig;
import fun.bm.pingmap.config.local.CommonConfig;
import fun.bm.pingmap.network.HandshakeNetworkHandler;
import fun.bm.pingmap.network.MainNetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Pingmap.MODID)
public class Pingmap {

    public static final String MODID = "pingmap";

    public static final Logger LOGGER = LogUtils.getLogger();

    public Pingmap(FMLJavaModLoadingContext context) {
        context.getModEventBus().addListener(this::commonSetup);
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(MainNetworkHandler::register);
        event.enqueueWork(HandshakeNetworkHandler::register);
    }
}
