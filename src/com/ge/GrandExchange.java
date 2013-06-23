package com.ge;


import java.util.ArrayList;
import java.util.Arrays;

import org.powerbot.core.script.job.Task;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.wrappers.Entity;
import org.powerbot.game.api.wrappers.Locatable;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Identifiable;

public class GrandExchange {
	public static final int[] EXCHANGE_NPC_IDS = new int[] { 1419, 2240, 2241,
			2593 };

	public static final int WIDGET_EXCHANGE = 105;
	public static final int WIDGET_BANKPIN = 13;

	public static final int WIDGET_MONEY_POUCH_PARENT = 746;
	public static final int WIDGET_MONEY_POUCH_TOTAL = 209;
	public static final int WIDGET_MONEY_POUCH_TOGGLE = 211;

	public static final int WIDGET_DIALOGUE_SEARCH = 389;
	public static final int WIDGET_DIALOGUE_SEARCH_INPUT = 9;
	public static final int WIDGET_DIALOGUE_SEARCH_RESULTS = 4;
	public static final int WIDGET_DIALOGUE_CUSTOM_QUANTITY_PRICE = 752;

	public static final int WIDGET_MAIN_WINDOW_IDENTIFIER = 18;

	public static final int WIDGET_BUY_0 = 30;
	public static final int WIDGET_BUY_1 = 46;
	public static final int WIDGET_BUY_2 = 62;
	public static final int WIDGET_BUY_3 = 82;
	public static final int WIDGET_BUY_4 = 100;
	public static final int WIDGET_BUY_5 = 119;
	public static final int WIDGET_SELL_0 = 29;
	public static final int WIDGET_SELL_1 = 45;
	public static final int WIDGET_SELL_2 = 61;
	public static final int WIDGET_SELL_3 = 83;
	public static final int WIDGET_SELL_4 = 99;
	public static final int WIDGET_SELL_5 = 118;

	public static final int WIDGET_SLOT_COUNT = 17;
	public static final int WIDGET_SLOT_ITEM = 18;
	public static final int WIDGET_SLOT_GOLD = 19;

	public static final int WIDGET_BUY_SEARCH = 139;
	public static final int WIDGET_BUY_QUANTITY_MINUS_ONE = 155;
	public static final int WIDGET_BUY_QUANTITY_PLUS_1 = 157; // 160 has same
																// effect
	public static final int WIDGET_BUY_QUANTITY_PLUS_10 = 162;
	public static final int WIDGET_BUY_QUANTITY_PLUS_100 = 164;
	public static final int WIDGET_BUY_QUANTITY_PLUS_1000 = 166;
	public static final int WIDGET_BUY_QUANTITY_CUSTOM = 168;
	public static final int WIDGET_BUY_PRICE_MINUS_ONE = 169;
	public static final int WIDGET_BUY_PRICE_PLUS_ONE = 171;
	public static final int WIDGET_BUY_PRICE_MINUS_FIVE_PERCENT = 181;
	public static final int WIDGET_BUY_PRICE_SET_TO_GUIDE = 175;
	public static final int WIDGET_BUY_PRICE_CUSTOM = 177;
	public static final int WIDGET_BUY_PRICE_PLUS_FICE_PERCENT = 179;

	public static final int WIDGET_MARKET_PRICE = 141;
	public static final int WIDGET_BACK = 128;
	public static final int WIDGET_CONFIRM_OFFER = 186;
	public static final int WIDGET_CANCEL_OFFER = 200;
	public static final int WIDGET_COLLECTION_LEFT = 206;
	public static final int WIDGET_COLLECTION_RIGHT = 208;
	public static final int WIDGET_CLOSE_EXCHANGE = 9;

	public static final boolean PRINT_DEBUG = true;

	public static final Slot ExchangeSlots[] = { new Slot(0, 19),
			new Slot(1, 35), new Slot(2, 51), new Slot(3, 70), new Slot(4, 89),
			new Slot(5, 108) };

	public static void DEBUG_OUT(String str) {
		if (PRINT_DEBUG) {
			System.out.println("geAPI[debug]: " + str);
		}
	}

	private static final Filter<Identifiable> ALL_FILTER = new Filter<Identifiable>() {
		@Override
		public boolean accept(final Identifiable exchange) {
			if (!isExchangeClerk(exchange))
				return false;

			return true;
		}
	};

	public static Widget getWidget() {
		return Widgets.get(WIDGET_EXCHANGE);
	}

	/**
	 * Reports whether the Grand Exchange window is open or not.
	 * 
	 * @return boolean TRUE if open, FALSE if not
	 */
	public static boolean isOpen() {
		final Widget exchange = getWidget();
		return exchange != null && exchange.validate();
	}

	/**
	 * Reports whether the Grand Exchange main window is open. The main window
	 * is the one that lists all 6 slots, and their status. The method isOpen
	 * simply reports if any Grand Exchange window is open (main, buy, sell,
	 * etc.).
	 * 
	 * @return
	 */
	public static boolean isMainWindowOpen() {
		WidgetChild widget = Widgets.get(WIDGET_EXCHANGE,
				WIDGET_MAIN_WINDOW_IDENTIFIER);

		if (widget == null || !widget.validate() || !widget.visible())
			return false;

		return true;
	}

	/**
	 * Locates the nearest Grand Exchange clerk and opens the Grand Exchange
	 * window. If the Grand Exchange is already open, but not on the main
	 * window, it goes to the main window.
	 * 
	 * @return TRUE if window is opened, FALSE if failed
	 */
	public static boolean open() {
		if (isMainWindowOpen()) {
			return true;
		}

		if (isOpen()) {
			// GrandExchange is open, but not the main window
			if (!goToMainWindow()) {
				DEBUG_OUT("failed to go to main window");
				return false;
			}

			return true;
		}

		final Entity exchange = getNearest();

		if (exchange == null) {
			return false;
		}

		if (!exchange.isOnScreen()
				&& (!Players.getLocal().isMoving() || Calculations.distance(
						Walking.getDestination(),
						((Locatable) exchange).getLocation()) > 4)) {
			Walking.walk((Locatable) exchange);
			Task.sleep(200, 400);
		}

		if (exchange.isOnScreen()) {
			boolean interacted = exchange.interact("Exchange");

			final Widget bankpin = Widgets.get(WIDGET_BANKPIN);
			final Timer t = new Timer(4000);

			while (t.isRunning() && interacted && !isOpen()
					&& (bankpin == null || !bankpin.validate())) {
				Task.sleep(10);
			}
		}

		return isOpen();
	}

