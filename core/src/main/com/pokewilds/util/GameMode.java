package com.pokewilds.util;

public enum GameMode {
    ORIGIN,
    SOULS;

    public String getDescription() {
        switch (this) {
            case ORIGIN:
                return "ORIGIN is Pokemon as you know it.";
            case SOULS:
                return "SOULS is a brand-new Pokemon experience.";
        }
        return null;
    }
}
