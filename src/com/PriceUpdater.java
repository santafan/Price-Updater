package com;

import java.util.ArrayList;

import org.powerbot.core.script.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.tab.Inventory;

import com.ge.GrandExchange;
import com.ge.Slot;
import com.item.Item;
import com.item.ItemContainer;

@Manifest(name = "GE Price Updater", authors = "SantaFan", description = "For use of SantaFan only.")
public class PriceUpdater extends ActiveScript {

	/*
	 * Items that you want to be added to itemID. This is all items you want
	 * searched.
	 */
	ItemContainer ORES = new ItemContainer(new Item(447), new Item(449), new Item(451));
	ItemContainer LOGS = new ItemContainer(new Item(1519), new Item(1517), new Item(1515), new Item(1513));
	ItemContainer PLANKS = new ItemContainer(new Item(960), new Item(8778), new Item(8780), new Item(8782));
	ItemContainer FOOD = new ItemContainer(new Item(383), new Item(385), new Item(7944), new Item(7946), new Item(15270), new Item(15272));
	ItemContainer RUNES = new ItemContainer(new Item(560), new Item(561), new Item(562), new Item(563), new Item(564), new Item(565), new Item(566), new Item(1436), new Item(7936));
	ItemContainer BONES = new ItemContainer(new Item(536), new Item(532), new Item(534), new Item(18830));
	ItemContainer HERBS = new ItemContainer(new Item(249), new Item(253), new Item(257), new Item(259), new Item(261), new Item(263), new Item(265), new Item(267), new Item(269), new Item(2481), new Item(2998));
	ItemContainer MISC = new ItemContainer(new Item(2), new Item(12539), new Item(1745));

	//This contains all the sub-containers.
	private ArrayList<ItemContainer> ITEMS = new ArrayList<ItemContainer>();
	private boolean END;

	@Override
	public void onStart() {
		loadItems();
	}

	@Override
	public int loop() {
		if (END)  {
			this.shutdown();
		}
		if (!GrandExchange.isOpen()) {
			GrandExchange.open();
		} else {
			for (ItemContainer container : this.ITEMS) {
				for (Item i : container.ITEMS) {
					checkItem(i);
				}
				if (container == MISC) {
					END = true;
				}
			}
		}
		return 100;
	}

	private void loadItems() {
		this.ITEMS.add(ORES);
		this.ITEMS.add(LOGS);
		this.ITEMS.add(PLANKS);
		this.ITEMS.add(FOOD);
		this.ITEMS.add(RUNES);
		this.ITEMS.add(BONES);
		this.ITEMS.add(HERBS);
		this.ITEMS.add(MISC);
	}


	public void checkItem(Item item) {
		if (!item.isBought()) {
			if (GrandExchange.isMainWindowOpen()) {
				Slot slot = GrandExchange.getSlot(0);
				if (slot != null) {
					GrandExchange.placeBuyOffer(0, item.getName(),
							item.getStartPrice(), 1);
					sleep(3000);
					if (slot.getStatus().isComplete) {
						GrandExchange.collectSlot(0, item, false, false);
						sleep(600, 800);
					}
					if (Inventory.contains(item.getID())) {
						item.ITEM_BOUGHT = true;
					}
				}
			}
		}
		if (!item.isSold()) {
			if (GrandExchange.isMainWindowOpen()) {
				Slot slot = GrandExchange.getSlot(0);
				if (slot != null) {
					GrandExchange.placeSellOffer(0, 0, 1, -1);
					sleep(3000);
					if (slot.getStatus().isComplete) {
						GrandExchange.collectSlot(0, item, true, false);
						sleep(600, 800);
						item.ITEM_SOLD = true;
					}
				}
			}
		}
		System.out.println("[" + item.getName() + "] buy: " + item.getBuyPrice() + ", sell: " + item.getSellPrice());
	}
}
