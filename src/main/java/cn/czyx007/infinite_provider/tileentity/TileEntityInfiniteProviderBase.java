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
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

/**
 * 无限供应器TileEntity基类
 * 提供基础功能，子类可以扩展特定类型的供应器
 */
public abstract class TileEntityInfiniteProviderBase extends TileEntity implements ITickable, IOutputProvider {

    // 输出系统相关
    protected OutputScheduler outputScheduler;
    protected OutputState outputState = OutputState.ACTIVE;
    
    protected ItemStackHandler itemInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            TileEntityInfiniteProviderBase.this.markDirty();
        }
    };

    protected FluidTank fluidInventory = new FluidTank(Integer.MAX_VALUE) {
        @Override
        public boolean canFill() {
            return false; // 无限供应器不接受流体输入
        }
        
        @Override
        public boolean canDrain() {
            return true;
        }
        
        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0; // 无限供应器不接受流体输入
        }
        
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource != null && resource.getFluid() == getProvidedFluid()) {
                return new FluidStack(getProvidedFluid(), resource.amount);
            }
            return null;
        }
        
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return new FluidStack(getProvidedFluid(), Math.min(maxDrain, Integer.MAX_VALUE));
        }
    };

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
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
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
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        // 物品供应器才暴露物品处理能力
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && 
            getProvidedItem() != null && !getProvidedItem().isEmpty()) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemInventory);
        }
        // 流体供应器才暴露流体处理能力，所有方向都支持
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && getProvidedFluid() != null) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidInventory);
        }
        return super.getCapability(capability, facing);
    }

    /**
     * 处理玩家左键交互
     */
    public boolean onLeftClick(EntityPlayer player, boolean isShiftClick) {
        ItemStack heldItem = player.getHeldItemMainhand();
        
        // 首先尝试处理流体取出（需要容器）
        if (getProvidedFluid() != null && !heldItem.isEmpty() && isFluidContainer(heldItem)) {
            return extractFluid(player, isShiftClick);
        }

        // 然后尝试处理物品取出（可以空手）
        ItemStack providedItem = getProvidedItem();
        if (providedItem != null && !providedItem.isEmpty()) {
            return extractItem(player, providedItem, isShiftClick);
        }

        return false;
    }

    /**
     * 取出物品
     */
    private boolean extractItem(EntityPlayer player, ItemStack providedItem, boolean isShiftClick) {
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
            return true;
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
                return true;
            }

            // 背包完全满了，无法放入任何物品
            return false;
        }
    }

    /**
     * 取出流体
     */
    private boolean extractFluid(EntityPlayer player, boolean isShiftClick) {
        ItemStack heldItem = player.getHeldItemMainhand();
        if (heldItem.isEmpty()) {
            return false;
        }

        // 检查是否是流体容器
        if (isFluidContainer(heldItem)) {
            // 获取流体容器能力
            IFluidHandlerItem fluidHandler = heldItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null) {
                // 获取容器属性
                IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
                if (tankProperties != null && tankProperties.length > 0) {
                    // 计算容器还能接受多少流体
                    int totalCapacity = 0;
                    int currentAmount = 0;
                    
                    for (IFluidTankProperties prop : tankProperties) {
                        if (prop != null) {
                            totalCapacity += prop.getCapacity();
                            FluidStack contents = prop.getContents();
                            if (contents != null) {
                                currentAmount += contents.amount;
                            }
                        }
                    }
                    
                    int availableSpace = totalCapacity - currentAmount;
                    
                    // 如果容器已满，无法继续填充
                    if (availableSpace <= 0) {
                        return false;
                    }
                    
                    // 计算填充量 - 填充容器的可用空间
                    int fillAmount = availableSpace;
                    
                    // 先模拟填充，检查是否可以填充
                    int simulatedFilled = fluidHandler.fill(new FluidStack(getProvidedFluid(), fillAmount), false);
                    if (simulatedFilled > 0) {
                        // 创建一个新的流体处理器实例，避免直接修改原物品
                        ItemStack containerCopy = heldItem.copy();
                        IFluidHandlerItem copyHandler = containerCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                        
                        if (copyHandler != null) {
                            // 确认可以填充，执行实际填充
                            int actualFilled = copyHandler.fill(new FluidStack(getProvidedFluid(), simulatedFilled), true);
                            if (actualFilled > 0) {
                                // 获取填充后的容器
                                ItemStack filledContainer = copyHandler.getContainer();
                                
                                // 检查填充后的容器是否有效
                                if (filledContainer != null && !filledContainer.isEmpty()) {
                                    // 只有在成功填充后才消耗玩家手中的容器
                                    heldItem.shrink(1);
                                    if (heldItem.getCount() <= 0) {
                                        player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
                                    }
                                    
                                    // 给玩家填充后的容器
                                    if (!player.inventory.addItemStackToInventory(filledContainer)) {
                                        // 如果背包满了，丢出物品
                                        if (!player.world.isRemote) {
                                            player.dropItem(filledContainer, false);
                                        }
                                    }
                                    
                                    player.inventoryContainer.detectAndSendChanges();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
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

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        // 读取输出状态
        if (compound.hasKey("outputState")) {
            outputState = OutputState.valueOf(compound.getString("outputState"));
        }
        
        // 读取物品库存
        itemInventory.deserializeNBT(compound.getCompoundTag("itemInventory"));
        
        // 读取流体库存
        if (compound.hasKey("fluidInventory")) {
            fluidInventory.readFromNBT(compound.getCompoundTag("fluidInventory"));
        }
        
        // 重新初始化输出调度器
        if (outputScheduler != null) {
            outputScheduler = new OutputScheduler(this, this);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        
        // 保存输出状态
        compound.setString("outputState", outputState.name());
        
        // 保存物品库存
        compound.setTag("itemInventory", itemInventory.serializeNBT());
        
        // 保存流体库存
        compound.setTag("fluidInventory", fluidInventory.writeToNBT(new NBTTagCompound()));
        
        return compound;
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
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
        return Integer.MAX_VALUE; // 2.1G
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