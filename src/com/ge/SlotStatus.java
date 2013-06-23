package com.ge;

public class SlotStatus
{
	public String  itemName;
	public int     progress;
	public int     count;
	public int     gold;
	public boolean isEmpty;
	public boolean isSelling;
	public boolean isBuying;
	public boolean isComplete;
	
	SlotStatus( )
	{
		this.itemName   = "";
		this.count      = 0;
		this.gold       = 0;
		this.isEmpty    = true;
		this.isBuying   = false;
		this.isSelling  = false;
		this.isComplete = false;
		this.progress   = 0;
	}
}
