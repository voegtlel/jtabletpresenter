package de.freiburg.uni.tablet.presenter.editor.rendering.toolbar;


/**
 * Adds a simple space for separation.
 * @author Lukas
 *
 */
public class ToolbarSpace implements IToolbarItem {
	public ToolbarSpace() {
	}
	
	@Override
	public ToolbarItemType getType() {
		return ToolbarItemType.Space;
	}
}
