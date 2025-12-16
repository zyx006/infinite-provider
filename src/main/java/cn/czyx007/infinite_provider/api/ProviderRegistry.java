package cn.czyx007.infinite_provider.api;

import cn.czyx007.infinite_provider.InfiniteProvider;
import cn.czyx007.infinite_provider.block.BlockInfiniteProviderBase;
import cn.czyx007.infinite_provider.item.ItemBlockInfiniteProvider;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderBase;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 供应器注册中心
 * 提供API供附属模组注册自定义供应器
 *
 * <p>使用示例（可详见example分支）：</p>
 * <pre>
 * // 在你的附属模组的 preInit 阶段调用
 * ProviderRegistry.registerProvider(new MyCustomProvider());
 *
 * // 如果你的供应器有自定义的TileEntity，也需要注册
 * ProviderRegistry.registerTileEntity(MyCustomTileEntity.class, "infinite_provider_my_custom_provider");
 * </pre>
 *
 * <p>注意：供应器注册必须在 Forge 的 Block/Item 注册事件之前完成，
 * 建议在附属模组的 preInit 阶段进行注册。</p>
 */
public class ProviderRegistry {

    private static final List<BlockInfiniteProviderBase> providers = new ArrayList<>();
    private static final List<TileEntityEntry> tileEntityEntries = new ArrayList<>();
    private static boolean frozen = false;

    /**
     * 注册一个供应器方块
     *
     * @param provider 要注册的供应器方块实例
     * @throws IllegalStateException 如果在注册阶段结束后调用
     */
    public static void registerProvider(BlockInfiniteProviderBase provider) {
        if (frozen) {
            throw new IllegalStateException("Cannot register provider after registration phase. " +
                    "Please register providers during FMLPreInitializationEvent.");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        providers.add(provider);
        InfiniteProvider.LOGGER.info("Registered provider: {}", provider.getRegistryName());
    }

    /**
     * 注册一个供应器的TileEntity
     *
     * @param tileEntityClass TileEntity类
     * @param key 注册名称
     */
    public static void registerTileEntity(Class<? extends TileEntityInfiniteProviderBase> tileEntityClass, String key) {
        if (frozen) {
            throw new IllegalStateException("Cannot register TileEntity after registration phase. " +
                    "Please register TileEntities during FMLPreInitializationEvent.");
        }
        if (tileEntityClass == null || key == null) {
            throw new IllegalArgumentException("TileEntity class and key cannot be null");
        }
        tileEntityEntries.add(new TileEntityEntry(tileEntityClass, key));
        InfiniteProvider.LOGGER.info("Queued TileEntity registration: {} -> {}", tileEntityClass.getSimpleName(), key);
    }

    /**
     * 获取所有已注册的供应器（只读）
     *
     * @return 不可修改的供应器列表
     */
    public static List<BlockInfiniteProviderBase> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    /**
     * 内部方法：注册所有方块
     * 由ModProviders在Block注册事件中调用
     */
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        frozen = true;
        for (BlockInfiniteProviderBase provider : providers) {
            event.getRegistry().register(provider);
        }
    }

    /**
     * 内部方法：注册所有物品
     * 由ModProviders在Item注册事件中调用
     */
    public static void registerItems(RegistryEvent.Register<Item> event) {
        for (BlockInfiniteProviderBase provider : providers) {
            ResourceLocation registryName = provider.getRegistryName();
            if (registryName != null) {
                event.getRegistry().register(
                        new ItemBlockInfiniteProvider(provider).setRegistryName(registryName)
                );
            }
        }
    }

    /**
     * 内部方法：注册所有模型
     * 由ModProviders在模型注册事件中调用
     */
    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        for (BlockInfiniteProviderBase provider : providers) {
            ResourceLocation registryName = provider.getRegistryName();
            if (registryName != null) {
                ModelLoader.setCustomModelResourceLocation(
                        Item.getItemFromBlock(provider), 0,
                        new ModelResourceLocation(registryName, "inventory")
                );
            }
        }
    }

    /**
     * 内部方法：注册所有TileEntity
     * 由ModTileEntities调用
     */
    public static void registerTileEntities() {
        for (TileEntityEntry entry : tileEntityEntries) {
            GameRegistry.registerTileEntity(entry.tileEntityClass, entry.key);
            InfiniteProvider.LOGGER.debug("Registered TileEntity: {} -> {}",
                    entry.tileEntityClass.getSimpleName(), entry.key);
        }
    }

    /**
     * TileEntity注册条目
     */
    private static class TileEntityEntry {
        final Class<? extends TileEntityInfiniteProviderBase> tileEntityClass;
        final String key;

        TileEntityEntry(Class<? extends TileEntityInfiniteProviderBase> tileEntityClass, String key) {
            this.tileEntityClass = tileEntityClass;
            this.key = key;
        }
    }
}

