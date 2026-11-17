package tnt.tarkovcraft.extendedkeybinds.input;

import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;

public final class TriggerStateMachine {

    private boolean physicallyDown;
    private boolean armed;
    private boolean activated;
    private long pressTimeMs;
    private long lastTapTimeMs = -1L;
    private int heldTicks;
    private int pulseTicks;
    private int pendingTriggers;

    public boolean onInput(boolean down, InputMode mode, long now, boolean phantom) {
        boolean wasDown = this.physicallyDown;
        this.physicallyDown = down;
        if (down && !wasDown) {
            return this.onPressEdge(mode, now, phantom);
        }
        if (!down && wasDown) {
            return this.onReleaseEdge(mode);
        }
        return false;
    }

    private boolean onPressEdge(InputMode mode, long now, boolean phantom) {
        if (phantom) {
            return false;
        }
        this.armed = true;
        this.activated = false;
        this.pressTimeMs = now;
        this.heldTicks = 0;
        return switch (mode) {
            case PRESS -> true;
            case DOUBLE_CLICK -> {
                if (this.lastTapTimeMs >= 0L && now - this.lastTapTimeMs <= ExtendedKeybindsApi.getDoubleClickInterval()) {
                    this.lastTapTimeMs = -1L;
                    this.activated = true;
                    this.pendingTriggers++;
                    yield true;
                }
                this.lastTapTimeMs = now;
                yield false;
            }
            case RELEASE, HOLD, CONTINUOUS -> false;
        };
    }

    private boolean onReleaseEdge(InputMode mode) {
        boolean wasArmed = this.armed;
        this.armed = false;
        this.activated = false;
        if (mode == InputMode.RELEASE && wasArmed) {
            this.pendingTriggers++;
            this.pulseTicks = 1;
            return true;
        }
        return false;
    }

    public boolean preTick(InputMode mode, long now) {
        if (this.armed) {
            this.heldTicks++;
        }
        return switch (mode) {
            case HOLD -> {
                if (this.armed && !this.activated && now - this.pressTimeMs >= ExtendedKeybindsApi.getRequiredHoldDuration()) {
                    this.activated = true;
                    this.pendingTriggers++;
                    yield true;
                }
                yield false;
            }
            case CONTINUOUS -> {
                if (this.armed) {
                    this.pendingTriggers = 1;
                    yield true;
                }
                yield false;
            }
            case DOUBLE_CLICK -> {
                if (this.lastTapTimeMs >= 0L && now - this.lastTapTimeMs > ExtendedKeybindsApi.getDoubleClickInterval()) {
                    this.lastTapTimeMs = -1L;
                }
                yield false;
            }
            case PRESS, RELEASE -> false;
        };
    }

    public void postTick() {
        if (this.pulseTicks > 0) {
            this.pulseTicks--;
        }
    }

    public boolean logicalDown(InputMode mode) {
        return switch (mode) {
            case PRESS -> this.physicallyDown;
            case RELEASE -> this.pulseTicks > 0;
            case HOLD, DOUBLE_CLICK -> this.activated;
            case CONTINUOUS -> this.armed;
        };
    }

    public boolean consumeTrigger() {
        if (this.pendingTriggers == 0) {
            return false;
        }
        this.pendingTriggers--;
        return true;
    }

    public void reset() {
        this.physicallyDown = false;
        this.armed = false;
        this.activated = false;
        this.pressTimeMs = 0L;
        this.lastTapTimeMs = -1L;
        this.heldTicks = 0;
        this.pulseTicks = 0;
        this.pendingTriggers = 0;
    }

    public boolean isArmed() {
        return this.armed;
    }

    public long getHeldMillis(long now) {
        return Math.max(0L, now - this.pressTimeMs);
    }

    public int getHeldTicks() {
        return this.heldTicks;
    }
}
