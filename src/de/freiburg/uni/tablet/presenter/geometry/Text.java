package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.TextFont;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.RenderMetric;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public class Text extends AbstractRenderable {
	private DataPoint _location;
	
	private Rectangle2D.Float _rect = null;
	
	private String[] _textLines;
	private float[] _textLineWidths;
	
	private TextFont _font;
	
	/**
	 * Clone ctor
	 * @param parent
	 * @param location
	 * @param text
	 * @param font
	 * @param rect
	 */
	private Text(final DocumentPage parent, final DataPoint location, final String[] textLines, final TextFont font, final Rectangle2D.Float rect, final float[] textLineWidths) {
		super(parent);
		_location = location;
		_textLines = textLines;
		_textLineWidths = textLineWidths;
		_font = font;
		_rect = rect;
	}
	
	/**
	 * Creates a new text
	 * @param parent
	 * @param location
	 * @param text
	 * @param font
	 */
	public Text(final DocumentPage parent, final DataPoint location, final String[] textLines, final TextFont font) {
		super(parent);
		_location = location;
		_textLines = textLines;
		_font = font;
	}
	
	@Override
	synchronized public Text cloneRenderable(final DocumentPage page) {
		System.out.println("clone text " + getId());
		return new Text(page, _location.clone(), _textLines, _font, (_rect!=null?(Rectangle2D.Float) _rect.clone():null), (_textLineWidths != null?_textLineWidths.clone():null));
	}
	
	@Override
	synchronized public Text cloneRenderable(final DocumentPage page, final float offsetX, final float offsetY) {
		return new Text(page, _location.clone(offsetX, offsetY), _textLines, _font, (_rect!=null?(Rectangle2D.Float) _rect.clone():null), (_textLineWidths != null?_textLineWidths.clone():null));
	}
	
	@Override
	synchronized public boolean eraseStart(final EraseInfo eraseInfo) {
		if (eraseInfo.getCollisionInfo().isCheckOnlyBoundaries()) {
			return false;
		}
		return true;
	}

	@Override
	synchronized public boolean eraseAt(final EraseInfo eraseInfo) {
		return false;
	}
	
	@Override
	synchronized public boolean eraseEnd(final EraseInfo eraseInfo) {
		return false;
	}
	
	@Override
	public boolean collides(final CollisionInfo collisionInfo) {
		if (collisionInfo.isFastCollide()) {
			return collisionInfo.collides(getMinX(), getMinY(), getMaxX(), getMaxY());
		} else {
			for (int i = 0; i < _textLines.length; i++) {
				if (collisionInfo.collides(getX(), getMinY() + _font.getLineHeight() * i, getX() + _textLineWidths[i], getMinY() + _font.getLineHeight() * (i + 1))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
		renderer.draw(_location.getX(), _location.getY(), _textLines, _font);
	}
	
	@Override
	synchronized public void renderHighlighted(final IPageBackRenderer renderer) {
		renderer.draw(_location.getX(), _location.getY(), _textLines, _font);
		renderer.draw(BitmapImage.HIGHLIGHTED_PEN, new Path2D.Float(new Rectangle2D.Float(getMinX(), getMinY(), getMaxX() - getMinX(), getMaxY() - getMinY())));
	}
	
	/**
	 * Calculates the internal _rect and _textLineWidths
	 */
	private void calcDimensions() {
		if (_rect == null) {
			_textLineWidths = new float[_textLines.length];
			for (int i = 0; i < _textLines.length; i++) {
				Rectangle2D.Float measureText = _font.measureText(_textLines[i]);
				_textLineWidths[i] = measureText.width;
				measureText.y += _font.getLineHeight() * i;
				if (_rect == null) {
					_rect = measureText;
				} else {
					if (_rect.x > measureText.x) {
						_rect.width += _rect.x - measureText.x;
						_rect.x = _rect.x;
					}
					if (_rect.y > measureText.y) {
						_rect.height += _rect.y - measureText.y;
						_rect.y = _rect.y;
					}
					if (_rect.x + _rect.width < measureText.x + measureText.width) {
						_rect.width = measureText.x + measureText.width - _rect.x;
					}
					if (_rect.y + _rect.height < measureText.y + measureText.height) {
						_rect.height = measureText.y + measureText.height - _rect.y;
					}
				}
			}
		}
	}
	
	/**
	 * Gets the line number by x, y coordinates (of screen coordinates) for specifying the caret position
	 * @param x
	 * @param y
	 * @return index of the line or -1 if the position is invalid
	 */
	public int getCaretLine(final float x, final float y) {
		calcDimensions();
		float h = _font.getLineHeight();
		float y0 = y - getMinY();
		float x0 = x - getMinX();
		for (int i = 0; i < _textLines.length; i++) {
			if (y0 >= h * i && y0 < h * (i + 1)) {
				if (x0 > 0 && x0 < _textLineWidths[i]) {
					return i;
				}
				return -1;
			}
		}
		return -1;
	}
	
	/**
	 * Bakes the object, so it can be added to a page again after modification
	 */
	public void bake() {
		_rect = null;
		_textLineWidths = null;
	}
	
	/**
	 * Sets the location. This may only be used, if the object is not added to a page.
	 * @param location
	 */
	public void setLocation(final DataPoint location) {
		_location = location;
	}
	
	public float getX() {
		return _location.getX();
	}
	
	public float getY() {
		return _location.getY();
	}
	
	/**
	 * Sets the internal text (is split in lines)
	 * @param text
	 */
	public void setText(final String text) {
		_textLines = text.split("\\r?\\n|\\r", -1);
		_textLineWidths = null;
		_rect = null;
	}
	
	public String[] getTextLines() {
		return _textLines;
	}
	
	@Override
	public float getMinX() {
		calcDimensions();
		return getX() + _rect.x;
	}

	@Override
	public float getMinY() {
		calcDimensions();
		return getY() + _rect.y;
	}

	@Override
	public float getMaxX() {
		calcDimensions();
		return getX() + _rect.x + _rect.width;
	}

	@Override
	public float getMaxY() {
		calcDimensions();
		return getY() + _rect.y + _rect.height;
	}

	@Override
	public float getRadius(final RenderMetric metric) {
		return 0;
	}

	public Text(final BinaryDeserializer reader) throws IOException {
		super(reader);
		deserializeData(reader);
	}

	@Override
	synchronized public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		serializeData(writer);
	}
	
	@Override
	synchronized public void deserializeData(final BinaryDeserializer reader) throws IOException {
		_location = reader.readSerializableClass();
		if (reader.readBoolean()) {
			_rect = new Rectangle2D.Float();
			_rect.x = reader.readFloat();
			_rect.y = reader.readFloat();
			_rect.width = reader.readFloat();
			_rect.height = reader.readFloat();
			_textLineWidths = new float[reader.readInt()];
			for (int i = 0; i < _textLineWidths.length; i++) {
				_textLineWidths[i] = reader.readFloat();
			}
		}
		int linesCount = reader.readInt();
		_textLines = new String[linesCount];
		for (int i = 0; i < linesCount; i++) {
			_textLines[i] = reader.readString();
		}
		_font = reader.readObjectTable();
		_parent.fireRenderableModified(this);
		_parent.fireRenderableModifyEnd(this);
	}
	
	@Override
	synchronized public void serializeData(final BinarySerializer writer) throws IOException {
		writer.writeSerializableClass(_location);
		writer.writeBoolean(_rect != null);
		if (_rect != null) {
			writer.writeFloat(_rect.x);
			writer.writeFloat(_rect.y);
			writer.writeFloat(_rect.width);
			writer.writeFloat(_rect.height);
			writer.writeInt(_textLineWidths.length);
			for (int i = 0; i < _textLineWidths.length; i++) {
				writer.writeFloat(_textLineWidths[i]);
			}
		}
		writer.writeInt(_textLines.length);
		for (int i = 0; i < _textLines.length; i++) {
			writer.writeString(_textLines[i]);
		}
		writer.writeObjectTable(_font);
	}

	public TextFont getFont() {
		return _font;
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder(super.toString() + " \"");
		res.append(_textLines[0]);
		for (int i = 1; i < _textLines.length; i++) {
			res.append('\n');
			res.append(_textLines[i]);
		}
		res.append('"');
		return res.toString();
	}

	public DataPoint getLocation() {
		return _location;
	}

	/**
	 * Joins the lines to a string with newlines
	 * @return
	 */
	public String getText() {
		StringBuilder result = new StringBuilder();
		result.append(_textLines[0]);
		for (int i = 1; i < _textLines.length; i++) {
			result.append('\n');
			result.append(_textLines[i]);
		}
		return result.toString();
	}
}
