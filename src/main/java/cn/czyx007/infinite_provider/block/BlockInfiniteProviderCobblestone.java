package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderCobblestone;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

/**
 * 无限圆石供应器
 */
public class BlockInfiniteProviderCobblestone extends BlockInfiniteProviderBase {
    
    public BlockInfiniteProviderCobblestone() {
        super(Tags.MOD_ID + "_cobblestone", Material.ROCK);
    }
    
    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderCobblestone();
    }
    

}