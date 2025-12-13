package cn.czyx007.infinite_provider.output;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

/**
 * 输出目标接口
 * 表示可以接收物品或流体的目标
 */
public interface OutputTarget {
    
    /**
     * 获取目标位置
     * @return 目标位置
     */
    BlockPos getPosition();
    
    /**
     * 获取目标所在世界
     * @return 目标世界
     */
    World getWorld();
    
    /**
     * 获取连接方向
     * @return 连接方向
     */
    EnumFacing getDirection();
    
    /**
     * 检查是否可以接收物品
     * @param stack 物品堆
     * @return 是否可以接收
     */
    boolean canReceiveItem(ItemStack stack);
    
    /**
     * 检查是否可以接收流体
     * @param stack 流体堆
     * @return 是否可以接收
     */
    boolean canReceiveFluid(FluidStack stack);
    
    /**
     * 尝试接收物品
     * @param stack 物品堆
     * @return 实际接收的数量
     */
    int receiveItem(ItemStack stack);
    
    /**
     * 尝试接收流体
     * @param stack 流体堆
     * @return 实际接收的数量
     */
    int receiveFluid(FluidStack stack);
    
    /**
     * 获取物品容量
     * @return 可接收的物品数量
     */
    int getItemCapacity(ItemStack stack);
    
    /**
     * 获取流体容量
     * @return 可接收的流体数量(mB)
     */
    int getFluidCapacity(FluidStack stack);
    
    /**
     * 检查目标是否有效
     * @return 目标是否仍然有效
     */
    boolean isValid();
}