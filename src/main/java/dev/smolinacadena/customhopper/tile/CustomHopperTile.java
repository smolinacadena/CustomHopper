package dev.smolinacadena.customhopper.tile;

import dev.smolinacadena.customhopper.CustomHopperTiles;
import dev.smolinacadena.customhopper.block.CustomHopperBlock;
import dev.smolinacadena.customhopper.container.CustomHopperContainer;
import dev.smolinacadena.customhopper.item.CustomHopperInventoryCodeHooks;
import dev.smolinacadena.customhopper.item.CustomHopperItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ISidedInventoryProvider;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CustomHopperTile extends LockableLootTileEntity implements IHopper, ITickableTileEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int cooldownTime = -1;
    private long tickedGameTime;

    public CustomHopperTile() {
        super(CustomHopperTiles.CUSTOM_HOPPER.get());
    }

    public void load(BlockState blockState, CompoundNBT compoundNBT) {
        super.load(blockState, compoundNBT);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundNBT)) {
            ItemStackHelper.loadAllItems(compoundNBT, this.items);
        }

        this.cooldownTime = compoundNBT.getInt("TransferCooldown");
    }

    public CompoundNBT save(CompoundNBT compoundNBT) {
        super.save(compoundNBT);
        if (!this.trySaveLootTable(compoundNBT)) {
            ItemStackHelper.saveAllItems(compoundNBT, this.items);
        }

        compoundNBT.putInt("TransferCooldown", this.cooldownTime);
        return compoundNBT;
    }

    public int getContainerSize() {
        return this.items.size();
    }

    public ItemStack removeItem(int index, int count) {
        this.unpackLootTable((PlayerEntity) null);
        return ItemStackHelper.removeItem(this.getItems(), index, count);
    }

    public void setItem(int index, ItemStack itemStack) {
        this.unpackLootTable((PlayerEntity) null);
        this.getItems().set(index, itemStack);
        if (itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }

    }

    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("container.customhopper.custom_hopper");
    }

    public void tick() {
        if (this.level != null && !this.level.isClientSide) {
            --this.cooldownTime;
            this.tickedGameTime = this.level.getGameTime();
            if (!this.isOnCooldown()) {
                this.setCooldown(0);
                this.tryMoveItems(() -> {
                    return suckInItems(this);
                });
            }

        }
    }

    private boolean tryMoveItems(Supplier<Boolean> supplier) {
        if (this.level != null && !this.level.isClientSide) {
            if (!this.isOnCooldown() && this.getBlockState().getValue(CustomHopperBlock.ENABLED)) {
                boolean flag = false;
                if (!this.isEmpty()) {
                    flag = this.ejectItems();
                }

                if (!this.inventoryFull()) {
                    flag |= supplier.get();
                }

                if (flag) {
                    this.setCooldown(8);
                    this.setChanged();
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private boolean inventoryFull() {
        for (ItemStack itemstack : this.items) {
            if (itemstack.isEmpty() || itemstack.getCount() != this.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    private boolean ejectItems() {
        if (CustomHopperInventoryCodeHooks.insertHook(this)) return true;
        IInventory attachedContainer = this.getAttachedContainer();
        if (attachedContainer == null) {
            return false;
        } else {
             Direction direction = this.getBlockState().getValue(CustomHopperBlock.FACING).getOpposite();
            if (this.isFullContainer(attachedContainer, direction)) {
                return false;
            } else {
                for (int i = 0; i < this.getContainerSize(); ++i) {
                    if(attachedContainer instanceof CustomHopperTile){
                        List<Item> itemsInAttachedContainer = ((CustomHopperTile) attachedContainer).getItems().stream().filter((itemStack -> !itemStack.isEmpty())).map(ItemStack::getItem).collect(Collectors.toList());
                        if (!this.getItem(i).isEmpty() && !itemsInAttachedContainer.contains(this.getItem(i).getItem())) {
                            ItemStack copy = this.getItem(i).copy();
                            ItemStack result = addItem(this, attachedContainer, this.removeItem(i, 1), direction);
                            if (result.isEmpty()) {
                                attachedContainer.setChanged();
                                return true;
                            }

                            this.setItem(i, copy);
                        }
                    }
                    else {
                        if (!this.getItem(i).isEmpty()) {
                            ItemStack copy = this.getItem(i).copy();
                            ItemStack result = addItem(this, attachedContainer, this.removeItem(i, 1), direction);
                            if (result.isEmpty()) {
                                attachedContainer.setChanged();
                                return true;
                            }

                            this.setItem(i, copy);
                        }
                    }
                }

                return false;
            }
        }
    }

    private static IntStream getSlots(IInventory inventory, Direction direction) {
        return inventory instanceof ISidedInventory ? IntStream.of(((ISidedInventory) inventory).getSlotsForFace(direction)) : IntStream.range(0, inventory.getContainerSize());
    }

    private boolean isFullContainer(IInventory inventory, Direction direction) {
        return getSlots(inventory, direction).allMatch((index) -> {
            ItemStack itemstack = inventory.getItem(index);
            return itemstack.getCount() >= getMaxStackSize();//itemstack.getMaxStackSize();
        });
    }

    private static boolean isEmptyContainer(IInventory inventory, Direction direction) {
        return getSlots(inventory, direction).allMatch((index) -> {
            return inventory.getItem(index).isEmpty();
        });
    }

    public static boolean suckInItems(IHopper hopper) {
        Boolean ret = CustomHopperInventoryCodeHooks.extractHook(hopper);
        if (ret != null) return ret;
        IInventory inventory = getSourceContainer(hopper);
        if (inventory != null) {
            Direction direction = Direction.DOWN;
            return isEmptyContainer(inventory, direction) ? false : getSlots(inventory, direction).anyMatch((index) -> {
                return tryTakeInItemFromSlot(hopper, inventory, index, direction);
            });
        } else {
            for (ItemEntity itemEntity : getItemsAtAndAbove(hopper)) {
                if (addItem(hopper, itemEntity)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean tryTakeInItemFromSlot(IHopper hopper, IInventory inventory, int index, Direction direction) {
        ItemStack itemstack = inventory.getItem(index);
        if (!itemstack.isEmpty() && canTakeItemFromContainer(inventory, itemstack, index, direction)) {
            ItemStack copy = itemstack.copy();
            ItemStack result = addItem(inventory, hopper, inventory.removeItem(index, 1), (Direction) null);
            if (result.isEmpty()) {
                inventory.setChanged();
                return true;
            }

            inventory.setItem(index, copy);
        }

        return false;
    }

    public static boolean addItem(IInventory inventory, ItemEntity itemEntity) {
        boolean flag = false;
        ItemStack copy = itemEntity.getItem().copy();
        ItemStack result = addItem((IInventory) null, inventory, copy, (Direction) null);
        if (result.isEmpty()) {
            flag = true;
            itemEntity.remove();
        } else {
            itemEntity.setItem(result);
        }

        return flag;
    }

    public static ItemStack addItem(@Nullable IInventory source, IInventory destination, ItemStack itemStack, @Nullable Direction direction) {
        if (destination instanceof ISidedInventory && direction != null) {
            ISidedInventory isidedinventory = (ISidedInventory) destination;
            int[] aint = isidedinventory.getSlotsForFace(direction);

            for (int k = 0; k < aint.length && !itemStack.isEmpty(); ++k) {
                itemStack = tryMoveInItem(source, destination, itemStack, aint[k], direction);
            }
        } else {
            int i = destination.getContainerSize();

            for (int j = 0; j < i && !itemStack.isEmpty(); ++j) {
                itemStack = tryMoveInItem(source, destination, itemStack, j, direction);
            }
        }

        return itemStack;
    }

    private static boolean canPlaceItemInContainer(IInventory inventory, ItemStack itemStack, int index, @Nullable Direction direction) {
        if (!inventory.canPlaceItem(index, itemStack)) {
            return false;
        } else {
            return !(inventory instanceof ISidedInventory) || ((ISidedInventory) inventory).canPlaceItemThroughFace(index, itemStack, direction);
        }
    }

    private static boolean canTakeItemFromContainer(IInventory inventory, ItemStack stack, int index, Direction direction) {
        return !(inventory instanceof ISidedInventory) || ((ISidedInventory) inventory).canTakeItemThroughFace(index, stack, direction);
    }

    private static ItemStack tryMoveInItem(@Nullable IInventory source, IInventory destination, ItemStack itemStack, int index, @Nullable Direction direction) {
        ItemStack itemstack = destination.getItem(index);
        if (canPlaceItemInContainer(destination, itemStack, index, direction)) {
            boolean shouldInsert = false;
            boolean destinationEmpty = destination.isEmpty();
            if (itemstack.isEmpty()) {
                destination.setItem(index, itemStack);
                itemStack = ItemStack.EMPTY;
                shouldInsert = true;
            } else if (canMergeItems(itemstack, itemStack)) {
                int i = 1 - itemstack.getCount(); //itemStack.getMaxStackSize() - itemstack.getCount();
                int j = Math.min(itemStack.getCount(), i);
                itemStack.shrink(j);
                itemstack.grow(j);
                shouldInsert = j > 0;
            }

            if (shouldInsert) {
                if (destinationEmpty && destination instanceof CustomHopperTile) {
                    CustomHopperTile hopper = (CustomHopperTile) destination;
                    if (!hopper.isOnCustomCooldown()) {
                        int cooldownAmount = 0;
                        if (source instanceof CustomHopperTile) {
                            CustomHopperTile customHopperTile = (CustomHopperTile) source;
                            if (hopper.tickedGameTime >= customHopperTile.tickedGameTime) {
                                cooldownAmount = 1;
                            }
                        }

                        hopper.setCooldown(8 - cooldownAmount);
                    }
                }

                destination.setChanged();
            }
        }

        return itemStack;
    }

    @Nullable
    private IInventory getAttachedContainer() {
        Direction direction = this.getBlockState().getValue(HopperBlock.FACING);
        return getContainerAt(this.getLevel(), this.worldPosition.relative(direction));
    }

    @Nullable
    public static IInventory getSourceContainer(IHopper hopper) {
        return getContainerAt(hopper.getLevel(), hopper.getLevelX(), hopper.getLevelY() + 1.0D, hopper.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(IHopper hopper) {
        return hopper.getSuckShape().toAabbs().stream().flatMap((box) -> {
            return hopper.getLevel().getEntitiesOfClass(ItemEntity.class, box.move(hopper.getLevelX() - 0.5D, hopper.getLevelY() - 0.5D, hopper.getLevelZ() - 0.5D), EntityPredicates.ENTITY_STILL_ALIVE).stream();
        }).collect(Collectors.toList());
    }

    @Nullable
    public static IInventory getContainerAt(World world, BlockPos blockPos) {
        return getContainerAt(world, (double) blockPos.getX() + 0.5D, (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D);
    }

    @Nullable
    public static IInventory getContainerAt(World world, double x, double y, double z) {
        IInventory targetInventory = null;
        BlockPos blockpos = new BlockPos(x, y, z);
        BlockState blockstate = world.getBlockState(blockpos);
        Block block = blockstate.getBlock();
        if (block instanceof ISidedInventoryProvider) {
            targetInventory = ((ISidedInventoryProvider) block).getContainer(blockstate, world, blockpos);
        } else if (blockstate.hasTileEntity()) {
            TileEntity tileentity = world.getBlockEntity(blockpos);
            if (tileentity instanceof IInventory) {
                targetInventory = (IInventory) tileentity;
                if (targetInventory instanceof ChestTileEntity && block instanceof ChestBlock) {
                    targetInventory = ChestBlock.getContainer((ChestBlock) block, blockstate, world, blockpos, true);
                }
            }
        }

        if (targetInventory == null) {
            List<Entity> list = world.getEntities((Entity) null, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntityPredicates.CONTAINER_ENTITY_SELECTOR);
            if (!list.isEmpty()) {
                targetInventory = (IInventory) list.get(world.random.nextInt(list.size()));
            }
        }

        return targetInventory;
    }

    private static boolean canMergeItems(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1.getItem() != itemStack2.getItem()) {
            return false;
        } else if (itemStack1.getDamageValue() != itemStack2.getDamageValue()) {
            return false;
        } else if (itemStack1.getCount() > 1) {//itemStack1.getMaxStackSize()) {
            return false;
        } else {
            return ItemStack.tagMatches(itemStack1, itemStack2);
        }
    }

    public double getLevelX() {
        return (double) this.worldPosition.getX() + 0.5D;
    }

    public double getLevelY() {
        return (double) this.worldPosition.getY() + 0.5D;
    }

    public double getLevelZ() {
        return (double) this.worldPosition.getZ() + 0.5D;
    }

    public void setCooldown(int ticks) {
        this.cooldownTime = ticks;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    public boolean isOnCustomCooldown() {
        return this.cooldownTime > 8;
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> itemStacks) {
        this.items = itemStacks;
    }

    public void entityInside(Entity entity) {
        if (entity instanceof ItemEntity) {
            BlockPos blockpos = this.getBlockPos();
            if (VoxelShapes.joinIsNotEmpty(VoxelShapes.create(entity.getBoundingBox().move((double) (-blockpos.getX()), (double) (-blockpos.getY()), (double) (-blockpos.getZ()))), this.getSuckShape(), IBooleanFunction.AND)) {
                this.tryMoveItems(() -> {
                    return addItem(this, (ItemEntity) entity);
                });
            }
        }

    }

    protected Container createMenu(int id, PlayerInventory playerInventory) {
        return new CustomHopperContainer(id, playerInventory, this);
    }

    @Override
    protected net.minecraftforge.items.IItemHandler createUnSidedHandler() {
        return new CustomHopperItemHandler(this);
    }

    public long getLastUpdateTime() {
        return this.tickedGameTime;
    }

    @Override
    public int getMaxStackSize(){
        return 1;
    }
}