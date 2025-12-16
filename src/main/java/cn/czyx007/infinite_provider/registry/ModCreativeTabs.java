package cn.czyx007.infinite_provider.registry;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 无限供应器模组的创造模式物品栏
 */
public class ModCreativeTabs extends CreativeTabs {

    public static final ModCreativeTabs INFINITE_PROVIDER_TAB = new ModCreativeTabs();

    private ModCreativeTabs() {
        super(Tags.MOD_ID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack createIcon() {
        return new ItemStack(ModProviders.INFINITE_PROVIDER_COBBLESTONE);
    }
}