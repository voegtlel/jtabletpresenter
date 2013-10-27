package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
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
	
	private boolean _extendsValid = false;
	private float _width;
	private float _height;
	
	private String _text;
	
	private TextFont _font;
	
	/**
	 * Clone ctor
	 * @param parent
	 * @param x
	 * @param y
	 * @param text
	 * @param font
	 * @param extendsValid
	 * @param width
	 * @param height
	 */
	private Text(final DocumentPage parent, final float x, final float y, final String text, final TextFont font, final boolean extendsValid, final float width, final float height) {
		super(parent);
		_x = x;
		_y = y;
		_text = text;
		_font = font;
		_extendsValid = extendsValid;
		_width = width;
		_height = height;
	}
	
	public Text(final DocumentPage parent, final float x, final float y, final String text, final TextFont font) {
		super(parent);
		_x = x;
		_y = y;
		_text = text;
		_font = font;
	}
	
	@Override
	synchronized public Text cloneRenderable(final DocumentPage page) {
		System.out.println("clone image " + getId());
		return new Text(page, _x, _y, _text, _font, _extendsValid, _width, _height);
	}
	
	@Override
	synchronized public Text cloneRenderable(final DocumentPage page, final float offsetX, final float offsetY) {
		return new Text(page, _x + offsetX, _y + offsetY, _text, _font, _extendsValid, _width, _height);
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
		final Point2D.Float size = _font.measureText(_text);
		return collisionInfo.collides(_x, _y, _x + size.x, _y + size.y);
	}

	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
		renderer.draw(_x, _y, _text, _font);
	}
	
	@Override
	synchronized public void renderHighlighted(final IPageBackRenderer renderer) {
		renderer.draw(_x, _y, _text, _font);
		renderer.draw(BitmapImage.HIGHLIGHTED_PEN, new Path2D.Float(new Rectangle2D.Float(_x, _y, _width, _height)));
	}
	
	private void calcDimensions() {
		if (!_extendsValid) {
			_extendsValid = true;
			final Point2D.Float size = _font.measureText(_text);
			_width = size.x;
			_height = size.y;
		}
	}
	
	public float getWidth() {
		calcDimensions();
		return _width;
	}
	
	public float getHeight() {
		calcDimensions();
		return _height;
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
		return _x;
	}

	@Override
	public float getMinY() {
		return _y - getHeight();
	}

	@Override
	public float getMaxX() {
		return _x + getWidth();
	}

	@Override
	public float getMaxY() {
		return _y;
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
		_extendsValid = reader.readBoolean();
		_width = reader.readFloat();
		_height = reader.readFloat();
		_text = reader.readString();
		_font = reader.readObjectTable();
		_parent.fireRenderableModified(this);
		_parent.fireRenderableModifyEnd(this);
	}
	
	@Override
	synchronized public void serializeData(final BinarySerializer writer) throws IOException {
		writer.writeFloat(_x);
		writer.writeFloat(_y);
		writer.writeBoolean(_extendsValid);
		writer.writeFloat(_width);
		writer.writeFloat(_height);
		writer.writeString(_text);
		writer.writeObjectTable(_font);
	}
}
