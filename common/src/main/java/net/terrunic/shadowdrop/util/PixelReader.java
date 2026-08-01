package net.terrunic.shadowdrop.util;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

import java.nio.ByteBuffer;

// Helper for reading back pixels from the framebuffer
public final class PixelReader {
    // Pooled buffer for full screen captures, grown as needed
    private static ByteBuffer screenBuffer = null;

    private PixelReader() {
    }

    // Get a reusable buffer of at least the requested size
    public static ByteBuffer screenBuffer(int bytes) {
        if (screenBuffer == null || screenBuffer.capacity() < bytes) {
            screenBuffer = BufferUtils.createByteBuffer(bytes);
        }
        return screenBuffer;
    }

    // Read an RGBA rect into dest
    public static void read(int x, int y, int width, int height, ByteBuffer dest) {
        int prevPackBuffer = GL11.glGetInteger(GL21.GL_PIXEL_PACK_BUFFER_BINDING);
        int prevAlignment = GL11.glGetInteger(GL11.GL_PACK_ALIGNMENT);
        int prevRowLength = GL11.glGetInteger(GL11.GL_PACK_ROW_LENGTH);
        int prevSkipPixels = GL11.glGetInteger(GL11.GL_PACK_SKIP_PIXELS);
        int prevSkipRows = GL11.glGetInteger(GL11.GL_PACK_SKIP_ROWS);

        if (prevPackBuffer != 0) GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);
        GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, 0);

        try {
            dest.clear();
            GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dest);
        } finally {
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, prevAlignment);
            GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, prevRowLength);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, prevSkipPixels);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, prevSkipRows);
            if (prevPackBuffer != 0) GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, prevPackBuffer);
        }
    }
}
