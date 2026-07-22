package net.terrunic.shadowdrop.compat;

import dev.yurisuika.raised.api.RaisedApi;
import net.terrunic.shadowdrop.platform.Services;

public class RaisedCompat {
    public static boolean isRaisedLoaded() {
        return Services.PLATFORM.isModLoaded("raised");
    }

    public static int getHotbarXOffset() {
        if (isRaisedLoaded()) {
            return getHotbarXOffsetInternal();
        }
        return 0;
    }

    public static int getHotbarYOffset() {
        if (isRaisedLoaded()) {
            return getHotbarYOffsetInternal();
        }
        return 0;
    }

    private static int getHotbarXOffsetInternal() {
        try {
            return RaisedApi.getX("minecraft:hotbar");
        } catch (Throwable t) {
            return 0;
        }
    }

    private static int getHotbarYOffsetInternal() {
        try {
            return RaisedApi.getY("minecraft:hotbar");
        } catch (Throwable t) {
            return 0;
        }
    }
}
