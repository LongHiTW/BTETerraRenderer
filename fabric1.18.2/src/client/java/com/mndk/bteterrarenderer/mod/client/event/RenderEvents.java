package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.tile.TileRenderer;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@UtilityClass
public class RenderEvents {

    public void registerEvents() {
        WorldRenderEvents.END.register(RenderEvents::onRender);
    }

    @SuppressWarnings("resource")
    public void onRender(WorldRenderContext renderContext) {
        World world = renderContext.world();
        MinecraftClient client = renderContext.gameRenderer().getClient();
        if(world == null) return;
        if(client.player == null) return;

        // While the player is the "rendering center" in 1.12.2,
        // In 1.18.2 it is the camera being that center.
        // So the camera's position should be given instead to TileRenderer.renderTiles(), unlike in 1.12.2.
        Vec3d cameraPos = renderContext.camera().getPos();
        world.getProfiler().swap("bteterrarenderer-hologram");
        TileRenderer.renderTiles(renderContext.matrixStack(), cameraPos.x, cameraPos.y, cameraPos.z);
    }
}
