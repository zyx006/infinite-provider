package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.api.ProviderRegistry;
import net.minecraftforge.fml.common.Loader;

/**
 * 无限供应器TileEntity注册类
 */
public class ModTileEntities {
    
    public static void registerTileEntities() {
        // 注册内置TileEntity
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderCobblestone.class, Tags.MOD_ID + "_cobblestone");
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderDirt.class, Tags.MOD_ID + "_dirt");
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderWater.class, Tags.MOD_ID + "_water");
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderLava.class, Tags.MOD_ID + "_lava");
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderMilk.class, Tags.MOD_ID + "_milk");
        if (Loader.isModLoaded("mekanism")) {
            ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderHeavyWater.class, Tags.MOD_ID + "_heavy_water");
            ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderLiquidBrine.class, Tags.MOD_ID + "_liquidbrine");
            ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderLiquidLithium.class, Tags.MOD_ID + "_liquidlithium");
        }

        ProviderRegistry.registerTileEntities();
    }
}