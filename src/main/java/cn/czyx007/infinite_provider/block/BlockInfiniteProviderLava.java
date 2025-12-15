package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderLava;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
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
    
    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderLava();
    }
    
    /**
     * 当实体行走在方块上时，给予火焰伤害
     */
    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof EntityLivingBase) {
            entity.attackEntityFrom(DamageSource.LAVA, 2.0F);
            entity.setFire(3);
        }
    }
}