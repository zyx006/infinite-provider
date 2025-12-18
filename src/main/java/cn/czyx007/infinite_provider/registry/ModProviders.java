package cn.czyx007.infinite_provider.registry;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.api.ProviderRegistry;
import cn.czyx007.infinite_provider.block.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 无限供应器注册类
 * 负责管理所有供应器方块实例
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class ModProviders {
    
    // 内置供应器实例
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_COBBLESTONE = new BlockInfiniteProviderCobblestone();
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_DIRT = new BlockInfiniteProviderDirt();
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_WATER = new BlockInfiniteProviderWater();
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_LAVA = new BlockInfiniteProviderLava();
    
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_MILK = new BlockInfiniteProviderMilk();
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_HEAVY_WATER = new BlockInfiniteProviderHeavyWater();
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_LIQUID_BRINE = new BlockInfiniteProviderLiquidBrine();
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_LIQUID_LITHIUM = new BlockInfiniteProviderLiquidLithium();

    /**
     * 初始化并注册内置供应器到注册中心
     * 在preInit阶段调用
     */
    public static void init() {
        ProviderRegistry.registerProvider(INFINITE_PROVIDER_COBBLESTONE);
        ProviderRegistry.registerProvider(INFINITE_PROVIDER_DIRT);
        ProviderRegistry.registerProvider(INFINITE_PROVIDER_WATER);
        ProviderRegistry.registerProvider(INFINITE_PROVIDER_LAVA);
        ProviderRegistry.registerProvider(INFINITE_PROVIDER_MILK);
        if (Loader.isModLoaded("mekanism")) {
            ProviderRegistry.registerProvider(INFINITE_PROVIDER_HEAVY_WATER);
            ProviderRegistry.registerProvider(INFINITE_PROVIDER_LIQUID_BRINE);
            ProviderRegistry.registerProvider(INFINITE_PROVIDER_LIQUID_LITHIUM);
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ProviderRegistry.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ProviderRegistry.registerItems(event);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ProviderRegistry.registerModels();
    }
}