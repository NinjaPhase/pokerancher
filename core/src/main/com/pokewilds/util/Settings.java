package com.pokewilds.util;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * <p>The {@code Settings} class handles loading and saving of various settings in the game, the settings are then
 * passed into this static.</p>
 */
public class Settings {
    private static final String SETTINGS_FILE = "settings.ini";

    private static ScreenMode screenMode = ScreenMode.WINDOWED;
    private static int width = 480, height = 320;
    private static UUID pid = UUID.randomUUID();
    private static boolean isMaximised = false;

    public static void load(Application.ApplicationType type) {
        if (type == Application.ApplicationType.Desktop || type == Application.ApplicationType.HeadlessDesktop) {
            File f = new File(SETTINGS_FILE);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    Gdx.app.error("PokeRancher", "Unable to create settings file", e);
                }
                Settings.save();
            }
            try {
                Ini ini = new Ini(f);
                if (ini.containsKey("game") && ini.get("game").containsKey("pid")) {
                    Settings.pid = UUID.fromString(ini.get("game", "pid"));
                }
                Settings.screenMode = ScreenMode.fromValue(ini.get("display", "screenmode"));
                String res = ini.get("display", "resolution");
                Settings.width = Integer.parseInt(res.split("x")[0]);
                Settings.height = Integer.parseInt(res.split("x")[1]);
                Settings.isMaximised = Boolean.parseBoolean(ini.get("display", "maximised"));
            } catch (IOException e) {
                if (Gdx.app != null) {
                    Gdx.app.error("PokeRancher", "Unable to load settings file, falling to defaults", e);
                } else {
                    System.err.println("Unable to load settinsg file, falling to defaults");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void save() {
        try {
            File f = new File(SETTINGS_FILE);
            Ini ini = new Ini(f);
            ini.clear();
            ini.add("game", "pid", Settings.pid.toString());
            ini.add("display", "screenmode", Settings.screenMode);
            ini.add("display", "resolution", Settings.width + "x" + Settings.height);
            ini.add("display", "maximised", Settings.isMaximised);
            ini.store();
        } catch (IOException e) {
            Gdx.app.error("PokeRancher", "Unable to save settings file, falling to defaults", e);
        }
    }

    public static void resize(int width, int height) {
        Settings.width = width;
        Settings.height = height;
    }

    public static ScreenMode getScreenMode() {
        return Settings.screenMode;
    }

    public static void setScreenMode(ScreenMode screenMode) {
        Settings.screenMode = screenMode;
    }

    public static int getWidth() {
        return Settings.width;
    }

    public static int getHeight() {
        return Settings.height;
    }

    public static boolean isMaximised() {
        return Settings.isMaximised;
    }

    public static void setMaximised(boolean maximised) {
        Settings.isMaximised = maximised;
    }

    public static UUID getPid() {
        return Settings.pid;
    }

    public static Graphics.DisplayMode getDisplayMode() {
        Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
        for (Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
            if (mode.width == width && mode.height == height && mode.bitsPerPixel == currentMode.bitsPerPixel &&
                mode.refreshRate == currentMode.refreshRate) {
                return mode;
            }
        }
        Graphics.DisplayMode highestRefresh = null;
        for (Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
            if (mode.width == width && mode.height == height && mode.bitsPerPixel == currentMode.bitsPerPixel) {
                if (highestRefresh == null || highestRefresh.refreshRate < mode.refreshRate) {
                    highestRefresh = mode;
                }
            }
        }
        if (highestRefresh != null) {
            return highestRefresh;
        }
        return currentMode;
    }

    private Settings() {

    }

}
