package com.item;




public class ItemContainer {
	
	public Item[] ITEMS;
	
	public ItemContainer(Item... i) {
		this.ITEMS = i;
	}
	
	public Item getItem(int i) {
		return ITEMS[i];
	}

}
