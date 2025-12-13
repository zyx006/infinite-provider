package cn.czyx007.infinite_provider.output;

import net.minecraft.util.EnumFacing;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

/**
 * 输出提供者接口
 * 定义了无限供应器输出物品或流体的基本功能
 */
public interface IOutputProvider {
    
    /**
     * 检查是否可以向指定方向输出
     * @param direction 输出方向
     * @return 是否可以输出
     */
    boolean canOutput(EnumFacing direction);
    
    /**
     * 尝试向指定方向输出物品
     * @param direction 输出方向
     * @param amount 尝试输出的数量
     * @return 实际输出的数量
     */
    int outputItem(EnumFacing direction, int amount);
    
    /**
     * 尝试向指定方向输出流体
     * @param direction 输出方向
     * @param amount 尝试输出的数量(mB)
     * @return 实际输出的数量
     */
    int outputFluid(EnumFacing direction, int amount);
    
    /**
     * 获取供应器提供的物品
     * @return 提供的物品
     */
    ItemStack getProvidedItem();
    
    /**
     * 获取供应器提供的流体
     * @return 提供的流体
     */
    Fluid getProvidedFluid();
    
    /**
     * 获取最大输出速率
     * @return 最大每tick的输出速率 (2.1G)
     */
    int getMaxOutputRate();
    
    /**
     * 获取输出状态
     * @return 当前输出状态
     */
    OutputState getOutputState();
    
    /**
     * 设置输出状态
     * @param state 新的输出状态
     */
    void setOutputState(OutputState state);
    
    /**
     * 输出状态枚举
     */
    enum OutputState {
        DEFAULT,  // 默认状态：每秒检测一次
        ACTIVE,   // 激活状态：每tick以最大速率输出
        SLEEPING  // 休眠状态：输出失败后，回到每秒检测一次的状态
    }
}