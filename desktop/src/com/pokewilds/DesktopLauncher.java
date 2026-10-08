package com.pokewilds;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.pokewilds.util.Settings;

public class DesktopLauncher {
    public static void main(String... argv) {
        Arguments args = new Arguments();
        JCommander.newBuilder().addObject(args).build().parse(argv);

        if (args.developer) {
            TexturePacker.Settings settings = new TexturePacker.Settings();
            settings.combineSubdirectories = true;
            settings.paddingX = settings.paddingY = 0;
            settings.stripWhitespaceX = settings.stripWhitespaceY = true;
            TexturePacker.process(settings, "assetpack/pokemon", "assets/graphics", "pokemon");
            TexturePacker.process(settings, "assetpack/battle", "assets/graphics", "battle");
            PokeWilds.IS_DEV = true;
        }
        if (args.immediate != null) {
            PokeWilds.IMMEDIATE_MODE = args.immediate.equalsIgnoreCase("server") ? 2 : 1;
        }
        if (args.server) {
            Settings.load(Application.ApplicationType.HeadlessDesktop);
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            new HeadlessApplication(PokeWilds.getInstance(), config);
            return;
        }
        Settings.load(Application.ApplicationType.Desktop);

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setForegroundFPS(60);
        config.setTitle("Pokemon Rancher");
        config.setWindowedMode(Settings.getWidth(), Settings.getHeight());
        config.setResizable(true);
        config.setMaximized(Settings.isMaximised());
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void maximized(boolean isMaximized) {
                Settings.setMaximised(isMaximized);
            }
        });
        new Lwjgl3Application(PokeWilds.getInstance(), config);
    }

    static class Arguments {
        @Parameter(names = {"-d", "--dev"}, description = "Launches PokéWilds in developer mode.")
        boolean developer = false;

        @Parameter(names = {"--server"}, description = "Launches PokéWilds in headless mode.")
        boolean server = false;

        @Parameter(names = {"--immediate"}, description = "Launches PokéWilds straight into a world.", required = false)
        String immediate = null;
    }
}
