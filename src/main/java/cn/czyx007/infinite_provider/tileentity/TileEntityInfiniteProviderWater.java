package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * 无限水供应器TileEntity
 */
public class TileEntityInfiniteProviderWater extends TileEntityInfiniteProviderBase {
    
    @Override
    public ItemStack getProvidedItem() {
        return null;
    }
    
    @Override
    public Fluid getProvidedFluid() {
        return FluidRegistry.WATER;
    }
    
    @Override
    public String getProviderTypeName() {
        return Tags.MOD_ID + "_water";
    }
}