	/**
	 * Closes the Grand Exchange window if it is open.
	 * 
	 * @return
	 */
	public static boolean close() {
		if (!isOpen()) {
			return true;
		}

		final WidgetChild closeButton = Widgets.get(WIDGET_EXCHANGE,
				WIDGET_CLOSE_EXCHANGE);
		return closeButton != null && closeButton.interact("Close");
	}

	/**
	 * Returns the nearest Grand Exchange clerk.
	 * 
	 * @return null if none found
	 */
	public static Entity getNearest() {
		final Locatable[] exchanges = getLoadedExchanges();
		Locatable nearest = null;

		for (final Locatable exchange : exchanges) {
			if (ALL_FILTER.accept((Identifiable) exchange)
					&& ((Entity) exchange).validate()) {
				if ((nearest == null || Calculations.distanceTo(exchange) < Calculations
						.distanceTo(nearest))) {
					nearest = exchange;
				}
			}
		}

		return (Entity) nearest;
	}

	/**
	 * Returns all loaded Grand Exchange clerks.
	 * 
	 * @return
	 */
	private static Locatable[] getLoadedExchanges() {
		final ArrayList<Locatable> exchanges = new ArrayList<Locatable>();
		final NPC[] loadedNPCs = NPCs.getLoaded(EXCHANGE_NPC_IDS);

		for (final NPC npc : loadedNPCs) {
			if (npc.validate() && ALL_FILTER.accept(npc)) {
				exchanges.add(npc);
			}
		}

		return exchanges.toArray(new Locatable[exchanges.size()]);
	}

	/**
	 * Places an order to buy the specified item, if available.
	 * 
	 * @param slot
	 *            Which slot to buy in [0-5]. Pass -1 for first free slot.
	 * @param name
	 *            Name of the item to buy
	 * @param price
	 *            Price to buy the item at. Pass -1 to leave at default.
	 * @param quanity
	 *            Amount of the item to buy.
	 * @return FALSE if failed for any reason (window not open, no empty slots,
	 *         etc.)
	 */
	public static boolean placeBuyOffer(int slot, String name, int price,
			int quantity) {
		if (!isOpen()) {
			DEBUG_OUT("grand exchange is not open to place buy order");
			return false;
		}

		if (!isMainWindowOpen()) {
			if (!goToMainWindow()) {
				DEBUG_OUT("failed to return to main window to place buy order");
				return false;
			}
		}

		if (price < 1 && price != -1)
			price = 1;

		// --------------------------------------------
		// Get the specified slot and click it

		Slot emptySlot = getSlot(slot);

		if (emptySlot == null || !emptySlot.getStatus().isEmpty) {
			DEBUG_OUT("buy - failed to find slot or slot is not empty");
			return false;
		}

		emptySlot.getBuyButton().click(true);

		// --------------------------------------------
		// Enter the search terms

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
				5000);

		if (!isBuySearchUp()) {
			WidgetChild searchButton = Widgets.get(WIDGET_EXCHANGE,
					WIDGET_BUY_SEARCH);

			if (searchButton == null || !searchButton.validate()) {
				DEBUG_OUT("buy - failed to find search button");
				return false;
			}

			searchButton.click(true);

			waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
					5000);

