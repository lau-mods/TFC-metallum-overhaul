package net.claustra01.tfcm.gametest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.common.metal.MetalForm;
import net.claustra01.tfcm.common.metal.TfcMetal;
import net.claustra01.tfcm.common.material.MaterialPart;
import net.claustra01.tfcm.common.ore.Ore;
import net.claustra01.tfcm.common.processing.ProcessingForm;
import net.claustra01.tfcm.common.processing.ProcessingSeries;
import net.claustra01.tfcm.common.processing.SluiceRecipe;
import net.claustra01.tfcm.common.processing.SluiceRecipeSync;
import net.claustra01.tfcm.common.tool.ToolMetal;
import net.claustra01.tfcm.common.tool.ToolPart;
import net.claustra01.tfcm.common.tool.area.AreaToolMetal;
import net.claustra01.tfcm.common.tool.area.AreaToolType;
import net.claustra01.tfcm.registry.TFCMItems;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.WeldingRecipe;
import net.dries007.tfc.common.component.size.ItemSizeManager;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(TFCM.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TFCMAssetIntegrityGameTests {
    private static final String BATCH = "tfcm_foundation";

    private TFCMAssetIntegrityGameTests() {
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void registeredContentHasModelsTranslationsAndBlockLoot(GameTestHelper helper) {
        JsonObject language = readJson("assets/tfcm/lang/en_us.json");
        checkItems(language);
        checkBlocks(language);
        checkFluids(language);
        checkShield("invar", "texture", "layer0");
        checkShield("titanium", "1", "2");
        checkShield("netherite", "1", "2");
        checkShield("tungsten_steel", "1", "2");
        helper.getLevel().getServer().getResourceManager().listResources("tags", id -> id.getPath().endsWith(".json"))
                .forEach(TFCMAssetIntegrityGameTests::checkTag);
        helper.succeed();
    }

    private static void checkItems(JsonObject language) {
        Set<ResourceLocation> items = new HashSet<>();
        BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals(TFCM.MOD_ID)).forEach(id -> {
            require(items.add(id), "Duplicate item " + id);
            checkModel(ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "item/" + id.getPath()));
            Item item = BuiltInRegistries.ITEM.get(id);
            require(language.has(item.getDescriptionId()), "Item translation " + id);
        });
    }

    private static void checkBlocks(JsonObject language) {
        BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals(TFCM.MOD_ID)).forEach(id -> {
            String statePath = "assets/tfcm/blockstates/" + id.getPath() + ".json";
            checkBlockstateModels(readJson(statePath));
            Block block = BuiltInRegistries.BLOCK.get(id);
            require(language.has(block.getDescriptionId()), "Block translation " + id);
            if (!(block instanceof LiquidBlock)) {
                ResourceLocation loot = block.getLootTable().location();
                if (!loot.equals(ResourceLocation.parse("minecraft:empty")))
                    checkLoot(readJson("data/" + loot.getNamespace() + "/loot_table/" + loot.getPath() + ".json"));
            }
        });
    }

    private static void checkFluids(JsonObject language) {
        BuiltInRegistries.FLUID.keySet().stream().filter(id -> id.getNamespace().equals(TFCM.MOD_ID)).forEach(id -> {
            Fluid fluid = BuiltInRegistries.FLUID.get(id);
            Block block = fluid.defaultFluidState().createLegacyBlock().getBlock();
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            require(blockId.getNamespace().equals(TFCM.MOD_ID), "Fluid block " + id);
            checkBlockstateModels(readJson("assets/tfcm/blockstates/" + blockId.getPath() + ".json"));
            require(language.has(block.getDescriptionId()), "Fluid translation " + id);
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void metalsOresAndToolsHaveTfcAttributes(GameTestHelper helper) {
        checkMetalShapes();
        checkMoltenFluids();
        checkMaterialsAndProcessing();
        checkEquipment();
        checkAreaTools();
        checkOreTags();
        helper.succeed();
    }

    private static void checkMetalShapes() {
        Set<String> metalShapes = new HashSet<>();
        for (Metal metal : Metal.values()) for (MetalForm form : MetalForm.values()) {
            require(metalShapes.add(form.item(metal)), "Duplicate metal shape " + form.item(metal));
            ItemStack stack = BuiltInRegistries.ITEM.get(ResourceLocation.parse(form.item(metal))).getDefaultInstance();
            require(!stack.isEmpty(), "Metal form " + form.item(metal));
            require(HeatCapability.getDefinition(stack) != null, "Metal heat " + form.item(metal));
            require(ItemSizeManager.getDefinition(stack) != null, "Metal size " + form.item(metal));
            if (form.isItem()) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM,
                        ResourceLocation.parse("c:" + form.tag() + "/" + metal.id()));
                require(stack.is(tag), "Metal form tag " + form.item(metal));
            }
        }
        for (TfcMetal metal : TfcMetal.values()) for (MetalForm form : MetalForm.values()) {
            if (!form.isAddedForm() || metal.isFirmaLife() && !ModList.get().isLoaded("firmalife")) continue;
            require(metalShapes.add(metal.item(form)), "Duplicate metal shape " + metal.item(form));
            ItemStack stack = BuiltInRegistries.ITEM.get(ResourceLocation.parse(metal.item(form))).getDefaultInstance();
            require(!stack.isEmpty(), "TFC metal form " + metal.item(form));
            require(HeatCapability.getDefinition(stack) != null, "TFC metal heat " + metal.item(form));
            require(ItemSizeManager.getDefinition(stack) != null, "TFC metal size " + metal.item(form));
            require(stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:" + form.tag() + "/" + metal.id())))
                    && stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse("c:" + form.tag()))),
                    "TFC metal form tag " + metal.item(form));
        }
    }

    private static void checkMoltenFluids() {
        for (Metal metal : Metal.values()) {
            ResourceLocation still = ResourceLocation.parse("tfcm:metal/" + metal.id());
            ResourceLocation flowing = ResourceLocation.parse("tfcm:metal/flowing_" + metal.id());
            TagKey<Fluid> molten = TagKey.create(Registries.FLUID,
                    ResourceLocation.parse("c:molten_" + metal.id()));
            require(BuiltInRegistries.FLUID.get(still).is(molten)
                    && BuiltInRegistries.FLUID.get(flowing).is(molten), "Molten fluid tag " + metal.id());
            require(resourceExists("data/tfcm/tfc/fluid_heat/" + metal.id() + ".json"),
                    "Molten fluid heat " + metal.id());
        }
    }

    private static void checkMaterialsAndProcessing() {
        for (MaterialPart part : MaterialPart.values()) {
            ResourceLocation id = ResourceLocation.parse("tfcm:" + part.id());
            require(ItemSizeManager.getDefinition(BuiltInRegistries.ITEM.get(id).getDefaultInstance()) != null,
                    "Material size " + id);
        }
        for (Ore ore : Ore.values()) {
            ResourceLocation id = ResourceLocation.parse("tfcm:ore/small_" + ore.id());
            if (BuiltInRegistries.ITEM.containsKey(id))
                require(HeatCapability.getDefinition(BuiltInRegistries.ITEM.get(id).getDefaultInstance()) != null,
                        "Small ore heat " + id);
        }
        for (ProcessingSeries series : ProcessingSeries.values()) for (ProcessingForm form : ProcessingForm.values()) {
            if (form != ProcessingForm.POWDER && form != ProcessingForm.PELLET && form != ProcessingForm.BRIQUET) continue;
            ResourceLocation id = ResourceLocation.parse("tfcm:ore_processing/" + form.id() + "/" + series.id());
            if (BuiltInRegistries.ITEM.containsKey(id) && series.recoveryFluid() != null
                    && (form == ProcessingForm.POWDER || !series.nonMetal()))
                require(HeatCapability.getDefinition(BuiltInRegistries.ITEM.get(id).getDefaultInstance()) != null,
                        "Processing heat " + id);
        }
    }

    private static void checkEquipment() {
        TagKey<Item> rack = TagKey.create(Registries.ITEM, ResourceLocation.parse("tfc:usable_on_tool_rack"));
        for (ToolMetal metal : ToolMetal.values()) for (ToolPart part : ToolPart.values()) {
            ItemStack stack = TFCMItems.METAL_ITEMS.get(metal).get(part).toStack();
            require(HeatCapability.getDefinition(stack) != null, "Tool heat " + part.id());
            require(ItemSizeManager.getDefinition(stack) != null, "Tool size " + part.id());
            if (part.isFinishedTool()) require(stack.is(rack), "Tool rack " + part.id());
        }
    }

    private static void checkAreaTools() {
        TagKey<Item> rack = TagKey.create(Registries.ITEM, ResourceLocation.parse("tfc:usable_on_tool_rack"));
        TagKey<Item> tools = TagKey.create(Registries.ITEM, ResourceLocation.parse("c:tools/mining_hammer"));
        TagKey<Item> mining = TagKey.create(Registries.ITEM, ResourceLocation.parse("c:tools/mining_tool"));
        for (AreaToolMetal metal : AreaToolMetal.values()) for (AreaToolType type : AreaToolType.values()) {
            ItemStack stack = TFCMItems.AREA_TOOLS.get(metal).get(type).toStack();
            ItemStack head = TFCMItems.AREA_TOOL_HEADS.get(metal).get(type).toStack();
            ItemStack ingot = BuiltInRegistries.ITEM.get(ResourceLocation.parse(metal.namespace()
                    + ":metal/ingot/" + metal.id())).getDefaultInstance();
            require(stack.is(rack), "Area tool rack " + type.id());
            if (type == AreaToolType.MINING_HAMMER) require(stack.is(tools), "Mining tool category");
            if (type == AreaToolType.EXCAVATOR) require(stack.is(ItemTags.SHOVELS), "Excavator category");
            else require(stack.is(ItemTags.PICKAXES) && stack.is(mining), "Hammer category " + type.id());
            require(HeatCapability.getDefinition(stack) != null && HeatCapability.getDefinition(head) != null,
                    "Area tool heat " + type.id());
            require(ItemSizeManager.getDefinition(stack) != null && ItemSizeManager.getDefinition(head) != null,
                    "Area tool size " + type.id());
            require(ItemSizeManager.getDefinition(stack).size() == Size.VERY_LARGE
                    && ItemSizeManager.getDefinition(stack).weight() == Weight.VERY_HEAVY
                    && ItemSizeManager.getDefinition(head).size() == Size.LARGE
                    && ItemSizeManager.getDefinition(head).weight() == Weight.MEDIUM,
                    "Area tool storage " + type.id());
            require(metal.repairIngredient().test(ingot), "Area tool repair " + type.id());
            HeatCapability.setTemperature(stack, metal.forgingTemperature());
            require(Math.abs(HeatCapability.getTemperature(stack) - metal.forgingTemperature()) < 0.01f,
                    "Area tool heating " + type.id());
        }
    }

    private static void checkOreTags() {
        TagKey<Block> ores = TagKey.create(Registries.BLOCK,
                ResourceLocation.parse("c:ores"));
        BuiltInRegistries.BLOCK.keySet().stream()
                .filter(id -> id.getNamespace().equals(TFCM.MOD_ID) && id.getPath().startsWith("ore/")
                        && !id.getPath().startsWith("ore/small_"))
                .forEach(id -> {
                    var block = BuiltInRegistries.BLOCK.get(id);
                    require(block.defaultBlockState().is(ores), "Ore tag " + id);
                    require(block.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE), "Ore pickaxe " + id);
                });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void neutroniumTexturesHaveAnimationFrames(GameTestHelper helper) {
        for (String form : new String[] {"ingot", "double_ingot", "sheet", "double_sheet", "rod", "foil",
                "gear", "heavy_sheet", "nail", "ring", "rivet", "screw", "stamen", "wire"}) {
            checkAnimation("item/metal/" + form + "/neutronium");
        }
        checkAnimation("block/metal/block/neutronium");
        checkHash("assets/tfcm/textures/item/metal/ingot/neutronium.png", "dc86ab921b1202467ca6eed71c33e14ca362a50f55b80c212e35c0f6d208d2bb");
        checkHash("assets/tfcm/textures/block/metal/block/neutronium.png", "31769d374ca6d87ed5b4ad200e735ce9b0f6868886aaa26f0b58f8ba708867a3");
        checkHash("assets/tfcm/textures/item/metal/mining_hammer/copper.png", "7b40b4374cdeab65a9881964604c5cc333437abfe89a5e3f71b188743f7de313");
        for (Metal metal : Metal.values()) {
            try (var nuggetStream = TFCMAssetIntegrityGameTests.class.getResourceAsStream(
                    "/assets/tfcm/textures/item/metal/nugget/" + metal.id() + ".png");
                 var dustStream = TFCMAssetIntegrityGameTests.class.getResourceAsStream(
                    "/assets/tfcm/textures/item/metal/dust/" + metal.id() + ".png")) {
                var nugget = ImageIO.read(nuggetStream);
                var dust = ImageIO.read(dustStream);
                boolean different = false;
                boolean transparent = false;
                for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                    different |= nugget.getRGB(x, y) != dust.getRGB(x, y);
                    transparent |= (nugget.getRGB(x, y) >>> 24) == 0 && (dust.getRGB(x, y) >>> 24) == 0;
                }
                require(different && transparent, "Distinct transparent metal shapes " + metal.id());
            } catch (IOException exception) {
                throw new GameTestAssertException("Cannot read metal shapes " + metal.id());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void restoredRecipesAndForgingBonusLoad(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (Metal metal : Metal.values()) {
                for (String suffix : new String[] {"", "_slab", "_stairs"}) {
                    requireRecipe(helper, "tfcm", "crafting/metal/block/" + metal.id() + suffix);
                }
            }
            for (ToolMetal metal : ToolMetal.values()) {
                String id = metal.getSerializedName();
                requireRecipe(helper, "tfcm", "crafting/metal/fishing_rod/" + id);
                require(((AnvilRecipe) requireRecipe(helper, "tfcm", "anvil/metal/sword_blade/" + id))
                        .shouldApplyForgingBonus(), "Forging bonus is not applied to the " + id + " Sword Blade");
            }
            require(((AnvilRecipe) requireRecipe(helper, "tfcm", "anvil/metal/mining_hammer_head/copper"))
                    .shouldApplyForgingBonus(), "Forging bonus is not applied to the Mining Hammer Head");
            for (String id : new String[] {"high_carbon_tungsten_steel", "netherite"}) {
                require(requireRecipe(helper, "tfcm", "welding/metal/ingot/" + id) instanceof WeldingRecipe,
                        "Ingot welding recipe " + id);
            }
            require(HeatCapability.getDefinition(stack("minecraft:netherite_scrap")) != null,
                    "Netherite Scrap cannot reach welding temperature");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void tfcToolsAssembleFromParts(GameTestHelper helper) {
        helper.succeedIf(() -> {
            for (TfcMetal metal : TfcMetal.values()) {
                if (!metal.hasTools()) continue;
                requireRecipe(helper, "tfc", "crafting/metal/sword/" + metal.id());
                requireRecipe(helper, "tfcm", "crafting/metal/shears/" + metal.id());
                for (String part : new String[] {"crossguard", "pommel"}) {
                    ItemStack stack = stack("tfcm:metal/" + part + "/" + metal.id());
                    require(!stack.isEmpty() && HeatCapability.getDefinition(stack) != null
                            && ItemSizeManager.getDefinition(stack) != null
                            && stack.is(tag("tfc:metal_item/" + metal.id())), "TFC sword part " + part + " " + metal.id());
                }
                requireRecipe(helper, "tfcm", "anvil/metal/crossguard/" + metal.id());
                requireRecipe(helper, "tfcm", "welding/metal/pommel/" + metal.id());
                requireRecipe(helper, "tfcm", "heating/metal/crossguard/" + metal.id());
                requireRecipe(helper, "tfcm", "heating/metal/pommel/" + metal.id());
            }
            for (String id : new String[] {"minecraft:bow", "minecraft:crossbow", "minecraft:arrow", "tfc:crafting/arrow"}) {
                require(helper.getLevel().getRecipeManager().byKey(ResourceLocation.parse(id)).isEmpty(),
                        "The part-less recipe is still active: " + id);
            }
            requireRecipe(helper, "tfcm", "crafting/material/arrow_obsidian");
            for (String item : new String[] {"tfcm:plant_string", "tfcm:bamboo_string", "tfcm:leather_string"}) {
                requireTags(item, "c:strings");
            }
            require(!stack("tfcm:leather_strap").is(tag("c:strings")), "Leather Strap must not count as string");
            CraftingRecipe pickaxe = (CraftingRecipe) requireRecipe(helper, "tfc", "crafting/metal/pickaxe/copper");
            ItemStack head = stack("tfc:metal/pickaxe_head/copper");
            ItemStack handle = stack("minecraft:stick");
            ItemStack string = stack("tfcm:plant_string");
            require(pickaxe.matches(CraftingInput.of(2, 2, List.of(string, head, handle, string)), helper.getLevel()),
                    "Copper Pickaxe does not take the string assembly");
            require(!pickaxe.matches(CraftingInput.of(1, 2, List.of(head, handle)), helper.getLevel()),
                    "TFC's Head and Stick recipe is still active");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void firmaLifeFormsFollowTheMod(GameTestHelper helper) {
        helper.succeedIf(() -> {
            boolean loaded = ModList.get().isLoaded("firmalife");
            for (TfcMetal metal : new TfcMetal[] {TfcMetal.CHROMIUM, TfcMetal.STAINLESS_STEEL}) {
                for (MetalForm form : MetalForm.values()) {
                    if (!form.isAddedForm()) continue;
                    String item = metal.item(form);
                    require(BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(item)) == loaded,
                            "FirmaLife form registration " + item);
                    String path = switch (form) {
                        case HEAVY_SHEET -> "welding/metal/heavy_sheet/" + metal.id();
                        case NUGGET -> "crafting/metal/nugget/" + metal.id();
                        case DUST -> "quern/metal/dust/" + metal.id();
                        default -> "anvil/metal/" + form.id + "/" + metal.id();
                    };
                    require(helper.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("tfcm", path))
                            .isPresent() == loaded, "FirmaLife form recipe " + path);
                }
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void restoredTagsContainTheirItems(GameTestHelper helper) {
        helper.succeedIf(() -> {
            requireTags("tfcm:metal/tuyere/invar", "tfc:blast_furnace_tuyeres", "tfc:tools/invar");
            requireTags("tfcm:metal/hammer/invar", "tfc:trip_hammers");
            requireTags("tfcm:metal/fishing_rod/invar", "tfc:holds_large_fishing_bait");
            requireTags("tfcm:metal/anvil/invar", "tfc:anvils");
            requireTags("tfcm:metal/crossguard/invar", "tfc:metal_item/invar");
            requireTags("tfcm:metal/sheet/invar", "tfc:sheets");
            requireTags("tfcm:metal/double_sheet/invar", "tfc:double_sheets");
            requireTags("tfcm:metal/double_ingot/invar", "tfc:double_ingots");
            requireTags("tfcm:ore/normal_galena", "tfc:metal_ores", "tfc:ore_pieces", "c:ores", "c:ores/galena",
                    "c:ores/lead/normal");
            requireTags("tfcm:ore/carobbiite", "tfc:metal_ores", "c:ores", "c:ores/beryllium");
            requireTags("tfcm:ore/quartz", "tfc:ore_pieces", "c:ores/quartz");
            requireTags("tfcm:ore/small_galena", "tfc:small_ore_pieces");
            for (ProcessingForm form : ProcessingForm.values()) {
                requireTags("tfcm:ore_processing/" + form.id() + "/galena", "c:metal/" + form.id(),
                        "c:metal/" + form.id() + "/galena");
            }
            requireTags("tfcm:ore_processing/powder/galena", "c:ores/clean_dusts");
            requireTags("tfcm:metal/mining_hammer/steel", "tfc:deals_crushing_damage", "c:tools", "tfc:tools/steel");
            requireTags("tfcm:metal/quarry_hammer/invar", "tfc:deals_crushing_damage", "c:tools", "tfc:tools/invar");
            requireTags("tfcm:metal/excavator/steel", "tfc:deals_crushing_damage", "c:tools");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void distinctItemsHaveDistinctSprites(GameTestHelper helper) {
        helper.succeedIf(() -> {
            String items = "assets/tfcm/textures/item/";
            for (Metal metal : Metal.values()) {
                require(metal.color() >>> 24 == 0xFF, "Molten " + metal.id() + " tint is not opaque");
                requireDifferent(items + "metal/plate/" + metal.id() + ".png", items + "metal/sheet/" + metal.id() + ".png");
            }
            for (String form : new String[] {"pellet", "briquet"}) {
                require(readJson("assets/tfcm/models/item/ore_processing/" + form + "/borax.json")
                        .getAsJsonObject("textures").get("layer0").getAsString()
                        .equals("tfcm:item/ore_processing/" + form + "/borax"), "Model of Borax " + form);
                for (String series : new String[] {"cinnabar", "cryolite", "graphite", "sulfur"}) {
                    require(!BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(
                            "tfcm:ore_processing/" + form + "/" + series)), "No melting recipe, no " + form + ": " + series);
                }
            }
            require(readJson("assets/tfcm/models/item/ore_processing/rock_powder.json").getAsJsonObject("textures")
                    .get("layer0").getAsString().equals("tfcm:item/ore_processing/rock_powder"), "Rock Powder sprite");
            require(readJson("assets/tfcm/models/item/leather_string.json").getAsJsonObject("textures")
                    .get("layer0").getAsString().equals("tfcm:item/leather_string"), "Leather String sprite");
            requireDifferent(items + "leather_string.png", items + "leather_strap.png");
            for (Ore ore : Ore.values()) {
                if (ore.graded()) require(readJson("assets/tfcm/models/item/ore/small_" + ore.id() + ".json")
                        .getAsJsonObject("textures").get("layer0").getAsString()
                        .equals("tfcm:item/ore/small_" + ore.id()), "Small ore item model " + ore.id());
            }
            require(!opaquePixels(items + "metal/dust/invar.png").equals(opaquePixels(items + "ore_processing/powder/galena.png"))
                    && !opaquePixels(items + "metal/dust/invar.png").equals(opaquePixels(items + "ore_processing/powder/bismuthinite.png")),
                    "Metal Dust shares an outline with Ore Powder");
            for (String model : new String[] {"pan/empty", "pan/gravel/andesite_full", "pan/gravel/andesite_half",
                    "pan/sand/black_full", "pan/rock_powder_half", "pan/rocky_chunks/galena_full", "pan/dirty_dust/galena_half"}) {
                checkModel(ResourceLocation.fromNamespaceAndPath("tfcm", "item/" + model));
            }
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void sluiceRecipesReachClientsWithLootChances(GameTestHelper helper) {
        helper.succeedIf(() -> {
            List<SluiceRecipe> recipes = SluiceRecipe.build(helper.getLevel().getServer());
            SluiceRecipe rocky = recipes.stream().filter(recipe -> recipe.input().test(
                    stack("tfcm:ore_processing/rocky_chunks/galena"))).findFirst().orElseThrow(
                    () -> new GameTestAssertException("Rocky Chunk Sluice recipe is missing"));
            require(rocky.outputs().size() == 4
                    && rocky.outputs().getFirst().stack().is(stack("tfcm:ore_processing/chunks/galena").getItem())
                    && rocky.outputs().getFirst().chance() == 1
                    && rocky.outputs().get(1).chance() == .3f && rocky.outputs().get(2).chance() == .15f
                    && rocky.outputs().get(3).chance() == .0002f, "Rocky Chunk chances: " + rocky.outputs());
            require(recipes.stream().anyMatch(recipe -> recipe.input().test(stack("tfc:deposit/cassiterite/andesite"))),
                    "TFC's Deposits must be listed too");
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                    helper.getLevel().registryAccess());
            SluiceRecipeSync.Payload.STREAM_CODEC.encode(buffer, new SluiceRecipeSync.Payload(recipes));
            List<SluiceRecipe> decoded = SluiceRecipeSync.Payload.STREAM_CODEC.decode(buffer).recipes();
            require(decoded.size() == recipes.size() && decoded.stream().anyMatch(recipe -> recipe.input().test(
                    stack("tfcm:ore_processing/rocky_chunks/galena")) && recipe.outputs().get(3).chance() == .0002f),
                    "Sluice recipes did not survive the network codec");
        });
    }

    @GameTest(template = "empty", batch = BATCH)
    public static void oreNamesFollowTfcAndJapaneseIsTranslated(GameTestHelper helper) {
        helper.succeedIf(() -> {
            JsonObject english = readJson("assets/tfcm/lang/en_us.json");
            JsonObject japanese = readJson("assets/tfcm/lang/ja_jp.json");
            for (String key : english.keySet()) {
                String name = english.get(key).getAsString();
                if (key.startsWith("block.tfcm.metal.block.")) require(name.contains(" Plated "), "Metal block name " + name);
                require(japanese.has(key), "Japanese key " + key);
                String translated = japanese.get(key).getAsString();
                require(key.equals("itemGroup.tfcm") || !translated.replace("HSLA", "").replace("%s", "").matches(".*[A-Za-z].*"),
                        "Untranslated Japanese name " + key + ": " + translated);
                if (!key.startsWith("block.tfcm.ore.")) continue;
                require(!name.contains(" in "), "Mechanical ore name " + key + ": " + name);
                if (!key.endsWith(".prospected")) continue;
                require(!name.matches("(Poor|Normal|Rich) .*"), "Prospected name has a grade " + key);
                String host = key.substring(0, key.length() - ".prospected".length());
                String full = english.get(host).getAsString();
                require(full.endsWith(name) && !full.equals(name), "Prospected name is not the ore name " + key);
            }
        });
    }

    private static Recipe<?> requireRecipe(GameTestHelper helper, String namespace, String path) {
        return helper.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(namespace, path))
                .orElseThrow(() -> new GameTestAssertException("Missing recipe " + namespace + ":" + path)).value();
    }

    private static ItemStack stack(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)).getDefaultInstance();
    }

    private static TagKey<Item> tag(String id) {
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
    }

    private static void requireTags(String item, String... tags) {
        for (String tag : tags) require(stack(item).is(tag(tag)), item + " is missing from " + tag);
    }

    private static Set<Integer> opaquePixels(String path) {
        try (var stream = TFCMAssetIntegrityGameTests.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "Sprite " + path);
            var image = ImageIO.read(stream);
            Set<Integer> pixels = new HashSet<>();
            for (int y = 0; y < image.getWidth(); y++) for (int x = 0; x < image.getWidth(); x++)
                if (image.getRGB(x, y) >>> 24 != 0) pixels.add(y * image.getWidth() + x);
            return pixels;
        } catch (IOException exception) {
            throw new GameTestAssertException("Cannot read " + path);
        }
    }

    private static void requireDifferent(String first, String second) {
        var loader = TFCMAssetIntegrityGameTests.class.getClassLoader();
        try (var a = loader.getResourceAsStream(first); var b = loader.getResourceAsStream(second)) {
            require(a != null && b != null, "Sprite " + first + " or " + second);
            require(!java.util.Arrays.equals(a.readAllBytes(), b.readAllBytes()), first + " repeats " + second);
        } catch (IOException exception) {
            throw new GameTestAssertException("Cannot compare " + first);
        }
    }

    private static void checkHash(String path, String expected) {
        try (var stream = TFCMAssetIntegrityGameTests.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null && HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(stream.readAllBytes())).equals(expected), "Changed source sprite " + path);
        } catch (java.security.NoSuchAlgorithmException | IOException exception) {
            throw new GameTestAssertException("Cannot verify sprite " + path);
        }
    }

    private static void checkAnimation(String texture) {
        String path = "assets/tfcm/textures/" + texture + ".png";
        JsonObject metadata = readJson(path + ".mcmeta");
        require(metadata.has("animation") && metadata.getAsJsonObject("animation").get("frametime").getAsInt() > 0,
                "Animation metadata " + texture);
        try (var stream = TFCMAssetIntegrityGameTests.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "Animation texture " + texture);
            var image = ImageIO.read(stream);
            require(image != null && image.getWidth() == 16 && image.getHeight() > image.getWidth()
                    && image.getHeight() % image.getWidth() == 0, "Animation frames " + texture);
            boolean changed = false;
            for (int y = 0; y < image.getWidth() && !changed; y++) for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != image.getRGB(x, y + image.getWidth())) {
                    changed = true;
                    break;
                }
            }
            require(changed, "Identical animation frames " + texture);
        } catch (java.io.IOException exception) {
            throw new GameTestAssertException("Cannot read " + path);
        }
    }

    private static JsonObject readJson(String path) {
        try (var stream = TFCMAssetIntegrityGameTests.class.getClassLoader().getResourceAsStream(path)) {
            require(stream != null, "Resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException exception) {
            throw new GameTestAssertException("Cannot read " + path);
        }
    }

    private static void checkBlockstateModels(JsonElement element) {
        if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals("model")) {
                    ResourceLocation model = ResourceLocation.parse(entry.getValue().getAsString());
                    if (model.getNamespace().equals(TFCM.MOD_ID)) checkModel(model);
                }
                else checkBlockstateModels(entry.getValue());
            }
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(TFCMAssetIntegrityGameTests::checkBlockstateModels);
        }
    }

    private static void checkLoot(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type") && object.get("type").getAsString().equals("minecraft:item") && object.has("name")) {
                ResourceLocation item = ResourceLocation.parse(object.get("name").getAsString());
                require(BuiltInRegistries.ITEM.containsKey(item), "Loot item " + item);
            }
            object.entrySet().forEach(entry -> checkLoot(entry.getValue()));
        } else if (element.isJsonArray()) element.getAsJsonArray().forEach(TFCMAssetIntegrityGameTests::checkLoot);
    }

    private static void checkShield(String metal, String first, String second) {
        JsonObject model = readJson("assets/tfcm/models/item/metal/shield/" + metal + ".json");
        JsonObject textures = model.getAsJsonObject("textures");
        require(textures.has(first) && textures.has(second), "Shield texture keys " + metal);
        for (var entry : textures.entrySet()) {
            ResourceLocation texture = ResourceLocation.parse(entry.getValue().getAsString());
            require(resourceExists("assets/" + texture.getNamespace() + "/textures/" + texture.getPath() + ".png"),
                    "Shield texture " + texture);
        }
    }

    private static void checkTag(ResourceLocation file, Resource resource) {
        String path = file.getPath();
        if (!path.startsWith("tags/item/") && !path.startsWith("tags/block/") && !path.startsWith("tags/fluid/")) return;
        try (var reader = resource.openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (!json.has("values")) return;
            for (JsonElement value : json.getAsJsonArray("values")) {
                String name = value.isJsonObject() ? value.getAsJsonObject().get("id").getAsString() : value.getAsString();
                if (name.startsWith("#")) continue;
                ResourceLocation id = ResourceLocation.parse(name);
                if (!id.getNamespace().equals(TFCM.MOD_ID)) continue;
                boolean exists = path.startsWith("tags/item/") ? BuiltInRegistries.ITEM.containsKey(id)
                        : path.startsWith("tags/block/") ? BuiltInRegistries.BLOCK.containsKey(id)
                        : BuiltInRegistries.FLUID.containsKey(id);
                boolean required = !value.isJsonObject() || !value.getAsJsonObject().has("required")
                        || value.getAsJsonObject().get("required").getAsBoolean();
                require(exists || !required, "Tag " + file + " references " + id);
            }
        } catch (IOException exception) {
            throw new GameTestAssertException("Cannot read tag " + file);
        }
    }

    private static void checkModel(ResourceLocation id) {
        checkModelReferences(readJson("assets/" + id.getNamespace() + "/models/" + id.getPath() + ".json"), new HashSet<>());
    }

    private static void checkModelReferences(JsonElement element, Set<ResourceLocation> visited) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(value -> checkModelReferences(value, visited));
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (var entry : object.entrySet()) {
                if ((entry.getKey().equals("parent") || entry.getKey().equals("model")) && entry.getValue().isJsonPrimitive()) {
                    ResourceLocation model = ResourceLocation.parse(entry.getValue().getAsString());
                    if (model.getPath().startsWith("builtin/") || !visited.add(model)) continue;
                    String path = "assets/" + model.getNamespace() + "/models/" + model.getPath() + ".json";
                    require(resourceExists(path), "Model dependency " + model);
                    if (model.getNamespace().equals(TFCM.MOD_ID)) checkModelReferences(readJson(path), visited);
                } else if (entry.getKey().equals("textures")) {
                    for (var texture : entry.getValue().getAsJsonObject().entrySet()) {
                        String name = texture.getValue().getAsString();
                        if (name.startsWith("#")) continue;
                        ResourceLocation id = ResourceLocation.parse(name);
                        require(!id.getPath().startsWith("port/"), "Noncanonical texture " + id);
                        require(resourceExists("assets/" + id.getNamespace() + "/textures/" + id.getPath() + ".png"),
                                "Texture " + id);
                    }
                } else checkModelReferences(entry.getValue(), visited);
            }
        }
    }

    private static boolean resourceExists(String path) {
        return TFCMAssetIntegrityGameTests.class.getClassLoader().getResource(path) != null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }
}
