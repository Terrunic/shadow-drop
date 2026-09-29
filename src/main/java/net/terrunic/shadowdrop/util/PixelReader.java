package net.terrunic.shadowdrop.util;

//? if <26.1 {
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

// Helper for capturing and reading back pixels from the framebuffer
public final class PixelReader {
    private static TextureTarget capture = null;
    private static boolean hasCapture = false;

    private PixelReader() {
    }

    public static boolean hasCapture() {
        return hasCapture;
    }

    public static void invalidateCapture() {
        hasCapture = false;
    }

    // Copy the currently bound framebuffer into the capture target
    public static void capture(int width, int height) {
        if (width <= 0 || height <= 0) return;

        int prevRead = GlStateManager._getInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int prevDraw = GlStateManager._getInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

        if (capture == null || capture.width != width || capture.height != height) {
            // Creating or resizing the target rebinds framebuffers and enables depth test
            boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            if (capture == null) {
                capture = new TextureTarget(width, height, false, Minecraft.ON_OSX);
            } else {
                capture.resize(width, height, Minecraft.ON_OSX);
            }
            if (!depthTest) RenderSystem.disableDepthTest();
        }

        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevRead);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, capture.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevRead);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, prevDraw);
        hasCapture = true;
    }

    // Whether the capture covers a framebuffer of the given size
    public static boolean captureMatches(int width, int height) {
        return hasCapture && capture != null && capture.width == width && capture.height == height;
    }

    // Read an RGBA rect from the bound framebuffer into liveDest, and an RGBA rect from the capture into captureDest
    public static void read(int x, int y, int width, int height, ByteBuffer liveDest,
                            int captureX, int captureY, int captureWidth, int captureHeight, ByteBuffer captureDest) {
        int prevPackBuffer = GL11.glGetInteger(GL21.GL_PIXEL_PACK_BUFFER_BINDING);
        int prevAlignment = GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT);
        int prevRowLength = GL11.glGetInteger(GL11.GL_PACK_ROW_LENGTH);
        int prevSkipPixels = GL11.glGetInteger(GL11.GL_PACK_SKIP_PIXELS);
        int prevSkipRows = GL11.glGetInteger(GL11.GL_PACK_SKIP_ROWS);
        int prevRead = GlStateManager._getInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);

        if (prevPackBuffer != 0) GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);
        GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, 0);

        try {
            liveDest.clear();
            GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, liveDest);

            captureDest.clear();
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, capture.frameBufferId);
            GL11.glReadPixels(captureX, captureY, captureWidth, captureHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, captureDest);
        } finally {
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, prevRead);
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, prevAlignment);
            GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, prevRowLength);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, prevSkipPixels);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, prevSkipRows);
            if (prevPackBuffer != 0) GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, prevPackBuffer);
        }
    }
}
//?}
