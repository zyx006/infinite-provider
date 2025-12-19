package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.config.GeneratorConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.Fluid;

/**
 * 无限圆石供应器TileEntity
 */
public class TileEntityInfiniteProviderCobblestone extends TileEntityInfiniteProviderBase {
    
    @Override
    public ItemStack getProvidedItem() {
        return new ItemStack(Item.getItemFromBlock(Blocks.COBBLESTONE), 1);
    }
    
    @Override
    public Fluid getProvidedFluid() {
        return null; // 圆石供应器不提供流体
    }
    
    @Override
    public String getProviderTypeName() {
        return Tags.MOD_ID + "_cobblestone";
    }

    @Override
    public int getProviderMaxOutputRate() {
        return GeneratorConfig.providerOutputRate.cobblestoneMaxOutputRate;
    }
}