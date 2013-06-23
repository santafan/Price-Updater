package com.item;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Item {

	public int ID;
	public int ORIGINAL_PRICE = -1;
	public int START_PRICE;
	public int BUY_PRICE = -1;
	public int SELL_PRICE = -1;
	public String NAME;
	public boolean ITEM_BOUGHT;
	public boolean ITEM_SOLD;
	
	public Item(int id) {
		this.ID = id;
		getInfo(id);
	}
	
	
	//Returns the ID of the item.
	public int getID() {
		return ID;
	}
	
	//Returns the orginal price that the item from the runescape website. If could not find returns -1.
	public int getOrginalPrice() {
		return ORIGINAL_PRICE;
	}
	
	//Returns the start price. This is th price to buy the item quickly.
	public int getStartPrice() {
		return START_PRICE;
	}
	
	
	//Returns the buy price of the item for what it was sold for (What to buy it at). If could not find returns -1.
	public int getBuyPrice() {
		return BUY_PRICE;
	}
	
	//Returns the sell price of the item for what it was bought for (What to sell it at). If could not find returns -1.
	public int getSellPrice() {
		return SELL_PRICE;
	}
	
	//Returns the name of item.
	public String getName() {
		return NAME;
	}
	
	//Returns if the item was bought.
	public boolean isBought() {
		return ITEM_BOUGHT;
	}
	
	//Returns if the item was sold.
	public boolean isSold() {
		return ITEM_SOLD;
	}
	
	//Grabs info from the RuneScape Website on the price, name etc. of the given item.
 	private void getInfo(int id) {
		try {
			URL site = new URL(
					"http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj="
							+ id);
			URLConnection spoof = site.openConnection();
			spoof.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					site.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("<h5 class=\"test\">")) {
					inputLine = in.readLine();
					this.NAME = inputLine.toLowerCase();
				}
				if (inputLine.contains("Current guide price:</th>")) {
					inputLine = in.readLine();
					String price_s = inputLine.substring(4,
							inputLine.indexOf("</"));
					price_s = price_s.replaceAll(",", "");
					price_s = price_s.replaceAll("\\.", "");
					if (price_s.contains("k")) {
						price_s = price_s.replaceAll("k", "");
						price_s = price_s + "00";
					}
					System.out.println(price_s);
					int price = Integer.parseInt(price_s);
					this.ORIGINAL_PRICE = price;
					if (ORIGINAL_PRICE != -1) {
						if (ORIGINAL_PRICE <= 1000) {
							this.START_PRICE = ORIGINAL_PRICE + 2000;
						} else if (ORIGINAL_PRICE <= 10000) {
							this.START_PRICE = ORIGINAL_PRICE + 3000;
						} else if (ORIGINAL_PRICE <= 50000) {
							this.START_PRICE = ORIGINAL_PRICE + 5000;
						} else {
							this.START_PRICE = ORIGINAL_PRICE + 10000;
						}
					}
				}
			}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
