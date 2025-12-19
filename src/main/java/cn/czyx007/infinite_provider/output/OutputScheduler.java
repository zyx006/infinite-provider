package cn.czyx007.infinite_provider.output;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

/**
 * 输出调度器
 * 负责管理无限供应器的输出逻辑和状态转换
 */
public class OutputScheduler {
    
    private final IOutputProvider provider;
    private final TileEntity tileEntity;
    
    // 输出状态相关
    private int tickCounter = 0;
    private static final int DEFAULT_CHECK_INTERVAL = 20; // 默认每秒检查一次 (20 ticks)
    private static final int ACTIVE_CHECK_INTERVAL = 1;  // 激活状态每tick检查
    
    public OutputScheduler(IOutputProvider provider, TileEntity tileEntity) {
        this.provider = provider;
        this.tileEntity = tileEntity;
    }
    
    /**
     * 每tick更新方法，由TileEntity的update方法调用
     */
    public void update() {
        if (tileEntity.getWorld().isRemote) {
            return; // 只在服务端执行
        }
        
        IOutputProvider.OutputState state = provider.getOutputState();
        tickCounter++;
        
        // 根据状态决定检查频率
        int checkInterval = (state == IOutputProvider.OutputState.ACTIVE) ? ACTIVE_CHECK_INTERVAL : DEFAULT_CHECK_INTERVAL;

        if (tickCounter >= checkInterval) {
            tickCounter = 0;
            performOutput();
        }
    }
    
    /**
     * 执行输出操作
     */
    private void performOutput() {
        boolean anyOutputSuccess = false;
        World world = tileEntity.getWorld();
        BlockPos pos = tileEntity.getPos();

        // 获取当前供应器的最大输出速率
        int maxOutputRate = provider.getMaxOutputRate();

        // 检查供应器类型
        ItemStack providedItem = provider.getProvidedItem();
        boolean hasItem = providedItem != null && !providedItem.isEmpty();
        boolean hasFluid = provider.getProvidedFluid() != null;

        // 向所有方向同时输出
        for (EnumFacing direction : EnumFacing.values()) {
            if (!provider.canOutput(direction)) {
                continue;
            }
            
            BlockPos targetPos = pos.offset(direction);
            TileEntity targetTE = world.getTileEntity(targetPos);
            
            if (targetTE == null) {
                continue;
            }

            // 如果是物品供应器，尝试输出物品
            if (hasItem && targetTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
                int itemOutput = provider.outputItem(direction, maxOutputRate);
                if (itemOutput > 0) {
                    anyOutputSuccess = true;
                }
            }
            
            // 如果是流体供应器，尝试输出流体
            if (hasFluid && targetTE.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite())) {
                int fluidOutput = provider.outputFluid(direction, maxOutputRate);
                if (fluidOutput > 0) {
                    anyOutputSuccess = true;
                }
            }
        }
        
        // 根据输出结果更新状态
        updateOutputState(anyOutputSuccess);
    }
    
    /**
     * 根据输出结果更新状态
     */
    private void updateOutputState(boolean outputSuccess) {
        IOutputProvider.OutputState currentState = provider.getOutputState();
        
        if (outputSuccess && currentState != IOutputProvider.OutputState.ACTIVE) {
            provider.setOutputState(IOutputProvider.OutputState.ACTIVE);
        } else if (!outputSuccess && currentState == IOutputProvider.OutputState.ACTIVE) {
            provider.setOutputState(IOutputProvider.OutputState.SLEEPING);
        }
    }
}