package de.freiburg.uni.tablet.presenter.editor.rendering.toolbar;

import java.awt.Point;

import javax.swing.ImageIcon;

import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.IButtonAction;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageEditor;

public class ToolbarAction implements IToolbarItem {
	private String _name;
	private ImageIcon _imageIcon;
	private IButtonAction _action;

	public ToolbarAction(final String name, final IButtonAction action, final int desiredSize) {
		_name = name;
		_imageIcon = new ImageIcon(action.getImageResource(desiredSize, desiredSize));
		_action = action;
	}

	public String getName() {
		return _name;
	}
	
	public ImageIcon getIcon() {
		return _imageIcon;
	}
	
	public void perform(final Point desiredLocation) {
		_action.perform(desiredLocation);
	}

	@Override
	public ToolbarItemType getType() {
		return ToolbarItemType.Action;
	}
}
