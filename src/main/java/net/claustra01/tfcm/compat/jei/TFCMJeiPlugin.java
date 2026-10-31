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
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class TFCMJeiPlugin implements IModPlugin {
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
        BuiltInRegistries.ITEM.entrySet().forEach(entry -> {
            ResourceLocation id = entry.getKey().location();
            if (id.getNamespace().equals("tfc") && id.getPath().startsWith("wood/sluice/"))
                registration.addRecipeCatalyst(entry.getValue(), SluiceRecipeCategory.TYPE);
        });
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
