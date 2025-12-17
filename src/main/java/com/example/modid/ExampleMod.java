package com.example.modid;

import cn.czyx007.infinite_provider.api.ProviderRegistry;
import com.example.modid.block.BlockInfiniteProviderDirt;
import com.example.modid.tileentity.TileEntityInfiniteProviderDirt;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:infinite_provider@[1.1.0,)")
public class ExampleMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * <a href="https://cleanroommc.com/wiki/forge-mod-development/event#overview">
     *     Take a look at how many FMLStateEvents you can listen to via the @Mod.EventHandler annotation here
     * </a>
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Hello From {}!", Tags.MOD_NAME);

        // 注册泥土供应器
        ProviderRegistry.registerProvider(new BlockInfiniteProviderDirt());
        // 注册泥土供应器的 TileEntity
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderDirt.class, "infinite_provider_dirt");
    }

}
