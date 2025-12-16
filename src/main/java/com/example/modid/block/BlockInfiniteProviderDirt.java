package com.example.modid.block;

import cn.czyx007.infinite_provider.block.BlockInfiniteProviderBase;
import com.example.modid.tileentity.TileEntityInfiniteProviderDirt;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

/**
 * 无限泥土供应器方块
 */
public class BlockInfiniteProviderDirt extends BlockInfiniteProviderBase {

    public BlockInfiniteProviderDirt() {
        super("infinite_provider_dirt", Material.GROUND);
    }

    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderDirt();
    }
}
