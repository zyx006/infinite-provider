package com.example.modid.tileentity;

import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

/**
 * 无限泥土供应器的TileEntity实现
 */
public class TileEntityInfiniteProviderDirt extends TileEntityInfiniteProviderBase {

    @Override
    public String getProviderTypeName() {
        return "infinite_provider_dirt";
    }

    @Override
    public ItemStack getProvidedItem() {
        return Blocks.DIRT != null ? new ItemStack(Blocks.DIRT) : ItemStack.EMPTY; // 提供泥土
    }

    @Override
    public Fluid getProvidedFluid() {
        return null; // 不提供流体
    }

}
