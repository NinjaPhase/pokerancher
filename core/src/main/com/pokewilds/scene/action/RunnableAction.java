package com.pokewilds.scene.action;

import com.pokewilds.scene.GameScene;

public class RunnableAction implements Action {

    private final ActionPriority actionPriority;
    private final Runnable action;
    private final Action next;

    public RunnableAction(final ActionPriority priority, Runnable action) {
        this(priority, action, null);
    }

    public RunnableAction(final ActionPriority priority, Runnable action, Action next) {
        this.actionPriority = priority;
        this.action = action;
        this.next = next;
    }

    @Override
    public boolean run(GameScene scene) {
        this.action.run();

        if (this.next != null) {
            scene.queueAction(this.next);
        }
        return true;
    }

    @Override
    public ActionPriority getPriority() {
        return this.actionPriority;
    }
}
