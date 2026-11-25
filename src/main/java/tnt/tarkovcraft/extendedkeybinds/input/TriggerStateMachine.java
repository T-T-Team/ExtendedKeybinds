package tnt.tarkovcraft.extendedkeybinds.input;

import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;
import tnt.tarkovcraft.extendedkeybinds.api.InputMode;
import tnt.tarkovcraft.extendedkeybinds.api.KeyInputHandler;

import java.util.Set;
import java.util.function.Supplier;

public final class TriggerStateMachine implements KeyInputHandler {

    private final Supplier<Set<InputMode>> competingModes;
    private final TriggerSink sink;

    private boolean physicallyDown;
    private boolean armed;
    private boolean consumed;
    private boolean activated;
    private long pressTimeMs;
    private int heldTicks;
    private long tapStartMs = -1L;
    private boolean tapPending;
    private long pendingHeldMillis;
    private int pendingHeldTicks;
    private boolean contestedByHold;
    private boolean contestedByDouble;
    private int pulseTicks;
    private int pendingTriggers;

    public TriggerStateMachine(Context context) {
        this.competingModes = context.competingModes();
        this.sink = context.sink();
    }

    @Override
    public void onInput(boolean down, InputMode mode, long now, boolean phantom) {
        boolean wasDown = this.physicallyDown;
        this.physicallyDown = down;
        if (down && !wasDown) {
            this.onPressEdge(mode, now, phantom);
        } else if (!down && wasDown) {
            this.onReleaseEdge(mode, now);
        }
    }

    private void onPressEdge(InputMode mode, long now, boolean phantom) {
        if (phantom) {
            if (mode == InputMode.CONTINUOUS) {
                this.startPress(now);
            }
            return;
        }
        Set<InputMode> competitors = this.competingModes.get();
        this.contestedByHold = competitors.contains(InputMode.HOLD);
        this.contestedByDouble = competitors.contains(InputMode.DOUBLE_CLICK);
        boolean withinDouble = this.tapStartMs >= 0L && now - this.tapStartMs <= ExtendedKeybindsApi.getDoubleClickInterval();
        boolean tracksDouble = mode == InputMode.DOUBLE_CLICK || this.contestedByDouble;
        if (this.tapPending && !(withinDouble && tracksDouble)) {
            this.flushPendingTap();
        }
        this.startPress(now);
        if (tracksDouble) {
            if (withinDouble) {
                this.tapStartMs = -1L;
                if (mode == InputMode.DOUBLE_CLICK) {
                    this.activated = true;
                    this.fire(0L, 0);
                } else {
                    this.consumed = true;
                    this.tapPending = false;
                }
                return;
            }
            this.tapStartMs = now;
        } else {
            this.tapStartMs = -1L;
        }
        if (mode == InputMode.PRESS && !this.isTapDeferred(mode)) {
            this.fire(0L, 0);
        }
    }

    private void onReleaseEdge(InputMode mode, long now) {
        boolean wasArmed = this.armed;
        boolean wasActivated = this.activated;
        boolean wasConsumed = this.consumed;
        this.armed = false;
        this.activated = false;
        this.consumed = false;
        if (!wasArmed || wasConsumed) {
            return;
        }
        long held = Math.max(0L, now - this.pressTimeMs);
        boolean holdReached = held >= ExtendedKeybindsApi.getRequiredHoldDuration();
        switch (mode) {
            case HOLD -> {
                if (holdReached) {
                    this.tapStartMs = -1L;
                    if (!wasActivated) {
                        // hold duration elapsed between ticks
                        this.fire(held, this.heldTicks);
                    }
                }
            }
            case PRESS, RELEASE -> {
                if (!this.isTapDeferred(mode)) {
                    if (mode == InputMode.RELEASE) {
                        this.fire(held, this.heldTicks);
                    }
                    return;
                }
                if (this.contestedByHold && holdReached) {
                    this.tapStartMs = -1L;
                    return;
                }
                if (this.contestedByDouble && this.tapStartMs >= 0L && now - this.tapStartMs <= ExtendedKeybindsApi.getDoubleClickInterval()) {
                    this.tapPending = true;
                    this.pendingHeldMillis = held;
                    this.pendingHeldTicks = this.heldTicks;
                    return;
                }
                this.tapStartMs = -1L;
                this.fire(held, this.heldTicks);
            }
            case DOUBLE_CLICK -> {
                if (this.contestedByHold && holdReached) {
                    this.tapStartMs = -1L;
                }
            }
            case CONTINUOUS -> {
            }
        }
    }

    @Override
    public void preTick(InputMode mode, long now) {
        if (this.armed) {
            this.heldTicks++;
        }
        long held = Math.max(0L, now - this.pressTimeMs);
        boolean holdReached = this.armed && !this.consumed && held >= ExtendedKeybindsApi.getRequiredHoldDuration();
        switch (mode) {
            case HOLD -> {
                if (holdReached && !this.activated) {
                    this.activated = true;
                    this.tapStartMs = -1L;
                    this.fire(held, this.heldTicks);
                }
            }
            case PRESS, RELEASE -> {
                if (holdReached && this.contestedByHold && this.isTapDeferred(mode)) {
                    this.consumed = true;
                    this.tapStartMs = -1L;
                }
            }
            case DOUBLE_CLICK -> {
                if (holdReached && this.contestedByHold) {
                    this.tapStartMs = -1L;
                }
            }
            case CONTINUOUS -> {
                if (this.armed) {
                    this.pendingTriggers = 1;
                    this.sink.onTrigger(held, this.heldTicks);
                }
            }
        }
        if (this.tapStartMs >= 0L && now - this.tapStartMs > ExtendedKeybindsApi.getDoubleClickInterval()) {
            this.tapStartMs = -1L;
            if (this.tapPending) {
                this.flushPendingTap();
            }
        }
    }

    @Override
    public void postTick() {
        if (this.pulseTicks > 0) {
            this.pulseTicks--;
        }
    }

    @Override
    public boolean logicalDown(InputMode mode) {
        return switch (mode) {
            case PRESS, RELEASE -> this.pulseTicks > 0;
            case HOLD, DOUBLE_CLICK -> this.activated || this.pulseTicks > 0;
            case CONTINUOUS -> this.armed;
        };
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
        this.armed = false;
        this.consumed = false;
        this.activated = false;
        this.pressTimeMs = 0L;
        this.heldTicks = 0;
        this.tapStartMs = -1L;
        this.tapPending = false;
        this.pendingHeldMillis = 0L;
        this.pendingHeldTicks = 0;
        this.contestedByHold = false;
        this.contestedByDouble = false;
        this.pulseTicks = 0;
        this.pendingTriggers = 0;
    }

    @Override
    public boolean isArmed() {
        return this.armed;
    }

    private void startPress(long now) {
        this.armed = true;
        this.consumed = false;
        this.activated = false;
        this.pressTimeMs = now;
        this.heldTicks = 0;
    }

    private boolean isTapDeferred(InputMode mode) {
        return (mode == InputMode.PRESS || mode == InputMode.RELEASE) && (this.contestedByHold || this.contestedByDouble);
    }

    private void flushPendingTap() {
        this.tapPending = false;
        this.fire(this.pendingHeldMillis, this.pendingHeldTicks);
    }

    private void fire(long heldMillis, int heldTicks) {
        this.pendingTriggers++;
        this.pulseTicks = 1;
        this.sink.onTrigger(heldMillis, heldTicks);
    }
}
