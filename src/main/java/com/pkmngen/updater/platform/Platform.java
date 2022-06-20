package com.pkmngen.updater.platform;

import java.util.Locale;

public enum Platform {
    UNKNOWN,
    WINDOWS,
    MAC,
    LINUX;

    public String getName() {
        return this.toString().toLowerCase(Locale.ROOT);
    }

    public String getDownloadFileName() {
        if (this == UNKNOWN) {
            throw new RuntimeException("Unknown platform");
        }

        return String.format("pokemon-wilds-%s64.zip", getName());
    }
}
