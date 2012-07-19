package com.oreilly.common.interaction.text.formatter;

import org.bukkit.ChatColor;

import com.oreilly.common.interaction.text.InteractionPage;
import com.oreilly.permitme.PermitMe;


public class WidthFormatter extends Formatter {
	
	public int width;
	
	
	public WidthFormatter() {
		this( 53 ); //default width
	}
	
	
	public WidthFormatter( int width ) {
		super();
		this.width = width;
	}
	
	
	@Override
	protected String format( String s, InteractionPage page ) {
		// DEBUG
		PermitMe.log.info( "[PermitSign] Width formatter begins:\n" + s );
		String result = "";
		for ( String line : s.split( "\n" ) ) {
			PermitMe.log.info( "Line is: " + line );
			int lineWidth = ChatColor.stripColor( line ).length();
			PermitMe.log.info( "Line width is: " + lineWidth );
			if ( lineWidth <= width )
				result += line + "\n";
			else {
				String remaining = line;
				PermitMe.log.info( "Splitting line: " + lineWidth + " > " + width );
				while ( remaining.length() > 0 ) {
					PermitMe.log.info( "Remainder is: " + remaining );
					// get the max index that results in a width wide line, allowing for colour codes
					int maxWidth = width;
					while ( ChatColor.stripColor( remaining.substring( 0, maxWidth ) ).length() < width )
						maxWidth++;
					// the keep decreasing the index, until we find a space
					int currentIndex = maxWidth;
					while ( remaining.charAt( currentIndex ) != ' ' ) {
						currentIndex -= 1;
						if ( currentIndex == 0 ) {
							currentIndex = maxWidth;
							break;
						}
					}
					result += remaining.substring( 0, currentIndex ) + "\n";
					remaining = remaining.substring( currentIndex );
					if ( ChatColor.stripColor( remaining ).length() < width ) {
						result += remaining + "\n";
						break;
					}
				}
			}
		}
		return result;
	}
	
	/*@Override
	protected String format( String s, InteractionPage page ) {
		// DEBUG
		PermitMe.log.info( "[PermitSign] Width formatter begins:\n" + s );
		String[] splitString = s.split( "\n" );
		String result = "";
		// for each existing line...
		for ( String line : splitString ) {
			// DEBUG:
			PermitMe.log.info( "Line is: " + line );
			// if, after we remove the color information, the line is too long...
			if ( ChatColor.stripColor( line ).length() > width ) {
				String remainder = line;
				// until we have processed the entire line
				while ( remainder.length() > 0 ) {
					int currentIndex = remainder.indexOf( " " );
					if ( currentIndex == -1 ) {
						result += remainder;
						continue;
					}
					int lastValidIndex = 0;
					String currentString = remainder.substring( 0, currentIndex );
					// test each segment, until we have the longest possible that fits within the width
					while ( ChatColor.stripColor( currentString ).length() < width ) {
						lastValidIndex = currentIndex;
						currentIndex = remainder.indexOf( " ", currentIndex + 1 );
						currentString = remainder.substring( 0, currentIndex );
						PermitMe.log.info( "CurrentString is: " + currentString );
					}
					// add the string (that fits) to the result,
					// and add what's left to our remaining string
					result += remainder.substring( 0, lastValidIndex ) + "\n";
					remainder = remainder.substring( lastValidIndex + 1, remainder.length() - 1 );
					PermitMe.log.info( "Remainder is now: " + remainder );
				}
			} else
				result += line + "\n";
		}
		// DEBUG
		PermitMe.log.info( "[PermitSign] Width formatter ends:\n" + result );
		return result;
	}*/
}
