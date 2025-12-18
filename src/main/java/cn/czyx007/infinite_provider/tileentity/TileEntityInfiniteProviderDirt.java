package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

public class TileEntityInfiniteProviderDirt extends TileEntityInfiniteProviderBase {
    
    @Override
    public ItemStack getProvidedItem() {
        return new ItemStack(Item.getItemFromBlock(Blocks.DIRT), 1);
    }
    
    @Override
    public Fluid getProvidedFluid() {
        return null;
    }
    
    @Override
    public String getProviderTypeName() {
        return Tags.MOD_ID + "_dirt";
    }
}