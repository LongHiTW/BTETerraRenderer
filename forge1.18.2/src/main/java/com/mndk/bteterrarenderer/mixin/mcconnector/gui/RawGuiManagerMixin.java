package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.RawGuiManager;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractGuiScreenCopy;
import com.mndk.bteterrarenderer.mcconnector.gui.component.AbstractWidgetCopy;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.ResourceLocationWrapper;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@UtilityClass
@Mixin(value = RawGuiManager.class, remap = false)
public class RawGuiManagerMixin {

    @Unique
    private final ResourceLocation bTETerraRenderer$CHECKBOX = new ResourceLocation("textures/gui/checkbox.png");

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static RawGuiManager makeInstance() { return new RawGuiManager() {

        public void displayGuiScreen(AbstractGuiScreenCopy gui) {
            Minecraft.getInstance().setScreen(new AbstractGuiScreenImpl(gui));
        }

        public void drawButton(DrawContextWrapper drawContextWrapper, int x, int y, int width, int height, AbstractWidgetCopy.HoverState hoverState) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int i = switch (hoverState) {
                case DISABLED -> 0;
                case DEFAULT -> 1;
                case MOUSE_OVER -> 2;
            };

            PoseStack poseStack = drawContextWrapper.get();
            GuiComponent.blit(poseStack, x, y, 0, 0, 46 + i * 20, width / 2, height, 256, 256);
            GuiComponent.blit(poseStack, x + width / 2, y, 0, 200 - (float) width / 2, 46 + i * 20, width / 2, height, 256, 256);
        }

        public void drawCheckBox(DrawContextWrapper drawContextWrapper, int x, int y, int width, int height, boolean focused, boolean checked) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, bTETerraRenderer$CHECKBOX);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();

            PoseStack poseStack = drawContextWrapper.get();
            Matrix4f matrix = poseStack.last().pose();

            float size = 20 / 64f;
            float u1 = focused ? size : 0, v1 = checked ? size : 0;
            float u2 = u1 + size, v2 = v1 + size;
            drawBufferPosTex(bufferbuilder, matrix, x, y, width, height, u1, v1, u2, v2);
        }

        public void drawTextFieldHighlight(DrawContextWrapper drawContextWrapper, int startX, int startY, int endX, int endY) {
            RenderSystem.setShader(GameRenderer::getPositionShader);
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
            RenderSystem.disableTexture();
            RenderSystem.enableColorLogicOp();
            RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();

            PoseStack poseStack = drawContextWrapper.get();
            Matrix4f matrix = poseStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferbuilder.vertex(matrix, startX, endY, 0).endVertex();
            bufferbuilder.vertex(matrix, endX, endY, 0).endVertex();
            bufferbuilder.vertex(matrix, endX, startY, 0).endVertex();
            bufferbuilder.vertex(matrix, startX, startY, 0).endVertex();
            tesselator.end();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableColorLogicOp();
            RenderSystem.enableTexture();
        }

        public void drawImage(DrawContextWrapper drawContextWrapper, ResourceLocationWrapper res, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, res.get());
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
            PoseStack poseStack = drawContextWrapper.get();
            Matrix4f matrix = poseStack.last().pose();
            drawBufferPosTex(bufferbuilder, matrix, x, y, w, h, u1, v1, u2, v2);
        }

        private void drawBufferPosTex(BufferBuilder bufferBuilder,
                                      Matrix4f matrix,
                                      int x, int y, int w, int h,
                                      float u1, float v1, float u2, float v2) {
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix, x, y+h, 0).uv(u1, v2).endVertex();
            bufferBuilder.vertex(matrix, x+w, y+h, 0).uv(u2, v2).endVertex();
            bufferBuilder.vertex(matrix, x+w, y, 0).uv(u2, v1).endVertex();
            bufferBuilder.vertex(matrix, x, y, 0).uv(u1, v1).endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
        }
    };}
}
