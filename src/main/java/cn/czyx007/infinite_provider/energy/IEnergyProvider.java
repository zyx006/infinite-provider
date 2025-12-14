package cn.czyx007.infinite_provider.energy;

import net.minecraft.util.EnumFacing;

/**
 * 能量提供者接口
 * 定义了无限供应器提供能量的基本功能
 */
public interface IEnergyProvider {
    
    /**
     * 检查是否可以向指定方向输出能量
     * @param direction 输出方向
     * @return 是否可以输出能量
     */
    boolean canOutputEnergy(EnumFacing direction);
    
    /**
     * 尝试向指定方向输出能量
     * @param direction 输出方向
     * @param amount 尝试输出的能量数量(FE)
     * @param simulate 是否为模拟操作（不实际输出能量）
     * @return 实际输出的能量数量
     */
    int outputEnergy(EnumFacing direction, int amount, boolean simulate);
    
    /**
     * 获取当前存储的能量
     * @return 当前存储的能量(FE)
     */
    int getEnergyStored();
    
    /**
     * 获取最大能量容量
     * @return 最大能量容量(FE)
     */
    int getMaxEnergyStored();
    
    /**
     * 获取最大能量输出速率
     * @return 最大每tick的能量输出速率(FE/t)
     */
    int getMaxEnergyOutput();
    
    /**
     * 获取当前能量生成速率
     * @return 当前每tick的能量生成速率(FE/t)
     */
    int getEnergyGenerationRate();
    
    /**
     * 设置能量生成速率
     * @param rate 新的能量生成速率(FE/t)
     */
    void setEnergyGenerationRate(int rate);
    
    /**
     * 检查是否正在生成能量
     * @return 是否正在生成能量
     */
    boolean isGeneratingEnergy();
    
    /**
     * 设置是否正在生成能量
     * @param generating 是否正在生成能量
     */
    void setGeneratingEnergy(boolean generating);
}