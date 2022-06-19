package com.pkmngen.updater.platform;

import java.util.Locale;

public final class PlatformUtil {
    public static Platform getPlatform() {
        var os = System.getProperty("os.name").toLowerCase(Locale.getDefault());

        if(os.contains("win")) {
            return Platform.WINDOWS;
        }

        if(os.contains("mac") || os.contains("darwin")) {
            return Platform.MAC;
        }

        if(os.contains("nux") || os.contains("nix") || os.contains("aix")) {
            return Platform.LINUX;
        }

        return Platform.UNKNOWN;
    }
}
