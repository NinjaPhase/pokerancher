package com.pokewilds.api.events;

import com.pokewilds.api.world.gen.WorldType;

import java.util.HashMap;

public interface PluginLoadEvent {
    HashMap<String, WorldType> getWorldTypes();

    void addWorldType(String type);

    void loadResource(String name);
}
