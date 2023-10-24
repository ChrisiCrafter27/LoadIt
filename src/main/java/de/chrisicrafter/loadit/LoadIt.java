package de.chrisicrafter.loadit;

import com.mojang.logging.LogUtils;
import de.chrisicrafter.loadit.networking.ModMessages;
import de.chrisicrafter.loadit.utils.BeaconData;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LoadIt.MOD_ID)
public class LoadIt {
    public static final String MOD_ID = "loadit";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static BeaconData beaconData = null;

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

    public static BeaconData getBeaconData(ServerLevel world) {
        if(beaconData == null) beaconData = world.getDataStorage().computeIfAbsent(BeaconData.factory(), "beacon_data");
        return beaconData;
    }
}
