package net.claustra01.tfcm.common.tool;

import java.util.Locale;

import net.claustra01.tfcm.common.metal.Metal;
import net.claustra01.tfcm.registry.TFCMArmorMaterials;
import net.claustra01.tfcm.registry.TFCMBlocks;
import net.dries007.tfc.common.LevelTier;
import net.dries007.tfc.util.Metal.BlockType;
import net.dries007.tfc.util.registry.RegistryMetal;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public enum ToolMetal implements RegistryMetal {
    INVAR(Metal.INVAR, 3, 2200, 8, 4.75f, 12, 429, 495, 528, 370),
    TITANIUM(Metal.TITANIUM, 5, 3300, 11, 7, 17, 650, 750, 800, 550),
    NETHERITE(Metal.NETHERITE, 6, 6500, 12, 9, 22, 884, 1020, 1010, 715),
    TUNGSTEN_STEEL(Metal.TUNGSTEN_STEEL, 7, 8125, 13, 10.5f, 25, 1105, 1275, 1360, 935);

    private final Metal metal;
    private final LevelTier tier;
    private final int bootsDurability;
    private final int leggingsDurability;
    private final int chestplateDurability;
    private final int helmetDurability;

    ToolMetal(Metal metal, int level, int uses, float speed, float attack, int enchantment,
            int bootsDurability, int leggingsDurability, int chestplateDurability, int helmetDurability) {
        this.metal = metal;
        this.tier = new ToolTier(level, uses, speed, attack, enchantment, metal);
        this.bootsDurability = bootsDurability;
        this.leggingsDurability = leggingsDurability;
        this.chestplateDurability = chestplateDurability;
        this.helmetDurability = helmetDurability;
    }

    public Metal metal() {
        return metal;
    }

    public Ingredient repairIngredient() {
        return repairIngredient(metal);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public LevelTier toolTier() {
        return tier;
    }

    @Override
    public Holder<ArmorMaterial> armorMaterial() {
        return TFCMArmorMaterials.MATERIALS.get(this);
    }

    @Override
    public int armorDurability(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> bootsDurability;
            case LEGGINGS -> leggingsDurability;
            case CHESTPLATE, BODY -> chestplateDurability;
            case HELMET -> helmetDurability;
        };
    }

    @Override
    public Block getBlock(BlockType type) {
        return TFCMBlocks.METAL_BLOCKS.get(metal).get();
    }

    @Override
    public MapColor mapColor() {
        return MapColor.METAL;
    }

    @Override
    public Rarity rarity() {
        return metal.rarity();
    }

    @Override
    public float weatheringResistance() {
        return 1;
    }

    private record ToolTier(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue,
            Metal metal) implements LevelTier {
        @Override
        public int getUses() {
            return uses;
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        @Override
        public float getAttackDamageBonus() {
            return attackDamageBonus;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return level <= 3 ? BlockTags.INCORRECT_FOR_IRON_TOOL : level == 4
                    ? BlockTags.INCORRECT_FOR_DIAMOND_TOOL : BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return enchantmentValue;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return repairIngredient(metal);
        }
    }

    private static Ingredient repairIngredient(Metal metal) {
        return Ingredient.of(TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "ingots/" + metal.id())));
    }
}