			if (!isBuySearchUp()) {
				DEBUG_OUT("buy - failed to initiate search");
				return false;
			}
		}

		Keyboard.sendText(name, false);

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_RESULTS,
				0, 5000);

		// --------------------------------------------
		// Select the proper item from the list

		WidgetChild searchResults = Widgets.get(WIDGET_DIALOGUE_SEARCH,
				WIDGET_DIALOGUE_SEARCH_RESULTS);

		if (searchResults == null || !searchResults.validate()) {
			DEBUG_OUT("buy - failed to get search results");
			return false;
		}

		WidgetChild individualResults[] = searchResults.getChildren();
		WidgetChild validResult = null;

		for (final WidgetChild result : individualResults) {
			if (result.getText().toUpperCase().compareTo(name.toUpperCase()) == 0) {
				validResult = result;
			}
		}

		if (validResult == null || !validResult.validate()) {
			DEBUG_OUT("buy - failed to get valid result");
			return false;
		}

		validResult.click(true);

		Task.sleep(1000, 1500);

		if (!setCustomBuyQuantity(quantity)) {
			DEBUG_OUT("buy - failed to set custom quantity");
			return false;
		}

		if (!setCustomBuyPrice(price)) {
			DEBUG_OUT("buy - failed to set custom price");
			return false;
		}

		return confirmOffer();
	}

	/**
	 * Performs same function as placeBuyOrder except that the price is based on
	 * the differential parameter. This parameter specifies how much above, or
	 * below (if negative), you wish to price the item compared to the Grand
	 * Exchange recommended price.
	 * 
	 * Example: item defaults to N amount of gold, but to be competitive you
	 * want to always under price it by 10 gold. This would be:
	 * placeBuyOrderDifferential( -1, "item", -10, 100 );
	 * 
	 * @param slot
	 * @param name
	 * @param differential
	 *            Price above or below the recommended
	 * @param quantity
	 * @return
	 */
	public static boolean placeBuyOfferDifferential(int slot, String name,
			int differential, int quantity) {
		if (!isOpen()) {
			DEBUG_OUT("grand exchange is not open to place buy order");
			return false;
		}

		if (!isMainWindowOpen()) {
			if (!goToMainWindow()) {
				DEBUG_OUT("failed to return to main window to place buy order");
				return false;
			}
		}

		// --------------------------------------------
		// Get the specified slot and click it

		Slot emptySlot = getSlot(slot);

		if (emptySlot == null || !emptySlot.getStatus().isEmpty) {
			DEBUG_OUT("buy - failed to find slot or slot is not empty");
			return false;
		}

		emptySlot.getBuyButton().click(true);

		// --------------------------------------------
		// Enter the search terms

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
				5000);

		if (!isBuySearchUp()) {
			WidgetChild searchButton = Widgets.get(WIDGET_EXCHANGE,
					WIDGET_BUY_SEARCH);

			if (searchButton == null || !searchButton.validate()) {
				DEBUG_OUT("buy - failed to find search button");
				return false;
			}

			searchButton.click(true);

			waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
					5000);

			if (!isBuySearchUp()) {
				DEBUG_OUT("buy - failed to initiate search");
				return false;
			}
		}

		Keyboard.sendText(name, false);

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_RESULTS,
				0, 5000);

		// --------------------------------------------
		// Select the proper item from the list

		WidgetChild searchResults = Widgets.get(WIDGET_DIALOGUE_SEARCH,
				WIDGET_DIALOGUE_SEARCH_RESULTS);

		if (searchResults == null || !searchResults.validate()) {
			DEBUG_OUT("buy - failed to get search results");
			return false;
		}

		WidgetChild individualResults[] = searchResults.getChildren();
		WidgetChild validResult = null;

		for (final WidgetChild result : individualResults) {
			if (result.getText().toUpperCase().compareTo(name.toUpperCase()) == 0) {
				validResult = result;
			}
		}

		if (validResult == null || !validResult.validate()) {
			DEBUG_OUT("buy - failed to get valid result");
			return false;
		}

		validResult.click(true);

		Task.sleep(1000, 1500);

		if (!setCustomBuyQuantity(quantity)) {
			DEBUG_OUT("buy - failed to set custom quantity");
			return false;
		}

		// --------------------------------------------
		// Get and set the price

		int price = getMarketPrice();

		if (price == -1) {
			DEBUG_OUT("buy - failed to get market price");
			return false;
		}

		price += differential;

		if (price <= 0)
			price = 1;

		if (!setCustomBuyPrice(price)) {
			DEBUG_OUT("buy - failed to set custom price");
			return false;
		}

		return confirmOffer();
	}

	/**
	 * Orders the maximum amount of the item as can be afforded by the money in
	 * the money pouch.
	 * 
	 * Example: player has 582 gold, and wants to buy maximum number of Nature
	 * Runes for the market price of 108 gold. This will order 5 nature runes
	 * with the following: placeBuyOfferMax( -1, "Nature rune", -1 );
	 * 
	 * @param slot
	 * @param name
	 * @param price
	 * @return
	 */
	public static boolean placeBuyOfferMax(int slot, String name, int price) {
		if (!isOpen()) {
			DEBUG_OUT("buyOfferMax - grand exchange is not open to place buy order");
			return false;
		}

		if (!isMainWindowOpen()) {
			if (!goToMainWindow()) {
				DEBUG_OUT("buyOfferMax - failed to return to main window to place buy order");
				return false;
			}
		}

		if (price < 1 && price != -1)
			price = 1;

		// --------------------------------------------
		// Get the specified slot and click it

		Slot emptySlot = getSlot(slot);

		if (emptySlot == null || !emptySlot.getStatus().isEmpty) {
			DEBUG_OUT("buyOfferMax - failed to find slot or slot is not empty");
			return false;
		}

		emptySlot.getBuyButton().click(true);

		// --------------------------------------------
		// Enter the search terms

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
				5000);

		if (!isBuySearchUp()) {
			WidgetChild searchButton = Widgets.get(WIDGET_EXCHANGE,
					WIDGET_BUY_SEARCH);

			if (searchButton == null || !searchButton.validate()) {
				DEBUG_OUT("buyOfferMax - failed to find search button");
				return false;
			}

			searchButton.click(true);

			waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
					5000);

			if (!isBuySearchUp()) {
				DEBUG_OUT("buyOfferMax - failed to initiate search");
				return false;
			}
		}

		Keyboard.sendText(name, false);

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_RESULTS,
				0, 5000);

		// --------------------------------------------
		// Select the proper item from the list

		WidgetChild searchResults = Widgets.get(WIDGET_DIALOGUE_SEARCH,
				WIDGET_DIALOGUE_SEARCH_RESULTS);

		if (searchResults == null || !searchResults.validate()) {
			DEBUG_OUT("buyOfferMax - failed to get search results");
			return false;
		}

		WidgetChild individualResults[] = searchResults.getChildren();
		WidgetChild validResult = null;

		for (final WidgetChild result : individualResults) {
			if (result.getText().toUpperCase().compareTo(name.toUpperCase()) == 0) {
				validResult = result;
			}
		}

		if (validResult == null || !validResult.validate()) {
			DEBUG_OUT("buyOfferMax - failed to get valid result");
			return false;
		}

		validResult.click(true);

		Task.sleep(1000, 1500);

		// --------------------------------------------
		// Get available cash and set quantity/price

		int cashAvailable = getMoneyPouchTotal();

		if (cashAvailable == -1) {
			DEBUG_OUT("buyOfferMax - failed to get money pouch total");
			return false;
		}

		if (price == -1) {
			price = getMarketPrice();

			if (price == -1) {
				DEBUG_OUT("buyOfferMax - failed to get market value");
				return false;
			}
		}

		int quantity = cashAvailable / price;

		if (!setCustomBuyQuantity(quantity)) {
			DEBUG_OUT("buyOfferMax - failed to set custom quanitity");
			return false;
		}

		if (!setCustomBuyPrice(price)) {
			DEBUG_OUT("buyOfferMax - failed to set custom price");
			return false;
		}

		return confirmOffer();
	}

	/**
	 * Orders the maximum amount of the item as can be afforded by the money in
	 * the money pouch. The price is set as: marketPrice + differential.
	 * 
	 * Example: player has 582 gold, and wants to buy maximum number of Nature
	 * Runes for 100 gold below the market price of 108 gold. This will order 72
	 * nature runes with the following: placeBuyOfferMaxDifferential( -1,
	 * "Nature rune", -100 );
	 * 
	 * @param slot
	 * @param name
	 * @param price
	 * @return
	 */
	public static boolean placeBuyOfferMaxDifferential(int slot, String name,
			int differential) {
		if (!isOpen()) {
			DEBUG_OUT("buyOfferMaxDiff - grand exchange is not open to place buy order");
			return false;
		}

		if (!isMainWindowOpen()) {
			if (!goToMainWindow()) {
				DEBUG_OUT("buyOfferMaxDiff - failed to return to main window to place buy order");
				return false;
			}
		}

		// --------------------------------------------
		// Get the specified slot and click it

		Slot emptySlot = getSlot(slot);

		if (emptySlot == null || !emptySlot.getStatus().isEmpty) {
			DEBUG_OUT("buyOfferMaxDiff - failed to find slot or slot is not empty");
			return false;
		}

		emptySlot.getBuyButton().click(true);

		// --------------------------------------------
		// Enter the search terms

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
				5000);

		if (!isBuySearchUp()) {
			WidgetChild searchButton = Widgets.get(WIDGET_EXCHANGE,
					WIDGET_BUY_SEARCH);

			if (searchButton == null || !searchButton.validate()) {
				DEBUG_OUT("buyOfferMaxDiff - failed to find search button");
				return false;
			}

			searchButton.click(true);

			waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
					5000);

			if (!isBuySearchUp()) {
				DEBUG_OUT("buyOfferMaxDiff - failed to initiate search");
				return false;
			}
		}

		Keyboard.sendText(name, false);

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_RESULTS,
				0, 5000);

		// --------------------------------------------
		// Select the proper item from the list

		WidgetChild searchResults = Widgets.get(WIDGET_DIALOGUE_SEARCH,
				WIDGET_DIALOGUE_SEARCH_RESULTS);

		if (searchResults == null || !searchResults.validate()) {
			DEBUG_OUT("buyOfferMaxDiff - failed to get search results");
			return false;
		}

		WidgetChild individualResults[] = searchResults.getChildren();
		WidgetChild validResult = null;

		for (final WidgetChild result : individualResults) {
			if (result.getText().toUpperCase().compareTo(name.toUpperCase()) == 0) {
				validResult = result;
			}
		}

		if (validResult == null || !validResult.validate()) {
			DEBUG_OUT("buyOfferMaxDiff - failed to get valid result");
			return false;
		}

		validResult.click(true);

		Task.sleep(1000, 1500);

		// --------------------------------------------
		// Get market price and cash available

		int marketPrice = getMarketPrice();

		if (marketPrice == -1) {
			DEBUG_OUT("buyOfferMaxDiff - failed to get market price");
			return false;
		}

		marketPrice += differential;

		if (marketPrice < 1)
			marketPrice = 1;

		int cashAvailable = getMoneyPouchTotal();

		if (cashAvailable == -1) {
			DEBUG_OUT("buyOfferMaxDiff - failed to get money pouch total");
			return false;
		}

		int quantity = cashAvailable / marketPrice;

		if (!setCustomBuyQuantity(quantity)) {
			DEBUG_OUT("buyOfferMaxDiff - failed to set custom quantity");
			return false;
		}

		if (!setCustomBuyPrice(marketPrice)) {
			DEBUG_OUT("buyOfferMaxDiff - failed to set custom price");
			return false;
		}

		return confirmOffer();
	}

	/**
	 * Creates a sell offer with the specified information.
	 * 
	 * @param slot
	 *            -1 for first available
	 * @param inventoryIndex
	 * @param price
	 *            -1 for market price
	 * @param quantity
	 *            -1 for all
	 * @return
	 */
	public static boolean placeSellOffer(int slot, int inventoryIndex,
			int price, int quantity) {
		if (!isOpen()) {
			DEBUG_OUT("placeSellOffer - Grand Exchange is not open");
			return false;
		}

		if (!isMainWindowOpen()) {
			goToMainWindow();
		}

		// --------------------------------------------

		Slot empty = getSlot(slot);

		if (empty == null) {
			DEBUG_OUT("placeSellOffer - failed to get empty slot");
			return false;
		}

		WidgetChild sellButton = empty.getSellButton();

		if (sellButton == null || !sellButton.validate()
				|| !sellButton.visible()) {
			DEBUG_OUT("placeSellOffer - failed to get sell button");
			return false;
		}

		sellButton.click(true);

		waitForWidget(WIDGET_EXCHANGE, WIDGET_BUY_PRICE_CUSTOM, 5000);

		// --------------------------------------------
		// Click the item in inventory to sell

		org.powerbot.game.api.wrappers.node.Item inventoryItem = Inventory.getItemAt(inventoryIndex);

		if (inventoryItem == null) {
			DEBUG_OUT("placeSellOffer - failed to get inventory item");
			return false;
		}

		WidgetChild inventoryItemWidget = inventoryItem.getWidgetChild();
		inventoryItemWidget.interact("Offer");

		Task.sleep(500, 800);

		// --------------------------------------------
		// Set quantity and price

		if (!setCustomSellQuantity(quantity)) {
			DEBUG_OUT("placeSellOffer - failed to set custom quantity");
			return false;
		}

		if (price == -1) {
			price = getMarketPrice();

			if (price == -1) {
				DEBUG_OUT("placeSellOffer - failed to get market price");
				return false;
			}
		}

		if (!setCustomSellPrice(price)) {

			DEBUG_OUT("placeSellOffer - failed to set custom price");
			return false;
		}

		return confirmOffer();
	}

	/**
	 * Creates a sell offer with the specified information.
	 * 
	 * @param slot
	 *            -1 for first available
	 * @param inventoryIndex
	 * @param differential
	 * @param quantity
	 *            -1 for all
	 * @return
	 */
	public static boolean placeSellOfferDifferential(int slot,
			int inventoryIndex, int differential, int quantity) {
		if (!isOpen()) {
			DEBUG_OUT("placeSellOfferDifferential - Grand Exchange is not open");
			return false;
		}

		if (!isMainWindowOpen()) {
			goToMainWindow();
		}

		// --------------------------------------------

		Slot empty = getSlot(slot);

		if (empty == null) {
			DEBUG_OUT("placeSellOfferDifferential - failed to get empty slot");
			return false;
		}

		WidgetChild sellButton = empty.getSellButton();

		if (sellButton == null || !sellButton.validate()
				|| !sellButton.visible()) {
			DEBUG_OUT("placeSellOfferDifferential - failed to get sell button");
			return false;
		}

		sellButton.click(true);

		waitForWidget(WIDGET_EXCHANGE, WIDGET_BUY_PRICE_CUSTOM, 5000);

		// --------------------------------------------
		// Click the item in inventory to sell

		WidgetChild inventoryItem = Inventory.getItemAt(inventoryIndex)
				.getWidgetChild();

		if (inventoryItem == null || !inventoryItem.validate()
				|| !inventoryItem.visible()
				|| inventoryItem.getChildStackSize() == 0) {
			DEBUG_OUT("placeSellOfferDifferential - failed to get inventory item");
			return false;
		}

		inventoryItem.interact("Offer");

		Task.sleep(500, 800);

		// --------------------------------------------
		// Set quantity and price

		if (!setCustomSellQuantity(quantity)) {
			DEBUG_OUT("placeSellOfferDifferential - failed to set custom quantity");
			return false;
		}

		int price = getMarketPrice();

		if (price == -1) {
			DEBUG_OUT("placeSellOfferDifferential - failed to get market price");
			return false;
		}

		price += differential;

		if (price <= 0)
			price = 1;

		if (!setCustomSellPrice(price)) {
			DEBUG_OUT("placeSellOfferDifferential - failed to set custom price");
			return false;
		}

		return confirmOffer();
	}

	public static boolean collectSlot(int index, boolean collectAsNotes) {
		if (!isOpen()) {
			DEBUG_OUT("collectSlot - Grand Exchange is not open");
			return false;
		}

		if (!isMainWindowOpen()) {
			goToMainWindow();
		}

		Slot slot = getSlot(index);

		if (slot == null)
			return false;

		if (slot.getStatus().isEmpty)
			return true;

		slot.openOffer().click(true);

		waitForWidget(WIDGET_EXCHANGE, WIDGET_COLLECTION_LEFT, 5000);

		WidgetChild left = Widgets.get(WIDGET_EXCHANGE, WIDGET_COLLECTION_LEFT);
		WidgetChild right = Widgets.get(WIDGET_EXCHANGE,
				WIDGET_COLLECTION_RIGHT);

		if (left == null || right == null || !left.validate()
				|| !right.validate() || !left.visible() || !right.visible()) {
			DEBUG_OUT("collectSlot - collection widgets missing");
			return false;
		}

		if (left.getChildStackSize() != 0) {
			if (left.getModelZoom() == 631) {
				left.interact(left.getActions()[0]);
			}

			Task.sleep(200, 300);
			left = Widgets.get(WIDGET_EXCHANGE, WIDGET_COLLECTION_LEFT);

			if (left != null && left.validate() && left.visible()
					&& left.getChildStackSize() != 0) {
				if (collectAsNotes) {
					left.interact("Collect-notes");
					Task.sleep(200, 300);
				} else {
					left.interact(left.getActions()[0]);
					Task.sleep(200, 300);
				}

				Task.sleep(200, 300);
				left = Widgets.get(WIDGET_EXCHANGE, WIDGET_COLLECTION_LEFT);

				if (left != null && left.validate() && left.visible()
						&& left.getChildStackSize() != 0) {
					left.click(true);
					Task.sleep(200, 300);
				}
			}
		}

		if (right != null && right.validate() && right.visible()
				&& right.getChildStackSize() != 0) {
			if (right.getModelZoom() == 631) {
				right.interact(right.getActions()[0]);
				Task.sleep(200, 300);
			}

			Task.sleep(200, 300);
			right = Widgets.get(WIDGET_EXCHANGE, WIDGET_COLLECTION_RIGHT);

			if (right != null && right.validate() && right.visible()
					&& right.getChildStackSize() != 0) {
				if (collectAsNotes) {
					right.interact("Collect-notes");
					Task.sleep(200, 300);
				} else {
					right.interact(right.getActions()[0]);
					Task.sleep(200, 300);
				}

				Task.sleep(200, 300);
				right = Widgets.get(WIDGET_EXCHANGE, WIDGET_COLLECTION_RIGHT);

				if (right != null && right.validate() && right.visible()
						&& right.getChildStackSize() != 0) {
					right.click(true);
					Task.sleep(200, 300);
				}
			}
		}
		return true;
	}

	public static boolean collectSlot(int index, com.item.Item item, boolean selling, boolean collectAsNotes) {
		if (!GrandExchange.isOpen()) {
			System.out.println("collectSlot - Grand Exchange is not open");
			return false;
		}

		if (!GrandExchange.isMainWindowOpen()) {
			GrandExchange.goToMainWindow();
		}

		Slot slot = GrandExchange.getSlot(index);

		if (slot == null)
			return false;

		if (slot.getStatus().isEmpty)
			return true;

		slot.openOffer().click(true);
		Task.sleep(600, 800);

		GrandExchange.waitForWidget(GrandExchange.WIDGET_EXCHANGE,
				GrandExchange.WIDGET_COLLECTION_LEFT, 5000);

		WidgetChild left = Widgets.get(GrandExchange.WIDGET_EXCHANGE,
				GrandExchange.WIDGET_COLLECTION_LEFT);
		WidgetChild right = Widgets.get(GrandExchange.WIDGET_EXCHANGE,
				GrandExchange.WIDGET_COLLECTION_RIGHT);
		if (!selling) {
			item.SELL_PRICE = getBoughtPrice();
		} else {
			item.BUY_PRICE = getBoughtPrice();
		}

		if (left == null || right == null || !left.validate()
				|| !right.validate() || !left.visible() || !right.visible()) {
			System.out.println("collectSlot - collection widgets missing");
			return false;
		}

		if (left.getChildStackSize() != 0) {
			if (left.getModelZoom() == 631) {
				left.interact(left.getActions()[0]);
			}

			Task.sleep(200, 300);
			left = Widgets.get(GrandExchange.WIDGET_EXCHANGE,
					GrandExchange.WIDGET_COLLECTION_LEFT);

			if (left != null && left.validate() && left.visible()
					&& left.getChildStackSize() != 0) {
				if (collectAsNotes) {
					left.interact("Collect-notes");
					Task.sleep(200, 300);
				} else {
					left.interact(left.getActions()[0]);
					Task.sleep(200, 300);
				}

				Task.sleep(200, 300);
				left = Widgets.get(GrandExchange.WIDGET_EXCHANGE,
						GrandExchange.WIDGET_COLLECTION_LEFT);

				if (left != null && left.validate() && left.visible()
						&& left.getChildStackSize() != 0) {
					left.click(true);
					Task.sleep(200, 300);
				}
			}
		}

		if (right != null && right.validate() && right.visible()
				&& right.getChildStackSize() != 0) {
			if (right.getModelZoom() == 631) {
				right.interact(right.getActions()[0]);
				Task.sleep(200, 300);
			}

			Task.sleep(200, 300);
			right = Widgets.get(GrandExchange.WIDGET_EXCHANGE,
					GrandExchange.WIDGET_COLLECTION_RIGHT);

			if (right != null && right.validate() && right.visible()
					&& right.getChildStackSize() != 0) {
				if (collectAsNotes) {
					right.interact("Collect-notes");
					Task.sleep(200, 300);
				} else {
					right.interact(right.getActions()[0]);
					Task.sleep(200, 300);
				}

				Task.sleep(200, 300);
				right = Widgets.get(GrandExchange.WIDGET_EXCHANGE,
						GrandExchange.WIDGET_COLLECTION_RIGHT);

				if (right != null && right.validate() && right.visible()
						&& right.getChildStackSize() != 0) {
					right.click(true);
					Task.sleep(200, 300);
				}
			}
		}
		return true;
	}
	
	public static boolean collectAllComplete(boolean collectAsNotes) {
		for (int i = 0; i < 6; i++) {
			if (ExchangeSlots[i].getStatus().isComplete)
				collectSlot(i, collectAsNotes);
		}

		return true;
	}

	public static int getBoughtPrice() {
		String w = Widgets.get(105, 198).getText();
		String pricestr = w.substring(w.indexOf("price of <col=cc9900>") + 21,
				w.indexOf("</col> gp."));
		pricestr = pricestr.replaceAll(",", "");
		return Integer.parseInt(pricestr);
	}
	
	public static boolean collectAllInProgress(boolean collectAsNotes) {
		SlotStatus status;

		for (int i = 0; i < 6; i++) {
			status = ExchangeSlots[i].getStatus();

			if (!status.isEmpty && !status.isComplete && status.progress != 0)
				collectSlot(i, collectAsNotes);
		}

		return true;
	}

	public static boolean collectAll(boolean collectAsNotes) {
		for (int i = 0; i < 6; i++) {
			collectSlot(i, collectAsNotes);
		}

		return true;
	}

	public static boolean cancelSlot(int index) {
		if (!isMainWindowOpen())
			return false;

		Slot slot = getSlot(index);

		if (slot == null)
			return false;

		if (slot.getStatus().isEmpty)
			return true;

		slot.openOffer().click(true);

		waitForWidget(WIDGET_EXCHANGE, WIDGET_CANCEL_OFFER, 5000);

		WidgetChild cancelButton = Widgets.get(WIDGET_EXCHANGE,
				WIDGET_CANCEL_OFFER);

		if (cancelButton == null || !cancelButton.validate()
				|| !cancelButton.visible())
			return false;

		cancelButton.click(true);

		goToMainWindow();

		return true;
	}

	public static boolean cancelAll() {
		SlotStatus status;

		for (int i = 0; i < 6; i++) {
			status = ExchangeSlots[i].getStatus();

			if (!status.isEmpty && !status.isComplete)
				cancelSlot(i);
		}

		return true;
	}

	/**
	 * Returns the specified slot. If index is [0,5], then this acts identical
	 * to GrandExchange.ExchangeSlots[index]. If index is -1, it returns the
	 * first empty slot. If no empty slots are found, then returns null.
	 */
	public static Slot getSlot(int index) {
		if (index < -1)
			index = 0;

		if (index > 5)
			index = 5;

		if (index != -1) {
			return ExchangeSlots[index];
		} else {
			for (int i = 0; i < 6; i++) {
				if (ExchangeSlots[i].getStatus().isEmpty)
					return ExchangeSlots[i];
			}
		}

		return null;
	}

	public static boolean isBuySearchUp() {
		WidgetChild widget = Widgets.get(WIDGET_DIALOGUE_SEARCH, 0);
		return widget != null && widget.validate();
	}

	public static boolean setCustomBuyQuantity(int quantity) {
		if (quantity < 0) {
			DEBUG_OUT("setCustomBuyQuantity - invalid quantity [" + quantity
					+ "]");
			return false;
		}

		if (quantity > 1) {

			WidgetChild button = Widgets.get(WIDGET_EXCHANGE,
					WIDGET_BUY_QUANTITY_CUSTOM);

			if (button == null || !button.validate()) {
				DEBUG_OUT("failed to get custom buy quantity button");
				return false;
			}

			button.click(true);

			waitForWidget(WIDGET_DIALOGUE_CUSTOM_QUANTITY_PRICE, 4, 5000);
			Keyboard.sendText(Integer.toString(quantity), true);
			waitForWidgetToClose(WIDGET_DIALOGUE_CUSTOM_QUANTITY_PRICE, 4, 5000);
			return true;
		} else {
			WidgetChild button = Widgets.get(WIDGET_EXCHANGE,
					WIDGET_BUY_QUANTITY_PLUS_1);
			button.click(true);
			return true;
		}
	}

	public static boolean setCustomBuyPrice(int price) {
		if (price < 0 && price != -1) {
			DEBUG_OUT("setCustomBuyPrice - invalid price [" + price + "]");
			return false;
		}

		if (price == -1) {
			price = getMarketPrice();

			if (price == -1) {
				DEBUG_OUT("setCustomBuyPrice - failed to get market price");
				return false;
			}
		}

		WidgetChild button = Widgets.get(WIDGET_EXCHANGE,
				WIDGET_BUY_PRICE_CUSTOM);

		if (button == null || !button.validate()) {
			DEBUG_OUT("setCustomBuyPrice - failed to get custom buy price button");
			return false;
		}

		button.click(true);

		waitForWidget(WIDGET_DIALOGUE_CUSTOM_QUANTITY_PRICE, 4, 5000);
		Keyboard.sendText(Integer.toString(price), true);
		waitForWidgetToClose(WIDGET_DIALOGUE_CUSTOM_QUANTITY_PRICE, 4, 5000);

		return true;
	}

	public static boolean setCustomSellQuantity(int quantity) {
		if (quantity < 0 && quantity != -1) {
			DEBUG_OUT("setCustomSellQuantity - invalid quantity [" + quantity
					+ "]");
			return false;
		}

		if (quantity == -1) {
			// Sell all of them
			WidgetChild sellAllButton = Widgets.get(WIDGET_EXCHANGE,
					WIDGET_BUY_QUANTITY_PLUS_1000);

			if (sellAllButton == null || !sellAllButton.validate()
					|| !sellAllButton.visible()) {
				DEBUG_OUT("setCustomSellQuantity - failed to get sell all button");
				return false;
			}

			sellAllButton.interact("Sell");

			Task.sleep(500, 800);

			return true;
		} else {
			return setCustomBuyQuantity(quantity);
		}
	}

	public static boolean setCustomSellPrice(int price) {
		return setCustomBuyPrice(price);
	}

	/**
	 * Presses the confirm offer button if it exists.
	 * 
	 * @return FALSE if failed to press
	 */
	public static boolean confirmOffer() {
		waitForWidget(WIDGET_EXCHANGE, WIDGET_CONFIRM_OFFER, 5000);

		WidgetChild button = Widgets.get(WIDGET_EXCHANGE, WIDGET_CONFIRM_OFFER);

		if (button == null || !button.validate()) {
			DEBUG_OUT("failed to retrieve confirm button");
			return false;
		}

		button.click(true);

		waitForWidgetToClose(WIDGET_EXCHANGE, WIDGET_CONFIRM_OFFER, 5000);

		return true;
	}

	public static boolean goToMainWindow() {
		if (!isOpen())
			return false;

		Task.sleep(300, 400);

		WidgetChild button = Widgets.get(WIDGET_EXCHANGE, WIDGET_BACK);

		if (button == null || !button.validate() || !button.visible())
			return false;

		button.click(true);

		waitForWidget(WIDGET_EXCHANGE, WIDGET_MAIN_WINDOW_IDENTIFIER, 5000);

		return true;
	}

	/**
	 * Returns the reported market price for the current item on the buy/sell
	 * screen.
	 * 
	 * @return -1 if failed to get price
	 */
	public static int getMarketPrice() {
		WidgetChild display = Widgets.get(WIDGET_EXCHANGE, WIDGET_MARKET_PRICE);

		if (display == null || !display.validate() || !display.visible())
			return -1;

		String displayText = display.getText();
		displayText = displayText.substring(0, displayText.length() - 3);
		displayText = displayText.replaceAll(",", "");

		return Integer.parseInt(displayText);
	}

	/**
	 * Parses the int from the passed string. The string is assumed to be in the
	 * style presented by the Grand Exchange in that it may feature commas, end
	 * with gp, and/or contain letters indicating value (K, M, B).
	 * 
	 * @param str
	 * @return
	 */
	public static int parseIntFromString(String str) {
		str = str.toUpperCase();

		// First check for existance of K, M, or B
		int multiplier = 1;

		if (str.contains("K"))
			multiplier = 1000;
		else if (str.contains("M"))
			multiplier = 1000000;
		else if (str.contains("B"))
			multiplier = 1000000000;

		// Remove unwanted symbols
		str = str.replaceAll("[^0-9]", "");

		return Integer.parseInt(str);
	}

	/**
	 * Returns the approximate amount of money in the money pouch. This number
	 * is not exact because the money pouch does not display the precise amount
	 * of money it holds at higher values. Example: 204K, 10M, etc.
	 * 
	 * @return -1 if failed
	 */
	public static int getMoneyPouchTotal() {
		// Note that the total does not have to visible in order to read the
		// value.
		// Just make sure the widget exists and is validated.

		WidgetChild pouchTotal = Widgets.get(WIDGET_MONEY_POUCH_PARENT,
				WIDGET_MONEY_POUCH_TOTAL);

		if (pouchTotal == null || !pouchTotal.validate()) {
			DEBUG_OUT("failed to read money pouch total");
			return -1;
		}

		String pouchValue = pouchTotal.getText();
		int multiple = 1;

		if (!Character.isDigit(pouchValue.charAt(pouchValue.length() - 1))) {
			switch (pouchValue.charAt(pouchValue.length() - 1)) {
			case 'K':
			case 'k':
				multiple = 1000;
				break;

			case 'M':
			case 'm':
				multiple = 1000000;
				break;

			case 'B':
			case 'b':
				multiple = 1000000000;
				break;
			}

			pouchValue = pouchValue.substring(0, pouchValue.length() - 1);
		}

		return Integer.parseInt(pouchValue) * multiple;
	}

	/**
	 * Returns the reported market value of the specified item.
	 * 
	 * This method may not be the most efficient, since it operates by
	 * initiating a new buy, searching for the item, then pulling the market
	 * price from the result.
	 * 
	 * @param item
	 * @return
	 */
	public static int getMarketValueOf(String item) {
		if (!isOpen()) {
			DEBUG_OUT("getMarketValueOf - GrandExchange is not open");
			return -1;
		}

		if (!isMainWindowOpen()) {
			goToMainWindow();
		}

		Slot empty = getSlot(-1);

		if (empty == null) {
			DEBUG_OUT("getMarketValueOf - failed to get an empty slot");
			return -1;
		}

		WidgetChild buyButton = empty.getBuyButton();

		if (buyButton == null || !buyButton.validate() || !buyButton.visible()) {
			DEBUG_OUT("getMarketValueOf - failed to get empty slot buy button");
			return -1;
		}

		buyButton.click(true);

		// --------------------------------------------
		// Enter the search terms

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
				5000);

		if (!isBuySearchUp()) {
			WidgetChild searchButton = Widgets.get(WIDGET_EXCHANGE,
					WIDGET_BUY_SEARCH);

			if (searchButton == null || !searchButton.validate()) {
				DEBUG_OUT("getMarketValueOf - failed to find search button");
				return -1;
			}

			searchButton.click(true);

			waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_INPUT,
					5000);

			if (!isBuySearchUp()) {
				DEBUG_OUT("getMarketValueOf - failed to initiate search");
				return -1;
			}
		}

		Keyboard.sendText(item, false);

		waitForWidget(WIDGET_DIALOGUE_SEARCH, WIDGET_DIALOGUE_SEARCH_RESULTS,
				0, 5000);

		// --------------------------------------------
		// Select the proper item from the list

		WidgetChild searchResults = Widgets.get(WIDGET_DIALOGUE_SEARCH,
				WIDGET_DIALOGUE_SEARCH_RESULTS);

		if (searchResults == null || !searchResults.validate()) {
			DEBUG_OUT("getMarketValueOf - failed to get search results");
			return -1;
		}

		WidgetChild individualResults[] = searchResults.getChildren();
		WidgetChild validResult = null;

		for (final WidgetChild result : individualResults) {
			if (result.getText().toUpperCase().compareTo(item.toUpperCase()) == 0) {
				validResult = result;
			}
		}

		if (validResult == null || !validResult.validate()) {
			DEBUG_OUT("getMarketValueOf - failed to get valid result");
			return -1;
		}

		validResult.click(true);

		Task.sleep(1000, 1500);

		// --------------------------------------------
		// Get price

		int marketPrice = getMarketPrice();

		goToMainWindow();

		return marketPrice;
	}

	/**
	 * Returns whether the money pouch total is visible or not.
	 * 
	 * @return
	 */
	public static boolean isMoneyPouchTotalVisible() {
		WidgetChild pouchTotal = Widgets.get(WIDGET_MONEY_POUCH_PARENT,
				WIDGET_MONEY_POUCH_TOTAL);
		return pouchTotal != null && pouchTotal.validate()
				&& pouchTotal.visible();
	}

	/**
	 * Shows the money pouch total if it is not visible.
	 * 
	 * @return
	 */
	public static boolean showMoneyPouchTotal() {
		if (isMoneyPouchTotalVisible())
			return true;

		WidgetChild pouchToggle = Widgets.get(WIDGET_MONEY_POUCH_PARENT,
				WIDGET_MONEY_POUCH_TOGGLE);

		if (pouchToggle == null || !pouchToggle.validate()
				|| !pouchToggle.visible()) {
			DEBUG_OUT("failed to toggle money pouch");
			return false;
		}

		pouchToggle.interact("Toggle");

		return true;
	}

	/**
	 * Hides the money pouch total if it is visible.
	 * 
	 * @return
	 */
	public static boolean hideMoneyPouchTotal() {
		if (!isMoneyPouchTotalVisible())
			return true;

		WidgetChild pouchToggle = Widgets.get(WIDGET_MONEY_POUCH_PARENT,
				WIDGET_MONEY_POUCH_TOGGLE);

		if (pouchToggle == null || !pouchToggle.validate()
				|| !pouchToggle.visible()) {
			DEBUG_OUT("failed to toggle money pouch");
			return false;
		}

		pouchToggle.interact("Toggle");

		return true;
	}

	/**
	 * Waits until the specified widget exists, is valid, and on screen.
	 * 
	 * @param parent
	 *            Parent widget
	 * @param child
	 *            Child widget
	 * @param timeout
	 *            Maximum time to wait
	 * @return TRUE if opened during time, FALSE if timed out
	 */
	public static boolean waitForWidget(int parent, int child, int timeout) {
		final Timer timer = new Timer(timeout);
		WidgetChild widget = Widgets.get(parent, child);

		while (timer.isRunning()
				&& (widget == null || !widget.validate() || !widget
						.isOnScreen())) {
			Task.sleep(10);
			widget = Widgets.get(parent, child);
		}

		if (timer.isRunning())
			return false;
		else
			return true;
	}

	/**
	 * Waits until the specified widget exists, is valid, and on screen. This
	 * waits for a sub-child. Used for selecting the proper search result
	 * 
	 * @param parent
	 *            Parent widget
	 * @param child
	 *            Child widget
	 * @param timeout
	 *            Maximum time to wait
	 * @return TRUE if opened during time, FALSE if timed out
	 */
	public static boolean waitForWidget(int parent, int child, int subchild,
			int timeout) {
		final Timer timer = new Timer(timeout);
		WidgetChild widget = Widgets.get(parent, child).getChild(subchild);

		while (timer.isRunning()
				&& (widget == null || !widget.validate() || !widget.visible())) {
			Task.sleep(10);
			widget = Widgets.get(parent, child).getChild(subchild);
		}

		if (timer.isRunning())
			return false;
		else
			return true;
	}

	/**
	 * Waits until the specified widget is no longer on screen.
	 * 
	 * @param parent
	 *            Parent widget
	 * @param child
	 *            Child widget
	 * @param timeout
	 *            Maximum time to wait
	 * @return TRUE if closed during time, FALSE if timed out
	 */
	public static boolean waitForWidgetToClose(int parent, int child,
			int timeout) {
		final Timer timer = new Timer(timeout);
		WidgetChild widget = Widgets.get(parent, child);

		while (timer.isRunning()
				&& (widget != null && widget.validate() && widget.isOnScreen())) {
			Task.sleep(10);
			widget = Widgets.get(parent, child);
		}

		if (timer.isRunning())
			return false;
		else
			return true;
	}

	public static boolean verifyWidget(WidgetChild widget) {
		if (widget == null || !widget.validate() || !widget.visible())
			return false;

		return true;
	}

	private static boolean isExchangeClerk(final Identifiable identifiable) {
		Arrays.sort(EXCHANGE_NPC_IDS);
		return identifiable instanceof NPC
				&& Arrays.binarySearch(EXCHANGE_NPC_IDS, identifiable.getId()) >= 0;
	}
}
