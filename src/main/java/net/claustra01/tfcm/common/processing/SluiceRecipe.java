package net.claustra01.tfcm.common.processing;

import java.util.ArrayList;
import java.util.List;

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

/** What a Sluice makes of one input: every output is its own independent chance. */
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
                JsonObject entryJson = entry.getAsJsonObject();
                if (!entryJson.get("type").getAsString().equals("minecraft:item")) continue;
                outputs.add(new Output(BuiltInRegistries.ITEM.get(ResourceLocation.parse(
                        entryJson.get("name").getAsString())).getDefaultInstance(), chance));
            }
        }
        return outputs;
    }

    private static float chance(JsonObject pool) {
        if (!pool.has("conditions")) return 1;
        float chance = 1;
        for (JsonElement condition : pool.getAsJsonArray("conditions")) {
            JsonObject json = condition.getAsJsonObject();
            if (json.get("condition").getAsString().equals("minecraft:random_chance"))
                chance *= json.get("chance").getAsFloat();
        }
        return chance;
    }
}
