package com.pokewilds.pokemon;

public class Pokemon {

    private final Species species;
    private String nickname;
    private int level;
    private Move[] moves;

    public Pokemon(String species, int level) {
        this.species = SpeciesRegistry.get(species);
        this.level = level;
    }

    public String getName() {
        if (this.nickname != null) {
            return this.nickname;
        }
        return this.species.getName();
    }

    public Species getSpecies() {
        return this.species;
    }

}
