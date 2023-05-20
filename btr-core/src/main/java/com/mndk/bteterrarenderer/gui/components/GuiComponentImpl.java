package com.mndk.bteterrarenderer.gui.components;

public abstract class GuiComponentImpl implements GuiEventListenerImpl {
    public float zLevel = 0;

    public void tick() {}
    public abstract void drawComponent(Object poseStack, double mouseX, double mouseY, float partialTicks);
}
