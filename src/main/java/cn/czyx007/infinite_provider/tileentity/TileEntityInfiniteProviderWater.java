package cn.czyx007.infinite_provider.tileentity;

import cn.czyx007.infinite_provider.Tags;
import cn.czyx007.infinite_provider.energy.IEnergyProvider;
import cn.czyx007.infinite_provider.energy.InfiniteProviderEnergyStorage;
import cn.czyx007.infinite_provider.energy.EnergyCompatibility;
import cn.czyx007.infinite_provider.energy.PowerGeneration;
import cn.czyx007.infinite_provider.config.GeneratorConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nullable;

/**
 * 无限水供应器TileEntity
 * 支持发电系统
 */
public class TileEntityInfiniteProviderWater extends TileEntityInfiniteProviderBase implements IEnergyProvider {

    // 能量存储
    protected InfiniteProviderEnergyStorage energyStorage;

    // 发电相关状态
    protected boolean isTopProvider = false;
    protected boolean isGenerating = false;
    protected int powerGeneration = 0;

    // 计时器
    protected int tickCounter = 0;

    // 构造函数
    public TileEntityInfiniteProviderWater() {
        super();
        // 使用配置中的能量存储参数初始化能量存储
        this.energyStorage = new InfiniteProviderEnergyStorage(
                GeneratorConfig.energyStorage.waterProviderCapacity,
                GeneratorConfig.energyStorage.waterProviderMaxOutput
        );
    }

    @Override
    public ItemStack getProvidedItem() {
        return null;
    }

    @Override
    public Fluid getProvidedFluid() {
        return FluidRegistry.WATER;
    }

    @Override
    public String getProviderTypeName() {
        return Tags.MOD_ID + "_water";
    }

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {
            return; // 只在服务端执行
        }

        tickCounter++;

        // 每秒更新一次发电状态 (20 ticks)
        if (tickCounter % 20 == 0) {
            updatePowerGeneration();
        }

        // 每5tick检查一次上方是否有变化
        if (tickCounter % 5 == 0) {
            BlockPos abovePos = pos.up();
            TileEntity aboveTE = world.getTileEntity(abovePos);
            boolean hasAboveWater = aboveTE instanceof TileEntityInfiniteProviderWater;

            // 如果上方状态与预期不符，需要更新
            if ((hasAboveWater && isTopProvider) || (!hasAboveWater && !isTopProvider)) {
                updatePowerGeneration();
            }
        }

        // 只有顶部水供应器才能生成能量
        if (isTopProvider && isGenerating) {
            generatePower();
        }

        // 非顶部水供应器向上传递能量到顶部
        if (!isTopProvider && energyStorage.getEnergyStored() > 0) {
            transferEnergyUpward();
        }

