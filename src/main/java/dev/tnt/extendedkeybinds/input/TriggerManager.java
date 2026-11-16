package dev.tnt.extendedkeybinds.input;

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

    public static void onPreTick(ClientTickEvent.Pre event) {
        long now = Util.getMillis();
        for (KeyMapping mapping : snapshot) {
            ((InternalKeyMapping) mapping).extendedkeybinds$preTick(now);
        }
    }

    public static void onPostTick(ClientTickEvent.Post event) {
        for (KeyMapping mapping : snapshot) {
            ((InternalKeyMapping) mapping).extendedkeybinds$postTick();
        }
    }
}
