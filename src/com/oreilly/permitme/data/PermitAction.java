package com.oreilly.permitme.data;

public enum PermitAction {
	BLOCKPLACE, BLOCKDESTORY, BLOCKUSE, ITEMUSE, ITEMCRAFT;
	
	public String asHumanString() {
		switch (this) {
		case BLOCKPLACE: return "placing a block";
		case BLOCKDESTORY: return "destorying a block";
		case BLOCKUSE: return "activating a block";
		case ITEMUSE: return "using an item";
		case ITEMCRAFT: return "crafting an item";
		default: return "an undefined action";
		}
	}
}
