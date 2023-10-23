package de.chrisicrafter.loadit;

import com.mojang.logging.LogUtils;
import de.chrisicrafter.loadit.networking.ModMessages;
import de.chrisicrafter.loadit.utils.BeaconData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LoadIt.MOD_ID)
public class LoadIt {
    public static final String MOD_ID = "loadit";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static BeaconData beaconData = null;
    public static boolean sendData = false;

    public LoadIt() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("COMMON SETUP");

        event.enqueueWork(() -> {
            ModMessages.register();

        });


    }
}
