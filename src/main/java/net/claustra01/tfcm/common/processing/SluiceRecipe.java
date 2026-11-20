package net.claustra01.tfcm.common.processing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.dries007.tfc.util.data.Deposit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootTable;

/** What a Sluice makes of one input: outputs and their chances in the Sluice loot context. */
public record SluiceRecipe(Ingredient input, List<Output> outputs) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SluiceRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, SluiceRecipe::input,
            Output.STREAM_CODEC.apply(ByteBufCodecs.list()), SluiceRecipe::outputs,
            SluiceRecipe::new);

    public record Output(ItemStack stack, float chance) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Output> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, Output::stack,
                ByteBufCodecs.FLOAT, Output::chance,
                Output::new);
    }

    /** Reads every Deposit's loot table as the server loaded it, data packs included. */
    public static List<SluiceRecipe> build(MinecraftServer server) {
        var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        List<SluiceRecipe> recipes = new ArrayList<>();
        for (Deposit deposit : Deposit.MANAGER.getValues()) {
            LootTable table = server.reloadableRegistries().getLootTable(deposit.lootTable());
            LootTable.DIRECT_CODEC.encodeStart(ops, table).ifSuccess(json -> {
                List<Output> outputs = outputs(json.getAsJsonObject());
                if (!outputs.isEmpty() && !deposit.ingredient().isEmpty())
                    recipes.add(new SluiceRecipe(deposit.ingredient(), outputs));
            });
        }
        return List.copyOf(recipes);
    }

    private static List<Output> outputs(JsonObject table) {
        List<Output> outputs = new ArrayList<>();
        if (!table.has("pools")) return outputs;
        for (JsonElement pool : table.getAsJsonArray("pools")) {
            JsonObject poolJson = pool.getAsJsonObject();
            float chance = chance(poolJson);
            for (JsonElement entry : poolJson.getAsJsonArray("entries")) {
                Map<ResourceLocation, Float> items = new LinkedHashMap<>();
                collectEntry(entry.getAsJsonObject(), chance, items);
                items.forEach((id, probability) -> outputs.add(
                        new Output(BuiltInRegistries.ITEM.get(id).getDefaultInstance(), probability)));
            }
        }
        return outputs;
    }

    /** Returns the probability that this entry succeeds, conditional on reaching it. */
    private static float collectEntry(JsonObject entry, float reach, Map<ResourceLocation, Float> items) {
        float success = chance(entry);
        if (reach == 0 || success == 0) return 0;
        switch (entry.get("type").getAsString()) {
            case "minecraft:item" -> items.merge(ResourceLocation.parse(entry.get("name").getAsString()),
                    reach * success, Float::sum);
            case "minecraft:alternatives" -> {
                float remaining = 1;
                for (JsonElement child : entry.getAsJsonArray("children")) {
                    float childSuccess = collectEntry(child.getAsJsonObject(), reach * success * remaining, items);
                    remaining *= 1 - childSuccess;
                    if (remaining == 0) break;
                }
                return success * (1 - remaining);
            }
        }
        // An empty entry can also succeed and prevent later alternatives from being evaluated.
        return success;
    }

    private static float chance(JsonObject node) {
        if (!node.has("conditions")) return 1;
        float chance = 1;
        for (JsonElement condition : node.getAsJsonArray("conditions"))
            chance *= conditionChance(condition.getAsJsonObject());
        return chance;
    }

    private static float conditionChance(JsonObject condition) {
        return switch (condition.get("condition").getAsString()) {
            case "minecraft:random_chance" -> condition.get("chance").getAsFloat();
            case "tfc:is_sluice" -> 1;
            case "minecraft:inverted" -> 1 - conditionChance(condition.getAsJsonObject("term"));
            default -> 1;
        };
    }
}
