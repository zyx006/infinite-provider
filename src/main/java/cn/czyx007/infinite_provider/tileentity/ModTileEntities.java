package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * 无限供应器TileEntity注册类
 */
public class ModTileEntities {
    
    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityInfiniteProviderCobblestone.class, Tags.MOD_ID + "_cobblestone");
        GameRegistry.registerTileEntity(TileEntityInfiniteProviderWater.class, Tags.MOD_ID + "_water");
        GameRegistry.registerTileEntity(TileEntityInfiniteProviderLava.class, Tags.MOD_ID + "_lava");
    }
}