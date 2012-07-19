package com.oreilly.common.interaction.text.formatter;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;

import com.oreilly.common.interaction.text.InteractionPage;
import com.oreilly.common.interaction.text.interfaces.HighlightClient;
import com.oreilly.permitme.PermitMe;


public class Highlighter extends Formatter {
	
	public ChatColor defaultHighlight = ChatColor.BLUE;
	public ChatColor defaultTextColor = ChatColor.WHITE;
	
	
	public Highlighter( HighlightClient client ) {
	}
	
	
	// chained init methods
	
	public Highlighter withDefaultHighlight( ChatColor color ) {
		defaultHighlight = color;
		return this;
	}
	
	
	@Override
	protected String format( String s, InteractionPage page ) {
		if ( !( page instanceof HighlightClient ) ) {
			PermitMe.log.info( "com.oreilly.common: Failed to format text with highlighter, as page " +
					page.getClass().toString() + " does not implement highlight client" );
			return s;
		} else {
			HighlightClient client = (HighlightClient)page;
			HashMap< String, Iterator< String >> data = client.getHighlightList();
			// get the text color
			Object rawTextColor = page.style.get( InteractionPage.STYLE_TEXT_COLOR );
			String textColor = null;
			if ( rawTextColor instanceof ChatColor )
				textColor = ( (ChatColor)rawTextColor ).toString();
			else
				textColor = defaultTextColor.toString();
			// for each highlighting group...
			for ( String highlightType : data.keySet() ) {
				// get the highlight color..
				Object rawHighlightColor = page.style.get( highlightType );
				String highlightColor = null;
				if ( rawHighlightColor instanceof ChatColor )
					highlightColor = ( (ChatColor)rawHighlightColor ).toString();
				else
					highlightColor = defaultHighlight.toString();
				// replace every entry in the list with a highlighed version
				Iterator< String > list = data.get( highlightType );
				if ( list != null )
					while ( list.hasNext() ) {
						String toReplace = list.next();
						s = s.replace( toReplace, highlightColor + toReplace + textColor );
					}
			}
		}
		return s;
	}
}
