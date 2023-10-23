package de.chrisicrafter.loadit.event;

import de.chrisicrafter.loadit.LoadIt;
import de.chrisicrafter.loadit.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = LoadIt.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeEventBusClientEvents {

    @SubscribeEvent
    public static void onRenderDebugScreen(CustomizeGuiOverlayEvent.DebugText event) {
        if(event.getLeft().isEmpty()) return;
        ArrayList<String> leftTexts = event.getLeft();
        String insertAfter = null;
        for (String string : leftTexts) {
            if(string.contains("Sounds") && string.contains("Mood")) {
                insertAfter = string;
                break;
            }
        }
        String debugString = Utils.getDebugString(Minecraft.getInstance().player.position().x, Minecraft.getInstance().player.position().z);
        if(!debugString.isEmpty()) {
            if(insertAfter != null) {
                leftTexts.add(leftTexts.indexOf(insertAfter) + 1, "");
                leftTexts.add(leftTexts.indexOf(insertAfter) + 2, debugString);
            } else {
                leftTexts.add("");
                leftTexts.add(debugString);
            }
        }
    }
}
