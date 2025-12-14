package cn.czyx007.infinite_provider.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;

/**
 * 能量存储实现类
 * 基于Forge Energy API实现能量存储和传输功能
 */
public class InfiniteProviderEnergyStorage extends EnergyStorage implements INBTSerializable<NBTTagCompound> {
    
    protected int generationRate = 0;
    protected boolean isGenerating = false;
    
    public InfiniteProviderEnergyStorage(int capacity) {
        super(capacity, capacity, capacity, 0);
    }
    
    public InfiniteProviderEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer, maxTransfer, 0);
    }
    
    public InfiniteProviderEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract, 0);
    }
    
    public InfiniteProviderEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }
    
    /**
     * 生成能量
     * @param amount 要生成的能量数量
     * @return 实际生成的能量数量（即使存储满也返回请求的生成量）
     */
    public int generateEnergy(int amount) {
        int energyReceived = Math.min(capacity - energy, Math.min(maxReceive, amount));
        if (energyReceived > 0) {
            energy += energyReceived;
        }
        // 即使能量存储满了，也返回请求的生成量，而不是实际接收的量
        // 这样可以确保即使存储满，也会尝试输出能量
        return amount;
    }
    
    /**
     * 设置能量生成速率
     * @param rate 新的生成速率(FE/t)
     */
    public void setGenerationRate(int rate) {
        this.generationRate = Math.max(0, rate);
    }
    
    /**
     * 获取能量生成速率
     * @return 当前的生成速率(FE/t)
     */
    public int getGenerationRate() {
        return this.generationRate;
    }
    
    /**
     * 设置是否正在生成能量
     * @param generating 是否正在生成能量
     */
    public void setGenerating(boolean generating) {
        this.isGenerating = generating;
    }
    
    /**
     * 检查是否正在生成能量
     * @return 是否正在生成能量
     */
    public boolean isGenerating() {
        return this.isGenerating;
    }
    
    /**
     * 检查是否可以接收能量
     * @return 是否可以接收能量
     */
    public boolean canReceive() {
        return maxReceive > 0;
    }
    
    /**
     * 检查是否可以提取能量
     * @return 是否可以提取能量
     */
    public boolean canExtract() {
        return maxExtract > 0;
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("energy", energy);
        tag.setInteger("generationRate", generationRate);
        tag.setBoolean("isGenerating", isGenerating);
        return tag;
    }
    
    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        energy = nbt.getInteger("energy");
        generationRate = nbt.getInteger("generationRate");
        isGenerating = nbt.getBoolean("isGenerating");
    }
}