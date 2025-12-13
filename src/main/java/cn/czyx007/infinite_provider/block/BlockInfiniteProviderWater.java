package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 无限水供应器
 */
public class BlockInfiniteProviderWater extends BlockInfiniteProviderBase {
    
    public BlockInfiniteProviderWater() {
        super(Tags.MOD_ID + "_water", Material.ROCK);
    }
    
    /**
     * 当实体站在方块上时，给予灭火效果
     */
    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            entity.extinguish();
        }
    }
    
    /**
     * 可以在未来扩展特定功能，比如右键生成水源等
     */
    @Override
    public Item getItemDropped(IBlockState state, java.util.Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }
    
    @Override
    public int quantityDropped(java.util.Random rand) {
        return 1;
    }
}