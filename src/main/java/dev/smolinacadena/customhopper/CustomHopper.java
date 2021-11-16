package dev.smolinacadena.customhopper;

import dev.smolinacadena.customhopper.item.group.MainItemGroup;
import dev.smolinacadena.customhopper.setup.ClientSetup;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(CustomHopper.ID)
public final class CustomHopper {

    public static final String ID = "customhopper";
    public static final ItemGroup MAIN_GROUP = new MainItemGroup(CustomHopper.ID);

    public CustomHopper() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::new);

        CustomHopperBlocks.register();
        CustomHopperItems.register();
        CustomHopperTiles.register();
        CustomHopperContainers.register();
    }
}