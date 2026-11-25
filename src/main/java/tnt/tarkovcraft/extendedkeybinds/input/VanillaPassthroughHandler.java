package tnt.tarkovcraft.extendedkeybinds.input;

import tnt.tarkovcraft.extendedkeybinds.api.InputMode;
import tnt.tarkovcraft.extendedkeybinds.api.KeyInputHandler;

public final class VanillaPassthroughHandler implements KeyInputHandler {

    private final TriggerSink sink;

    private boolean physicallyDown;
    private int pendingTriggers;

    public VanillaPassthroughHandler(Context context) {
        this.sink = context.sink();
    }

    @Override
    public void onInput(boolean down, InputMode mode, long now, boolean phantom) {
        boolean wasDown = this.physicallyDown;
        this.physicallyDown = down;
        if (down && !wasDown && !phantom) {
            this.pendingTriggers++;
            this.sink.onTrigger(0L, 0);
        }
    }

    @Override
    public void preTick(InputMode mode, long now) {
    }

    @Override
    public void postTick() {
    }

    @Override
    public boolean logicalDown(InputMode mode) {
        return this.physicallyDown;
    }

    @Override
    public boolean consumeTrigger() {
        if (this.pendingTriggers == 0) {
            return false;
        }
        this.pendingTriggers--;
        return true;
    }

    @Override
    public void reset() {
        this.physicallyDown = false;
        this.pendingTriggers = 0;
    }

    @Override
    public boolean isArmed() {
        return this.physicallyDown;
    }

    @Override
    public boolean resolvesGestures() {
        return false;
    }
}
