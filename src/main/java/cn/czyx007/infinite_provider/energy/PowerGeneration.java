package cn.czyx007.infinite_provider.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderWater;
import cn.czyx007.infinite_provider.tileentity.TileEntityInfiniteProviderLava;
import cn.czyx007.infinite_provider.config.GeneratorConfig;

/**
 * 发电逻辑和功率计算类
 * 处理水供应器和岩浆供应器组合发电的功率计算
 */
public class PowerGeneration {

    // 岩浆集群加成常量
    private static final float SINGLE_BONUS = 1.0f;
    private static final float SIDE_BONUS = 2.0f;
    private static final float CLUSTER_BONUS = 4.0f;

    /**
     * 计算发电功率
     * @param world 世界对象
     * @param topWaterPos 最顶部水供应器位置
     * @return 计算得出的功率(FE/t)
     */
    public static int calculatePowerGeneration(World world, BlockPos topWaterPos) {
        if (world == null || topWaterPos == null) {
            return 0;
        }

        // 计算水供应器数量
        int waterProviderCount = countWaterProviders(world, topWaterPos);
        if (waterProviderCount <= 0) {
            return 0;
        }

        // 找到水链条的最底部位置
        BlockPos bottomWaterPos = findBottomWaterProvider(world, topWaterPos);
        if (bottomWaterPos == null) {
            return 0;
        }

        // 计算岩浆供应器集群加成（应该检查最底部水供应器的下方）
        float lavaBonus = calculateLavaClusterBonus(world, bottomWaterPos);

        // 如果没有岩浆加成，不发电
        if (lavaBonus <= 0) {
            return 0;
        }

        // 功率计算使用的水供应器数量为最多8个
        int effectiveWaterCount = Math.min(waterProviderCount, 8);

        // 计算基础功率（只考虑前8个水供应器）
        int basePower = calculateBasePower(effectiveWaterCount);

        // 最终功率 = 基础功率 × 岩浆集群加成倍数
        return Math.round(basePower * lavaBonus);
    }

    /**
     * 找到水供应器链的最底部
     * @param world 世界对象
     * @param topPos 最顶部水供应器位置
     * @return 最底部水供应器位置，如果没有找到则返回null
     */
    public static BlockPos findBottomWaterProvider(World world, BlockPos topPos) {
        if (world == null || topPos == null) {
            return null;
        }

        BlockPos currentPos = topPos;
        BlockPos bottomPos = topPos;

        // 不将检测数量限制为8个，找到真正的最底部
        for (int i = 0; i < 256; i++) {
            TileEntity te = world.getTileEntity(currentPos);
            if (te instanceof TileEntityInfiniteProviderWater) {
                bottomPos = currentPos;
                currentPos = currentPos.down();
            } else {
                break;
            }
        }

        return bottomPos;
    }

    /**
     * 计算指定位置水供应器的发电功率
     * 这个方法用于非顶部水供应器的功率计算
     * @param world 世界对象
     * @param pos 水供应器位置
     * @return 计算得出的功率(FE/t)
     */
    public static int calculatePowerGenerationForProvider(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return 0;
        }

