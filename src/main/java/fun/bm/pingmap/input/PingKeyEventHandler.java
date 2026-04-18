package fun.bm.pingmap.input;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.pingmanager.LocalPingManager;
import fun.bm.pingmap.pingmanager.RemotePingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.TimeUnit;

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
                LocalPingManager manager = LocalPingManager.get(minecraft);
                if (manager != null) {
                    manager.addEntityPing(target, dimension, minecraft.player.getUUID(), PingType.Enemy);
                    Pingmap.LOGGER.debug("已标记实体: {}", target.getName().getString());

                    if (!minecraft.hasSingleplayerServer()) {
                        RemotePingManager.sendEntityPing(target);
                    }
                }
            }
        } else {
            HitResult blockHit = minecraft.player.pick(maxDistance, 0.0F, false);

            if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
                Vec3 hitVec = blockHit.getLocation();
                String dimension = minecraft.level.dimension().location().toString();

                LocalPingManager manager = LocalPingManager.get(minecraft);
                if (manager != null) {
                    manager.addPointPing(hitVec.x, hitVec.y, hitVec.z, dimension, minecraft.player.getUUID());

                    Pingmap.LOGGER.debug("已添加标记点: X={} Y={} Z={}", hitVec.x, hitVec.y, hitVec.z);

                    if (!minecraft.hasSingleplayerServer()) {
                        RemotePingManager.sendPointPing(hitVec.x, hitVec.y, hitVec.z);
                    }
                }
            } else {
                Pingmap.LOGGER.debug("无效目标！");
            }
        }
    }
}
