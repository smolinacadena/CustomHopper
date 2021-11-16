package dev.smolinacadena.customhopper.item.group;

import dev.smolinacadena.customhopper.CustomHopperBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class MainItemGroup extends ItemGroup {
    public MainItemGroup(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(CustomHopperBlocks.CUSTOM_HOPPER.get());
    }
}
