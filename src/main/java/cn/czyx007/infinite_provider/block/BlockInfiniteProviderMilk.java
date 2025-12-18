package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderMilk;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockInfiniteProviderMilk extends BlockInfiniteProviderBase {
    public BlockInfiniteProviderMilk() {
        super(Tags.MOD_ID + "_milk", Material.ROCK);
    }

    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderMilk();
    }

}
