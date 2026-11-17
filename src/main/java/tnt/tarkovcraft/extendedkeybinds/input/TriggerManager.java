package tnt.tarkovcraft.extendedkeybinds.input;

import net.minecraft.client.KeyMapping;
import net.minecraft.util.Util;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class TriggerManager {

    private static final Set<KeyMapping> TRACKED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static KeyMapping[] snapshot = new KeyMapping[0];
    private static boolean syncingKeyStates;
    private static long dispatchTime = -1L;

    private TriggerManager() {
    }

    public static void track(KeyMapping mapping) {
        if (TRACKED.add(mapping)) {
            snapshot = TRACKED.toArray(KeyMapping[]::new);
        }
    }

    public static boolean isSyncingKeyStates() {
        return syncingKeyStates;
    }

    public static void setSyncingKeyStates(boolean syncing) {
        syncingKeyStates = syncing;
    }

    /**
     * Pins the timestamp for the duration of a single key event dispatch, so all key mappings sharing
     * the key evaluate their timing rules against the same value
     */
    public static void beginDispatch() {
        dispatchTime = Util.getMillis();
    }

    public static void endDispatch() {
        dispatchTime = -1L;
    }

    public static long now() {
        return dispatchTime >= 0L ? dispatchTime : Util.getMillis();
    }

    public static void onPreTick(ClientTickEvent.Pre event) {
        long now = Util.getMillis();
        for (KeyMapping mapping : snapshot) {
            ((TickableKeyMapping) mapping).extendedkeybinds$preTick(now);
        }
    }

    public static void onPostTick(ClientTickEvent.Post event) {
        for (KeyMapping mapping : snapshot) {
            ((TickableKeyMapping) mapping).extendedkeybinds$postTick();
        }
    }
}
