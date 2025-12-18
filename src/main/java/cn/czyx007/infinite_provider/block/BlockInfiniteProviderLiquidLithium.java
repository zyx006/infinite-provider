package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderLiquidLithium;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockInfiniteProviderLiquidLithium extends BlockInfiniteProviderBase {
    public BlockInfiniteProviderLiquidLithium() {
        super(Tags.MOD_ID + "_liquidlithium", Material.ROCK);
    }

    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderLiquidLithium();
    }

}
