package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class BitmapImage extends AbstractRenderable {
	private final static int DATA_ID_GRAPHICS2D = 0;
	private final static int DATA_ID_ELLIPSE = 1;
	
	final static IPen HIGHLIGHTED_PEN = new SolidPen(2.0f, Color.yellow);
	
	private float _x;
	private float _y;
	
	private float _width;
	private float _height;
	
	private byte[] _fileData;
	private BufferedImage _image;

	public BitmapImage(final DocumentPage parent, final File file, final float x, final float y, final float width, final float height) throws IOException {
		this(parent, FileHelper.readFile(file), null, x, y, width, height);
	}
	
	public BitmapImage(final DocumentPage parent, final byte[] fileData, final BufferedImage image, final float x, final float y, final float width, final float height) {
		super(parent);
		_fileData = fileData;
		_x = x;
		_y = y;
		_width = width;
		_height = height;
		if (image == null) {
			try {
				_image = readImage(fileData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			_image = image;
		}
	}
	
	public static BufferedImage readImage(final byte[] data) throws IOException {
		final BufferedImage input = ImageIO.read(new ByteArrayInputStream(data));
		int w = input.getWidth();
		int h = input.getHeight();
		System.out.println("Read image " + w + "x" + h);
		final BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		final Graphics g = result.getGraphics();
		g.drawImage(input, 0, 0, w, h, 0, 0, w, h, null);
		g.dispose();
		return result;
	}

	@Override
	synchronized public BitmapImage cloneRenderable(final DocumentPage page) {
		System.out.println("clone image " + getId());
		final ColorModel m = _image.getColorModel();
		final WritableRaster wr = _image.copyData(null);
		final BufferedImage clone = new BufferedImage(m, wr, m.isAlphaPremultiplied(), null);
		return new BitmapImage(page, _fileData, clone, _x, _y, _width, _height);
	}
	
	@Override
	synchronized public BitmapImage cloneRenderable(final DocumentPage page, final float offsetX, final float offsetY) {
		System.out.println("clone image " + getId());
		final ColorModel m = _image.getColorModel();
		final WritableRaster wr = _image.copyData(null);
		final BufferedImage clone = new BufferedImage(m, wr, m.isAlphaPremultiplied(), null);
		return new BitmapImage(page, _fileData, clone, _x + offsetX, _y + offsetY, _width, _height);
	}
	
	@Override
	synchronized public boolean eraseStart(final EraseInfo eraseInfo) {
		if (eraseInfo.getCollisionInfo().isCheckOnlyBoundaries()) {
			return false;
		}
		final Ellipse2D.Float ellipse = new Ellipse2D.Float();
		final Graphics2D graphics = _image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setComposite(AlphaComposite.Clear);
		graphics.setColor(new Color(0, 0, 0, 0));
		eraseInfo.addObjectData(this, DATA_ID_GRAPHICS2D, graphics);
		eraseInfo.addObjectData(this, DATA_ID_ELLIPSE, ellipse);
		return true;
	}

	@Override
	synchronized public boolean eraseAt(final EraseInfo eraseInfo) {
		final Graphics2D g = (Graphics2D)eraseInfo.getObjectData(this, DATA_ID_GRAPHICS2D);
		final Ellipse2D.Float ellipse = (Ellipse2D.Float)eraseInfo.getObjectData(this, DATA_ID_ELLIPSE);
		final CollisionInfo collisionInfo = eraseInfo.getCollisionInfo();
		
		if (collides(collisionInfo)) {
			_parent.fireRenderableModified(this);
			ellipse.x = collisionInfo.getCenterX() - collisionInfo.getRadiusX() - _x;
			ellipse.y = collisionInfo.getCenterY() - collisionInfo.getRadiusY() - _y;
			ellipse.width = collisionInfo.getRadiusX() * 2f;
			ellipse.height = collisionInfo.getRadiusY() * 2f;
			ellipse.x *= _image.getWidth() / _width;
			ellipse.y *= _image.getHeight() / _height;
			ellipse.width *= _image.getWidth() / _width;
			ellipse.height *= _image.getHeight() / _height;
			System.out.println("Clear ellipse: " + ellipse.x + ", " + ellipse.y + ", " + ellipse.width + "x" + ellipse.height);
			g.fill(ellipse);
			_parent.fireRenderableModified(this);
		}
		return true;
	}
	
	@Override
	synchronized public boolean eraseEnd(final EraseInfo eraseInfo) {
		final Graphics2D graphics = eraseInfo.getObjectData(this, DATA_ID_GRAPHICS2D);
		graphics.dispose();
		final Rectangle newRect = new Rectangle();
		final BufferedImage reducedImage = BitmapHelper.reduceImage(_image, newRect);
		if (reducedImage == null) {
			System.out.println("Full Reduced");
			return false;
		}
		if (_image != reducedImage) {
			_parent.fireRenderableModified(this);
			_image = reducedImage;
			try {
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(reducedImage, "PNG", bos);
				_fileData = bos.toByteArray();
				final int origWidth = _image.getWidth();
				final int origHeight = _image.getHeight();
				_x = _x + _width * newRect.x / origWidth;
				_y = _y + _height * newRect.y / origHeight;
				_width = _width * newRect.width / origWidth;
				_height = _height * newRect.height / origHeight;
				System.out.println("Stored new image data for " + _x + ", " + _y + ", " + _width + "x" + _height);
				_parent.fireRenderableModified(this);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		_parent.fireRenderableModifyEnd(this);
		System.out.println("End Successfull");
		return true;
	}
	
	@Override
	public boolean collides(final CollisionInfo collisionInfo) {
		return collisionInfo.collides(_x, _y, _x + _width, _y + _height);
	}

	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
		renderer.draw(_image, _x, _y, _width, _height);
	}
	
	@Override
	synchronized public void renderHighlighted(final IPageBackRenderer renderer) {
		renderer.draw(_image, _x, _y, _width, _height);
		renderer.draw(HIGHLIGHTED_PEN, new Path2D.Float(new Rectangle2D.Float(_x, _y, _width, _height)));
	}
	
	public void setLocation(final float x, final float y) {
		_x = x;
		_y = y;
	}
	
	public void setSize(final float width, final float height) {
		_width = width;
		_height = height;
	}
	
	public BufferedImage getImage() {
		return _image;
	}
	
	public float getX() {
		return _x;
	}
	
	public float getY() {
		return _y;
	}
	
	@Override
	public float getMinX() {
		return Math.min(_x, _x + _width);
	}

	@Override
	public float getMinY() {
		return Math.min(_y, _y + _height);
	}

	@Override
	public float getMaxX() {
		return Math.max(_x, _x + _width);
	}

	@Override
	public float getMaxY() {
		return Math.max(_y, _y + _height);
	}

	@Override
	public float getRadius() {
		return 0;
	}

	public BitmapImage(final BinaryDeserializer reader) throws IOException {
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
		_width = reader.readFloat();
		_height = reader.readFloat();
		_fileData = reader.readByteArray();
		_image = readImage(_fileData);
		_parent.fireRenderableModified(this);
		_parent.fireRenderableModifyEnd(this);
	}
	
	@Override
	synchronized public void serializeData(final BinarySerializer writer) throws IOException {
		writer.writeFloat(_x);
		writer.writeFloat(_y);
		writer.writeFloat(_width);
		writer.writeFloat(_height);
		writer.writeByteArray(_fileData, 0, _fileData.length);
	}
}