        // 只有顶部水供应器才能对外输出能量
        if (isTopProvider && energyStorage.getEnergyStored() > 0) {
            outputEnergyToAllSides();
        }
    }

    /**
     * 更新发电状态和功率
     */
    protected void updatePowerGeneration() {
        // 检查是否为最顶部水供应器
        boolean wasTopProvider = isTopProvider;
        isTopProvider = PowerGeneration.isTopWaterProvider(world, pos);

        // 如果顶层状态发生变化，通知整个水链条更新
        if (wasTopProvider != isTopProvider) {
            notifyWaterChainUpdate();
        }

        // 只有顶部水供应器才计算功率
        if (isTopProvider) {
            powerGeneration = PowerGeneration.calculatePowerGenerationForProvider(world, pos);
            isGenerating = powerGeneration > 0;
        } else {
            // 非顶部水供应器不发电
            powerGeneration = 0;
            isGenerating = false;
        }

        // 更新能量存储的生成速率
        energyStorage.setGenerationRate(powerGeneration);
        energyStorage.setGenerating(isGenerating);
    }

    /**
     * 通知水供应器链更新状态
     */
    private void notifyWaterChainUpdate() {
        if (world == null) {
            return;
        }

        // 通知下方的水供应器更新
        BlockPos currentPos = pos.down();
        while (true) {
            TileEntity te = world.getTileEntity(currentPos);
            if (te instanceof TileEntityInfiniteProviderWater) {
                TileEntityInfiniteProviderWater waterProvider = (TileEntityInfiniteProviderWater) te;
                waterProvider.updatePowerGeneration();
                currentPos = currentPos.down();
            } else {
                break;
            }
        }

        // 通知上方的水供应器更新
        currentPos = pos.up();
        while (true) {
            TileEntity te = world.getTileEntity(currentPos);
            if (te instanceof TileEntityInfiniteProviderWater) {
                TileEntityInfiniteProviderWater waterProvider = (TileEntityInfiniteProviderWater) te;
                waterProvider.updatePowerGeneration();
                currentPos = currentPos.up();
            } else {
                break;
            }
        }
    }

    /**
     * 生成能量
     */
    protected void generatePower() {
        if (isGenerating && powerGeneration > 0) {
            // 生成能量（即使存储满了也会尝试生成）
            energyStorage.generateEnergy(powerGeneration);

            // 尝试输出能量到相邻方块
            outputEnergyToAllSides();
        }
    }

    /**
     * **新增方法：向上传递能量到顶部水供应器**
     */
    protected void transferEnergyUpward() {
        if (world == null || isTopProvider) {
            return; // 顶部不需要向上传递
        }

        int storedEnergy = energyStorage.getEnergyStored();
        if (storedEnergy <= 0) {
            return; // 没有能量可传递
        }

        // 查找上方的水供应器
        BlockPos abovePos = pos.up();
        TileEntity aboveTE = world.getTileEntity(abovePos);

        if (aboveTE instanceof TileEntityInfiniteProviderWater) {
            TileEntityInfiniteProviderWater aboveProvider = (TileEntityInfiniteProviderWater) aboveTE;

            // 计算可以传递的能量（考虑上方的接收能力）
            int maxTransfer = Math.min(storedEnergy, GeneratorConfig.energyStorage.waterProviderMaxOutput);
            int spaceAvailable = aboveProvider.energyStorage.getMaxEnergyStored() - aboveProvider.energyStorage.getEnergyStored();
            int transferAmount = Math.min(maxTransfer, spaceAvailable);

            if (transferAmount > 0) {
                // 从当前供应器提取能量
                int extracted = energyStorage.extractEnergy(transferAmount, false);
                // 传递给上方供应器
                int received = aboveProvider.energyStorage.receiveEnergy(extracted, false);

                // 如果上方接收的少于提取的，放回未传递的部分
                if (received < extracted) {
                    energyStorage.receiveEnergy(extracted - received, false);
                }
            }
        }
    }

    /**
     * 向所有方向输出能量（只有顶部水供应器使用）
     */
    protected void outputEnergyToAllSides() {
        // 计算可输出的面数
        int outputFaces = 0;
        for (EnumFacing facing : EnumFacing.values()) {
            if (EnergyCompatibility.canReceiveEnergy(world, pos, facing)) {
                outputFaces++;
            }
        }

        if (outputFaces <= 0) {
            return; // 没有可输出的面
        }

        // 获取当前存储的能量
        int totalEnergy = energyStorage.getEnergyStored();
        if (totalEnergy <= 0) {
            return; // 没有能量可输出
        }

        // 计算可输出能量，受发电功率限制而不是maxExtract
        // 每tick最多输出等于发电功率的能量（允许快速清空存储）
        int maxOutputPerTick = Math.max(powerGeneration, GeneratorConfig.energyStorage.waterProviderMaxOutput);
        int energyToDistribute = Math.min(totalEnergy, maxOutputPerTick);

        // 计算每个面的能量分配
        int[] distribution = PowerGeneration.distributeEnergy(energyToDistribute, outputFaces);

        // 向各个面输出能量
        int faceIndex = 0;
        for (EnumFacing facing : EnumFacing.values()) {
            if (EnergyCompatibility.canReceiveEnergy(world, pos, facing)) {
                if (faceIndex < distribution.length) {
                    int energyToOutput = distribution[faceIndex];
                    // 从我们的能量存储中提取能量（现在不受maxExtract限制）
                    int extracted = energyStorage.extractEnergy(energyToOutput, false);
                    if (extracted > 0) {
                        // 尝试输出到相邻方块
                        int actualOutput = EnergyCompatibility.outputEnergy(world, pos, facing, extracted, false);
                        // 如果实际输出少于提取的，将未输出的能量放回
                        if (actualOutput < extracted) {
                            energyStorage.receiveEnergy(extracted - actualOutput, false);
                        }
                    }
                    faceIndex++;
                }
            }
        }
    }

    @Override
    public boolean hasCapability(@javax.annotation.Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        // 只有顶部水供应器才对外暴露能量能力
        if (capability == CapabilityEnergy.ENERGY) {
            return isTopProvider;
        }

        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@javax.annotation.Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        // 只有顶部水供应器才返回能量存储（防止外界提取）
        if (capability == CapabilityEnergy.ENERGY) {
            if (isTopProvider) {
                return (T) energyStorage;
            }
            return null;
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(@javax.annotation.Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);

        // 读取能量存储（所有水供应器都保存，以便传递）
        if (compound.hasKey("energyStorage")) {
            energyStorage.deserializeNBT(compound.getCompoundTag("energyStorage"));
        }

        // 读取发电状态
        isTopProvider = compound.getBoolean("isTopProvider");
        isGenerating = compound.getBoolean("isGenerating");
        powerGeneration = compound.getInteger("powerGeneration");
    }

    @Override
    @javax.annotation.Nonnull
    public NBTTagCompound writeToNBT(@javax.annotation.Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);

        // 保存能量存储（所有水供应器都保存，以便传递）
        compound.setTag("energyStorage", energyStorage.serializeNBT());

        // 保存发电状态
        compound.setBoolean("isTopProvider", isTopProvider);
        compound.setBoolean("isGenerating", isGenerating);
        compound.setInteger("powerGeneration", powerGeneration);

        return compound;
    }

    /**
     * 获取当前能量存储量
     * @return 当前存储的能量
     */
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    /**
     * 获取最大能量容量
     * @return 最大能量容量
     */
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    // ============ IEnergyProvider 接口实现 ============

    @Override
    public boolean canOutputEnergy(EnumFacing direction) {
        // 只有顶部水供应器才能对外输出能量
        return isTopProvider && energyStorage != null && energyStorage.canExtract();
    }

    @Override
    public int outputEnergy(EnumFacing direction, int amount, boolean simulate) {
        // 只有顶部水供应器才能对外输出能量
        if (!isTopProvider || energyStorage == null) {
            return 0;
        }
        return energyStorage.extractEnergy(amount, simulate);
    }

    @Override
    public int getMaxEnergyOutput() {
        // 只有顶部水供应器才有对外输出速率
        return isTopProvider && energyStorage != null ? GeneratorConfig.energyStorage.waterProviderMaxOutput : 0;
    }

    @Override
    public int getEnergyGenerationRate() {
        return energyStorage != null ? energyStorage.getGenerationRate() : 0;
    }

    @Override
    public void setEnergyGenerationRate(int rate) {
        if (energyStorage != null) {
            energyStorage.setGenerationRate(rate);
        }
    }

    @Override
    public boolean isGeneratingEnergy() {
        return energyStorage != null && energyStorage.isGenerating();
    }

    @Override
    public void setGeneratingEnergy(boolean generating) {
        if (energyStorage != null) {
            energyStorage.setGenerating(generating);
        }
    }
}