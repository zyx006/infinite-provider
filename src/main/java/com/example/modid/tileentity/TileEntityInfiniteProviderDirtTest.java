package com.example.modid.tileentity;

import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

/**
 * Infinite Dirt Provider TileEntity Implementation
 */
public class TileEntityInfiniteProviderDirtTest extends TileEntityInfiniteProviderBase {

    @Override
    public String getProviderTypeName() {
        return "infinite_provider_dirt_test";
    }

    @Override
    public ItemStack getProvidedItem() {
        return new ItemStack(Item.getItemFromBlock(Blocks.DIRT), 1);
    }

    @Override
    public Fluid getProvidedFluid() {
        return null; //No fluid provided
    }

    @Override
    public int getProviderMaxOutputRate() {
        return Integer.MAX_VALUE;
    }
}
