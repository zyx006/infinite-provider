package cn.czyx007.infinite_provider.output;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.EnumMap;
import java.util.Map;

/**
 * 输出调度器
 * 负责管理无限供应器的输出逻辑和状态转换
 * 使用缓存机制提高性能，避免每tick都查询相邻方块
 */
public class OutputScheduler {
    
    private final IOutputProvider provider;
    private final TileEntity tileEntity;
    
    // 输出状态相关
    private int tickCounter = 0;
    private static final int DEFAULT_CHECK_INTERVAL = 20; // 默认每秒检查一次 (20 ticks)
    private static final int ACTIVE_CHECK_INTERVAL = 1;  // 激活状态每tick检查
    
    // 缓存相邻方块的Capability handler
    private final Map<EnumFacing, IItemHandler> itemHandlerCache = new EnumMap<>(EnumFacing.class);
    private final Map<EnumFacing, IFluidHandler> fluidHandlerCache = new EnumMap<>(EnumFacing.class);

    // 缓存是否需要刷新
    private boolean needsRefresh = true;

    public OutputScheduler(IOutputProvider provider, TileEntity tileEntity) {
        this.provider = provider;
        this.tileEntity = tileEntity;
    }
    
    /**
     * 当相邻方块更新时调用，标记需要刷新缓存
     */
    public void markDirty() {
        needsRefresh = true;
    }

    /**
     * 刷新相邻方块的Capability缓存
     */
    private void refreshCache() {
        itemHandlerCache.clear();
        fluidHandlerCache.clear();

        World world = tileEntity.getWorld();
        BlockPos pos = tileEntity.getPos();

        // 检查供应器类型
        ItemStack providedItem = provider.getProvidedItem();
        boolean hasItem = providedItem != null && !providedItem.isEmpty();
        boolean hasFluid = provider.getProvidedFluid() != null;

        // 遍历所有方向，缓存有效的handler
        for (EnumFacing direction : EnumFacing.values()) {
            if (!provider.canOutput(direction)) {
                continue;
            }

            BlockPos targetPos = pos.offset(direction);
            TileEntity targetTE = world.getTileEntity(targetPos);

            if (targetTE == null) {
                continue;
            }

            // 缓存物品handler
            if (hasItem && targetTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
                IItemHandler itemHandler = targetTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
                if (itemHandler != null) {
                    itemHandlerCache.put(direction, itemHandler);
                }
            }

            // 缓存流体handler
            if (hasFluid && targetTE.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite())) {
                IFluidHandler fluidHandler = targetTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
                if (fluidHandler != null) {
                    fluidHandlerCache.put(direction, fluidHandler);
                }
            }
        }

        needsRefresh = false;
    }

    /**
     * 每tick更新方法，由TileEntity的update方法调用
     */
    public void update() {
        if (tileEntity.getWorld().isRemote) {
            return; // 只在服务端执行
        }
        
        // 如果需要刷新缓存，先刷新
        if (needsRefresh) {
            refreshCache();
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
     * 直接使用缓存的handler，无需每次都查询TileEntity
     */
    private void performOutput() {
        boolean anyOutputSuccess = false;

        // 获取当前供应器的最大输出速率
        int maxOutputRate = provider.getMaxOutputRate();

        // 遍历缓存的物品handler并尝试输出
        for (Map.Entry<EnumFacing, IItemHandler> entry : itemHandlerCache.entrySet()) {
            EnumFacing direction = entry.getKey();
            int itemOutput = provider.outputItem(direction, maxOutputRate);
            if (itemOutput > 0) {
                anyOutputSuccess = true;
            }
        }

        // 遍历缓存的流体handler并尝试输出
        for (Map.Entry<EnumFacing, IFluidHandler> entry : fluidHandlerCache.entrySet()) {
            EnumFacing direction = entry.getKey();
            int fluidOutput = provider.outputFluid(direction, maxOutputRate);
            if (fluidOutput > 0) {
                anyOutputSuccess = true;
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