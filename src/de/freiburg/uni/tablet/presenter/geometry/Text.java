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
	private float _x;
	private float _y;
	
	private Rectangle2D.Float _rect = null;
	
	private String _text;
	
	private TextFont _font;
	
	/**
	 * Clone ctor
	 * @param parent
	 * @param x
	 * @param y
	 * @param text
	 * @param font
	 * @param rect
	 */
	private Text(final DocumentPage parent, final float x, final float y, final String text, final TextFont font, final Rectangle2D.Float rect) {
		super(parent);
		_x = x;
		_y = y;
		_text = text;
		_font = font;
		_rect = rect;
	}
	
	/**
	 * Creates a new text
	 * @param parent
	 * @param x
	 * @param y
	 * @param text
	 * @param font
	 */
	public Text(final DocumentPage parent, final float x, final float y, final String text, final TextFont font) {
		super(parent);
		_x = x;
		_y = y;
		_text = text;
		_font = font;
	}
	
	@Override
	synchronized public Text cloneRenderable(final DocumentPage page) {
		System.out.println("clone text " + getId());
		return new Text(page, _x, _y, _text, _font, (Rectangle2D.Float) _rect.clone());
	}
	
	@Override
	synchronized public Text cloneRenderable(final DocumentPage page, final float offsetX, final float offsetY) {
		return new Text(page, _x + offsetX, _y + offsetY, _text, _font, (Rectangle2D.Float)_rect.clone());
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
		renderer.draw(_x, _y, _text, _font);
	}
	
	@Override
	synchronized public void renderHighlighted(final IPageBackRenderer renderer) {
		renderer.draw(_x, _y, _text, _font);
		renderer.draw(BitmapImage.HIGHLIGHTED_PEN, new Path2D.Float(new Rectangle2D.Float(getMinX(), getMinY(), getMaxX() - getMinX(), getMaxY() - getMinY())));
	}
	
	private void calcDimensions() {
		if (_rect == null) {
			_rect = _font.measureText(_text);
		}
	}
	
	public void setLocation(final float x, final float y) {
		_x = x;
		_y = y;
	}
	
	public float getX() {
		return _x;
	}
	
	public float getY() {
		return _y;
	}
	
	@Override
	public float getMinX() {
		calcDimensions();
		return _x + _rect.x;
	}

	@Override
	public float getMinY() {
		calcDimensions();
		return _y - (_rect.y + _rect.height);
	}

	@Override
	public float getMaxX() {
		calcDimensions();
		return _x + _rect.x + _rect.width;
	}

	@Override
	public float getMaxY() {
		calcDimensions();
		return _y - _rect.y;
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
		_x = reader.readFloat();
		_y = reader.readFloat();
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
		writer.writeFloat(_x);
		writer.writeFloat(_y);
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
}
