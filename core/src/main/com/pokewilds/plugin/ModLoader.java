package com.pokewilds.plugin;

import com.badlogic.gdx.Gdx;
import com.pokewilds.api.Mod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModLoader {
    private static ModLoader instance;

    private final ModClassLoader classLoader;
    private final List<ModInstance> mods;

    public static ModLoader getInstance() {
        if (instance == null) {
            instance = new ModLoader();
        }
        return instance;
    }

    private ModLoader() {
        this.classLoader = new ModClassLoader();
        this.mods = new ArrayList<>();
    }

    public void loadMod(Object obj) {
        if (!obj.getClass().isAnnotationPresent(Mod.class)) {
            return;
        }
        Mod mod = obj.getClass().getAnnotation(Mod.class);
        Gdx.app.log("ModLoader", "Loading mod " + mod.name());
        ModInstance plugin = new ModInstance(obj);
        mods.add(plugin);
    }

    public void loadFolder(File modFolder) {
        Gdx.app.log("ModLoader", "Loading mod from " + modFolder.getAbsolutePath());
        for (File f : Objects.requireNonNull(modFolder.listFiles())) {
            if (!f.getName().toLowerCase().endsWith(".jar")) {
                continue;
            }
            Gdx.app.log("ModLoader", "Loading mod " + f.getName());
            load(f);
            Gdx.app.log("ModLoader", "Loaded mod " + f.getName());
        }
    }

    public void load(File f) {
        List<String> classes = new ArrayList<>();

        try (JarInputStream jis = new JarInputStream(new FileInputStream(f))) {
            JarEntry e;
            while ((e = jis.getNextJarEntry()) != null) {
                if (!e.getName().toLowerCase().endsWith(".class")) {
                    continue;
                }
                String className = e.getName().replace("/", ".").substring(0, e.getName().length() - 6);
                classes.add(className);
                byte[] data = new byte[(int) e.getSize()];
                int total = 0;
                int i;
                while (total < data.length) {
                    i = jis.read(data, total, Math.min(data.length - total, 1024));
                    if (i == -1) {
                        break;
                    }
                    total += i;
                }
                this.classLoader.addClass(className, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String cls : classes) {
            try {
                Class<?> c = this.classLoader.findClass(cls);

                if (c.isAnnotationPresent(Mod.class)) {
                    this.loadMod(c.getConstructor().newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<ModInstance> getMods() {
        return this.mods;
    }
}
