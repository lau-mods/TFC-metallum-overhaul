package net.claustra01.tfcm.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.claustra01.tfcm.worldgen.WorldgenDefinition.Dimension;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.Vein;
import net.claustra01.tfcm.worldgen.WorldgenDefinition.WeightedBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * What recipe viewers show of one YAML vein. {@link #rocks()} and every {@link Ore#stacks()} are aligned by index,
 * so the ore shown for a rock is the block that replaces that rock.
 */
public record VeinDisplay(String dimension, String id, String type, int minY, int maxY, int rarity, float density,
        int size, int height, int radius, List<ItemStack> rocks, List<Ore> ores, Optional<Indicator> indicator) {
    private static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> STACKS =
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list());
    public static final StreamCodec<RegistryFriendlyByteBuf, VeinDisplay> STREAM_CODEC =
            StreamCodec.of(VeinDisplay::encode, VeinDisplay::decode);

    /** One weighted block of the vein, across all rocks, with its share of the vein's total weight. */
    public record Ore(List<ItemStack> stacks, float chance) {
        static final StreamCodec<RegistryFriendlyByteBuf, Ore> STREAM_CODEC = StreamCodec.composite(
                STACKS, Ore::stacks,
                ByteBufCodecs.FLOAT, Ore::chance,
                Ore::new);
    }

    public record Indicator(List<ItemStack> stacks, int rarity, int depth, int undergroundRarity,
            int undergroundCount) {
        static final StreamCodec<RegistryFriendlyByteBuf, Indicator> STREAM_CODEC = StreamCodec.composite(
                STACKS, Indicator::stacks,
                ByteBufCodecs.VAR_INT, Indicator::rarity,
                ByteBufCodecs.VAR_INT, Indicator::depth,
                ByteBufCodecs.VAR_INT, Indicator::undergroundRarity,
                ByteBufCodecs.VAR_INT, Indicator::undergroundCount,
                Indicator::new);
    }

    /** Every vein of every dimension in file order; veins whose blocks have no item are left out. */
    public static List<VeinDisplay> build(Map<String, Dimension> definitions, MinecraftServer server) {
        List<VeinDisplay> result = new ArrayList<>();
        definitions.forEach((dimension, definition) -> definition.veins()
                .forEach(vein -> of(dimension, vein, server).ifPresent(result::add)));
        return List.copyOf(result);
    }

    static Optional<VeinDisplay> of(String dimension, Vein vein, MinecraftServer server) {
        List<String> rockIds = List.copyOf(vein.blocksByRock().keySet());
        List<List<WeightedBlock>> outputs = rockIds.stream().map(vein.blocksByRock()::get).toList();
        if (outputs.isEmpty()) return Optional.empty();
        // {rock} and {tier} expansion gives every rock the same entries in the same order and weights
        List<WeightedBlock> first = outputs.getFirst();
        double total = first.stream().mapToDouble(WeightedBlock::weight).sum();
        List<Integer> columns = IntStream.range(0, first.size())
                .filter(column -> outputs.stream().anyMatch(row -> !stack(row.get(column).block()).isEmpty()))
                .boxed().toList();
        List<Integer> rows = IntStream.range(0, rockIds.size())
                .filter(row -> !stack(rockIds.get(row)).isEmpty() && columns.stream()
                        .allMatch(column -> !stack(outputs.get(row).get(column).block()).isEmpty()))
                .boxed().toList();
        if (columns.isEmpty() || rows.isEmpty()) return Optional.empty();

        List<ItemStack> rocks = rows.stream().map(row -> stack(rockIds.get(row))).toList();
        List<Ore> ores = columns.stream().map(column -> new Ore(rows.stream()
                .map(row -> stack(outputs.get(row).get(column).block())).toList(),
                (float) (first.get(column).weight() / total))).toList();
        Optional<Indicator> indicator = Optional.ofNullable(vein.indicator()).map(value -> new Indicator(
                value.blocks().stream().map(block -> indicator(block.block(), server))
                        .filter(stack -> !stack.isEmpty()).toList(),
                value.rarity(), value.depth(), value.undergroundRarity(), value.undergroundCount()));
        return Optional.of(new VeinDisplay(dimension, vein.id(), vein.type(), vein.minY(), vein.maxY(),
                vein.rarity(), vein.density(), vein.size(), vein.height(), vein.radius(), rocks, ores, indicator));
    }

    private static ItemStack stack(String block) {
        return new ItemStack(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block)).asItem());
    }

    /** Indicators without an item, such as TFCM's ungraded samples, are shown by the first item they drop. */
    private static ItemStack indicator(String block, MinecraftServer server) {
        ItemStack stack = stack(block);
        if (!stack.isEmpty()) return stack;
        LootTable table = server.reloadableRegistries().getLootTable(
                BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block)).getLootTable());
        var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        return LootTable.DIRECT_CODEC.encodeStart(ops, table).result()
                .map(json -> firstItem(json.getAsJsonObject())).orElse(ItemStack.EMPTY);
    }

    private static ItemStack firstItem(JsonObject table) {
        if (!table.has("pools")) return ItemStack.EMPTY;
        for (JsonElement pool : table.getAsJsonArray("pools")) {
            for (JsonElement entry : pool.getAsJsonObject().getAsJsonArray("entries")) {
                JsonObject json = entry.getAsJsonObject();
                if (!json.get("type").getAsString().equals("minecraft:item")) continue;
                ItemStack drop = BuiltInRegistries.ITEM.get(ResourceLocation.parse(json.get("name").getAsString()))
                        .getDefaultInstance();
                if (!drop.isEmpty()) return drop;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void encode(RegistryFriendlyByteBuf buffer, VeinDisplay value) {
        buffer.writeUtf(value.dimension);
        buffer.writeUtf(value.id);
        buffer.writeUtf(value.type);
        buffer.writeVarInt(value.minY);
        buffer.writeVarInt(value.maxY);
        buffer.writeVarInt(value.rarity);
        buffer.writeFloat(value.density);
        buffer.writeVarInt(value.size);
        buffer.writeVarInt(value.height);
        buffer.writeVarInt(value.radius);
        STACKS.encode(buffer, value.rocks);
        Ore.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, value.ores);
        ByteBufCodecs.optional(Indicator.STREAM_CODEC).encode(buffer, value.indicator);
    }

    private static VeinDisplay decode(RegistryFriendlyByteBuf buffer) {
        return new VeinDisplay(buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readFloat(), buffer.readVarInt(),
                buffer.readVarInt(), buffer.readVarInt(), STACKS.decode(buffer),
                Ore.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer),
                ByteBufCodecs.optional(Indicator.STREAM_CODEC).decode(buffer));
    }
}
