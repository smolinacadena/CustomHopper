package dev.smolinacadena.customhopper.item;

import dev.smolinacadena.customhopper.tile.CustomHopperTile;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;

public class CustomHopperItemHandler extends InvWrapper {
    private final CustomHopperTile hopper;

    public CustomHopperItemHandler(CustomHopperTile hopper)
    {
        super(hopper);
        this.hopper = hopper;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        boolean insert = true;
        boolean isEmpty = getInv().isEmpty();
        boolean containsItemStackType = getInv().countItem(stack.getItem()) > 0;

        if (containsItemStackType && !isEmpty) {
            insert = false;
        }

        if (simulate)
        {
            return insert ? super.insertItem(slot, stack, true) : stack;
        }
        else
        {
            int originalStackSize = stack.getCount();
            stack = insert ? super.insertItem(slot, stack, false) : stack;

            if (isEmpty && originalStackSize > stack.getCount())
            {
                if (!hopper.isOnCustomCooldown())
                {
                    // This cooldown is always set to 8 in vanilla with one exception:
                    // Hopper -> Hopper transfer sets this cooldown to 7 when this hopper
                    // has not been updated as recently as the one pushing items into it.
                    // This vanilla behavior is preserved by VanillaInventoryCodeHooks#insertStack,
                    // the cooldown is set properly by the hopper that is pushing items into this one.
                    hopper.setCooldown(8);
                }
            }

            return stack;
        }
    }
}
