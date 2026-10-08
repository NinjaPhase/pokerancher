package com.pokewilds.scene.action;


import com.pokewilds.scene.GameScene;

public class CompletableAction implements Action {

    private final ActionPriority actionPriority;
    private final CompletedRunnable action;
    private final Action next;

    public CompletableAction(final ActionPriority priority, CompletedRunnable action) {
        this(priority, action, null);
    }

    public CompletableAction(final ActionPriority priority, CompletedRunnable action, Action next) {
        this.actionPriority = priority;
        this.action = action;
        this.next = next;
    }

    @Override
    public boolean run(GameScene scene) {
        boolean done = this.action.run();

        if (done && this.next != null) {
            scene.queueAction(this.next);
        }
        return done;
    }

    @Override
    public ActionPriority getPriority() {
        return this.actionPriority;
    }
}
