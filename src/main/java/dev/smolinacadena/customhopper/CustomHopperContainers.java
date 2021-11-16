package dev.smolinacadena.customhopper;

import dev.smolinacadena.customhopper.container.CustomHopperContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomHopperContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, CustomHopper.ID);

    public static final RegistryObject<ContainerType<CustomHopperContainer>> CUSTOM_HOPPER;

    static {
        CUSTOM_HOPPER = CONTAINERS.register("custom_hopper", () -> new ContainerType<>(CustomHopperContainer::new));
    }

    private CustomHopperContainers(){
    }

    public static void register() {
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
