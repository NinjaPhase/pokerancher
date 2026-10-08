package com.pokewilds.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * The {@code Plugin} contains the information of the plugin and defines the class which will be instantiated by
 * the plugin handler, it is also used to check for collisions for multiplayer.
 * </p>
 *
 * <p>
 * This class should also contain the subscribers for events.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mod {
    /**
     * <p>The name of the plugin.</p>
     */
    String name();

    /**
     * <p>The author of the plugin.</p>
     */
    String author();

    /**
     * <p>The version of the plugin.</p>
     */
    String version();
}
