package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.config.GeneratorConfig;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class TileEntityInfiniteProviderHeavyWater extends TileEntityInfiniteProviderBase{
    @Override
    public ItemStack getProvidedItem() {
        return null;
    }

    @Override
    public Fluid getProvidedFluid() {
        return FluidRegistry.getFluid("heavywater");
    }

    @Override
    public String getProviderTypeName() {
        return Tags.MOD_ID + "_heavy_water";
    }

    @Override
    public int getProviderMaxOutputRate() {
        return GeneratorConfig.providerOutputRate.heavyWaterMaxOutputRate;
    }
}
