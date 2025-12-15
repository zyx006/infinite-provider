package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.output.IOutputProvider;
import cn.czyx007.infinite_provider.output.OutputScheduler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 无限供应器TileEntity基类
 * 提供基础功能，子类可以扩展特定类型的供应器
 */
public abstract class TileEntityInfiniteProviderBase extends TileEntity implements ITickable, IOutputProvider {

    // 输出系统相关
    protected OutputScheduler outputScheduler;
    protected OutputState outputState = OutputState.DEFAULT;

    /**
     * 获取供应器提供的物品
     */
    public abstract ItemStack getProvidedItem();

    /**
     * 获取供应器提供的流体
     */
    public abstract Fluid getProvidedFluid();

    /**
     * 获取供应器类型名称
     */
    public abstract String getProviderTypeName();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        // 物品供应器才暴露物品处理能力
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && 
            getProvidedItem() != null && !getProvidedItem().isEmpty()) {
            return true;
        }
        // 流体供应器才暴露流体处理能力
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && getProvidedFluid() != null) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        // 物品供应器才暴露物品处理能力
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && 
            getProvidedItem() != null && !getProvidedItem().isEmpty()) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new InfiniteItemHandler());
        }
        // 流体供应器才暴露流体处理能力
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && getProvidedFluid() != null) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new InfiniteFluidHandler());
        }
        return super.getCapability(capability, facing);
    }
    
    /**
     * 无限物品处理器 - 提供无限物品
     */
    private class InfiniteItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            ItemStack provided = getProvidedItem();
            if (provided != null && !provided.isEmpty()) {
                ItemStack stack = provided.copy();
                stack.setCount(Integer.MAX_VALUE);
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack; // 无限供应器不接受物品输入
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack provided = getProvidedItem();
            if (provided != null && !provided.isEmpty()) {
                ItemStack stack = provided.copy();
                stack.setCount(amount);
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }
    }
    
    /**
     * 无限流体处理器 - 提供无限流体
     */
    private class InfiniteFluidHandler implements IFluidHandler {
        @Override
        public IFluidTankProperties[] getTankProperties() {
            Fluid fluid = getProvidedFluid();
            if (fluid != null) {
                return new IFluidTankProperties[] {
                    new IFluidTankProperties() {
                        @Override
                        public FluidStack getContents() {
                            return new FluidStack(fluid, Integer.MAX_VALUE);
                        }

                        @Override
                        public int getCapacity() {
                            return Integer.MAX_VALUE;
                        }

                        @Override
                        public boolean canFill() {
                            return false;
                        }

                        @Override
                        public boolean canDrain() {
                            return true;
                        }

                        @Override
                        public boolean canFillFluidType(FluidStack fluidStack) {
                            return false;
                        }

                        @Override
                        public boolean canDrainFluidType(FluidStack fluidStack) {
                            return fluidStack != null && fluidStack.getFluid() == fluid;
                        }
                    }
                };
            }
            return new IFluidTankProperties[0];
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0; // 无限供应器不接受流体输入
        }

        @Override
        @javax.annotation.Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            Fluid fluid = getProvidedFluid();
            if (fluid != null && resource != null && resource.getFluid() == fluid) {
                return new FluidStack(fluid, resource.amount);
            }
            return null;
        }

        @Override
        @javax.annotation.Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            Fluid fluid = getProvidedFluid();
            if (fluid != null) {
                return new FluidStack(fluid, maxDrain);
            }
            return null;
        }
    }

    /**
     * 处理玩家左键交互
     */
    public void onLeftClick(EntityPlayer player, boolean isShiftClick) {
        ItemStack heldItem = player.getHeldItemMainhand();
        
        // 首先尝试处理流体取出（需要容器）
        if (getProvidedFluid() != null && !heldItem.isEmpty() && isFluidContainer(heldItem)) {
            extractFluid(player, isShiftClick);
            return;
        }

        // 然后尝试处理物品取出（可以空手）
        ItemStack providedItem = getProvidedItem();
        if (providedItem != null && !providedItem.isEmpty()) {
            extractItem(player, providedItem, isShiftClick);
        }
    }
    
    /**
     * 处理玩家右键交互
     */
    public void onRightClick(EntityPlayer player, boolean isShiftClick) {
        ItemStack heldItem = player.getHeldItemMainhand();
        
        if (heldItem.isEmpty()) {
            return;
        }
        
        // 检查是否是同种物品
        ItemStack providedItem = getProvidedItem();
        if (providedItem != null && !providedItem.isEmpty()) {
            if (ItemStack.areItemsEqual(heldItem, providedItem) && 
                ItemStack.areItemStackTagsEqual(heldItem, providedItem)) {
                // 接受同种物品输入（直接消耗掉）
                heldItem.shrink(heldItem.getCount());
                player.inventoryContainer.detectAndSendChanges();
                return;
            }
        }
        
        // 检查是否是装有同种流体的容器
        Fluid providedFluid = getProvidedFluid();
        if (providedFluid != null && isFluidContainer(heldItem)) {
            // 创建单个容器的副本进行处理
            ItemStack singleContainer = heldItem.copy();
            singleContainer.setCount(1);
            
            IFluidHandlerItem fluidHandler = singleContainer.getCapability(
                CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null) {
                IFluidTankProperties[] props = fluidHandler.getTankProperties();
                if (props != null && props.length > 0) {
                    FluidStack contents = props[0].getContents();
                    if (contents != null && contents.getFluid() == providedFluid) {
                        // 接受同种流体输入（清空容器）
                        FluidStack drained = fluidHandler.drain(Integer.MAX_VALUE, true);
                        if (drained != null && drained.amount > 0) {
                            // 从副本的FluidHandler获取清空后的容器
                            ItemStack emptyContainer = fluidHandler.getContainer();
                            
                            // 消耗原手持物品中的一个
                            heldItem.shrink(1);
                            
                            // 处理空容器的放置
                            int currentSlot = player.inventory.currentItem;
                            if (heldItem.getCount() <= 0) {
                                // 手持槽空了，直接放空容器
                                player.inventory.setInventorySlotContents(currentSlot, emptyContainer);
                            } else {
                                // 手持槽还有容器，尝试添加到背包
                                if (!player.inventory.addItemStackToInventory(emptyContainer)) {
                                    // 背包满了，丢出空容器
                                    if (!player.world.isRemote) {
                                        player.dropItem(emptyContainer, false);
                                    }
                                }
                            }
                            player.inventoryContainer.detectAndSendChanges();
                        }
                    }
                }
            }
        }
    }

    /**
     * 取出物品
     */
    private void extractItem(EntityPlayer player, ItemStack providedItem, boolean isShiftClick) {
        ItemStack stackToGive = providedItem.copy();

        if (isShiftClick) {
            // Shift+左键：取出一组
            stackToGive.setCount(stackToGive.getMaxStackSize());
        } else {
            // 单击左键：取出1个
            stackToGive.setCount(1);
        }

        // 尝试给玩家物品
        if (player.inventory.addItemStackToInventory(stackToGive)) {
            player.inventoryContainer.detectAndSendChanges();
        } else {
            // 如果背包满了，尝试逐个放入
            int actuallyGiven = 0;
            int maxStackSize = stackToGive.getMaxStackSize();

            if (isShiftClick) {
                // Shift+点击时，尝试逐个放入直到背包满或达到最大堆叠数
                for (int i = 0; i < maxStackSize; i++) {
                    ItemStack singleItem = stackToGive.copy();
                    singleItem.setCount(1);
                    if (player.inventory.addItemStackToInventory(singleItem)) {
                        actuallyGiven++;
                    } else {
                        break;
                    }
                }
            } else {
                // 单击时，只尝试放入1个
                ItemStack singleItem = stackToGive.copy();
                singleItem.setCount(1);
                if (player.inventory.addItemStackToInventory(singleItem)) {
                    actuallyGiven = 1;
                }
            }

            if (actuallyGiven > 0) {
                player.inventoryContainer.detectAndSendChanges();
            }
        }
    }

    /**
     * 取出流体
     */
    private void extractFluid(EntityPlayer player, boolean isShiftClick) {
        ItemStack heldItem = player.getHeldItemMainhand();
        if (heldItem.isEmpty()) {
            return;
        }

        int containerCount = heldItem.getCount();
        int currentSlot = player.inventory.currentItem;

        // Shift左键且容器数量>1时，先检查是否可以原地替换
        if (isShiftClick && containerCount > 1) {
            // 测试第一个容器
            ItemStack testContainer = heldItem.copy();
            testContainer.setCount(1);

            IFluidHandlerItem testHandler = testContainer.getCapability(
                CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (testHandler != null) {
                IFluidTankProperties[] testProps = testHandler.getTankProperties();
                if (testProps != null && testProps.length > 0) {
                    int[] spaceInfo = calculateFluidContainerSpace(testProps);
                    int testSpace = spaceInfo[2]; // 可用空间

                    if (testSpace > 0) {
                        int testFilled = testHandler.fill(new FluidStack(getProvidedFluid(), testSpace), true);
                        if (testFilled > 0) {
                            ItemStack testFilledContainer = testHandler.getContainer();

                            // 如果填充后的容器可堆叠，直接原地替换所有容器
                            if (testFilledContainer.isStackable()) {
                                // 填充所有容器
                                ItemStack accumulatedFilled = ItemStack.EMPTY;
                                int successCount = 0;

                                for (int i = 0; i < containerCount; i++) {
                                    ItemStack singleContainer = heldItem.copy();
                                    singleContainer.setCount(1);

                                    ItemStack filledResult = fillSingleContainer(singleContainer);
                                    if (filledResult == null) break;

                                    if (accumulatedFilled.isEmpty()) {
                                        accumulatedFilled = filledResult.copy();
                                    } else {
                                        accumulatedFilled.grow(1);
                                    }
                                    successCount++;
                                }

                                // 直接替换手持槽
                                if (successCount > 0) {
                                    player.inventory.setInventorySlotContents(currentSlot, accumulatedFilled);
                                    player.inventoryContainer.detectAndSendChanges();
                                }
                                return; // 完成处理
                            }
                        }
                    }
                }
            }
        }

        // 正常处理模式（不可堆叠容器或单击或背包有空间时逐个处理）
        int filledCount = 0;
        int maxFillCount = isShiftClick ? containerCount : 1;

        for (int i = 0; i < maxFillCount; i++) {
            // 为每个容器创建单独的副本
            ItemStack singleContainer = heldItem.copy();
            singleContainer.setCount(1);
            
            // 填充容器
            ItemStack filledContainer = fillSingleContainer(singleContainer);
            if (filledContainer == null) {
                continue;
            }

            // 记录当前状态
            boolean willEmptySlot = heldItem.getCount() == 1;

            // 先消耗原容器（这样会腾出空间）
            heldItem.shrink(1);

            // 如果消耗后手持槽变空了，优先把填充后的容器放回手持槽
            if (willEmptySlot) {
                player.inventory.setInventorySlotContents(currentSlot, filledContainer);
                filledCount++;
                // 不需要继续处理，因为已经填充了唯一的容器
                break;
            } else {
                // 否则尝试添加到背包其他位置
                if (!player.inventory.addItemStackToInventory(filledContainer)) {
                    // 如果添加失败，丢出物品
                    if (!player.world.isRemote) {
                        player.dropItem(filledContainer, false);
                    }
                }
                filledCount++;
            }
        }
        
        // 如果手中的物品用完了且没有成功填充，清空手持槽
        if (heldItem.getCount() <= 0 && filledCount == 0) {
            player.inventory.setInventorySlotContents(currentSlot, ItemStack.EMPTY);
        }
        
        if (filledCount > 0) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    /**
     * 检查物品是否是流体容器
     */
    private boolean isFluidContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        
        // 检查是否有流体容器能力
        if (!stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return false;
        }
        
        // 进一步检查容器能力
        IFluidHandlerItem fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler == null) {
            return false;
        }
        
        // 检查是否有有效的储罐属性
        IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
        return tankProperties != null && tankProperties.length > 0;
    }

    /**
     * 计算流体容器的可用空间
     * @param tankProperties 流体储罐属性数组
     * @return 数组：[0]=总容量, [1]=当前量, [2]=可用空间
     */
    private int[] calculateFluidContainerSpace(IFluidTankProperties[] tankProperties) {
        int totalCapacity = 0;
        int currentAmount = 0;

        if (tankProperties != null) {
            for (IFluidTankProperties prop : tankProperties) {
                if (prop != null) {
                    totalCapacity += prop.getCapacity();
                    FluidStack contents = prop.getContents();
                    if (contents != null) {
                        currentAmount += contents.amount;
                    }
                }
            }
        }

        int availableSpace = totalCapacity - currentAmount;
        return new int[]{totalCapacity, currentAmount, availableSpace};
    }

    /**
     * 填充单个流体容器
     * @param container 要填充的容器
     * @return 填充后的容器，如果失败返回null
     */
    private ItemStack fillSingleContainer(ItemStack container) {
        IFluidHandlerItem fluidHandler = container.getCapability(
            CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler == null) {
            return null;
        }

        IFluidTankProperties[] tankProps = fluidHandler.getTankProperties();
        if (tankProps == null || tankProps.length == 0) {
            return null;
        }

        int[] spaceInfo = calculateFluidContainerSpace(tankProps);
        int availableSpace = spaceInfo[2];

        if (availableSpace <= 0) {
            return null;
        }

        int actualFilled = fluidHandler.fill(new FluidStack(getProvidedFluid(), availableSpace), true);
        if (actualFilled <= 0) {
            return null;
        }

        return fluidHandler.getContainer();
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        // 读取输出状态
        if (compound.hasKey("outputState")) {
            outputState = OutputState.valueOf(compound.getString("outputState"));
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        
        // 保存输出状态
        compound.setString("outputState", outputState.name());
        
        return compound;
    }


    @Override
    public void update() {
        // 初始化输出调度器
        if (outputScheduler == null) {
            outputScheduler = new OutputScheduler(this, this);
        }
        
        // 更新输出系统
        outputScheduler.update();
    }
    
    // ============ IOutputProvider 接口实现 ============
    
    @Override
    public boolean canOutput(EnumFacing direction) {
        return true; // 默认所有方向都可以输出
    }
    
    @Override
    public int outputItem(EnumFacing direction, int amount) {
        ItemStack providedItem = getProvidedItem();
        if (providedItem == null || providedItem.isEmpty()) {
            return 0;
        }
        
        // 直接以最大速率输出，不进行分批处理
        // 创建要输出的物品堆
        ItemStack outputStack = providedItem.copy();
        outputStack.setCount(amount);
        
        // 尝试输出到相邻的方块
        return outputToAdjacentInventory(direction, outputStack);
    }
    
    @Override
    public int outputFluid(EnumFacing direction, int amount) {
        Fluid providedFluid = getProvidedFluid();
        if (providedFluid == null) {
            return 0;
        }
        
        // 直接以最大速率输出，不进行分批处理
        // 创建要输出的流体堆
        FluidStack outputStack = new FluidStack(providedFluid, amount);
        
        // 尝试输出到相邻的方块
        return outputToAdjacentFluidHandler(direction, outputStack);
    }
    
    @Override
    public int getMaxOutputRate() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public OutputState getOutputState() {
        return outputState;
    }
    
    @Override
    public void setOutputState(OutputState state) {
        this.outputState = state;
    }
    
    /**
     * 尝试将物品输出到相邻的物品处理器
     */
    protected int outputToAdjacentInventory(EnumFacing direction, ItemStack stack) {
        if (world == null || stack.isEmpty()) {
            return 0;
        }
        
        BlockPos targetPos = pos.offset(direction);
        TileEntity targetTE = world.getTileEntity(targetPos);
        
        if (targetTE != null && targetTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
            IItemHandler handler = targetTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler != null) {
                int inserted = 0;
                ItemStack remaining = stack.copy();
                
                for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
                    int beforeInsert = remaining.getCount();
                    remaining = handler.insertItem(i, remaining, false);
                    inserted += (beforeInsert - remaining.getCount());
                }
                
                return inserted;
            }
        }
        
        return 0;
    }
    
    /**
     * 尝试将流体输出到相邻的流体处理器
     */
    protected int outputToAdjacentFluidHandler(EnumFacing direction, FluidStack stack) {
        if (world == null || stack == null || stack.amount <= 0) {
            return 0;
        }
        
        BlockPos targetPos = pos.offset(direction);
        TileEntity targetTE = world.getTileEntity(targetPos);
        
        if (targetTE != null && targetTE.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite())) {
            IFluidHandler handler = targetTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
            if (handler != null) {
                return handler.fill(stack, true);
            }
        }
        
        return 0;
    }

}