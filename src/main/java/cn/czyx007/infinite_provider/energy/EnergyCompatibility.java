package cn.czyx007.infinite_provider.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * 能量兼容性类
 * 处理Forge Energy (FE)系统的兼容性
 */
public class EnergyCompatibility {
    
    /**
     * 检查相邻方块是否有能量接收能力
     * @param world 世界对象
     * @param pos 当前位置
     * @param direction 检查方向
     * @return 是否有能量接收能力
     */
    public static boolean canReceiveEnergy(World world, BlockPos pos, EnumFacing direction) {
        if (world == null) return false;
        
        BlockPos targetPos = pos.offset(direction);
        TileEntity targetTE = world.getTileEntity(targetPos);
        
        if (targetTE != null && targetTE.hasCapability(CapabilityEnergy.ENERGY, direction.getOpposite())) {
            IEnergyStorage energyStorage = targetTE.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite());
            return energyStorage != null && energyStorage.canReceive();
        }
        
        return false;
    }
    
    /**
     * 向相邻方块输出能量
     * @param world 世界对象
     * @param pos 当前位置
     * @param direction 输出方向
     * @param amount 尝试输出的能量数量
     * @param simulate 是否为模拟操作
     * @return 实际输出的能量数量
     */
    public static int outputEnergy(World world, BlockPos pos, EnumFacing direction, int amount, boolean simulate) {
        if (world == null || amount <= 0) return 0;
        
        BlockPos targetPos = pos.offset(direction);
        TileEntity targetTE = world.getTileEntity(targetPos);
        
        if (targetTE != null && targetTE.hasCapability(CapabilityEnergy.ENERGY, direction.getOpposite())) {
            IEnergyStorage energyStorage = targetTE.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite());
            if (energyStorage != null && energyStorage.canReceive()) {
                return energyStorage.receiveEnergy(amount, simulate);
            }
        }
        
        return 0;
    }
    
    /**
     * 获取相邻方块的能量存储信息
     * @param world 世界对象
     * @param pos 当前位置
     * @param direction 检查方向
     * @return 能量存储对象，如果没有则返回null
     */
    public static IEnergyStorage getAdjacentEnergyStorage(World world, BlockPos pos, EnumFacing direction) {
        if (world == null) return null;
        
        BlockPos targetPos = pos.offset(direction);
        TileEntity targetTE = world.getTileEntity(targetPos);
        
        if (targetTE != null && targetTE.hasCapability(CapabilityEnergy.ENERGY, direction.getOpposite())) {
            return targetTE.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite());
        }
        
        return null;
    }
    
    /**
     * 检查TileEntity是否有能量能力
     * @param te TileEntity对象
     * @param direction 方向
     * @return 是否有能量能力
     */
    public static boolean hasEnergyCapability(TileEntity te, EnumFacing direction) {
        return te != null && te.hasCapability(CapabilityEnergy.ENERGY, direction);
    }
    
    /**
     * 获取TileEntity的能量能力
     * @param te TileEntity对象
     * @param direction 方向
     * @return 能量存储对象
     */
    public static IEnergyStorage getEnergyCapability(TileEntity te, EnumFacing direction) {
        if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, direction)) {
            return te.getCapability(CapabilityEnergy.ENERGY, direction);
        }
        return null;
    }
}