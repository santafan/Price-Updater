package com.ge;

import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

public class Slot
{	
	public int index;
	public int childNumber;
	
	private final SlotStatus status = new SlotStatus( );
	
	Slot( int index, int childNumber )
	{
		this.index = index;
		this.childNumber = childNumber;
	}
	
	public SlotStatus getStatus( )
	{		
		if( !GrandExchange.isMainWindowOpen( ) )
		{
			GrandExchange.DEBUG_OUT( "slotStatus.getStatus - main window not open - failed to update status" );
			return status;
		}
		
		WidgetChild widget;
		
		status.isEmpty    = true;
		status.isBuying   = false;
		status.isSelling  = false;
		status.isComplete = false;
		status.progress   = 0;
		
		widget = Widgets.get( GrandExchange.WIDGET_EXCHANGE, childNumber + 11 );
		
		if( widget == null || !widget.validate( ) || !widget.visible( ) )
		{
			status.isEmpty = false;
			
			widget = Widgets.get( GrandExchange.WIDGET_EXCHANGE, childNumber ).getChild( 10 );
			
			if( widget.getText( ).compareTo( "Buy" ) == 0 )
			{
				status.isBuying = true;
			}
			else
			{
				status.isSelling = true;
			}
			
			int maxLength = Widgets.get( 105, childNumber ).getChild( 12 ).getWidth( );
			int length    = Widgets.get( 105, childNumber ).getChild( 13 ).getWidth( );
			
			status.progress = ( int )Math.ceil( ( double )( length / maxLength ) * 100 );
			
			if( status.progress == 100 )
				status.isComplete = true;
			
			//------------------------------------------------------------
			// If the slot is in use, get the item, count, and gold associated
			
			if( !status.isEmpty )
			{
				WidgetChild parent = Widgets.get( GrandExchange.WIDGET_EXCHANGE, childNumber );
				
				if( !GrandExchange.verifyWidget( parent ) )
				{
					return status;
				}
				
				WidgetChild item  = parent.getChild( GrandExchange.WIDGET_SLOT_ITEM );
				WidgetChild count = parent.getChild( GrandExchange.WIDGET_SLOT_COUNT );
				WidgetChild gold  = parent.getChild( GrandExchange.WIDGET_SLOT_GOLD );
				
				if( !GrandExchange.verifyWidget( item ) || !GrandExchange.verifyWidget( count ) || !GrandExchange.verifyWidget( gold ) )
				{
					return status;
				}
				
				status.itemName = item.getText( );
				status.count    = count.getChildStackSize( );
				status.gold     = GrandExchange.parseIntFromString( gold.getText( ) );
				
			}
		}
		
		return status;
	}
	
	public WidgetChild getBuyButton( )
	{
		return Widgets.get( 105, childNumber + 11 );
	}
	
	public WidgetChild getSellButton( )
	{
		return Widgets.get( 105, childNumber + 10 );
	}
	
	public WidgetChild openOffer( )
	{
		return Widgets.get( 105, childNumber );
	}
	
}
