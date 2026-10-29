package net.claustra01.tfcm.gametest;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.tool.area.AreaBreakHandler;
import net.claustra01.tfcm.common.tool.area.AreaMiningTool;
import net.claustra01.tfcm.common.tool.area.AreaToolMetal;
import net.claustra01.tfcm.common.tool.area.AreaToolType;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.dries007.tfc.common.recipes.KnappingRecipe;
import net.dries007.tfc.util.data.KnappingPattern;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMAreaToolGameTests {
    private static final String BATCH = "tfcm_area_tools";
    private static BlockPos protectedPosition;
    private static int playerSequence;

    static {
        NeoForge.EVENT_BUS.addListener(TFCMAreaToolGameTests::protectCandidate);
    }

    private TFCMAreaToolGameTests() {
    }

    @BeforeBatch(batch = BATCH)
    public static void configureReport(net.minecraft.server.level.ServerLevel level) throws Exception {
        TFCMFoundationGameTests.configureReport(level);
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void miningHammerUsesCenteredPlanesAndFiltersCandidates(GameTestHelper helper) {
        helper.succeedIf(() -> {
            BlockPos origin = helper.absolutePos(new BlockPos(5, 4, 5));
            for (Direction face : Direction.values()) {
                List<BlockPos> positions = AreaBreakHandler.positions(origin, face, AreaToolType.MINING_HAMMER);
                require(positions.size() == 8 && new HashSet<>(positions).size() == 8,
                        "Mining Hammer does not plan exactly eight additional blocks");
                require(positions.stream().allMatch(pos -> planeDistance(origin, pos, face) == 0),
                        "Mining Hammer planned a block outside the hit plane");
            }

            ServerPlayer player = survivalPlayer(helper);
            ItemStack hammer = tool(AreaToolMetal.COPPER, AreaToolType.MINING_HAMMER);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, hammer);
            List<BlockPos> positions = AreaBreakHandler.positions(origin, Direction.NORTH, AreaToolType.MINING_HAMMER);
            positions.forEach(pos -> helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3));
            helper.getLevel().setBlock(positions.get(0), Blocks.DIRT.defaultBlockState(), 3);
            helper.getLevel().setBlock(positions.get(1), Blocks.OBSIDIAN.defaultBlockState(), 3);
            AreaBreakHandler.breakArea(helper.getLevel(), player, hammer, origin, Direction.NORTH,
                    AreaToolType.MINING_HAMMER);
            require(helper.getLevel().getBlockState(positions.get(0)).is(Blocks.DIRT),
                    "Mining Hammer broke a shovel candidate");
            require(helper.getLevel().getBlockState(positions.get(1)).is(Blocks.OBSIDIAN),
                    "Mining Hammer broke a candidate above its tier");
            require(positions.stream().skip(2).allMatch(pos -> helper.getLevel().isEmptyBlock(pos)),
                    "Mining Hammer failed to break valid pickaxe candidates");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void normalPlayerBreakTriggersCenteredAreaMining(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ServerPlayer player = survivalPlayer(helper);
            ItemStack hammer = tool(AreaToolMetal.COPPER, AreaToolType.MINING_HAMMER);
            player.setItemInHand(InteractionHand.MAIN_HAND, hammer);
            BlockPos origin = helper.absolutePos(new BlockPos(5, 4, 5));
            helper.getLevel().setBlock(origin, Blocks.STONE.defaultBlockState(), 3);
            List<BlockPos> candidates = AreaBreakHandler.positions(origin, Direction.NORTH,
                    AreaToolType.MINING_HAMMER);
            candidates.forEach(pos -> helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3));
            lookAtFace(player, origin, Direction.NORTH);
            require(player.gameMode.destroyBlock(origin), "The normal player path did not break the origin");
            require(candidates.stream().allMatch(helper.getLevel()::isEmptyBlock),
                    "The normal player break path did not trigger all eight additional breaks");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void allMetalsRegisterMatchingToolsHeadsAndTiers(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (AreaToolMetal metal : AreaToolMetal.values()) {
                ItemStack repair = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                        metal.namespace(), "metal/ingot/" + metal.id())).getDefaultInstance();
                for (AreaToolType type : AreaToolType.values()) {
                    AreaMiningTool tool = TFCMItems.AREA_TOOLS.get(metal).get(type).get();
                    require(TFCMItems.AREA_TOOL_HEADS.get(metal).get(type).isBound(), "Area tool head is not registered");
                    require(tool.getDefaultInstance().getMaxDamage() == metal.tier().getUses(),
                            "Area tool durability differs from its metal tier");
                    require(close(tool.getTier().getSpeed(), metal.tier().getSpeed() * .4f),
                            "Area tool speed is not forty percent of its metal tier");
                    require(tool.isValidRepairItem(tool.getDefaultInstance(), repair),
                            "Area tool does not use its tier repair ingredient");
                }
                AreaMiningTool mining = TFCMItems.AREA_TOOLS.get(metal).get(AreaToolType.MINING_HAMMER).get();
                AreaMiningTool quarry = TFCMItems.AREA_TOOLS.get(metal).get(AreaToolType.QUARRY_HAMMER).get();
                require(mining.getTier().getUses() == quarry.getTier().getUses()
                        && close(mining.getTier().getSpeed(), quarry.getTier().getSpeed())
                        && mining.getTier().getIncorrectBlocksForDrops().equals(
                                quarry.getTier().getIncorrectBlocksForDrops()),
                        "Quarry Hammer has properties other than the Mining Hammer range");
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void quarryHammerUsesSpecifiedPlanesInEveryDirection(GameTestHelper helper) {
        helper.succeedIf(() -> {
            BlockPos origin = helper.absolutePos(new BlockPos(5, 4, 5));
            for (Direction face : Direction.values()) {
                List<BlockPos> positions = AreaBreakHandler.positions(origin, face, AreaToolType.QUARRY_HAMMER);
                Set<BlockPos> all = new HashSet<>(positions);
                all.add(origin);
                require(positions.size() == 24 && all.size() == 25,
                        "Quarry Hammer wall does not contain 25 unique positions");
                if (face.getAxis().isHorizontal()) {
                    int minY = all.stream().mapToInt(BlockPos::getY).min().orElseThrow();
                    int maxY = all.stream().mapToInt(BlockPos::getY).max().orElseThrow();
                    require(minY == origin.getY() - 1 && maxY == origin.getY() + 3,
                            "Quarry Hammer wall orientation did not keep three above and one below");
                } else {
                    require(positions.stream().allMatch(pos -> pos.getY() == origin.getY()),
                            "Quarry Hammer floor or ceiling is not a centered 5x5");
                }
                require(all.stream().allMatch(pos -> planeDistance(origin, pos, face) == 0),
                        "Quarry Hammer escaped the hit plane");
                ItemStack quarry = tool(AreaToolMetal.COPPER, AreaToolType.QUARRY_HAMMER);
                ServerPlayer player = survivalPlayer(helper);
                player.setItemInHand(InteractionHand.MAIN_HAND, quarry);
                positions.forEach(pos -> helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3));
                AreaBreakHandler.breakArea(helper.getLevel(), player, quarry, origin, face,
                        AreaToolType.QUARRY_HAMMER);
                require(positions.stream().allMatch(helper.getLevel()::isEmptyBlock),
                        "Quarry Hammer did not break all 24 valid candidates for " + face);
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void quarryAndAllToolsRespectCandidateProtection(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ServerPlayer player = survivalPlayer(helper);
            int x = 3;
            for (AreaToolType type : AreaToolType.values()) {
                BlockPos origin = helper.absolutePos(new BlockPos(x, 4, 5));
                ItemStack stack = tool(AreaToolMetal.COPPER, type);
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
                List<BlockPos> candidates = AreaBreakHandler.positions(origin, Direction.NORTH, type);
                candidates.forEach(pos -> helper.getLevel().setBlock(pos,
                        (type == AreaToolType.EXCAVATOR ? Blocks.DIRT : Blocks.STONE).defaultBlockState(), 3));
                protectedPosition = candidates.get(0);
                int damage = stack.getDamageValue();
                AreaBreakHandler.breakArea(helper.getLevel(), player, stack, origin, Direction.NORTH, type);
                require(!helper.getLevel().isEmptyBlock(protectedPosition), "Canceled candidate was changed");
                require(stack.getDamageValue() - damage == candidates.size() - 1,
                        "Canceled candidate consumed durability or another valid candidate was skipped");
                x += 7;
            }
            for (Direction face : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                BlockPos origin = helper.absolutePos(new BlockPos(20, 6, 20));
                ItemStack quarry = tool(AreaToolMetal.COPPER, AreaToolType.QUARRY_HAMMER);
                player.setItemInHand(InteractionHand.MAIN_HAND, quarry);
                List<BlockPos> candidates = AreaBreakHandler.positions(origin, face, AreaToolType.QUARRY_HAMMER);
                candidates.forEach(pos -> helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3));
                protectedPosition = candidates.get(0);
                helper.getLevel().setBlock(candidates.get(1), Blocks.DIRT.defaultBlockState(), 3);
                helper.getLevel().setBlock(candidates.get(2), Blocks.OBSIDIAN.defaultBlockState(), 3);
                AABB dropsArea = new AABB(origin).inflate(5);
                helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropsArea).forEach(ItemEntity::discard);
                int damage = quarry.getDamageValue();
                AreaBreakHandler.breakArea(helper.getLevel(), player, quarry, origin, face,
                        AreaToolType.QUARRY_HAMMER);
                require(!helper.getLevel().isEmptyBlock(protectedPosition),
                        "Quarry Hammer changed a protected candidate for " + face);
                require(helper.getLevel().getBlockState(candidates.get(1)).is(Blocks.DIRT)
                        && helper.getLevel().getBlockState(candidates.get(2)).is(Blocks.OBSIDIAN),
                        "Quarry Hammer changed an inappropriate or over-tier candidate for " + face);
                require(candidates.stream().skip(3).allMatch(helper.getLevel()::isEmptyBlock)
                        && quarry.getDamageValue() - damage == 21,
                        "Quarry Hammer did not apply per-candidate breaking and durability for " + face);
                int drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropsArea).stream()
                        .map(ItemEntity::getItem).filter(stack -> stack.is(Items.COBBLESTONE))
                        .mapToInt(ItemStack::getCount).sum();
                require(drops == 21, "Quarry Hammer did not create normal loot for " + face);
            }
            protectedPosition = null;
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void excavatorOnlyBreaksShovelCandidates(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ServerPlayer player = survivalPlayer(helper);
            ItemStack excavator = tool(AreaToolMetal.COPPER, AreaToolType.EXCAVATOR);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, excavator);
            BlockPos origin = helper.absolutePos(new BlockPos(5, 4, 5));
            List<BlockPos> positions = AreaBreakHandler.positions(origin, Direction.UP, AreaToolType.EXCAVATOR);
            positions.forEach(pos -> helper.getLevel().setBlock(pos, Blocks.DIRT.defaultBlockState(), 3));
            helper.getLevel().setBlock(positions.get(0), Blocks.STONE.defaultBlockState(), 3);
            AreaBreakHandler.breakArea(helper.getLevel(), player, excavator, origin, Direction.UP,
                    AreaToolType.EXCAVATOR);
            require(helper.getLevel().getBlockState(positions.get(0)).is(Blocks.STONE),
                    "Excavator broke a pickaxe candidate");
            require(positions.stream().skip(1).allMatch(pos -> helper.getLevel().isEmptyBlock(pos)),
                    "Excavator did not process its centered shovel candidates");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void areaBreakPreservesLootAndBlockEntities(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ServerPlayer player = survivalPlayer(helper);
            ItemStack hammer = tool(AreaToolMetal.COPPER, AreaToolType.MINING_HAMMER);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, hammer);
            BlockPos origin = helper.absolutePos(new BlockPos(5, 4, 5));
            BlockPos chestPos = AreaBreakHandler.positions(origin, Direction.NORTH,
                    AreaToolType.MINING_HAMMER).get(0);
            helper.getLevel().setBlock(chestPos, Blocks.HOPPER.defaultBlockState(), 3);
            HopperBlockEntity chest = (HopperBlockEntity) helper.getLevel().getBlockEntity(chestPos);
            chest.setItem(0, new ItemStack(Items.DIAMOND));
            AreaBreakHandler.breakArea(helper.getLevel(), player, hammer, origin, Direction.NORTH,
                    AreaToolType.MINING_HAMMER);
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                    new AABB(chestPos).inflate(2));
            require(drops.stream().map(ItemEntity::getItem).filter(stack -> stack.is(Items.HOPPER)).count() == 1,
                    "Block entity block did not drop exactly one normal block loot stack");
            require(drops.stream().map(ItemEntity::getItem).filter(stack -> stack.is(Items.DIAMOND)).count() == 1,
                    "Block entity contents were duplicated or lost");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void durabilityStopsAndCreativeHasNoCostOrDrops(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ServerPlayer player = survivalPlayer(helper);
            ItemStack hammer = tool(AreaToolMetal.COPPER, AreaToolType.MINING_HAMMER);
            hammer.setDamageValue(hammer.getMaxDamage() - 2);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, hammer);
            BlockPos origin = helper.absolutePos(new BlockPos(5, 4, 5));
            List<BlockPos> positions = AreaBreakHandler.positions(origin, Direction.NORTH,
                    AreaToolType.MINING_HAMMER);
            positions.forEach(pos -> helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3));
            AreaBreakHandler.breakArea(helper.getLevel(), player, hammer, origin, Direction.NORTH,
                    AreaToolType.MINING_HAMMER);
            require(positions.stream().filter(helper.getLevel()::isEmptyBlock).count() == 2,
                    "Area mining did not stop when the tool broke");

            player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
            ItemStack creativeHammer = tool(AreaToolMetal.COPPER, AreaToolType.MINING_HAMMER);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, creativeHammer);
            positions.forEach(pos -> helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3));
            helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(5)).forEach(ItemEntity::discard);
            AreaBreakHandler.breakArea(helper.getLevel(), player, creativeHammer, origin, Direction.NORTH,
                    AreaToolType.MINING_HAMMER);
            require(creativeHammer.getDamageValue() == 0, "Creative area mining damaged the tool");
            require(helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(5)).isEmpty(),
                    "Creative area mining spawned drops");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void layersPeelOnceAndAreaDoesNotRecurse(GameTestHelper helper) {
        helper.succeedIf(() -> {
            ServerPlayer player = survivalPlayer(helper);
            BlockPos origin = helper.absolutePos(new BlockPos(5, 4, 5));
            BlockPos layerPos = AreaBreakHandler.positions(origin, Direction.UP, AreaToolType.EXCAVATOR).get(0);
            helper.getLevel().setBlock(layerPos,
                    Blocks.SNOW.defaultBlockState().setValue(BlockStateProperties.LAYERS, 8), 3);
            ItemStack excavator = tool(AreaToolMetal.COPPER, AreaToolType.EXCAVATOR);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, excavator);
            AreaBreakHandler.breakArea(helper.getLevel(), player, excavator, origin, Direction.UP,
                    AreaToolType.EXCAVATOR);
            require(helper.getLevel().getBlockState(layerPos).getValue(BlockStateProperties.LAYERS) == 7,
                    "Layered candidate did not lose exactly one layer");
            helper.getLevel().setBlock(layerPos,
                    Blocks.SNOW.defaultBlockState().setValue(BlockStateProperties.LAYERS, 8), 3);
            protectedPosition = layerPos;
            AreaBreakHandler.breakArea(helper.getLevel(), player, excavator, origin, Direction.UP,
                    AreaToolType.EXCAVATOR);
            require(helper.getLevel().getBlockState(layerPos).getValue(BlockStateProperties.LAYERS) == 8,
                    "Canceled layered candidate was changed");
            protectedPosition = null;

            ItemStack hammer = tool(AreaToolMetal.COPPER, AreaToolType.MINING_HAMMER);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, hammer);
            for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) {
                BlockPos pos = origin.offset(x, y, 0);
                if (!pos.equals(origin)) helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
            }
            AreaBreakHandler.breakArea(helper.getLevel(), player, hammer, origin, Direction.NORTH,
                    AreaToolType.MINING_HAMMER);
            int remaining = 0;
            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -2, 0), origin.offset(2, 2, 0))) {
                if (helper.getLevel().getBlockState(pos).is(Blocks.STONE)) remaining++;
            }
            require(remaining == 16, "Additional breaks recursively expanded beyond the 3x3 plane");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void moldsForgeCastAndAssembleAllTools(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (AreaToolType type : AreaToolType.values()) {
                require(TFCMItems.UNFIRED_AREA_TOOL_MOLDS.get(type).isBound()
                        && TFCMItems.AREA_TOOL_MOLDS.get(type).isBound(), "Area tool mold is not registered");
                require(recipeExists(helper, "knapping/ceramic/unfired_" + type.id() + "_head_mold")
                        && recipeExists(helper, "heating/ceramic/" + type.id() + "_head_mold"),
                        "Area tool mold cannot be knapped and fired");
                KnappingRecipe knapping = (KnappingRecipe) helper.getLevel().getServer().getRecipeManager().byKey(
                        ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID,
                                "knapping/ceramic/unfired_" + type.id() + "_head_mold")).orElseThrow().value();
                require(knapping.getPattern().matches(knappingPattern(type))
                        && knapping.assemble().is(TFCMItems.UNFIRED_AREA_TOOL_MOLDS.get(type).get()),
                        "Area tool mold knapping pattern does not match its own result");
                for (AreaToolMetal metal : AreaToolMetal.values()) {
                    boolean castingMatchesTfc = recipeExists(helper,
                            "casting/" + metal.id() + "_" + type.id() + "_head") == metal.isCastable();
                    require(recipeExists(helper, "anvil/metal/" + type.id() + "_head/" + metal.id())
                            && castingMatchesTfc && recipeExists(helper,
                                    "crafting/metal/" + type.id() + "/" + metal.id()),
                            "Area tool head processing or assembly is missing");
                    ItemStack finished = tool(metal, type);
                    ItemStack head = TFCMItems.AREA_TOOL_HEADS.get(metal).get(type).toStack();
                    require(HeatCapability.getDefinition(finished) != null && HeatCapability.getDefinition(head) != null,
                            "Area tool or head is missing TFC heat data");
                    require(ItemSizeManager.getDefinition(finished) != null
                            && ItemSizeManager.getDefinition(head) != null,
                            "Area tool or head is missing TFC size data");
                    TagKey<Item> damage = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("tfc",
                            type == AreaToolType.EXCAVATOR ? "deals_crushing_damage" : "deals_piercing_damage"));
                    TagKey<Item> rack = TagKey.create(Registries.ITEM,
                            ResourceLocation.fromNamespaceAndPath("tfc", "usable_on_tool_rack"));
                    require(finished.is(damage), "Area tool is missing its matching TFC damage category");
                    require(finished.is(rack), "Area tool is missing the TFC tool rack tag");
                }
            }
        });
    }

    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        String name = "tfcm-area-" + playerSequence++;
        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name);
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), profile);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        return player;
    }

    private static ItemStack tool(AreaToolMetal metal, AreaToolType type) {
        return TFCMItems.AREA_TOOLS.get(metal).get(type).toStack();
    }

    private static boolean recipeExists(GameTestHelper helper, String id) {
        return helper.getLevel().getServer().getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, id)).isPresent();
    }

    private static int planeDistance(BlockPos origin, BlockPos pos, Direction face) {
        return switch (face.getAxis()) {
            case X -> pos.getX() - origin.getX();
            case Y -> pos.getY() - origin.getY();
            case Z -> pos.getZ() - origin.getZ();
        };
    }

    private static void protectCandidate(BlockEvent.BreakEvent event) {
        if (protectedPosition != null && protectedPosition.equals(event.getPos())) event.setCanceled(true);
    }

    private static boolean close(float first, float second) {
        return Math.abs(first - second) < .001f;
    }

    private static void lookAtFace(ServerPlayer player, BlockPos origin, Direction face) {
        Vec3 position = Vec3.atCenterOf(origin.relative(face, 3));
        player.setPos(position.x, position.y - player.getEyeHeight(), position.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(origin));
    }

    private static KnappingPattern knappingPattern(AreaToolType type) {
        return switch (type) {
            case MINING_HAMMER -> KnappingPattern.from(true, "#####", "     ", "     ", "## ##", "#####");
            case QUARRY_HAMMER -> KnappingPattern.from(true, "#####", " # # ", "     ", "#   #", "#####");
            case EXCAVATOR -> KnappingPattern.from(true, "#####", " ### ", "  #  ", "  #  ", "     ");
        };
    }

    private static void require(boolean value, String message) {
        if (!value) throw new GameTestAssertException(message);
    }
}
