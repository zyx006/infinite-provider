package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderDirt;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockInfiniteProviderDirt extends BlockInfiniteProviderBase {
    public BlockInfiniteProviderDirt() {
        super(Tags.MOD_ID + "_dirt", Material.ROCK);
    }

    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderDirt();
    }

}
