package com.example.modid;

import cn.czyx007.infinite_provider.api.ProviderRegistry;
import com.example.modid.block.BlockInfiniteProviderDirtTest;
import com.example.modid.tileentity.TileEntityInfiniteProviderDirtTest;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, dependencies = "required-after:infinite_provider@[1.3.0,)")
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

        // Register Dirt Provider
        ProviderRegistry.registerProvider(new BlockInfiniteProviderDirtTest());
        // Registry the TileEntity for the Infinite Dirt Test Provider
        ProviderRegistry.registerTileEntity(TileEntityInfiniteProviderDirtTest.class, "infinite_provider_dirt_test");
    }

}
