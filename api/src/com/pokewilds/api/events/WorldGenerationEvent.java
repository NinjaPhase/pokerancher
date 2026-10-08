package com.pokewilds.api.events;

import com.pokewilds.api.world.gen.WorldType;

public interface WorldGenerationEvent {
    WorldType getWorldType();
}
