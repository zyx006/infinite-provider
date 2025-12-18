package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class TileEntityInfiniteProviderMilk extends TileEntityInfiniteProviderBase {
    @Override
    public ItemStack getProvidedItem() {
        return null;
    }

    @Override
    public Fluid getProvidedFluid() {
        return FluidRegistry.getFluid("milk");
    }

    @Override
    public String getProviderTypeName() {
        return Tags.MOD_ID + "_milk";
    }
}