        // 检查当前位置是否为水供应器
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityInfiniteProviderWater)) {
            return 0;
        }

        // 找到最顶部的水供应器
        BlockPos topPos = findTopWaterProvider(world, pos);
        if (topPos == null) {
            return 0;
        }

        // 检查当前位置是否在有效的水供应器链中
        if (!isInValidWaterChain(world, pos, topPos)) {
            return 0;
        }

        // 只有最顶部的水供应器才能发电
        if (!topPos.equals(pos)) {
            return 0;
        }

        // 使用顶部水供应器的位置计算功率
        return calculatePowerGeneration(world, topPos);
    }

    /**
     * 检查指定位置是否在有效的水供应器链中
     * @param world 世界对象
     * @param checkPos 要检查的位置
     * @param topPos 最顶部水供应器位置
     * @return 是否在有效的水供应器链中
     */
    private static boolean isInValidWaterChain(World world, BlockPos checkPos, BlockPos topPos) {
        if (world == null || checkPos == null || topPos == null) {
            return false;
        }

        // 从顶部开始向下检查，直到找到检查位置或到达底部
        BlockPos currentPos = topPos;

        // 不限制检查数量
        for (int i = 0; i < 256; i++) {
            // 如果当前位置是要检查的位置，返回true
            if (currentPos.equals(checkPos)) {
                return true;
            }

            // 检查下方是否还有水供应器
            BlockPos belowPos = currentPos.down();
            TileEntity belowTE = world.getTileEntity(belowPos);

            if (belowTE instanceof TileEntityInfiniteProviderWater) {
                currentPos = belowPos;
            } else {
                // 下方没有水供应器，检查结束
                break;
            }
        }

        // 如果检查完整个链都没有找到要检查的位置，返回false
        return false;
    }

    /**
     * 找到水供应器链的最顶部
     * @param world 世界对象
     * @param startPos 起始位置
     * @return 最顶部水供应器位置，如果没有找到则返回null
     */
    public static BlockPos findTopWaterProvider(World world, BlockPos startPos) {
        if (world == null || startPos == null) {
            return null;
        }

        BlockPos currentPos = startPos;
        BlockPos topPos = null;

        // 不限制检查数量
        for (int i = 0; i < 256; i++) {
            TileEntity te = world.getTileEntity(currentPos);
            if (te instanceof TileEntityInfiniteProviderWater) {
                topPos = currentPos;
                currentPos = currentPos.up();
            } else {
                break;
            }
        }

        return topPos;
    }

    /**
     * 计算水供应器数量
     * @param world 世界对象
     * @param topWaterPos 最顶部水供应器位置
     * @return 水供应器数量
     */
    public static int countWaterProviders(World world, BlockPos topWaterPos) {
        if (world == null || topWaterPos == null) {
            return 0;
        }

        int count = 0;
        BlockPos currentPos = topWaterPos;

        // 不限制检查数量，统计实际的水供应器数量
        while (count < 256) {
            TileEntity te = world.getTileEntity(currentPos);
            if (te instanceof TileEntityInfiniteProviderWater) {
                count++;
                currentPos = currentPos.down();
            } else {
                break;
            }
        }

        return count;
    }

    /**
     * 计算岩浆供应器集群加成倍数
     * @param world 世界对象
     * @param bottomWaterPos 最底部水供应器位置
     * @return 集群加成倍数
     */
    public static float calculateLavaClusterBonus(World world, BlockPos bottomWaterPos) {
        if (!GeneratorConfig.lavaClusterBonus.enableClusterDetection) {
            return 1.0f;
        }

        if (world == null || bottomWaterPos == null) {
            return 0.0f;
        }

        // 岩浆供应器应该在最底部水供应器下方
        BlockPos lavaCheckPos = bottomWaterPos.down();

        // 检查单个岩浆供应器
        if (isLavaProvider(world, lavaCheckPos)) {
            // 检查是否为3×3集群
            if (isLavaCluster(world, lavaCheckPos, 1)) {
                return CLUSTER_BONUS;
            }
            // 检查是否为侧面4个
            else if (isLavaSideGroup(world, lavaCheckPos)) {
                return SIDE_BONUS;
            }
            // 单个岩浆供应器
            else {
                return SINGLE_BONUS;
            }
        }

        return 0.0f;
    }

    /**
     * 检查指定位置是否为岩浆供应器
     * @param world 世界对象
     * @param pos 检查位置
     * @return 是否为岩浆供应器
     */
    private static boolean isLavaProvider(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }

        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityInfiniteProviderLava;
    }

    /**
     * 检查是否为3×3岩浆供应器集群
     * @param world 世界对象
     * @param centerPos 中心位置
     * @param radius 检查半径
     * @return 是否为3×3集群
     */
    private static boolean isLavaCluster(World world, BlockPos centerPos, int radius) {
        if (world == null || centerPos == null) {
            return false;
        }

        // 检查3×3区域内的所有位置
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = centerPos.add(x, 0, z);
                if (!isLavaProvider(world, checkPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 检查是否为侧面4个岩浆供应器组
     * @param world 世界对象
     * @param centerPos 中心位置
     * @return 是否为侧面4个
     */
    private static boolean isLavaSideGroup(World world, BlockPos centerPos) {
        if (world == null || centerPos == null) {
            return false;
        }

        // 检查四个方向(东、西、南、北)的岩浆供应器
        return isLavaProvider(world, centerPos.east()) &&
                isLavaProvider(world, centerPos.west()) &&
                isLavaProvider(world, centerPos.south()) &&
                isLavaProvider(world, centerPos.north());
    }

    /**
     * 计算基础功率
     * @param waterProviderCount 水供应器数量（最多生效8个）
     * @return 基础功率(FE/t)
     */
    private static int calculateBasePower(int waterProviderCount) {
        int[] powerLevels = GeneratorConfig.getBasicPowerLevels();

        if (waterProviderCount >= 1 && waterProviderCount <= 8) {
            return powerLevels[waterProviderCount - 1];
        }

        // 即使超过8个，也返回8个的功率
        if (waterProviderCount > 8) {
            return powerLevels[7]; // 返回第8级的功率
        }

        return 0;
    }

    /**
     * 检查水供应器是否为最顶部
     * @param world 世界对象
     * @param pos 检查位置
     * @return 是否为最顶部水供应器
     */
    public static boolean isTopWaterProvider(World world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }

        // 检查当前位置是否为水供应器
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityInfiniteProviderWater)) {
            return false;
        }

        // 检查上方是否还有水供应器
        BlockPos abovePos = pos.up();
        TileEntity aboveTE = world.getTileEntity(abovePos);

        // 如果上方没有水供应器，则当前为最顶部
        return !(aboveTE instanceof TileEntityInfiniteProviderWater);
    }

    /**
     * 计算能量分配
     * @param totalEnergy 总能量
     * @param outputFaces 输出面数量
     * @return 每个面的能量分配
     */
    public static int[] distributeEnergy(int totalEnergy, int outputFaces) {
        if (outputFaces <= 0) {
            return new int[0];
        }

        int[] distribution = new int[outputFaces];
        int baseEnergy = totalEnergy / outputFaces;
        int remainder = totalEnergy % outputFaces;

        // 均匀分配能量，余数分配给前几个面
        for (int i = 0; i < outputFaces; i++) {
            distribution[i] = baseEnergy + (i < remainder ? 1 : 0);
        }

        return distribution;
    }
}