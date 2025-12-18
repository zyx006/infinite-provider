package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderHeavyWater;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockInfiniteProviderHeavyWater extends BlockInfiniteProviderBase {
    public BlockInfiniteProviderHeavyWater() {
        super(Tags.MOD_ID + "_heavy_water", Material.ROCK);
    }

    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderHeavyWater();
    }

}
