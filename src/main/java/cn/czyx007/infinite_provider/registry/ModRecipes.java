package cn.czyx007.infinite_provider.registry;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * 配方注册类
 * 负责注册所有无限供应器的合成配方
 * 支持根据mod加载情况动态调整配方
 */
public class ModRecipes {

    /**
     * 注册所有配方
     * 在init阶段调用
     */
    public static void registerRecipes() {
        // 注册基础配方（不依赖其他mod）
        registerBasicRecipes();

        // 根据mod加载情况注册可选配方
        registerConditionalRecipes();
    }

    /**
     * 注册基础配方（不依赖其他mod）
     */
    private static void registerBasicRecipes() {
        // 圆石供应器配方
        registerShapedRecipe("infinite_provider_cobblestone",
            new ItemStack(ModProviders.INFINITE_PROVIDER_COBBLESTONE),
            "OGO",
            "GCG",
            "OGO",
            'O', Blocks.OBSIDIAN,
            'G', Blocks.GLASS,
            'C', Blocks.COBBLESTONE
        );

        // 泥土供应器配方
        registerShapedRecipe("infinite_provider_dirt",
            new ItemStack(ModProviders.INFINITE_PROVIDER_DIRT),
            "OGO",
            "GDG",
            "OGO",
            'O', Blocks.OBSIDIAN,
            'G', Blocks.GLASS,
            'D', Blocks.DIRT
        );

        // 水供应器配方
        registerShapedRecipe("infinite_provider_water",
            new ItemStack(ModProviders.INFINITE_PROVIDER_WATER),
            "OGO",
            "GWG",
            "OGO",
            'O', Blocks.OBSIDIAN,
            'G', Blocks.GLASS,
            'W', Items.WATER_BUCKET
        );

        // 岩浆供应器配方
        registerShapedRecipe("infinite_provider_lava",
            new ItemStack(ModProviders.INFINITE_PROVIDER_LAVA),
            "OGO",
            "GLG",
            "OGO",
            'O', Blocks.OBSIDIAN,
            'G', Blocks.GLASS,
            'L', Items.LAVA_BUCKET
        );
    }

    /**
     * 注册依赖其他mod的配方
     */
    private static void registerConditionalRecipes() {
        // 牛奶供应器配方（根据是否安装 CookingForBlockheads 使用不同配方）
        registerMilkRecipe();

        // Mekanism mod 配方
        if (Loader.isModLoaded("mekanism")) {
            registerMekanismRecipes();
        }
    }

    /**
     * 注册牛奶供应器配方
     * 如果安装了 CookingForBlockheads mod，使用 cow_jar 作为材料
     * 否则使用原版的 milk_bucket 作为材料
     */
    private static void registerMilkRecipe() {
        Object milkIngredient = Items.MILK_BUCKET;

        // 如果安装了 CookingForBlockheads，尝试使用 cow_jar
        if (Loader.isModLoaded("cookingforblockheads")) {
            try {
                Item cowJar = Item.getByNameOrId("cookingforblockheads:cow_jar");
                if (cowJar != null) {
                    milkIngredient = cowJar;
                }
            } catch (Exception e) {
                // 如果加载失败，继续使用默认的 milk_bucket
            }
        }

        registerShapedRecipe("infinite_provider_milk",
            new ItemStack(ModProviders.INFINITE_PROVIDER_MILK),
            "OGO",
            "GMG",
            "OGO",
            'O', Blocks.OBSIDIAN,
            'G', Blocks.GLASS,
            'M', milkIngredient
        );
    }

    /**
     * 注册 Mekanism 相关配方
     */
    private static void registerMekanismRecipes() {
        try {
            // 获取 Mekanism 物品
            Item machineBlock = Item.getByNameOrId("mekanism:machineblock");
            Item filterUpgrade = Item.getByNameOrId("mekanism:filterupgrade");
            Item salt = Item.getByNameOrId("mekanism:salt");
            Item basicBlock = Item.getByNameOrId("mekanism:basicblock");
            Item basicBlock2 = Item.getByNameOrId("mekanism:basicblock2");

            // 重水供应器配方
            if (machineBlock != null && filterUpgrade != null) {
                registerShapedRecipe("infinite_provider_heavy_water",
                    new ItemStack(ModProviders.INFINITE_PROVIDER_HEAVY_WATER),
                    "PFP",
                    "FWF",
                    "PFP",
                    'P', new ItemStack(machineBlock, 1, 12), // Electric Pump
                    'F', filterUpgrade,
                    'W', ModProviders.INFINITE_PROVIDER_WATER
                );
            }

            // 盐水供应器配方
            if (salt != null) {
                registerShapedRecipe("infinite_provider_liquid_brine",
                    new ItemStack(ModProviders.INFINITE_PROVIDER_LIQUID_BRINE),
                    "SSS",
                    "SWS",
                    "SSS",
                    'S', salt,
                    'W', ModProviders.INFINITE_PROVIDER_WATER
                );
            }

            // 锂供应器配方
            if (basicBlock != null && basicBlock2 != null) {
                registerShapedRecipe("infinite_provider_liquid_lithium",
                    new ItemStack(ModProviders.INFINITE_PROVIDER_LIQUID_LITHIUM),
                    "TCT",
                    "VBV",
                    "TVT",
                    'T', new ItemStack(basicBlock2, 1, 0), // Thermal Evaporation Block
                    'C', new ItemStack(basicBlock, 1, 14), // Thermal Evaporation Controller
                    'V', new ItemStack(basicBlock, 1, 15), // Thermal Evaporation Valve
                    'B', ModProviders.INFINITE_PROVIDER_LIQUID_BRINE
                );
            }
        } catch (Exception e) {
            // 如果加载失败，忽略这些配方
        }
    }

    /**
     * 注册有序合成配方的辅助方法
     *
     * @param name 配方名称
     * @param output 输出物品
     * @param recipe 配方参数（pattern和ingredients交替）
     */
    private static void registerShapedRecipe(String name, ItemStack output, Object... recipe) {
        ResourceLocation location = new ResourceLocation(Tags.MOD_ID, name);
        IRecipe shapedRecipe = new ShapedOreRecipe(location, output, recipe);
        shapedRecipe.setRegistryName(location);
        ForgeRegistries.RECIPES.register(shapedRecipe);
    }
}
