package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.registry.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * 无限供应器基类
 * 提供基本功能，子类可以扩展特定类型的供应器
 */
public class BlockInfiniteProviderBase extends Block {

    protected final String typeName;

    public BlockInfiniteProviderBase(String typeName, Material material) {
        super(material);
        this.typeName = typeName;
        setTranslationKey(Tags.MOD_ID + "." + typeName);
        setRegistryName(Tags.MOD_ID + ":" + typeName);
        setCreativeTab(ModCreativeTabs.INFINITE_PROVIDER_TAB);
        setHardness(3.0F);
        setResistance(10.0F);
    }

    /**
     * 获取供应器类型名称
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * 获取对应的物品
     */
    public Item getItemBlock() {
        return Item.getItemFromBlock(this);
    }
}