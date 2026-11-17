package tnt.tarkovcraft.extendedkeybinds.input;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.loading.FMLPaths;
import tnt.tarkovcraft.extendedkeybinds.ExtendedKeybindsClient;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeybindsApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class KeymappingFileStorage {

    private static final Codec<Map<String, InputMode>> CODEC = Codec.unboundedMap(Codec.STRING, InputMode.CODEC);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Map<String, InputMode> MISSING_KEYMAPPINGS = new LinkedHashMap<>();

    private KeymappingFileStorage() {
    }

    private static Path getMappingFilePath() {
        return FMLPaths.CONFIGDIR.get().resolve("extendedkeybinds/key_types.json");
    }

    public static void load(KeyMapping[] keyMappings) {
        Path path = getMappingFilePath();
        // add default inputs
        for (KeyMapping mapping : keyMappings) {
            ExtendedKeyMapping extended = (ExtendedKeyMapping) mapping;
            extended.extendedkeybinds$setInputMode(extended.extendedkeybinds$getDefaultInputMode());
        }
        if (!Files.exists(path)) {
            return;
        }

        JsonElement json;
        try {
            String raw = Files.readString(path);
            json = GSON.toJsonTree(raw);
        } catch (Exception e) {
            ExtendedKeybindsClient.LOGGER.error("Failed to read {}, using defaults", path, e);
            return;
        }

        DataResult<Map<String, InputMode>> result = CODEC.parse(JsonOps.COMPRESSED, json);
        var saveData = result.getOrThrow();
        for (var entry : saveData.entrySet()) {
            String keyName = entry.getKey();
            KeyMapping mapping = KeyMapping.get(keyName);
            InputMode inputMode = entry.getValue();
            if (mapping != null) {
                ExtendedKeybindsApi.setInputMode(mapping, inputMode);
            } else {
                MISSING_KEYMAPPINGS.put(keyName, inputMode);
            }
        }
    }

    public static void save(KeyMapping[] keyMappings) {
        Map<String, InputMode> saveData = new LinkedHashMap<>();
        // Save registered mappings if mode is different from default
        for (KeyMapping mapping : keyMappings) {
            InputMode defaultMode = ExtendedKeybindsApi.getDefaultInputMode(mapping);
            InputMode configuredMode = ExtendedKeybindsApi.getInputMode(mapping);
            if (configuredMode != defaultMode) {
                saveData.put(mapping.getName(), configuredMode);
            }
        }
        // Save unidentified entries. In theory can save default modes, but it is not a big issue
        Set<String> recovered = new HashSet<>();
        MISSING_KEYMAPPINGS.forEach((name, mode) -> {
            InputMode existing = saveData.putIfAbsent(name, mode);
            if (existing != null) { // previously missing keybinding identified, mark for removal
                recovered.add(name);
            }
        });
        recovered.forEach(MISSING_KEYMAPPINGS::remove); // clear recovered keybinds

        DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.COMPRESSED, saveData);
        JsonElement json = result.getOrThrow();
        Path file = getMappingFilePath();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(json), StandardCharsets.UTF_8);
        } catch (IOException e) {
            ExtendedKeybindsClient.LOGGER.error("Failed to save {}", file, e);
        }
    }
}
