package tnt.tarkovcraft.extendedkeybinds.input;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import tnt.tarkovcraft.extendedkeybinds.ExtendedKeybindsClient;
import tnt.tarkovcraft.extendedkeybinds.api.ExtendedKeyMapping;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public final class KeymappingFileStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String OPTIONS = "options";
    private static final String KEYMAP = "keymap";

    private static InputSettings inputSettings = InputSettings.DEFAULT;
    private static final Map<String, JsonElement> UNKNOWN_ENTRIES = new LinkedHashMap<>();

    private KeymappingFileStorage() {
    }

    public static InputSettings getInputSettings() {
        return inputSettings;
    }

    public static Path getConfigFile() {
        return FMLPaths.CONFIGDIR.get().resolve("extendedkeybinds.json");
    }

    public static void load(KeyMapping[] keyMappings) {
        Path file = getConfigFile();
        inputSettings = InputSettings.DEFAULT;
        UNKNOWN_ENTRIES.clear();
        for (KeyMapping mapping : keyMappings) {
            ExtendedKeyMapping extended = (ExtendedKeyMapping) mapping;
            extended.extendedkeybinds$setInputMode(extended.extendedkeybinds$getDefaultInputMode());
        }
        if (!Files.exists(file)) {
            return;
        }
        JsonObject root;
        try {
            root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            ExtendedKeybindsClient.LOGGER.error("Failed to read {}, using defaults", file, e);
            backupBrokenFile(file);
            return;
        }

        JsonElement options = root.get(OPTIONS);
        if (options != null) {
            inputSettings = InputSettings.CODEC.parse(JsonOps.INSTANCE, options)
                    .resultOrPartial(error -> ExtendedKeybindsClient.LOGGER.warn("Invalid input options in {}: {}", file, error))
                    .orElse(InputSettings.DEFAULT);
        }

        JsonElement keymapElement = root.get(KEYMAP);
        if (keymapElement == null || !keymapElement.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : keymapElement.getAsJsonObject().entrySet()) {
            String name = entry.getKey();
            KeyMapping mapping = KeyMapping.get(name);
            if (mapping == null) {
                UNKNOWN_ENTRIES.put(name, entry.getValue().deepCopy());
                continue;
            }
            KeybindSettings.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> ExtendedKeybindsClient.LOGGER.warn("Invalid settings for key mapping {} in {}: {}", name, file, error))
                    .ifPresent(settings -> ((ExtendedKeyMapping) mapping).extendedkeybinds$setInputMode(settings.inputMode()));
        }
    }

    public static void save(KeyMapping[] keyMappings) {
        JsonObject keymap = new JsonObject();
        UNKNOWN_ENTRIES.forEach((name, json) -> keymap.add(name, json.deepCopy()));
        for (KeyMapping mapping : keyMappings) {
            ExtendedKeyMapping extended = (ExtendedKeyMapping) mapping;
            InputMode mode = extended.extendedkeybinds$getInputMode();
            if (mode != extended.extendedkeybinds$getDefaultInputMode()) {
                keymap.add(mapping.getName(), KeybindSettings.CODEC.encodeStart(JsonOps.INSTANCE, new KeybindSettings(mode)).getOrThrow());
            }
        }
        JsonObject root = new JsonObject();
        root.add(OPTIONS, InputSettings.CODEC.encodeStart(JsonOps.INSTANCE, inputSettings).getOrThrow());
        root.add(KEYMAP, keymap);

        Path file = getConfigFile();
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            ExtendedKeybindsClient.LOGGER.error("Failed to save {}", file, e);
        }
    }

    private static void backupBrokenFile(Path file) {
        Path backup = file.resolveSibling(file.getFileName() + ".broken");
        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            ExtendedKeybindsClient.LOGGER.warn("Moved unreadable {} to {}", file, backup);
        } catch (IOException e) {
            ExtendedKeybindsClient.LOGGER.error("Failed to back up unreadable {}", file, e);
        }
    }
}
