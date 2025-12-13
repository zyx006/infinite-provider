package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * 无限岩浆供应器TileEntity
 */
public class TileEntityInfiniteProviderLava extends TileEntityInfiniteProviderBase {
    
    @Override
    public ItemStack getProvidedItem() {
        return null;
    }
    
    @Override
    public Fluid getProvidedFluid() {
        return FluidRegistry.LAVA;
    }
    
    @Override
    public String getProviderTypeName() {
        return Tags.MOD_ID + "_lava";
    }
}