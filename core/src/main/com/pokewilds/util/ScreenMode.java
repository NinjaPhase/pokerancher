package com.pokewilds.util;

public enum ScreenMode {
    FULLSCREEN,
    BORDERLESS,
    WINDOWED;

    public static ScreenMode fromValue(String s) {
        if (s.equalsIgnoreCase("fullscreen")) {
            return ScreenMode.FULLSCREEN;
        } else if (s.equalsIgnoreCase("borderless")) {
            return ScreenMode.BORDERLESS;
        }
        return ScreenMode.WINDOWED;
    }

    public String getValue() {
        switch (this) {
            case FULLSCREEN:
                return "fullscreen";
            case BORDERLESS:
                return "borderless";
            case WINDOWED:
                return "windowed";
        }
        throw new IllegalArgumentException("window mode not recognised");
    }
}
