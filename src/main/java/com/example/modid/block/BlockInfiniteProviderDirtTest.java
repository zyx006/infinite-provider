package com.example.modid.block;

import cn.czyx007.infinite_provider.block.BlockInfiniteProviderBase;
import com.example.modid.tileentity.TileEntityInfiniteProviderDirtTest;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

/**
 * Infinite Dirt Test Provider Block
 */
public class BlockInfiniteProviderDirtTest extends BlockInfiniteProviderBase {

    public BlockInfiniteProviderDirtTest() {
        super("infinite_provider_dirt_test", Material.GROUND);
    }

    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderDirtTest();
    }
}
