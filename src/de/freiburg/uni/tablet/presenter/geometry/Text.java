package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.TextFont;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public class Text extends AbstractRenderable {
	private DataPoint _location;
	
	private Rectangle2D.Float _rect = null;
	
	private String _text;
	
	private TextFont _font;
	
	/**
	 * Clone ctor
	 * @param parent
	 * @param location
	 * @param text
	 * @param font
	 * @param rect
	 */
	private Text(final DocumentPage parent, final DataPoint location, final String text, final TextFont font, final Rectangle2D.Float rect) {
		super(parent);
		_location = location;
		_text = text;
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
	public Text(final DocumentPage parent, final DataPoint location, final String text, final TextFont font) {
		super(parent);
		_location = location;
		_text = text;
		_font = font;
	}
	
	@Override
	synchronized public Text cloneRenderable(final DocumentPage page) {
		System.out.println("clone text " + getId());
		return new Text(page, _location.clone(), _text, _font, (Rectangle2D.Float) _rect.clone());
	}
	
	@Override
	synchronized public Text cloneRenderable(final DocumentPage page, final float offsetX, final float offsetY) {
		return new Text(page, _location.clone(offsetX, offsetY), _text, _font, (Rectangle2D.Float)_rect.clone());
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
		return collisionInfo.collides(getMinX(), getMinY(), getMaxX(), getMaxY());
	}

	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
		renderer.draw(_location.getX(), _location.getY(), _text, _font);
	}
	
	@Override
	synchronized public void renderHighlighted(final IPageBackRenderer renderer) {
		renderer.draw(_location.getX(), _location.getY(), _text, _font);
		renderer.draw(BitmapImage.HIGHLIGHTED_PEN, new Path2D.Float(new Rectangle2D.Float(getMinX(), getMinY(), getMaxX() - getMinX(), getMaxY() - getMinY())));
	}
	
	private void calcDimensions() {
		if (_rect == null) {
			_rect = _font.measureText(_text);
		}
	}
	
	public void bake() {
		_rect = null;
	}
	
	public void setLocation(final DataPoint location) {
		_location = location;
	}
	
	public float getX() {
		return _location.getX();
	}
	
	public float getY() {
		return _location.getY();
	}
	
	public void setText(final String text) {
		_text = text;
	}
	
	public String getText() {
		return _text;
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
	public float getRadius() {
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
		}
		_text = reader.readString();
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
		}
		writer.writeString(_text);
		writer.writeObjectTable(_font);
	}

	public TextFont getFont() {
		return _font;
	}
	
	@Override
	public String toString() {
		return super.toString() + " \"" + _text + "\"";
	}

	public DataPoint getLocation() {
		return _location;
	}
}
