package fun.bm.pingmap.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fun.bm.pingmap.Pingmap;
import fun.bm.pingmap.api.pingmanager.ping.Ping;
import fun.bm.pingmap.config.local.ClientConfig;
import fun.bm.pingmap.enums.PingType;
import fun.bm.pingmap.pingmanager.LocalPingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Pingmap.MODID, value = Dist.CLIENT)
public class WorldPingRender {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        LocalPingManager manager = LocalPingManager.get(minecraft);
        if (manager == null) {
            return;
        }

        String currentDimension = minecraft.level.dimension().location().toString();
        List<Ping> pings = manager.getPingsForDimension(currentDimension, true);

        if (pings.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        double cameraX = event.getCamera().getPosition().x();
        double cameraY = event.getCamera().getPosition().y();
        double cameraZ = event.getCamera().getPosition().z();

        poseStack.translate(-cameraX, -cameraY, -cameraZ);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        poseStack.popPose();

        renderPings(event.getPoseStack(), minecraft, pings, cameraX, cameraY, cameraZ);
    }

    private static void renderPings(PoseStack eventPoseStack, Minecraft minecraft, List<Ping> pings,
                                    double cameraX, double cameraY, double cameraZ) {
        if (pings.isEmpty()) {
            return;
        }

        Player player = minecraft.player;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (Ping ping : pings) {
            double dx = ping.getX() - player.getX();
            double dy = ping.getY() - player.getY();
            double dz = ping.getZ() - player.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > 1000) {
                continue;
            }

            eventPoseStack.pushPose();

            eventPoseStack.translate(ping.getX() - cameraX, ping.getY() + 0.5 - cameraY, ping.getZ() - cameraZ);
            eventPoseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());

            float baseScale = 0.025F;
            float distanceScale = (float) Math.max(distance / 10.0, 1.0);
            float scale = baseScale * distanceScale * ClientConfig.getLabelScaleMultiplier(distance);

            eventPoseStack.scale(-scale, -scale, scale);

            String distanceText = String.format("%.1fm", distance);
            int textWidth = minecraft.font.width(distanceText);

            Font.DisplayMode displayMode = ping.getType() == PingType.Friendly
                    ? Font.DisplayMode.NORMAL
                    : Font.DisplayMode.SEE_THROUGH;

            eventPoseStack.pushPose();
            eventPoseStack.translate(0, -10, 0);

            minecraft.font.drawInBatch(
                    Component.literal(ping.getIcon()),
                    -minecraft.font.width(ping.getIcon()) / 2.0F,
                    0,
                    ping.getColor(),
                    false,
                    eventPoseStack.last().pose(),
                    minecraft.renderBuffers().bufferSource(),
                    displayMode,
                    0,
                    15728880
            );
            minecraft.renderBuffers().bufferSource().endBatch();

            eventPoseStack.popPose();

            if (ping.showDistance()) {
                minecraft.font.drawInBatch(
                        Component.literal(distanceText),
                        -textWidth / 2.0F,
                        0,
                        0xFFFFFFFF,
                        false,
                        eventPoseStack.last().pose(),
                        minecraft.renderBuffers().bufferSource(),
                        displayMode,
                        0,
                        15728880
                );
            }
            minecraft.renderBuffers().bufferSource().endBatch();

            eventPoseStack.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
