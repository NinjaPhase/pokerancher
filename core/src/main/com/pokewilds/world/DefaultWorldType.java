package com.pokewilds.world;

import com.pokewilds.api.world.biome.Biome;
import com.pokewilds.api.world.biome.BiomeVoid;
import com.pokewilds.api.world.gen.WorldType;

public class DefaultWorldType implements WorldType {

    public DefaultWorldType() {
        this.addBiome(new BiomeVoid());
    }

    @Override
    public void addBiome(Biome biome) {

    }

    @Override
    public String getName() {
        return "Default";
    }
}
