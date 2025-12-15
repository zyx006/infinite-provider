package cn.czyx007.infinite_provider.item;

import cn.czyx007.infinite_provider.block.BlockInfiniteProviderBase;
import cn.czyx007.infinite_provider.block.BlockInfiniteProviderLava;
import cn.czyx007.infinite_provider.block.BlockInfiniteProviderWater;
import cn.czyx007.infinite_provider.config.GeneratorConfig;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 无限供应器物品块
 * 提供Tooltip显示功能
 */
public class ItemBlockInfiniteProvider extends ItemBlock {

    public ItemBlockInfiniteProvider(Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        Block block = this.getBlock();
        if (!(block instanceof BlockInfiniteProviderBase)) {
            return;
        }

        BlockInfiniteProviderBase provider = (BlockInfiniteProviderBase) block;
        String typeName = provider.getTypeName();

        // 添加基础提示信息
        String tooltipKey = "infinite_provider.tooltip." + getSimpleTypeName(typeName);
        if (I18n.hasKey(tooltipKey)) {
            tooltip.add(I18n.format(tooltipKey));
        }

        // 对于水和岩浆供应器，如果按住Shift显示发电系统详细信息
        if (block instanceof BlockInfiniteProviderWater || block instanceof BlockInfiniteProviderLava) {
            if (flag.isAdvanced() || isShiftKeyDown()) {
                addGeneratorTooltips(tooltip, block);
            } else {
                // 提示按住Shift查看更多信息
                tooltip.add(I18n.format("infinite_provider.tooltip.shift_for_more"));
            }
        }
    }

    /**
     * 添加发电系统相关的Tooltip
     */
    @SideOnly(Side.CLIENT)
    private void addGeneratorTooltips(List<String> tooltip, Block block) {
        if (block instanceof BlockInfiniteProviderWater) {
            addWaterGeneratorTooltips(tooltip);
        } else if (block instanceof BlockInfiniteProviderLava) {
            addLavaGeneratorTooltips(tooltip);
        }
    }

    /**
     * 添加水供应器发电系统Tooltip
     */
    @SideOnly(Side.CLIENT)
    private void addWaterGeneratorTooltips(List<String> tooltip) {
        tooltip.add("");
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.title"));

        // 基础发电量信息
        int[] powerLevels = GeneratorConfig.getBasicPowerLevels();
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.level1", formatPower(powerLevels[0])));
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.level2", formatPower(powerLevels[1])));
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.level3", formatPower(powerLevels[2])));
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.level4", formatPower(powerLevels[3])));
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.level5", formatPower(powerLevels[4])));
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.level6", formatPower(powerLevels[5])));
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.level7", formatPower(powerLevels[6])));
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.level8", formatPower(powerLevels[7])));

        tooltip.add("");
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.structure"));

        // 能量存储信息
        tooltip.add("");
        int capacity = GeneratorConfig.energyStorage.waterProviderCapacity;
        int maxOutput = GeneratorConfig.energyStorage.waterProviderMaxOutput;
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.water.energy",
            formatPower(capacity), formatPower(maxOutput)));
    }

    /**
     * 添加岩浆供应器发电系统Tooltip
     */
    @SideOnly(Side.CLIENT)
    private void addLavaGeneratorTooltips(List<String> tooltip) {
        tooltip.add("");
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.lava.title"));

        // 岩浆集群加成信息
        double[] bonuses = GeneratorConfig.getLavaClusterBonuses();

        // 始终显示单个岩浆供应器加成
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.lava.single", formatMultiplier(bonuses[0])));

        // 仅当启用岩浆集群检测时，显示集群加成
        if (GeneratorConfig.lavaClusterBonus.enableClusterDetection) {
            tooltip.add(I18n.format("infinite_provider.tooltip.generator.lava.cross", formatMultiplier(bonuses[1])));
            tooltip.add(I18n.format("infinite_provider.tooltip.generator.lava.cluster", formatMultiplier(bonuses[2])));
        }

        tooltip.add("");
        tooltip.add(I18n.format("infinite_provider.tooltip.generator.lava.structure"));
    }

    /**
     * 格式化功率值
     */
    private String formatPower(int power) {
        if (power >= 1_000_000_000) {
            // 大于等于1G，显示为G
            double value = power / 1_000_000_000.0;
            return String.format("%.3fG", value);
        } else if (power >= 1_000_000) {
            // 大于等于1M，显示为M
            double value = power / 1_000_000.0;
            return String.format("%.2fM", value);
        } else if (power >= 1_000) {
            // 大于等于1k，显示为k
            double value = power / 1_000.0;
            return String.format("%.1fk", value);
        }
        return String.valueOf(power);
    }

    /**
     * 格式化倍数值
     */
    private String formatMultiplier(double multiplier) {
        if (multiplier == (int) multiplier) {
            return String.format("%.0fx", multiplier);
        }
        return String.format("%.1fx", multiplier);
    }

    /**
     * 从完整类型名称中提取简单名称
     * 例如: "infinite_provider_water" -> "water"
     */
    private String getSimpleTypeName(String fullTypeName) {
        if (fullTypeName.startsWith("infinite_provider_")) {
            return fullTypeName.substring("infinite_provider_".length());
        }
        return fullTypeName;
    }

    /**
     * 检测是否按下Shift键
     */
    @SideOnly(Side.CLIENT)
    private boolean isShiftKeyDown() {
        return net.minecraft.client.gui.GuiScreen.isShiftKeyDown();
    }
}

