/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonTools extends AbstractButtonMenuTool {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonTools(final IToolPageEditor editor) {
		super("tools", editor, "Tools", "/buttons/document-properties.png", 10);
		this._frame.addItem(new ButtonDocumentNew(editor), true);
		this._frame.addItem(new ButtonOpenFrom(editor), true);
		this._frame.addItem(new ButtonSaveAs(editor), true);
		//this._frame.addItem(new ButtonPreferences(editor), true);
		this._frame.addItem(new ButtonPrimary(editor), true);
		this._frame.addItem(new ButtonSecondary(editor), true);
		this._frame.addItem(new ButtonToolPage(editor), true);
		this._frame.addItem(new ButtonToolDocument(editor), true);
		this._frame.addItem(new ButtonThickness(editor), true);
		this._frame.addItem(new ButtonToolScreenshot(editor, "screenshotAreaHide", "Area Screenshot", ButtonToolScreenshot.MODE_SELECT_RECTANGLE, true, false), true);
		this._frame.addItem(new ButtonToolBlank(editor, "blankScreen", "Blank", false), true);
		this._frame.addItemDummy(new ButtonToggleShowToolbar(editor));
		this._frame.addItemDummy(new ButtonToggleShowAutoToolbar(editor));
		this._frame.addItemDummy(new ButtonToolScreenshot(editor, "screenshotArea", "Area Screenshot", ButtonToolScreenshot.MODE_SELECT_RECTANGLE, false, false));
		this._frame.addItemDummy(new ButtonToolScreenshot(editor, "screenshotFull", "Screenshot", ButtonToolScreenshot.MODE_SELECT_RECTANGLE, false, true));
		this._frame.addItemDummy(new ButtonToolScreenshot(editor, "screenshotDirect", "Screenshot", ButtonToolScreenshot.MODE_CURRENT_SCREEN, false, true));
		this._frame.addItemDummy(new ButtonToolBlank(editor, "blankScreenAll", "Blank All", false));
		
		this._frame.addItemDummy(new ActionZoomPan(editor, "zoom", ActionZoomPan.MODE_ZOOM));
		this._frame.addItemDummy(new ActionZoomPan(editor, "pan", ActionZoomPan.MODE_PAN));
		this._frame.addItemDummy(new ActionZoomPan(editor, "resetView", ActionZoomPan.MODE_RESET));
	}
}
