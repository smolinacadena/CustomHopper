package dev.smolinacadena.customhopper;

import dev.smolinacadena.customhopper.block.CustomHopperBlock;
import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomHopperBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CustomHopper.ID);

    public static final RegistryObject<CustomHopperBlock> CUSTOM_HOPPER;

    static {
        CUSTOM_HOPPER = BLOCKS.register("custom_hopper", CustomHopperBlock::new);
    }

    private CustomHopperBlocks() {
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
