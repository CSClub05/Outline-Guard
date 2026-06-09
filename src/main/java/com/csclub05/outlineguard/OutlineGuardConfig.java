package com.csclub05.outlineguard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class OutlineGuardConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Identifier DEFAULT_MARKER = Identifier.of("minecraft", "netherrack");

    private final Set<Identifier> markerBlocks = new LinkedHashSet<>();
    private Path configPath;

    public void load() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("outlineguard-client.json");
        this.markerBlocks.clear();

        if (!Files.exists(this.configPath)) {
            this.markerBlocks.add(DEFAULT_MARKER);
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(this.configPath)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root != null && root.has("marker_blocks") && root.get("marker_blocks").isJsonArray()) {
                JsonArray array = root.getAsJsonArray("marker_blocks");
                for (JsonElement element : array) {
                    if (!element.isJsonPrimitive()) {
                        continue;
                    }
                    Identifier id = Identifier.tryParse(element.getAsString());
                    if (id != null && Registries.BLOCK.containsId(id)) {
                        this.markerBlocks.add(id);
                    }
                }
            }
        } catch (Exception exception) {
            OutlineGuard.LOGGER.warn("Failed to read Outline Guard client config. Recreating defaults.", exception);
        }

        if (this.markerBlocks.isEmpty()) {
            this.markerBlocks.add(DEFAULT_MARKER);
            save();
        }
    }

    public void save() {
        if (this.configPath == null) {
            this.configPath = FabricLoader.getInstance().getConfigDir().resolve("outlineguard-client.json");
        }

        JsonObject root = new JsonObject();
        JsonArray array = new JsonArray();
        for (Identifier id : this.markerBlocks) {
            array.add(id.toString());
        }
        root.add("marker_blocks", array);

        try {
            Files.createDirectories(this.configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(this.configPath)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException exception) {
            OutlineGuard.LOGGER.error("Failed to save Outline Guard client config.", exception);
        }
    }

    public boolean setOnlyMarker(Identifier id) {
        if (!Registries.BLOCK.containsId(id)) {
            return false;
        }
        this.markerBlocks.clear();
        this.markerBlocks.add(id);
        save();
        return true;
    }

    public boolean addMarker(Identifier id) {
        if (!Registries.BLOCK.containsId(id)) {
            return false;
        }
        boolean changed = this.markerBlocks.add(id);
        if (changed) {
            save();
        }
        return changed;
    }

    public boolean removeMarker(Identifier id) {
        boolean changed = this.markerBlocks.remove(id);
        if (changed) {
            save();
        }
        return changed;
    }

    public boolean isMarker(Block block) {
        Identifier id = Registries.BLOCK.getId(block);
        return this.markerBlocks.contains(id);
    }

    public List<Identifier> markerBlocks() {
        return new ArrayList<>(this.markerBlocks);
    }
}
