package dev.smolinacadena.customhopper.container;

import dev.smolinacadena.customhopper.CustomHopperContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class CustomHopperContainer extends Container {
    private final IInventory hopperInventory;

    public CustomHopperContainer(int windowId, PlayerInventory playerInventory)
    {
        this(windowId, playerInventory, new Inventory(5));
    }

    public CustomHopperContainer(int windowId, PlayerInventory playerInventory, IInventory hopperInventory)
    {
        super(CustomHopperContainers.CUSTOM_HOPPER.get(), windowId);
        this.hopperInventory = hopperInventory;
        checkContainerSize(hopperInventory, 5);
        hopperInventory.startOpen(playerInventory.player);

        for(int i = 0; i < 5; ++i)
        {
            this.addSlot(new Slot(hopperInventory, i, 44 + i * 18, 20));
        }

        for(int i = 0; i < 3; ++i)
        {
            for(int j = 0; j < 9; ++j)
            {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 51));
            }
        }

        for(int i = 0; i < 9; ++i)
        {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 109));
        }

    }

    @Override
    public boolean stillValid(PlayerEntity playerIn)
    {
        return this.hopperInventory.stillValid(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index)
    {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if(slot != null && slot.hasItem())
        {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if(index < this.hopperInventory.getContainerSize())
            {
                if(!this.moveItemStackTo(slotStack, this.hopperInventory.getContainerSize(), this.slots.size(), true))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if(!this.moveItemStackTo(slotStack, 0, this.hopperInventory.getContainerSize(), false))
            {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }
            else
            {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public void removed(PlayerEntity playerIn)
    {
        super.removed(playerIn);
        this.hopperInventory.stopOpen(playerIn);
    }
}
