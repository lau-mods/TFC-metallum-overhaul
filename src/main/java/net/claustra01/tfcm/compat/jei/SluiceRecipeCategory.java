package net.claustra01.tfcm.compat.jei;

import java.util.Locale;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.processing.SluiceRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

final class SluiceRecipeCategory implements IRecipeCategory<SluiceRecipe> {
    static final RecipeType<SluiceRecipe> TYPE = RecipeType.create(TFCM.MOD_ID, "sluice", SluiceRecipe.class);
    private static final int OUTPUT_X = 48;
    private static final int COLUMNS = 5;
    private static final int ROWS = 2;
    private static final int CELL_WIDTH = 22;
    private static final int CELL_HEIGHT = 28;

    private final IDrawable icon;
    private final IDrawableStatic arrow;

    SluiceRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("tfc", "wood/sluice/oak")).getDefaultInstance());
        arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<SluiceRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tfcm.jei.sluice");
    }

    @Override
    public int getWidth() {
        return OUTPUT_X + COLUMNS * CELL_WIDTH;
    }

    @Override
    public int getHeight() {
        return ROWS * CELL_HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SluiceRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).addIngredients(recipe.input()).setStandardSlotBackground();
        for (int index = 0; index < recipe.outputs().size(); index++) {
            SluiceRecipe.Output output = recipe.outputs().get(index);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x(index), y(index)).addItemStack(output.stack())
                    .setStandardSlotBackground()
                    .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                            Component.translatable("tfcm.jei.sluice.chance", percent(output.chance()))));
        }
    }

    @Override
    public void draw(SluiceRecipe recipe, IRecipeSlotsView view, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 21, 19);
        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().scale(.5f, .5f, 1);
        for (int index = 0; index < recipe.outputs().size(); index++) {
            String text = percent(recipe.outputs().get(index).chance()) + "%";
            graphics.drawString(font, text, (x(index) + 8) * 2 - font.width(text) / 2, (y(index) + 19) * 2, 0xFF555555, false);
        }
        graphics.pose().popPose();
    }

    private static int x(int index) {
        return OUTPUT_X + index % COLUMNS * CELL_WIDTH + 2;
    }

    private static int y(int index) {
        return index / COLUMNS * CELL_HEIGHT + 1;
    }

    private static String percent(float chance) {
        return String.format(Locale.ROOT, "%.2f", chance * 100).replaceFirst("\\.?0+$", "");
    }
}
