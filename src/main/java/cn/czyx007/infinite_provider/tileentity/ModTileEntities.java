package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.api.ProviderRegistry;

/**
 * 无限供应器TileEntity注册类
 */
public class ModTileEntities {
    
    public static void registerTileEntities() {
        // 注册内置TileEntity
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderCobblestone.class, Tags.MOD_ID + "_cobblestone");
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderWater.class, Tags.MOD_ID + "_water");
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderLava.class, Tags.MOD_ID + "_lava");

        ProviderRegistry.registerTileEntities();
    }
}