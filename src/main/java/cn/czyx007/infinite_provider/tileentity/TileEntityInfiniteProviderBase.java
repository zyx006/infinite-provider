package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.output.IOutputProvider;
import cn.czyx007.infinite_provider.output.OutputScheduler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
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

    // 双击检测相关
    private static final long DOUBLE_CLICK_INTERVAL = 500; // 500ms内算双击
    private long lastRightClickTime = 0;
    private String lastRightClickPlayerUUID = "";

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
            if (stack.isEmpty()) {
                return stack;
            }

            // 检查是否是同种物品
            ItemStack providedItem = getProvidedItem();
            if (providedItem != null && !providedItem.isEmpty()) {
                if (ItemStack.areItemsEqual(stack, providedItem) &&
                    ItemStack.areItemStackTagsEqual(stack, providedItem)) {
                    // 接受同种物品输入（销毁）
                    return ItemStack.EMPTY; // 返回空表示全部接受
                }
            }

            // 不是同种物品，拒绝输入
            return stack;
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
                            return true; // 允许填充同种流体
                        }

                        @Override
                        public boolean canDrain() {
                            return true;
                        }

                        @Override
                        public boolean canFillFluidType(FluidStack fluidStack) {
                            // 只能填充同种流体
                            return fluidStack != null && fluidStack.getFluid() == fluid;
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
            if (resource == null || resource.amount <= 0) {
                return 0;
            }

            // 检查是否是同种流体
            Fluid providedFluid = getProvidedFluid();
            if (providedFluid != null && resource.getFluid() == providedFluid) {
                // 接受同种流体输入（销毁）
                return resource.amount; // 返回输入量表示全部接受
            }

            // 不是同种流体，拒绝输入
            return 0;
        }

        @Override
        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            Fluid fluid = getProvidedFluid();
            if (fluid != null && resource != null && resource.getFluid() == fluid) {
                return new FluidStack(fluid, resource.amount);
            }
            return null;
        }

        @Override
        @Nullable
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
     * 优化后的逻辑：
     * - 单击右键：放入手持的所有同种物品/流体容器
     * - 双击右键：放入背包中所有同种物品/流体容器
     */
    public void onRightClick(EntityPlayer player) {
        // 检测是否双击（必须在检查手持物品之前！）
        boolean isDoubleClick = false;
        if (!world.isRemote) {
            long currentTime = System.currentTimeMillis();
            String playerUUID = player.getUniqueID().toString();
            
            if (playerUUID.equals(lastRightClickPlayerUUID) && 
                (currentTime - lastRightClickTime) <= DOUBLE_CLICK_INTERVAL) {
                isDoubleClick = true;
            }
            
            lastRightClickTime = currentTime;
            lastRightClickPlayerUUID = playerUUID;
        }

        // 双击模式：即使手持物品为空，也要处理背包中的物品
        if (isDoubleClick) {
            // 获取供应器提供的物品类型
            ItemStack providedItem = getProvidedItem();
            if (providedItem != null && !providedItem.isEmpty()) {
                IItemHandler itemHandler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                if (itemHandler != null) {
                    int totalAccepted = acceptAllMatchingItemsFromInventory(player, providedItem, itemHandler);
                    if (totalAccepted > 0) {
                        player.inventoryContainer.detectAndSendChanges();
                        return;
                    }
                }
            }

            // 流体容器的双击处理
            Fluid providedFluid = getProvidedFluid();
            if (providedFluid != null) {
                int processedCount = acceptAllMatchingFluidContainersFromInventory(player, providedFluid);
                if (processedCount > 0) {
                    player.inventoryContainer.detectAndSendChanges();
                    return;
                }
            }

            // 双击处理完成，直接返回
            return;
        }

        // 单击模式：只处理手持物品
        ItemStack heldItem = player.getHeldItemMainhand();

        if (heldItem.isEmpty()) {
            return;
        }

        // 首先尝试处理同种物品输入（通过物品 Capability）
        if (tryAcceptMatchingItemViaCapability(player, heldItem)) {
            return;
        }

        // 然后尝试处理同种流体容器输入（通过流体 Capability）
        tryAcceptMatchingFluidContainerViaCapability(player, heldItem);
    }

    /**
     * 通过物品 Capability 接受匹配的物品输入（仅单击模式）
     * @return 是否成功处理
     */
    private boolean tryAcceptMatchingItemViaCapability(EntityPlayer player, ItemStack heldItem) {
        ItemStack providedItem = getProvidedItem();
        if (providedItem == null || providedItem.isEmpty()) {
            return false;
        }

        // 检查手持物品是否是同种物品
        if (!ItemStack.areItemsEqual(heldItem, providedItem) ||
            !ItemStack.areItemStackTagsEqual(heldItem, providedItem)) {
            return false;
        }

        IItemHandler itemHandler = getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (itemHandler == null) {
            return false;
        }

        // 单击右键：放入手持的所有物品
        ItemStack toInsert = heldItem.copy();
        ItemStack remaining = itemHandler.insertItem(0, toInsert, false);
        int totalAccepted = toInsert.getCount() - remaining.getCount();
        if (totalAccepted > 0) {
            heldItem.shrink(totalAccepted);
            player.inventoryContainer.detectAndSendChanges();
            return true;
        }

        return false;
    }

    /**
     * 从背包中接受所有匹配的物品
     * @return 接受的物品总数
     */
    private int acceptAllMatchingItemsFromInventory(EntityPlayer player, ItemStack template, 
                                                     IItemHandler itemHandler) {
        int totalAccepted = 0;

        // 遍历整个背包（包括手持）
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack slotStack = player.inventory.getStackInSlot(i);
            
            if (slotStack.isEmpty()) {
                continue;
            }

            // 检查是否是同种物品
            if (ItemStack.areItemsEqual(slotStack, template) &&
                ItemStack.areItemStackTagsEqual(slotStack, template)) {

                // 尝试输入（销毁）
                ItemStack remaining = itemHandler.insertItem(0, slotStack.copy(), false);
                int accepted = slotStack.getCount() - remaining.getCount();
                
                if (accepted > 0) {
                    totalAccepted += accepted;
                    
                    // 更新槽位内容
                    int newCount = slotStack.getCount() - accepted;
                    if (newCount <= 0) {
                        // 如果槽位空了，清空它
                        player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                    } else {
                        // 否则更新数量
                        slotStack.setCount(newCount);
                        player.inventory.setInventorySlotContents(i, slotStack);
                    }
                }
            }
        }

        return totalAccepted;
    }

    /**
     * 通过流体 Capability 接受匹配的流体容器输入（仅单击模式）
     */
    private void tryAcceptMatchingFluidContainerViaCapability(EntityPlayer player, ItemStack heldItem) {
        Fluid providedFluid = getProvidedFluid();
        if (providedFluid == null || !isFluidContainer(heldItem)) {
            return;
        }

        // 单击右键：处理手持的所有容器
        int currentSlot = player.inventory.currentItem;
        int maxProcess = heldItem.getCount();
        int processedCount = processFluidContainers(player, heldItem, currentSlot, maxProcess, providedFluid);

        if (processedCount > 0) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }

    /**
     * 处理指定数量的流体容器
     * @return 实际处理的容器数量
     */
    private int processFluidContainers(EntityPlayer player, ItemStack heldItem, int currentSlot, 
                                       int maxProcess, Fluid providedFluid) {
        int processedCount = 0;

        for (int i = 0; i < maxProcess; i++) {
            if (heldItem.isEmpty()) {
                break;
            }

            // 创建单个容器的副本
            ItemStack singleContainer = heldItem.copy();
            singleContainer.setCount(1);

            // 尝试排空容器（通过流体 Capability）
            ItemStack emptyContainer = drainContainerViaCapability(singleContainer, providedFluid);
            if (emptyContainer == null) {
                break; // 容器不匹配或无法排空，停止处理
            }

            // 消耗原容器
            heldItem.shrink(1);
            processedCount++;

            // 处理空容器的放置
            boolean slotIsEmpty = heldItem.getCount() <= 0;
            if (slotIsEmpty) {
                // 手持槽空了，直接放空容器
                player.inventory.setInventorySlotContents(currentSlot, emptyContainer);
                // 更新heldItem引用，以便继续处理
                heldItem = player.getHeldItemMainhand();
            } else {
                // 手持槽还有容器，尝试添加空容器到背包
                if (!player.inventory.addItemStackToInventory(emptyContainer)) {
                    // 背包满了，丢出空容器
                    if (!player.world.isRemote) {
                        player.dropItem(emptyContainer, false);
                    }
                }
            }
        }

        return processedCount;
    }

    /**
     * 从背包中接受所有匹配的流体容器
     * @return 处理的容器总数
     */
    private int acceptAllMatchingFluidContainersFromInventory(EntityPlayer player, Fluid providedFluid) {
        int totalProcessed = 0;

        // 遍历整个背包
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack slotStack = player.inventory.getStackInSlot(i);

            if (slotStack.isEmpty() || !isFluidContainer(slotStack)) {
                continue;
            }

            // 检查容器中是否包含目标流体
            IFluidHandlerItem fluidHandler = getFluidHandler(slotStack.copy());
            if (fluidHandler == null) {
                continue;
            }

            IFluidTankProperties[] props = fluidHandler.getTankProperties();
            if (props == null || props.length == 0) {
                continue;
            }

            FluidStack contents = props[0].getContents();
            if (contents == null || contents.getFluid() != providedFluid) {
                continue; // 不是目标流体
            }

            // 逐个处理该槽位的所有容器
            int containerCount = slotStack.getCount();
            for (int j = 0; j < containerCount; j++) {
                ItemStack singleContainer = slotStack.copy();
                singleContainer.setCount(1);

                ItemStack emptyContainer = drainContainerViaCapability(singleContainer, providedFluid);
                if (emptyContainer == null) {
                    break;
                }

                // 消耗原容器
                slotStack.shrink(1);
                totalProcessed++;

                // 尝试放入空容器
                if (slotStack.getCount() <= 0) {
                    // 原槽位空了，放入空容器
                    player.inventory.setInventorySlotContents(i, emptyContainer);
                    slotStack = emptyContainer; // 更新引用
                } else {
                    // 尝试添加到背包
                    if (!player.inventory.addItemStackToInventory(emptyContainer)) {
                        // 背包满了，丢出
                        if (!player.world.isRemote) {
                            player.dropItem(emptyContainer, false);
                        }
                    }
                }
            }
        }

        return totalProcessed;
    }

    /**
     * 通过流体 Capability 排空流体容器（如果包含匹配的流体）
     * @param container 要排空的容器
     * @param requiredFluid 需要的流体类型
     * @return 排空后的容器，如果不匹配或失败则返回null
     */
    private ItemStack drainContainerViaCapability(ItemStack container, Fluid requiredFluid) {
        IFluidHandlerItem fluidHandler = getFluidHandler(container);
        if (fluidHandler == null) {
            return null;
        }

        // 检查容器内容
        IFluidTankProperties[] props = fluidHandler.getTankProperties();
        if (props == null || props.length == 0) {
            return null;
        }

        FluidStack contents = props[0].getContents();
        if (contents == null || contents.getFluid() != requiredFluid) {
            return null; // 容器为空或流体类型不匹配
        }

        // 排空容器，然后通过供应器的 Capability 接受（销毁）
        FluidStack drained = fluidHandler.drain(Integer.MAX_VALUE, true);
        if (drained == null || drained.amount <= 0) {
            return null;
        }

        // 通过 Capability 接受流体（会被销毁）
        IFluidHandler providerFluidHandler = getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        if (providerFluidHandler != null) {
            providerFluidHandler.fill(drained, true);
        }

        return fluidHandler.getContainer();
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
        } else if (isShiftClick) {
            // 如果背包满了但是Shift+点击，计算剩余容量并批量放入
            int remainingCapacity = calculateInventoryCapacity(player.inventory, providedItem);
            if (remainingCapacity > 0) {
                ItemStack partialStack = providedItem.copy();
                partialStack.setCount(remainingCapacity);
                if (player.inventory.addItemStackToInventory(partialStack)) {
                    player.inventoryContainer.detectAndSendChanges();
                }
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
     * 获取物品的流体处理器
     * @param stack 要检查的物品
     * @return 流体处理器，如果物品没有流体处理能力则返回null
     */
    @Nullable
    private IFluidHandlerItem getFluidHandler(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        if (!stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            return null;
        }

        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    /**
     * 计算背包对于特定物品的剩余容量
     * @param inventory 玩家背包
     * @param itemTemplate 要放入的物品模板
     * @return 还能放入的物品数量
     */
    private int calculateInventoryCapacity(InventoryPlayer inventory, ItemStack itemTemplate) {
        if (itemTemplate == null || itemTemplate.isEmpty()) {
            return 0;
        }

        int capacity = 0;
        int maxStackSize = itemTemplate.getMaxStackSize();

        // 遍历主背包（不包括盔甲槽和副手槽）
        for (int i = 0; i < inventory.mainInventory.size(); i++) {
            ItemStack slotStack = inventory.mainInventory.get(i);

            if (slotStack.isEmpty()) {
                // 空槽位可以放满整组
                capacity += maxStackSize;
            } else if (ItemStack.areItemsEqual(slotStack, itemTemplate) &&
                       ItemStack.areItemStackTagsEqual(slotStack, itemTemplate)) {
                // 同种物品且NBT相同，可以叠加
                int spaceInSlot = maxStackSize - slotStack.getCount();
                if (spaceInSlot > 0) {
                    capacity += spaceInSlot;
                }
            }
        }

        return capacity;
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
        IFluidHandlerItem fluidHandler = getFluidHandler(container);
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






