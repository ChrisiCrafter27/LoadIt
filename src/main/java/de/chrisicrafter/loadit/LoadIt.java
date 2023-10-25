package de.chrisicrafter.loadit;

import com.mojang.logging.LogUtils;
import de.chrisicrafter.loadit.networking.ModMessages;
import de.chrisicrafter.loadit.data.BeaconData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.HashMap;

@Mod(LoadIt.MOD_ID)
public class LoadIt {
    public static final String MOD_ID = "loadit";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static HashMap<ResourceKey<Level>, BeaconData> BEACON_DATA = new HashMap<>();

    public LoadIt() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("COMMON SETUP");

        event.enqueueWork(ModMessages::register);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        BEACON_DATA = new HashMap<>();
    }

    public static BeaconData getBeaconData(ServerLevel level) {
        if(!BEACON_DATA.containsKey(level.dimension())) BEACON_DATA.put(level.dimension(), level.getDataStorage().computeIfAbsent(BeaconData.factory(), "beacon_data"));
        return BEACON_DATA.get(level.dimension());
    }
}
