package fun.bm.pingmap;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.TimeUnit;

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
    private static boolean keyWasPressed = false;

    private static final Cache<Integer, Long> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(15).build();

    private static int taskId = 0;

    private static int generateNewId() {
        return taskId++;
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        int keyCode = event.getKey();
        boolean isPressed = event.getAction() == GLFW.GLFW_PRESS;

        if (keyCode == KeyInputHandler.PING_KEY.getKey().getValue()) {

            if (isPressed && !keyWasPressed) {
                keyWasPressed = true;
                handlePingKey();
            } else if (!isPressed) {
                keyWasPressed = false;
            }
        }
    }

    private static void handlePingKey() {
        if (cache.size() >= 15) {
            cache.cleanUp();
            if (cache.size() >= 15) {
                return;
            }
        }

        cache.put(generateNewId(), System.currentTimeMillis());

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        double maxDistance = 500.0D;

        HitResult blockHit = minecraft.player.pick(maxDistance, 0.0F, false);

        Vec3 eyePosition = minecraft.player.getEyePosition(1.0F);
        Vec3 lookAngle = minecraft.player.getViewVector(1.0F);
        Vec3 endPosition = eyePosition.add(lookAngle.scale(maxDistance));

        AABB searchBox = minecraft.player.getBoundingBox().expandTowards(lookAngle.scale(maxDistance)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                minecraft.player,
                eyePosition,
                endPosition,
                searchBox,
                entity -> !entity.isSpectator() && entity.isPickable(),
                maxDistance * maxDistance
        );

        if (entityHit != null && entityHit.getType() == HitResult.Type.ENTITY) {
            Entity target = entityHit.getEntity();
            if (target != null) {
                String dimension = minecraft.level.dimension().location().toString();
                PingManager manager = PingManager.get(minecraft);
                if (manager != null) {
                    manager.addEntityPing(target, dimension, minecraft.player.getUUID());
                    minecraft.player.sendSystemMessage(
                            Component.literal(String.format("§a已标记实体: %s", target.getName().getString()))
                    );
                }
            }
        } else if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
            Vec3 hitVec = blockHit.getLocation();
            String dimension = minecraft.level.dimension().location().toString();

            PingManager manager = PingManager.get(minecraft);
            if (manager != null) {
                manager.addPointPing(hitVec.x, hitVec.y, hitVec.z, dimension, minecraft.player.getUUID());

                minecraft.player.sendSystemMessage(
                        Component.literal(String.format("§a已添加标记点: X=%.2f Y=%.2f Z=%.2f",
                                hitVec.x, hitVec.y, hitVec.z))
                );
            }
        } else {
            minecraft.player.sendSystemMessage(
                    Component.literal("§c无效目标！")
            );
        }
    }
}
