package fun.bm.pingmap;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Pingmap.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyInputHandler {

    public static final KeyMapping PING_KEY = new KeyMapping(
            "key.pingmap.ping",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            "key.categories.pingmap"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(PING_KEY);
    }
}

@Mod.EventBusSubscriber(modid = Pingmap.MODID, value = Dist.CLIENT)
class PingKeyEventHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyInputHandler.PING_KEY.consumeClick()) {
            handlePingKey();
        }
    }

    private static void handlePingKey() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        net.minecraft.world.phys.HitResult hitResult = minecraft.player.pick(500.0D, 0.0F, false);

        if (hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.world.phys.BlockHitResult blockHit = (net.minecraft.world.phys.BlockHitResult) hitResult;
            net.minecraft.world.phys.Vec3 hitVec = blockHit.getLocation();
            String dimension = minecraft.level.dimension().location().toString();

            PingManager manager = PingManager.get(minecraft);
            if (manager != null) {
                manager.addPing(hitVec.x, hitVec.y, hitVec.z, dimension);

                minecraft.player.sendSystemMessage(
                        Component.literal(String.format("§a已添加标记点: X=%.2f Y=%.2f Z=%.2f",
                                hitVec.x, hitVec.y, hitVec.z))
                );
            }
        } else {
            minecraft.player.sendSystemMessage(
                    Component.literal("§c请看向一个方块！")
            );
        }
    }
}
