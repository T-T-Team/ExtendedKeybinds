package tnt.tarkovcraft.extendedkeybinds.api;

import java.util.Set;
import java.util.function.Supplier;

public interface KeyInputHandler {

    void onInput(boolean down, InputMode mode, long now, boolean phantom);

    void preTick(InputMode mode, long now);

    void postTick();

    boolean logicalDown(InputMode mode);

    boolean consumeTrigger();

    void reset();

    boolean isArmed();

    default boolean resolvesGestures() {
        return true;
    }

    record Context(Supplier<Set<InputMode>> competingModes, TriggerSink sink) {
    }

    @FunctionalInterface
    interface Factory {
        KeyInputHandler create(Context context);
    }

    @FunctionalInterface
    interface TriggerSink {
        void onTrigger(long heldMillis, int heldTicks);
    }
}
