package cn.czyx007.infinite_provider.registry;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.block.BlockInfiniteProviderBase;
import cn.czyx007.infinite_provider.block.BlockInfiniteProviderCobblestone;
import cn.czyx007.infinite_provider.block.BlockInfiniteProviderLava;
import cn.czyx007.infinite_provider.block.BlockInfiniteProviderWater;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
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
    
    // 供应器实例
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_COBBLESTONE = new BlockInfiniteProviderCobblestone();
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_WATER = new BlockInfiniteProviderWater();
    public static final BlockInfiniteProviderBase INFINITE_PROVIDER_LAVA = new BlockInfiniteProviderLava();
    
    // 供应器实例数组
    public static final BlockInfiniteProviderBase[] PROVIDERS = {
        INFINITE_PROVIDER_COBBLESTONE,
        INFINITE_PROVIDER_WATER,
        INFINITE_PROVIDER_LAVA
    };
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        for (BlockInfiniteProviderBase provider : PROVIDERS) {
            registerBlock(event, provider);
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (BlockInfiniteProviderBase provider : PROVIDERS) {
            registerItemBlock(event, provider);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (BlockInfiniteProviderBase provider : PROVIDERS) {
            registerModel(provider);
        }
    }
    
    private static void registerBlock(RegistryEvent.Register<Block> event, BlockInfiniteProviderBase block) {
        event.getRegistry().register(block);
    }
    
    private static void registerItemBlock(RegistryEvent.Register<Item> event, BlockInfiniteProviderBase block) {
        event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
    }
    
    @SideOnly(Side.CLIENT)
    private static void registerModel(BlockInfiniteProviderBase block) {
        ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(block), 0,
                new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }
}