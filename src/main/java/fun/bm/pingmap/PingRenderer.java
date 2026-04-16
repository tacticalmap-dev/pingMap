package fun.bm.pingmap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(modid = Pingmap.MODID, value = Dist.CLIENT)
public class PingRenderer {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        PingManager manager = PingManager.get(minecraft);
        if (manager == null) {
            return;
        }

        String currentDimension = minecraft.level.dimension().location().toString();
        List<PingManager.Ping> pings = manager.getPingsForDimension(currentDimension);

        if (pings.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        Player player = minecraft.player;
        double cameraX = event.getCamera().getPosition().x();
        double cameraY = event.getCamera().getPosition().y();
        double cameraZ = event.getCamera().getPosition().z();

        poseStack.translate(-cameraX, -cameraY, -cameraZ);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        for (PingManager.Ping ping : pings) {
            if (ping.expired()) {
                manager.cancelPing(ping);
            } else {
                renderPing(poseStack, bufferBuilder, ping, player.tickCount);
            }
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();

        renderPingLabels(event.getPoseStack(), minecraft, pings, cameraX, cameraY, cameraZ);
    }

    private static void renderPing(PoseStack poseStack, BufferBuilder bufferBuilder,
                                   PingManager.Ping ping, float tickCount) {
        float pulse = (float) Math.sin(tickCount * 0.1F) * 0.2F + 0.8F;

        Matrix4f matrix = poseStack.last().pose();

        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        double x = ping.getX();
        double y = ping.getY() + 0.01;
        double z = ping.getZ();

        float red = 1.0F;
        float green = pulse;
        float blue = 0.0F;
        float alpha = 0.8F;

        double radius = 0.3;
        int segments = 16;

        for (int i = 0; i <= segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            double x1 = x + Math.cos(angle1) * radius;
            double z1 = z + Math.sin(angle1) * radius;
            double x2 = x + Math.cos(angle2) * radius;
            double z2 = z + Math.sin(angle2) * radius;

            bufferBuilder.vertex(matrix, (float) x1, (float) y, (float) z1)
                    .color(red, green, blue, alpha).endVertex();
            bufferBuilder.vertex(matrix, (float) x2, (float) y, (float) z2)
                    .color(red, green, blue, alpha).endVertex();
        }

        Tesselator.getInstance().end();

        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        double beamHeight = 64;
        bufferBuilder.vertex(matrix, (float) x, (float) y, (float) z)
                .color(red, green, blue, alpha * 0.5F).endVertex();
        bufferBuilder.vertex(matrix, (float) x, (float) (y + beamHeight), (float) z)
                .color(red, green, blue, 0.0F).endVertex();

        Tesselator.getInstance().end();
    }

    private static void renderPingLabels(PoseStack eventPoseStack, Minecraft minecraft, List<PingManager.Ping> pings,
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

        for (PingManager.Ping ping : pings) {
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
            float scale = baseScale * distanceScale * 1.5F; // 调整缩放比例

            eventPoseStack.scale(-scale, -scale, scale);

            String distanceText = String.format("%.1fm", distance);
            int textWidth = minecraft.font.width(distanceText);

            eventPoseStack.pushPose();
            eventPoseStack.translate(0, -10, 0);

            minecraft.font.drawInBatch(
                    Component.literal(ping.getType().getIcon()),
                    -minecraft.font.width(ping.getType().getIcon()) / 2.0F,
                    0,
                    ping.getType().getColor(),
                    false,
                    eventPoseStack.last().pose(),
                    minecraft.renderBuffers().bufferSource(),
                    ping.getType().getDisplayMode(),
                    0,
                    15728880
            );
            minecraft.renderBuffers().bufferSource().endBatch();

            eventPoseStack.popPose();

            minecraft.font.drawInBatch(
                    Component.literal(distanceText),
                    -textWidth / 2.0F,
                    0,
                    0xFFFFFFFF,
                    false,
                    eventPoseStack.last().pose(),
                    minecraft.renderBuffers().bufferSource(),
                    ping.getType().getDisplayMode(),
                    0,
                    15728880
            );
            minecraft.renderBuffers().bufferSource().endBatch();

            eventPoseStack.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
