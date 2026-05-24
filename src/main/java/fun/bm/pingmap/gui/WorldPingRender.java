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

            Font.DisplayMode displayMode = Font.DisplayMode.SEE_THROUGH;

            eventPoseStack.pushPose();
            eventPoseStack.translate(0, -10, 0);

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
            int color = ping.getColor();
            float a = ((color >> 24) & 0xFF) / 255.0F;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, a);

            net.minecraft.resources.ResourceLocation tex = new net.minecraft.resources.ResourceLocation("pingmap", "textures/gui/ping_point.png");
            if (ping.getType() == fun.bm.pingmap.enums.PingType.Enemy) {
                tex = new net.minecraft.resources.ResourceLocation("pingmap", "textures/gui/ping_enemy.png");
            } else if (ping.getType() == fun.bm.pingmap.enums.PingType.Friendly) {
                tex = new net.minecraft.resources.ResourceLocation("pingmap", "textures/gui/ping_friendly.png");
            }
            RenderSystem.setShaderTexture(0, tex);

            com.mojang.blaze3d.vertex.Tesselator tesselator = com.mojang.blaze3d.vertex.Tesselator.getInstance();
            com.mojang.blaze3d.vertex.BufferBuilder bufferbuilder = tesselator.getBuilder();
            org.joml.Matrix4f matrix4f = eventPoseStack.last().pose();

            bufferbuilder.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
            float s = 6.0F;
            bufferbuilder.vertex(matrix4f, -s, -s, 0).uv(0.0F, 0.0F).endVertex();
            bufferbuilder.vertex(matrix4f, -s,  s, 0).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(matrix4f,  s,  s, 0).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(matrix4f,  s, -s, 0).uv(1.0F, 0.0F).endVertex();
            tesselator.end();

            // Reset shader color for text rendering that follows
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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

            // Highlight the teammate who generated the ping
            Player generator = minecraft.level.getPlayerByUUID(ping.getGeneratorId());
            if (generator != null && generator != player) {
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                // 1. Render a line from the ping to the teammate
                RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
                RenderSystem.lineWidth(2.0F);
                bufferbuilder.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.DEBUG_LINES, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR);
                
                float r = ((color >> 16) & 0xFF) / 255.0F;
                float g = ((color >> 8) & 0xFF) / 255.0F;
                float bC = (color & 0xFF) / 255.0F;
                float lineAlpha = 0.4F;

                org.joml.Matrix4f mat = eventPoseStack.last().pose();
                bufferbuilder.vertex(mat, (float)(ping.getX() - cameraX), (float)(ping.getY() + 0.5 - cameraY), (float)(ping.getZ() - cameraZ)).color(r, g, bC, lineAlpha).endVertex();
                bufferbuilder.vertex(mat, (float)(generator.getX() - cameraX), (float)(generator.getY() + generator.getBbHeight() / 2.0 - cameraY), (float)(generator.getZ() - cameraZ)).color(r, g, bC, lineAlpha).endVertex();
                tesselator.end();
                
                // 2. Render an indicator above the teammate's head
                eventPoseStack.pushPose();
                eventPoseStack.translate(generator.getX() - cameraX, generator.getY() + generator.getBbHeight() + 0.5 - cameraY, generator.getZ() - cameraZ);
                eventPoseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
                eventPoseStack.scale(-scale, -scale, scale);
                
                RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, a);
                RenderSystem.setShaderTexture(0, new net.minecraft.resources.ResourceLocation("pingmap", "textures/gui/ping_friendly.png"));
                
                bufferbuilder.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX);
                bufferbuilder.vertex(eventPoseStack.last().pose(), -s, -s, 0).uv(0.0F, 0.0F).endVertex();
                bufferbuilder.vertex(eventPoseStack.last().pose(), -s,  s, 0).uv(0.0F, 1.0F).endVertex();
                bufferbuilder.vertex(eventPoseStack.last().pose(),  s,  s, 0).uv(1.0F, 1.0F).endVertex();
                bufferbuilder.vertex(eventPoseStack.last().pose(),  s, -s, 0).uv(1.0F, 0.0F).endVertex();
                tesselator.end();
                
                eventPoseStack.popPose();
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}
