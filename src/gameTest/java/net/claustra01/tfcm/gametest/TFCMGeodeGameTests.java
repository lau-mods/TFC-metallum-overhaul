package net.claustra01.tfcm.gametest;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.block.geode.GeodeMineral;
import net.claustra01.tfcm.common.block.geode.MineralBuddingBlock;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMGeodeGameTests {
    private static final String BATCH = "tfcm_foundation";

    private TFCMGeodeGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void threeMineralsHaveEveryBlockItemModelAndDrop(GameTestHelper helper) {
        helper.succeedIf(() -> {
            require(TFCMBlocks.MINERAL_BLOCKS.size() == 3 && TFCMBlocks.BUDDING_BLOCKS.size() == 3
                    && TFCMBlocks.SMALL_BUDS.size() == 3 && TFCMBlocks.MEDIUM_BUDS.size() == 3
                    && TFCMBlocks.LARGE_BUDS.size() == 3 && TFCMBlocks.CLUSTERS.size() == 3,
                    "Every geode mineral must register six blocks");
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            BlockPos position = new BlockPos(1, 2, 1);
            helper.setBlock(position.below(), Blocks.STONE);
            for (GeodeMineral mineral : GeodeMineral.values()) {
                List<Block> blocks = List.of(TFCMBlocks.MINERAL_BLOCKS.get(mineral).get(),
                        TFCMBlocks.BUDDING_BLOCKS.get(mineral).get(), TFCMBlocks.SMALL_BUDS.get(mineral).get(),
                        TFCMBlocks.MEDIUM_BUDS.get(mineral).get(), TFCMBlocks.LARGE_BUDS.get(mineral).get(),
                        TFCMBlocks.CLUSTERS.get(mineral).get());
                for (Block block : blocks) {
                    ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
                    require(BuiltInRegistries.ITEM.containsKey(id) && block.asItem() != Items.AIR,
                            "Geode block has no BlockItem: " + id);
                    require(resourceExists("/assets/tfcm/blockstates/" + id.getPath() + ".json"),
                            "Geode block has no blockstate: " + id);
                    require(resourceExists("/assets/tfcm/models/block/" + id.getPath() + ".json"),
                            "Geode block has no block model: " + id);
                    require(resourceExists("/assets/tfcm/models/item/" + id.getPath() + ".json"),
                            "Geode block has no item model: " + id);
                    helper.setBlock(position, Blocks.AIR);
                    ItemStack blockItem = block.asItem().getDefaultInstance();
                    player.setItemInHand(InteractionHand.MAIN_HAND, blockItem);
                    helper.placeAt(player, blockItem, position.below(), Direction.UP);
                    require(helper.getBlockState(position).is(block), "Geode BlockItem did not place its block: " + id);
                    ItemStack drop = singleDrop(helper, position, block);
                    Item expected = block == TFCMBlocks.CLUSTERS.get(mineral).get()
                            ? BuiltInRegistries.ITEM.get(ResourceLocation.parse(mineral.clusterDrop())) : block.asItem();
                    int count = block == TFCMBlocks.CLUSTERS.get(mineral).get() ? 2 : 1;
                    require(drop.is(expected) && drop.getCount() == count, "Geode block has the wrong drop: " + id);
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void geodesUseSpecifiedLayersPlacementGrowthAndDrops(GameTestHelper helper) {
        helper.succeedIf(() -> {
            var configuredFeatures = helper.getLevel().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
            var placedFeatures = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
            for (GeodeMineral mineral : GeodeMineral.values()) {
                ResourceLocation id = id(mineral.id() + "_geode");
                var configured = configuredFeatures.get(id);
                require(configured != null && configured.config() instanceof GeodeConfiguration,
                        "Configured geode is not loaded: " + id);
                GeodeConfiguration geode = (GeodeConfiguration) configured.config();
                require(providerBlock(geode.geodeBlockSettings.outerLayerProvider).equals("tfc:rock/hardened/basalt"),
                        "Geode outer layer is not hardened basalt");
                require(providerBlock(geode.geodeBlockSettings.middleLayerProvider).equals("tfc:rock/raw/quartzite"),
                        "Geode middle layer is not raw quartzite");
                JsonObject inner = encode(BlockStateProvider.CODEC, geode.geodeBlockSettings.innerLayerProvider);
                var entries = inner.getAsJsonArray("entries");
                require(entries.size() == 3 && entries.get(0).getAsJsonObject().get("weight").getAsInt() == 2
                        && entries.get(1).getAsJsonObject().get("weight").getAsInt() == 5
                        && entries.get(2).getAsJsonObject().get("weight").getAsInt() == 1
                        && weightedBlock(entries.get(0)).equals("tfcm:mineral/budding_" + mineral.id())
                        && weightedBlock(entries.get(1)).equals("tfcm:mineral/" + mineral.id() + "_block")
                        && weightedBlock(entries.get(2)).equals("tfc:rock/raw/quartzite"),
                        "Inner layer does not use budding:mineral:quartzite weights 2:5:1");
                require(!geode.placementsRequireLayer0Alternate
                        && providerBlock(geode.geodeBlockSettings.alternateInnerLayerProvider)
                                .equals("tfcm:mineral/budding_" + mineral.id()),
                        "Geode bud placement must use the weighted inner layer without an alternate-layer requirement");
                var placed = placedFeatures.get(id);
                require(placed != null, "Placed geode is not loaded: " + id);
                RarityFilter rarity = modifier(placed.placement(), RarityFilter.class);
                JsonObject rarityData = encode(PlacementModifier.CODEC, rarity);
                require(rarityData.get("chance").getAsInt() == 300,
                        "Geode rarity is not one in 300 chunks");
                HeightRangePlacement height = modifier(placed.placement(), HeightRangePlacement.class);
                JsonObject range = encode(PlacementModifier.CODEC, height).getAsJsonObject("height");
                require(range.getAsJsonObject("min_inclusive").get("absolute").getAsInt() == -48
                        && range.getAsJsonObject("max_inclusive").get("absolute").getAsInt() == 32,
                        "Geode height is not Y -48 through 32");
                verifyGrowth(helper, mineral);
            }
            var modifiers = helper.getLevel().registryAccess().registryOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS);
            var modifier = modifiers.get(id("add_geodes"));
            require(modifier instanceof BiomeModifiers.AddFeaturesBiomeModifier,
                    "All three geodes are not attached to TFC overworld biomes");
            var addFeatures = (BiomeModifiers.AddFeaturesBiomeModifier) modifier;
            require(addFeatures.biomes().unwrapKey().isPresent()
                    && addFeatures.biomes().unwrapKey().get().location().equals(id("tfc_overworld"))
                    && addFeatures.biomes().size() > 0
                    && addFeatures.features().size() == 3
                    && addFeatures.step() == GenerationStep.Decoration.UNDERGROUND_ORES,
                    "Geode biome tag, feature count, or generation step is wrong");
            for (GeodeMineral mineral : GeodeMineral.values()) {
                ResourceLocation feature = id(mineral.id() + "_geode");
                require(addFeatures.features().stream().anyMatch(holder -> holder.is(feature)),
                        "Biome modifier is missing placed feature " + feature);
            }
        });
    }

    private static void verifyGrowth(GameTestHelper helper, GeodeMineral mineral) {
        BlockPos buddingPosition = helper.absolutePos(new BlockPos(2 + mineral.ordinal() * 2, 1, 2));
        BlockPos growthPosition = buddingPosition.above();
        MineralBuddingBlock budding = TFCMBlocks.BUDDING_BLOCKS.get(mineral).get();
        helper.getLevel().setBlockAndUpdate(buddingPosition, budding.defaultBlockState());
        for (Block expected : List.of(TFCMBlocks.SMALL_BUDS.get(mineral).get(),
                TFCMBlocks.MEDIUM_BUDS.get(mineral).get(), TFCMBlocks.LARGE_BUDS.get(mineral).get(),
                TFCMBlocks.CLUSTERS.get(mineral).get())) {
            require(budding.grow(helper.getLevel(), growthPosition, Direction.UP),
                    "Budding block did not advance a growth stage");
            require(helper.getLevel().getBlockState(growthPosition).is(expected),
                    "Budding block advanced to the wrong growth stage");
        }
        require(helper.getLevel().getBlockState(growthPosition).getValue(AmethystClusterBlock.FACING)
                == Direction.UP, "Grown cluster faces away from its budding block");
    }

    private static ItemStack singleDrop(GameTestHelper helper, BlockPos relativePosition, Block block) {
        BlockPos position = helper.absolutePos(relativePosition);
        List<ItemStack> drops = Block.getDrops(helper.getLevel().getBlockState(position), helper.getLevel(), position,
                null, null,
                Items.DIAMOND_PICKAXE.getDefaultInstance());
        require(drops.size() == 1, "Expected one geode block drop stack");
        return drops.getFirst();
    }

    private static String providerBlock(BlockStateProvider provider) {
        return BuiltInRegistries.BLOCK.getKey(provider.getState(RandomSource.create(0), BlockPos.ZERO).getBlock())
                .toString();
    }

    private static String weightedBlock(JsonElement entry) {
        return entry.getAsJsonObject().getAsJsonObject("data").get("Name").getAsString();
    }

    private static <T extends PlacementModifier> T modifier(List<PlacementModifier> modifiers, Class<T> type) {
        return modifiers.stream().filter(type::isInstance).map(type::cast).findFirst()
                .orElseThrow(() -> new GameTestAssertException("Missing placement modifier " + type.getSimpleName()));
    }

    private static <T> JsonObject encode(Codec<T> codec, T value) {
        JsonElement json = codec.encodeStart(JsonOps.INSTANCE, value)
                .getOrThrow(GameTestAssertException::new);
        return json.getAsJsonObject();
    }

    private static boolean resourceExists(String path) {
        return TFCMGeodeGameTests.class.getResource(path) != null;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, path);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new GameTestAssertException(message);
    }
}
