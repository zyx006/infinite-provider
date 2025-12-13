package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

/**
 * 无限圆石供应器
 */
public class BlockInfiniteProviderCobblestone extends BlockInfiniteProviderBase {
    
    public BlockInfiniteProviderCobblestone() {
        super(Tags.MOD_ID + "_cobblestone", Material.ROCK);
    }
    
    /**
     * 可以在未来扩展特定功能，比如右键生成圆石等
     */
    @Override
    public Item getItemDropped(net.minecraft.block.state.IBlockState state, java.util.Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }
    
    @Override
    public int quantityDropped(java.util.Random rand) {
        return 1;
    }
}