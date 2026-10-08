package com.pokewilds.plugin;

import com.pokewilds.api.Mod;

import java.lang.reflect.Method;

public class ModInstance {

    private final Object instance;
    private final String name, author, version;

    public ModInstance(Object instance) {
        this.instance = instance;
        Mod p = this.instance.getClass().getAnnotation(Mod.class);
        this.name = p.name();
        this.author = p.author();
        this.version = p.version();
    }

    private void invoke(Object... params) {
        Method[] methods = instance.getClass().getDeclaredMethods();
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getVersion() {
        return version;
    }

}
