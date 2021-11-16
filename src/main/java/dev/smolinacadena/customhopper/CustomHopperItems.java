package dev.smolinacadena.customhopper;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomHopperItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CustomHopper.ID);

    static {
        registerBlockItemFor(CustomHopperBlocks.CUSTOM_HOPPER);
    }

    private CustomHopperItems() {
    }

    private static <T extends Block> RegistryObject<BlockItem> registerBlockItemFor(RegistryObject<T> block) {
        return ITEMS.register(
                block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties().tab(CustomHopper.MAIN_GROUP).stacksTo(64)));
    }

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
