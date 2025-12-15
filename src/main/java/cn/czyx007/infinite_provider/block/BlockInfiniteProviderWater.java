package cn.czyx007.infinite_provider.block;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderWater;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 无限水供应器
 */
public class BlockInfiniteProviderWater extends BlockInfiniteProviderBase {
    
    public BlockInfiniteProviderWater() {
        super(Tags.MOD_ID + "_water", Material.ROCK);
    }
    
    @Override
    protected TileEntity createProviderTileEntity() {
        return new TileEntityInfiniteProviderWater();
    }
    
    /**
     * 当实体行走在方块上时，给予灭火效果
     */
    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof EntityLivingBase) {
            entity.extinguish();
        }
    }
}