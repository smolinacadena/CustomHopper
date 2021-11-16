package dev.smolinacadena.customhopper.setup;

import dev.smolinacadena.customhopper.CustomHopperContainers;
import dev.smolinacadena.customhopper.screen.CustomHopperScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientSetup {
    public ClientSetup() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent e) {
        ScreenManager.register(CustomHopperContainers.CUSTOM_HOPPER.get(), CustomHopperScreen::new);
    }
}
