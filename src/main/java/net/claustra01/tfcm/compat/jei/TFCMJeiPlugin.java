package net.claustra01.tfcm.compat.jei;

import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.claustra01.tfcm.TFCM;
import net.claustra01.tfcm.common.processing.SluiceRecipe;
import net.claustra01.tfcm.common.processing.SluiceRecipeSync;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

@JeiPlugin
public final class TFCMJeiPlugin implements IModPlugin {
    private static final TagKey<Item> SLUICES = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("tfc", "sluices"));

    private IJeiRuntime runtime;
    private List<SluiceRecipe> shown = List.of();

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(TFCM.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SluiceRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        SluiceRecipeSync.setListener(this::refresh);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // addons declare their Sluices through TFC's tag
        BuiltInRegistries.ITEM.getTagOrEmpty(SLUICES).forEach(
                item -> registration.addRecipeCatalyst(item.value(), SluiceRecipeCategory.TYPE));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        shown = List.of();
        refresh();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    // the server's recipes can arrive before JEI starts or, after /reload, while it is running
    private void refresh() {
        if (runtime == null) return;
        if (!shown.isEmpty()) runtime.getRecipeManager().hideRecipes(SluiceRecipeCategory.TYPE, shown);
        shown = SluiceRecipeSync.received();
        if (!shown.isEmpty()) runtime.getRecipeManager().addRecipes(SluiceRecipeCategory.TYPE, shown);
    }
}
