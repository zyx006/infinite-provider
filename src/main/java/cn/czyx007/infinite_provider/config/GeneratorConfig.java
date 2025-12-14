package cn.czyx007.infinite_provider.config;

import cn.czyx007.infinite_provider.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 发电机配置类
 * 管理无限供应器发电系统的各种配置选项
 */
@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID + "/generator")
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class GeneratorConfig {
    
    // ============ 基础发电配置 ============
    
    @Config.Comment("基础发电配置")
    public static BasicPower basicPower = new BasicPower();
    
    public static class BasicPower {
        @Config.Comment("1个水供应器的基础发电量 [默认: 256 RF/t]")
        @Config.RangeInt(min = 0)
        public int powerLevel1 = 256;
        
        @Config.Comment("2个水供应器的基础发电量 [默认: 512 RF/t]")
        @Config.RangeInt(min = 0)
        public int powerLevel2 = 512;
        
        @Config.Comment("3个水供应器的基础发电量 [默认: 768 RF/t]")
        @Config.RangeInt(min = 0)
        public int powerLevel3 = 768;
        
        @Config.Comment("4个水供应器的基础发电量 [默认: 1024 RF/t]")
        @Config.RangeInt(min = 0)
        public int powerLevel4 = 1024;
        
        @Config.Comment("5个水供应器的基础发电量 [默认: 2048 RF/t]")
        @Config.RangeInt(min = 0)
        public int powerLevel5 = 2048;
        
        @Config.Comment("6个水供应器的基础发电量 [默认: 4096 RF/t]")
        @Config.RangeInt(min = 0)
        public int powerLevel6 = 4096;
        
        @Config.Comment("7个水供应器的基础发电量 [默认: 8192 RF/t]")
        @Config.RangeInt(min = 0)
        public int powerLevel7 = 8192;
        
        @Config.Comment("8个及更多水供应器的基础发电量 [默认: 16384 RF/t]")
        @Config.RangeInt(min = 0)
        public int powerLevel8 = 16384;
    }
    
    // ============ 岩浆集群加成配置 ============
    
    @Config.Comment("岩浆集群加成配置")
    public static LavaClusterBonus lavaClusterBonus = new LavaClusterBonus();
    
    public static class LavaClusterBonus {
        @Config.Comment("单个岩浆供应器的加成倍数 [默认: 1.0]")
        @Config.RangeDouble(min = 0.1, max = 10.0)
        public double singleBonus = 1.0;
        
        @Config.Comment("十字岩浆集群的加成倍数 [默认: 2.0]")
        @Config.RangeDouble(min = 0.1, max = 10.0)
        public double crossBonus = 2.0;
        
        @Config.Comment("3x3岩浆集群的加成倍数 [默认: 4.0]")
        @Config.RangeDouble(min = 0.1, max = 10.0)
        public double clusterBonus = 4.0;
        
        @Config.Comment("是否启用岩浆集群检测 [默认: true]")
        public boolean enableClusterDetection = true;
    }
    
    // ============ 能量存储配置 ============
    
    @Config.Comment("能量存储配置")
    public static EnergyStorage energyStorage = new EnergyStorage();
    
    public static class EnergyStorage {
        @Config.Comment("水供应器的能量存储容量 [默认: 2.1G RF]")
        @Config.RangeInt(min = 0)
        public int waterProviderCapacity = Integer.MAX_VALUE;
        
        @Config.Comment("水供应器的最大能量输出速率 [默认: 2.1G RF/t]")
        @Config.RangeInt(min = 0)
        public int waterProviderMaxOutput = Integer.MAX_VALUE;
    }
    
    // ============ 配置变更处理 ============
    
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Tags.MOD_ID)) {
            ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
        }
    }
    
    /**
     * 同步配置
     */
    public static void syncConfig() {
        ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
    }
    
    /**
     * 获取基础发电量数组
     * @return 基础发电量数组
     */
    public static int[] getBasicPowerLevels() {
        return new int[] {
            basicPower.powerLevel1,
            basicPower.powerLevel2,
            basicPower.powerLevel3,
            basicPower.powerLevel4,
            basicPower.powerLevel5,
            basicPower.powerLevel6,
            basicPower.powerLevel7,
            basicPower.powerLevel8
        };
    }
    
    /**
     * 获取岩浆集群加成倍数数组
     * @return 岩浆集群加成倍数数组 [单个, 3x3, 3x3x3]
     */
    public static double[] getLavaClusterBonuses() {
        return new double[] {
            lavaClusterBonus.singleBonus,
            lavaClusterBonus.crossBonus,
            lavaClusterBonus.clusterBonus
        };
    }
}