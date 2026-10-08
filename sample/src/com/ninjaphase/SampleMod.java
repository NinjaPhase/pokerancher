package com.ninjaphase;

import com.pokewilds.api.Mod;
import com.pokewilds.api.Subscribe;
import com.pokewilds.api.events.PluginLoadEvent;
import com.pokewilds.api.events.WorldGenerationEvent;

import java.io.File;

@Mod(name = "Default Mod", author = "NinjaPhase", version = "1.0.0")
public class SampleMod {

    @Subscribe
    public void onPluginLoad(PluginLoadEvent loadEvent) {
        File f = new File("hello world");
    }

    @Subscribe
    public void onWorldCreation(WorldGenerationEvent world) {

    }

}
