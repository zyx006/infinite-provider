package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 无限岩浆供应器
 */
public class BlockInfiniteProviderLava extends BlockInfiniteProviderBase {
    
    public BlockInfiniteProviderLava() {
        super(Tags.MOD_ID + "_lava", Material.ROCK);
    }
    
    /**
     * 当实体站在方块上时，给予火焰伤害
     */
    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            entity.attackEntityFrom(DamageSource.LAVA, 1.0F);
            entity.setFire(1);
        }
    }
    
    /**
     * 可以在未来扩展特定功能，比如右键生成岩浆源等
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