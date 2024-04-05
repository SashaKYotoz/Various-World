
package net.sashakyotoz.variousworld.client.inventory;

import net.sashakyotoz.variousworld.block.entity.MycolocyfarographBlockEntity;
import net.sashakyotoz.variousworld.init.VariousWorldModBlocks;
import net.sashakyotoz.variousworld.init.VariousWorldModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class MycolocyfarographGUIMenu extends AbstractContainerMenu {
	public final MycolocyfarographBlockEntity blockEntity;
	private final Level level;
	private final ContainerData data;

	public MycolocyfarographGUIMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
	}

	public MycolocyfarographGUIMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
		super(VariousWorldModMenus.MYCOLOCYFAROGRAPH_GUI.get(), id);
		checkContainerSize(inv, 3);
		blockEntity = (MycolocyfarographBlockEntity) entity;
		this.level = inv.player.level();
		this.data = data;

		addPlayerInventory(inv);
		addPlayerHotbar(inv);

		this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
			this.addSlot(new SlotItemHandler(handler, 0, 12, 15));
			this.addSlot(new SlotItemHandler(handler, 1, 86, 15));
			this.addSlot(new SlotItemHandler(handler, 2, 86, 60));
		});

		addDataSlots(data);
	}
	public boolean isCrafting() {
		return data.get(0) > 0;
	}
	public int getScaledProgress() {
		int progress = this.data.get(0);
		int maxProgress = this.data.get(1);  // Max Progress
		int progressArrowSize = 16; // This is the height in pixels of your arrow

		return maxProgress != 0 && progress != 0 ? progress * progressArrowSize / maxProgress : 0;
	}
	private static final int HOTBAR_SLOT_COUNT = 9;
	private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
	private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
	private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
	private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
	private static final int VANILLA_FIRST_SLOT_INDEX = 0;
	private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
	private static final int TE_INVENTORY_SLOT_COUNT = 3;

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot sourceSlot = slots.get(index);
		if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
		ItemStack sourceStack = sourceSlot.getItem();
		ItemStack copyOfSourceStack = sourceStack.copy();
		if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
			if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
					+ TE_INVENTORY_SLOT_COUNT, false)) {
				return ItemStack.EMPTY;
			}
		} else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
			if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
				return ItemStack.EMPTY;
			}
		} else {
			System.out.println("Invalid slotIndex:" + index);
			return ItemStack.EMPTY;
		}
		if (sourceStack.getCount() == 0) {
			sourceSlot.set(ItemStack.EMPTY);
		} else {
			sourceSlot.setChanged();
		}
		sourceSlot.onTake(playerIn, sourceStack);
		return copyOfSourceStack;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
				player, VariousWorldModBlocks.MYCOLOCYFAROGRAPH.get());
	}

	private void addPlayerInventory(Inventory playerInventory) {
		for (int i = 0; i < 3; ++i) {
			for (int l = 0; l < 9; ++l) {
				this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 86 + i * 18));
			}
		}
	}

	private void addPlayerHotbar(Inventory playerInventory) {
		for (int i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 144));
		}
	}
}