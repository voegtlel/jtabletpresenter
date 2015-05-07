package de.freiburg.uni.tablet.presenter.editor.rendering.toolbar;


/**
 * Adds a filler for separation.
 * @author Lukas
 *
 */
public class ToolbarFill implements IToolbarItem {
	public ToolbarFill() {
	}
	
	@Override
	public ToolbarItemType getType() {
		return ToolbarItemType.Fill;
	}
}
