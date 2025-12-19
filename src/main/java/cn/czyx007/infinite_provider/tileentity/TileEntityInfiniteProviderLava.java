package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.energy.PowerGeneration;
import cn.czyx007.infinite_provider.config.GeneratorConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * 无限岩浆供应器TileEntity
 * 支持集群检测和倍率计算功能
 */
public class TileEntityInfiniteProviderLava extends TileEntityInfiniteProviderBase {
    
    // 集群相关状态
    protected float clusterBonus = 0.0f;
    protected boolean isPartOfCluster = false;
    protected int tickCounter = 0;
    
    // 构造函数
    public TileEntityInfiniteProviderLava() {
        super();
    }
    
    @Override
    public ItemStack getProvidedItem() {
        return null;
    }
    
    @Override
    public Fluid getProvidedFluid() {
        return FluidRegistry.LAVA;
    }
    
    @Override
    public String getProviderTypeName() {
        return Tags.MOD_ID + "_lava";
    }
    
    @Override
    public int getProviderMaxOutputRate() {
        return GeneratorConfig.providerOutputRate.lavaMaxOutputRate;
    }

    @Override
    public void update() {
        super.update();
        
        // 每秒更新一次集群状态
        if (!world.isRemote && tickCounter++ % 20 == 0) {
            updateClusterStatus();
        }
    }
    
    /**
     * 更新集群状态和倍率
     */
    protected void updateClusterStatus() {
        // 检查是否为集群的一部分
        isPartOfCluster = checkClusterMembership();
        
        if (isPartOfCluster) {
            // 计算集群加成倍数
            clusterBonus = calculateClusterBonus();
        } else {
            clusterBonus = 0.0f;
        }
    }
    
    /**
     * 检查是否为集群的一部分
     * @return 是否为集群的一部分
     */
    protected boolean checkClusterMembership() {
        // 检查上方是否有水供应器
        BlockPos abovePos = pos.up();
        if (world.getTileEntity(abovePos) instanceof TileEntityInfiniteProviderWater) {
            return true; // 上方有水供应器，说明是发电系统的一部分
        }
        
        // 检查周围是否有其他岩浆供应器
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // 跳过自身
                
                BlockPos checkPos = pos.add(x, 0, z);
                if (world.getTileEntity(checkPos) instanceof TileEntityInfiniteProviderLava) {
                    return true; // 周围有其他岩浆供应器
                }
            }
        }
        
        return false;
    }
    
    /**
     * 计算集群加成倍数
     * @return 集群加成倍数
     */
    protected float calculateClusterBonus() {
        // 检查上方是否有水供应器
        BlockPos abovePos = pos.up();
        if (world.getTileEntity(abovePos) instanceof TileEntityInfiniteProviderWater) {
            // 作为发电系统的一部分，使用PowerGeneration类中的计算方法
            return PowerGeneration.calculateLavaClusterBonus(world, abovePos);
        }
        
        double[] bonuses = GeneratorConfig.getLavaClusterBonuses();
        
        // 检查是否为3×3集群
        if (is3x3Cluster()) {
            return (float) bonuses[2]; // 3×3集群倍率
        }
        
        // 检查是否为侧面4个
        if (isSideGroup()) {
            return (float) bonuses[1]; // 侧面4个倍率
        }
        
        // 单个岩浆供应器
        return (float) bonuses[0];
    }
    
    /**
     * 检查是否为3×3集群
     * @return 是否为3×3集群
     */
    protected boolean is3x3Cluster() {
        // 检查3×3区域内的所有位置
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue; // 跳过自身
                
                BlockPos checkPos = pos.add(x, 0, z);
                if (!(world.getTileEntity(checkPos) instanceof TileEntityInfiniteProviderLava)) {
                    return false; // 发现非岩浆供应器，不是3×3集群
                }
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否为侧面4个
     * @return 是否为侧面4个
     */
    protected boolean isSideGroup() {
        // 检查四个方向(东、西、南、北)的岩浆供应器
        return world.getTileEntity(pos.east()) instanceof TileEntityInfiniteProviderLava &&
               world.getTileEntity(pos.west()) instanceof TileEntityInfiniteProviderLava &&
               world.getTileEntity(pos.south()) instanceof TileEntityInfiniteProviderLava &&
               world.getTileEntity(pos.north()) instanceof TileEntityInfiniteProviderLava;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        // 读取集群状态
        clusterBonus = compound.getFloat("clusterBonus");
        isPartOfCluster = compound.getBoolean("isPartOfCluster");
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        
        // 保存集群状态
        compound.setFloat("clusterBonus", clusterBonus);
        compound.setBoolean("isPartOfCluster", isPartOfCluster);
        
        return compound;
    }
}