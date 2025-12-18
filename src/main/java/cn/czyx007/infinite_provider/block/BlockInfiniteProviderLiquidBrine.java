package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderLiquidBrine;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockInfiniteProviderLiquidBrine extends BlockInfiniteProviderBase {
    public BlockInfiniteProviderLiquidBrine() {
        super(Tags.MOD_ID + "_liquidbrine", Material.ROCK);
    }

    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderLiquidBrine();
    }

}
