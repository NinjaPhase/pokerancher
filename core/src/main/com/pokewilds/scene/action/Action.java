package com.pokewilds.scene.action;

import com.pokewilds.scene.GameScene;

public interface Action {
    static ActionBuilder newBuilder() {
        return new ActionBuilder();
    }

    /**
     * <p>
     * Runs this action in the action queue, if the action is not yet done
     * then this returns false, if true is returned the action will be removed
     * from the action queue.
     * </p>
     *
     * @param scene The scene.
     *
     * @return Whether to remove from the action queue or not.
     */
    boolean run(GameScene scene);

    /**
     * <p>
     * Gets the priority of this action, higher priorities will be put at
     * the top of the action queue.
     * </p>
     *
     * @return The priority.
     */
    ActionPriority getPriority();
}
