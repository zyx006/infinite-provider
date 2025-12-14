package cn.czyx007.infinite_provider.output;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

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
    
    // 输出速率配置
    private static final int MAX_CAPACITY = Integer.MAX_VALUE; // 最大容量 2.1G
    
    // 输出目标缓存
    private final Map<EnumFacing, OutputTarget> outputTargets = new HashMap<>();
    
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
        boolean shouldCheck = false;
        switch (state) {
            case DEFAULT:
            case SLEEPING:
                shouldCheck = (tickCounter >= DEFAULT_CHECK_INTERVAL);
                break;
            case ACTIVE:
                shouldCheck = (tickCounter >= ACTIVE_CHECK_INTERVAL);
                break;
        }
        
        if (shouldCheck) {
            tickCounter = 0;
            updateOutputTargets();
            performOutput();
        }
    }
    
    /**
     * 更新输出目标
     */
    private void updateOutputTargets() {
        World world = tileEntity.getWorld();
        BlockPos pos = tileEntity.getPos();
        
        // 清除无效的目标
        outputTargets.entrySet().removeIf(entry -> !entry.getValue().isValid());
        
        // 检查所有方向
        for (EnumFacing direction : EnumFacing.values()) {
            // 如果已经有有效的目标，跳过
            if (outputTargets.containsKey(direction) && outputTargets.get(direction).isValid()) {
                continue;
            }
            
            // 尝试找到新的输出目标
            BlockPos targetPos = pos.offset(direction);
            TileEntity targetTE = world.getTileEntity(targetPos);
            
            if (targetTE != null) {
                OutputTarget target = createOutputTarget(targetTE, direction);
                if (target != null) {
                    outputTargets.put(direction, target);
                }
            }
        }
    }
    
    /**
     * 创建输出目标
     */
    @Nullable
    private OutputTarget createOutputTarget(TileEntity targetTE, EnumFacing direction) {
        // 检查是否有物品处理能力
        if (targetTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
            return new ItemOutputTarget(targetTE, direction);
        }
        
        // 检查是否有流体处理能力
        if (targetTE.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite())) {
            return new FluidOutputTarget(targetTE, direction);
        }
        
        return null;
    }
    
    /**
     * 执行输出操作
     */
    private void performOutput() {
        boolean anyOutputSuccess = false;
        
        // 直接使用最大容量（2.1G）进行输出，不进行容量计算
        int itemOutputRate = MAX_CAPACITY;
        int fluidOutputRate = MAX_CAPACITY;

        // 检查供应器类型
        boolean hasItem = provider.getProvidedItem() != null && !provider.getProvidedItem().isEmpty();
        boolean hasFluid = provider.getProvidedFluid() != null;
        
        // 向所有方向同时输出
        for (Map.Entry<EnumFacing, OutputTarget> entry : outputTargets.entrySet()) {
            EnumFacing direction = entry.getKey();
            OutputTarget target = entry.getValue();
            
            // 如果是物品供应器，只输出物品
            if (hasItem && target.canReceiveItem(provider.getProvidedItem())) {
                int itemOutput = provider.outputItem(direction, itemOutputRate);
                if (itemOutput > 0) {
                    anyOutputSuccess = true;
                }
            }
            
            // 如果是流体供应器，只输出流体
            if (hasFluid && target.canReceiveFluid(new FluidStack(provider.getProvidedFluid(), 1))) {
                int fluidOutput = provider.outputFluid(direction, fluidOutputRate);
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
        
        switch (currentState) {
            case DEFAULT:
            case SLEEPING:
                if (outputSuccess) {
                    provider.setOutputState(IOutputProvider.OutputState.ACTIVE);
                }
                break;
            case ACTIVE:
                // 如果输出失败，进入休眠状态
                if (!outputSuccess) {
                    provider.setOutputState(IOutputProvider.OutputState.SLEEPING);
                }
                break;
        }
    }
    
    /**
     * 物品输出目标实现
     */
    private static class ItemOutputTarget implements OutputTarget {
        private final TileEntity tileEntity;
        private final EnumFacing direction;
        
        public ItemOutputTarget(TileEntity tileEntity, EnumFacing direction) {
            this.tileEntity = tileEntity;
            this.direction = direction;
        }
        
        @Override
        public BlockPos getPosition() {
            return tileEntity.getPos();
        }
        
        @Override
        public World getWorld() {
            return tileEntity.getWorld();
        }
        
        @Override
        public EnumFacing getDirection() {
            return direction;
        }
        
        @Override
        public boolean canReceiveItem(ItemStack stack) {
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler == null) {
                return false;
            }
            
            // 模拟插入操作检查是否有空间
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack remainder = handler.insertItem(i, stack, true);
                if (remainder.getCount() < stack.getCount()) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean canReceiveFluid(FluidStack stack) {
            return false; // 这是物品输出目标，不接收流体
        }
        
        @Override
        public int receiveItem(ItemStack stack) {
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler == null) {
                return 0;
            }
            
            int inserted = 0;
            ItemStack remaining = stack.copy();
            
            for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
                int beforeInsert = remaining.getCount();
                remaining = handler.insertItem(i, remaining, false);
                inserted += (beforeInsert - remaining.getCount());
            }
            
            return inserted;
        }
        
        @Override
        public int receiveFluid(FluidStack stack) {
            return 0; // 这是物品输出目标，不接收流体
        }
        
        @Override
        public int getItemCapacity(ItemStack stack) {
            IItemHandler handler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler == null) {
                return 0;
            }
            
            int capacity = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack remainder = handler.insertItem(i, stack, true);
                capacity += (stack.getCount() - remainder.getCount());
            }
            return capacity;
        }
        
        @Override
        public int getFluidCapacity(FluidStack stack) {
            return 0; // 这是物品输出目标，不接收流体
        }
        
        @Override
        public boolean isValid() {
            return !tileEntity.isInvalid() && 
                   tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
        }
    }
    
    /**
     * 流体输出目标实现
     */
    private static class FluidOutputTarget implements OutputTarget {
        private final TileEntity tileEntity;
        private final EnumFacing direction;
        
        public FluidOutputTarget(TileEntity tileEntity, EnumFacing direction) {
            this.tileEntity = tileEntity;
            this.direction = direction;
        }
        
        @Override
        public BlockPos getPosition() {
            return tileEntity.getPos();
        }
        
        @Override
        public World getWorld() {
            return tileEntity.getWorld();
        }
        
        @Override
        public EnumFacing getDirection() {
            return direction;
        }
        
        @Override
        public boolean canReceiveItem(ItemStack stack) {
            return false; // 这是流体输出目标，不接收物品
        }
        
        @Override
        public boolean canReceiveFluid(FluidStack stack) {
            IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler == null) {
                return false;
            }
            
            // 模拟填充操作检查是否有空间
            int filled = handler.fill(stack, false);
            return filled > 0;
        }
        
        @Override
        public int receiveItem(ItemStack stack) {
            return 0; // 这是流体输出目标，不接收物品
        }
        
        @Override
        public int receiveFluid(FluidStack stack) {
            IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler == null) {
                return 0;
            }
            
            return handler.fill(stack, true);
        }
        
        @Override
        public int getItemCapacity(ItemStack stack) {
            return 0; // 这是流体输出目标，不接收物品
        }
        
        @Override
        public int getFluidCapacity(FluidStack stack) {
            IFluidHandler handler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler == null) {
                return 0;
            }
            
            return handler.fill(stack, false);
        }
        
        @Override
        public boolean isValid() {
            return !tileEntity.isInvalid() && 
                   tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
        }
    }
}