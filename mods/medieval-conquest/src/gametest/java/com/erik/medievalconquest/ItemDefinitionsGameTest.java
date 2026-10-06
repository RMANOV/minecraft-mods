package com.erik.medievalconquest;

import com.erik.medievalconquest.registry.ModAnomalyBlocks;
import com.erik.medievalconquest.registry.ModCreativeTab;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Registry-driven asset audit: every item, block and entity the running mod registers must
 * render with its own model and original texture, show a translated name and sit in the mod tab.
 */
public final class ItemDefinitionsGameTest {
    private static final String NS = MedievalConquestMod.MOD_ID;
    /** Vanilla parent models that exist in the 1.21.11 client assets (template_spawn_egg does not). */
    private static final Set<String> VANILLA_PARENTS = Set.of(
            "minecraft:block/cube_all", "minecraft:block/cube_column", "minecraft:block/cube_column_horizontal",
            "minecraft:block/leaves", "minecraft:block/cross", "minecraft:item/generated", "minecraft:item/handheld");

    private static final class Assets {
        private final ModContainer mod = FabricLoader.getInstance().getModContainer(NS).orElseThrow();

        Optional<Path> path(String relative) {
            return mod.findPath("assets/" + NS + "/" + relative).filter(Files::exists);
        }

        Optional<JsonObject> json(String relative) {
            return path(relative).map(file -> {
                try {
                    JsonElement value = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                    return value.isJsonObject() ? value.getAsJsonObject() : null;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }

        byte[] bytes(Path file) {
            try {
                return Files.readAllBytes(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /** Resolves a model's parent chain and returns the original texture files it draws with. */
    private static Map<String, Path> textures(Assets assets, String modelId, String owner, List<String> problems) {
        Map<String, String> variables = new HashMap<>();
        String current = modelId;
        for (int depth = 0; current != null; depth++) {
            if (depth > 8) {
                problems.add(owner + ": model parent chain too deep at " + current);
                return Map.of();
            }
            Identifier id = Identifier.parse(current);
            if (!id.getNamespace().equals(NS)) {
                if (!VANILLA_PARENTS.contains(id.toString()))
                    problems.add(owner + ": model " + id + " is not a mod model or a known vanilla parent");
                break;
            }
            Optional<JsonObject> model = assets.json("models/" + id.getPath() + ".json");
            if (model.isEmpty()) {
                problems.add(owner + ": missing model models/" + id.getPath() + ".json");
                return Map.of();
            }
            if (model.get().has("textures"))
                for (var entry : model.get().getAsJsonObject("textures").entrySet())
                    variables.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
            current = model.get().has("parent") ? model.get().get("parent").getAsString() : null;
        }
        Map<String, Path> files = new LinkedHashMap<>();
        for (var entry : variables.entrySet()) {
            String value = entry.getValue();
            for (int hops = 0; value.startsWith("#") && hops < 8; hops++)
                value = variables.getOrDefault(value.substring(1), "#");
            if (value.startsWith("#")) {
                problems.add(owner + ": unresolved texture variable " + entry.getKey());
                continue;
            }
            Identifier texture = Identifier.parse(value);
            if (!texture.getNamespace().equals(NS)) {
                problems.add(owner + ": borrows non-original texture " + texture);
                continue;
            }
            Optional<Path> png = assets.path("textures/" + texture.getPath() + ".png");
            if (png.isEmpty()) {
                problems.add(owner + ": missing texture textures/" + texture.getPath() + ".png");
                continue;
            }
            byte[] data = assets.bytes(png.get());
            boolean header = data.length > 24 && (data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N'
                    && data[3] == 'G' && data[12] == 'I' && data[13] == 'H' && data[14] == 'D' && data[15] == 'R';
            if (!header || readInt(data, 16) != 16 || readInt(data, 20) != 16)
                problems.add(owner + ": texture " + texture + " is not a 16x16 PNG");
            files.put(texture.toString(), png.get());
        }
        if (files.isEmpty() && variables.isEmpty())
            problems.add(owner + ": model " + modelId + " defines no texture");
        return files;
    }

    private static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
    }

    private static List<String> blockModels(JsonObject blockstate) {
        List<String> models = new ArrayList<>();
        List<JsonElement> entries = new ArrayList<>();
        if (blockstate.has("variants"))
            blockstate.getAsJsonObject("variants").entrySet().forEach(e -> entries.add(e.getValue()));
        if (blockstate.has("multipart"))
            blockstate.getAsJsonArray("multipart").forEach(part -> entries.add(part.getAsJsonObject().get("apply")));
        for (JsonElement entry : entries) {
            if (entry.isJsonArray()) entry.getAsJsonArray().forEach(e -> models.add(e.getAsJsonObject().get("model").getAsString()));
            else models.add(entry.getAsJsonObject().get("model").getAsString());
        }
        return models;
    }

    private static Set<Item> buildTab(GameTestHelper h) {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(ModCreativeTab.KEY);
        h.assertTrue(tab != null, "mod creative tab is registered as " + ModCreativeTab.KEY);
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(
                h.getLevel().enabledFeatures(), true, h.getLevel().registryAccess()));
        return tab.getDisplayItems().stream().map(ItemStack::getItem).collect(Collectors.toSet());
    }

    private static void assertNoProblems(GameTestHelper h, List<String> problems, int checked) {
        h.assertTrue(checked > 0, "registry audit must check at least one mod entry");
        h.assertTrue(problems.isEmpty(), problems.size() + " asset problems: " + String.join(" | ", problems));
    }

    @GameTest
    public void everyRegisteredItemHasDefinitionModelTextureLangAndTab(GameTestHelper h) {
        var assets = new Assets();
        JsonObject lang = assets.json("lang/en_us.json").orElseThrow();
        Set<Item> tab = buildTab(h);
        List<String> problems = new ArrayList<>();
        Set<String> registered = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals(NS)) continue;
            registered.add(id.getPath());
            String owner = "item " + id;
            Optional<JsonObject> definition = assets.json("items/" + id.getPath() + ".json");
            if (definition.isEmpty()) problems.add(owner + ": missing items/" + id.getPath() + ".json");
            else {
                JsonObject model = definition.get().getAsJsonObject("model");
                if (model == null || !"minecraft:model".equals(model.get("type").getAsString()))
                    problems.add(owner + ": item definition is not a plain minecraft:model");
                else textures(assets, model.get("model").getAsString(), owner, problems);
            }
            String key = item.getDescriptionId();
            if (!lang.has(key) || lang.get(key).getAsString().isBlank())
                problems.add(owner + ": no en_us name for " + key);
            if (!tab.contains(item)) problems.add(owner + ": missing from the mod creative tab");
        }
        try (Stream<Path> files = Files.list(assets.path("items").orElseThrow())) {
            files.map(file -> file.getFileName().toString().replaceFirst("\\.json$", ""))
                    .filter(name -> !registered.contains(name))
                    .forEach(name -> problems.add("items/" + name + ".json has no registered item"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertNoProblems(h, problems, registered.size());
        h.succeed();
    }

    @GameTest
    public void everyRegisteredBlockAndEntityRendersWithTranslatedName(GameTestHelper h) {
        var assets = new Assets();
        JsonObject lang = assets.json("lang/en_us.json").orElseThrow();
        List<String> problems = new ArrayList<>();
        int checked = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (!id.getNamespace().equals(NS)) continue;
            checked++;
            String owner = "block " + id;
            Optional<JsonObject> blockstate = assets.json("blockstates/" + id.getPath() + ".json");
            if (blockstate.isEmpty()) problems.add(owner + ": missing blockstates/" + id.getPath() + ".json");
            else {
                List<String> models = blockModels(blockstate.get());
                if (models.isEmpty()) problems.add(owner + ": blockstate names no model");
                models.forEach(model -> textures(assets, model, owner, problems));
            }
            if (!lang.has(block.getDescriptionId())) problems.add(owner + ": no en_us name for " + block.getDescriptionId());
        }
        for (var type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals(NS)) continue;
            checked++;
            if (!lang.has(type.getDescriptionId())) problems.add("entity " + id + ": no en_us name");
        }
        var title = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(ModCreativeTab.KEY).getDisplayName().getContents();
        if (!(title instanceof TranslatableContents translatable) || !lang.has(translatable.getKey()))
            problems.add("creative tab title is not a translated en_us key");
        assertNoProblems(h, problems, checked);
        h.succeed();
    }

    @GameTest
    public void icyAndBarrierBlocksUseThreeDistinctOriginalTextures(GameTestHelper h) {
        var assets = new Assets();
        List<String> problems = new ArrayList<>();
        Set<String> digests = new TreeSet<>();
        Set<String> textureIds = new HashSet<>();
        int files = 0;
        for (Block block : List.of(ModAnomalyBlocks.ICY_OBSIDIAN, ModAnomalyBlocks.TEMPORARY_BARRIER,
                ModAnomalyBlocks.PERMANENT_BARRIER)) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            JsonObject blockstate = assets.json("blockstates/" + id.getPath() + ".json").orElseThrow();
            for (String model : blockModels(blockstate))
                for (var texture : textures(assets, model, "block " + id, problems).entrySet()) {
                    files++;
                    textureIds.add(texture.getKey());
                    digests.add(sha256(assets.bytes(texture.getValue())));
                }
        }
        h.assertTrue(problems.isEmpty(), String.join(" | ", problems));
        h.assertTrue(files == 3 && textureIds.size() == 3 && digests.size() == 3,
                "icy obsidian, temporary and permanent barrier need three different texture images, got "
                        + textureIds + " with " + digests.size() + " distinct contents");
        h.succeed();
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
