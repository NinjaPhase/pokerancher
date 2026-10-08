package com.pokewilds.plugin;

import java.util.HashMap;
import java.util.Map;

public class ModClassLoader extends ClassLoader {
    private final Map<String, byte[]> classDefinitions;

    public ModClassLoader() {
        this.classDefinitions = new HashMap<>();
    }

    public void addClass(String className, byte[] data) {
        this.classDefinitions.put(className, data);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = this.classDefinitions.remove(name);
        if (data != null) {
            return defineClass(name, data, 0, data.length);
        }
        return super.findClass(name);
    }

}
