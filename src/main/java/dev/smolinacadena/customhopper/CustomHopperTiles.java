package dev.smolinacadena.customhopper;

import dev.smolinacadena.customhopper.tile.CustomHopperTile;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomHopperTiles {

    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, CustomHopper.ID);

    public static final RegistryObject<TileEntityType<CustomHopperTile>> CUSTOM_HOPPER;

    static {
        CUSTOM_HOPPER = TILES.register("custom_hopper", () -> TileEntityType.Builder.of(CustomHopperTile::new, new Block[]{CustomHopperBlocks.CUSTOM_HOPPER.get()}).build(null));
    }

    public static void register() {
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
