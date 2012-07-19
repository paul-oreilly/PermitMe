package com.oreilly.common.interaction.text.formatter;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import com.oreilly.common.interaction.text.InteractionPage;
import com.oreilly.common.interaction.text.interfaces.HasTitle;


// TODO: Interface with style's based on context data

public class Border extends Formatter {
	
	public Character horizontalPattern = '-';
	public Character topRightCorner = '\\';
	public Character topLeftCorner = '/';
	public Character bottomLeftCorner = '\\';
	public Character bottomRightCorner = '/';
	public ChatColor textBorderColor = ChatColor.DARK_GREEN;
	public ChatColor titleBorderColor = ChatColor.DARK_BLUE;
	public ChatColor titleColor = ChatColor.AQUA;
	
	
	public Border() {
	}
	
	
	// chained init functions
	public Border withCorners( char... corners ) {
		if ( corners.length == 1 ) {
			topRightCorner = corners[0];
			topLeftCorner = corners[0];
			bottomRightCorner = corners[0];
			bottomLeftCorner = corners[0];
		}
		if ( corners.length == 4 ) {
			topLeftCorner = corners[0];
			topRightCorner = corners[1];
			bottomRightCorner = corners[2];
			bottomLeftCorner = corners[3];
		}
		return this;
	}
	
	
	public Border withHorizontalPattern( char pattern ) {
		horizontalPattern = pattern;
		return this;
	}
	
	
	public Border withTextBorderColor( ChatColor color ) {
		textBorderColor = color;
		return this;
	}
	
	
	public Border withTextColor( ChatColor color ) {
		textBorderColor = color;
		return this;
	}
	
	
	public Border withTitleBorderColor( ChatColor color ) {
		titleBorderColor = color;
		return this;
	}
	
	
	public Border withTitleColor( ChatColor color ) {
		titleColor = color;
		return this;
	}
	
	
	// main function
	
	@Override
	protected String format( String s, InteractionPage page ) {
		String result = "";
		// title, if one exists
		if ( page instanceof HasTitle ) {
			result += makeLine( LineType.TITLE_TOP );
			HasTitle withTitle = (HasTitle)page;
			result += titleColor.toString() + "  " + withTitle.getTitle() + "\n";
			result += makeLine( LineType.TITLE_BOTTOM );
		}
		// page body
		result += makeLine( LineType.TEXT_TOP );
		for ( String line : s.split( "\n" ) )
			result += "  " + line + "\n";
		// lower line
		result += makeLine( LineType.TEXT_BOTTOM );
		return result;
	}
	
	
	protected String makeLine( LineType lineType ) {
		String leftCorner = "";
		String rightCorner = "";
		String color = "";
		switch ( lineType ) {
			case TEXT_TOP:
				leftCorner = topLeftCorner.toString();
				rightCorner = topRightCorner.toString();
				color = textBorderColor.toString();
				break;
			case TITLE_TOP:
				leftCorner = topLeftCorner.toString();
				rightCorner = topRightCorner.toString();
				color = titleBorderColor.toString();
				break;
			case TEXT_BOTTOM:
				leftCorner = bottomLeftCorner.toString();
				rightCorner = bottomRightCorner.toString();
				color = textBorderColor.toString();
				break;
			case TITLE_BOTTOM:
				leftCorner = bottomLeftCorner.toString();
				rightCorner = bottomRightCorner.toString();
				color = titleBorderColor.toString();
				break;
		}
		return color + leftCorner +
				StringUtils.repeat( horizontalPattern.toString(), 51 ) +
				rightCorner + "\n";
	}
	
}


enum LineType {
	TITLE_TOP, TITLE_BOTTOM, TEXT_TOP, TEXT_BOTTOM
}