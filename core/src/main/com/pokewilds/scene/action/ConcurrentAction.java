package com.pokewilds.scene.action;

import com.pokewilds.scene.GameScene;

public class ConcurrentAction implements Action {

    private final Action[] actions;
    private final boolean[] done;

    public ConcurrentAction(Action... actions) {
        this.actions = actions;
        this.done = new boolean[actions.length];
    }

    @Override
    public boolean run(GameScene scene) {
        int doneCount = 0;
        for (int i = 0; i < actions.length; i++) {
            if (done[i]) {
                doneCount++;
                continue;
            }
            if (actions[i].run(scene)) {
                done[i] = true;
                doneCount++;
            }
        }
        return doneCount == actions.length - 1;
    }

    @Override
    public ActionPriority getPriority() {
        int highest = -1;
        for (Action action : actions) {
            highest = Math.max(highest, action.getPriority().ordinal());
        }
        if (highest <= -1) {
            return ActionPriority.LOWEST;
        }
        return ActionPriority.values()[highest];
    }
}
