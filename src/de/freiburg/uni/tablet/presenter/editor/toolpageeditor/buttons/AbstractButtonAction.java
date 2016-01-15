/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.IButtonAction;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageEditor;

/**
 * @author lukas
 * 
 */
public abstract class AbstractButtonAction extends AbstractAction implements IButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String _name;
	
	protected final IToolPageEditor _editor;
	private final String _text;
	private final String _imageResource;
	private BufferedImage _lastImage;
	
	private long _lastTimestamp = 0;

	/**
	 * Creates the action with an editor.
	 */
	public AbstractButtonAction(final String name, final IToolPageEditor editor,
			final String text, final String imageResource) {
		_name = name;
		_editor = editor;
		_text = text;
		_imageResource = imageResource;
	}
	
	public String getName() {
		return _name;
	}

	@Override
	public String getText() {
		return _text;
	}

	@Override
	public String getImageResource() {
		return _imageResource;
	}

	@Override
	public Image getImageResource(int width, int height) {
		if (_lastImage == null || _lastImage.getWidth() != width || _lastImage.getHeight() != height) {
			_lastImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			URI svgUri = SVGCache.getSVGUniverse().loadSVG(JPageEditor.class.getResource(_imageResource + ".svg"));
			SVGIcon icon = new SVGIcon();
			icon.setSvgURI(svgUri);
			Graphics2D iconGraphics = _lastImage.createGraphics();
			icon.setPreferredSize(new Dimension(width, height));
			icon.setAntiAlias(true);
			icon.setScaleToFit(true);
			icon.paintIcon(null, iconGraphics, 0, 0);
		}
		return _lastImage;
	}

	@Override
	public Component getControl() {
		return null;
	}

	@Override
	public void perform(final Point desiredLocation) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				performLater(_editor.getPageEditor().getContainerComponent());
			}
		});
	}
	
	public void performLater(final Component component) {
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		if (_lastTimestamp + 50 < System.currentTimeMillis()) {
			// Maximum of 20 per second
			return;
		}
		_lastTimestamp = System.currentTimeMillis();
		final Component c = (Component)e.getSource();
		final Point loc = c.getLocation();
		loc.x += c.getWidth();
		perform(loc);
	}
	
	@Override
	public IButtonAction getButton(final String name) {
		if (name.equals(this._name)) {
			return this;
		}
		return null;
	}
	
	@Override
	public void dispose() {
	}
}
