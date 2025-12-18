package cn.czyx007.infinite_provider;

import cn.czyx007.infinite_provider.registry.ModProviders;
import cn.czyx007.infinite_provider.registry.ModRecipes;
import cn.czyx007.infinite_provider.tileentity.ModTileEntities;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class InfiniteProvider {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);
        // 注册内置供应器到注册中心
        ModProviders.init();
        // 注册TileEntity
        ModTileEntities.registerTileEntities();
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 注册合成配方
        ModRecipes.registerRecipes();
        LOGGER.info("{} initialization complete!", Tags.MOD_NAME);
    }
}
