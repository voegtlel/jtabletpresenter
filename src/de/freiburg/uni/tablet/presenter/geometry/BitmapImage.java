package de.freiburg.uni.tablet.presenter.geometry;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public class BitmapImage extends AbstractRenderable {
	private final static int DATA_ID_GRAPHICS2D = 0;
	private final static int DATA_ID_ELLIPSE = 1;
	
	private float _x;
	private float _y;
	
	private float _width;
	private float _height;
	
	private byte[] _fileData;
	private BufferedImage _image;

	public BitmapImage(final long id, final File file, float x, float y, float width, float height) {
		super(id);
		_x = x;
		_y = y;
		_width = width;
		_height = height;
		try {
			_fileData = readFile(file);
			_image = readImage(_fileData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BitmapImage(final long id, final byte[] fileData, BufferedImage image, float x, float y, float width, float height) {
		super(id);
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
	
	public static byte[] readFile(final File file) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		byte[] data = new byte[(int) raf.getChannel().size()];
		raf.readFully(data);
		raf.close();
		System.out.println("Read file " + file);
		return data;
	}
	
	public static BufferedImage readImage(final byte[] data) throws IOException {
		BufferedImage input = ImageIO.read(new ByteArrayInputStream(data));
		int w = input.getWidth();
		int h = input.getHeight();
		System.out.println("Read image " + w + "x" + h);
		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = result.getGraphics();
		g.drawImage(input, 0, 0, w, h, 0, 0, w, h, null);
		g.dispose();
		return result;
	}

	@Override
	public IRenderable cloneRenderable(final long id) {
		System.out.println("clone image " + getId() + " -> " + id);
		ColorModel m = _image.getColorModel();
		WritableRaster wr = _image.copyData(null);
		BufferedImage clone = new BufferedImage(m, wr, m.isAlphaPremultiplied(), null);
		return new BitmapImage(id, _fileData, clone, _x, _y, _width, _height);
	}

	@Override
	public void eraseAt(final EraseInfo eraseInfo) {
		BitmapImage modifiedObjectInstance = (BitmapImage) eraseInfo
				.getModifiedObject(this);
		if (modifiedObjectInstance == null) {
			if (collides(eraseInfo.getCollisionInfo())) {
				modifiedObjectInstance = (BitmapImage) eraseInfo
						.addModifiedObject(this);
				if (!eraseInfo.getCollisionInfo().isCheckOnlyBoundaries()) {
					Ellipse2D.Float ellipse = new Ellipse2D.Float();
					Graphics2D graphics = (Graphics2D)modifiedObjectInstance._image.createGraphics();
					graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
					graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					graphics.setComposite(AlphaComposite.Clear);
					graphics.setColor(new Color(0, 0, 0, 0));
					eraseInfo.addObjectData(modifiedObjectInstance, DATA_ID_GRAPHICS2D, graphics);
					eraseInfo.addObjectData(modifiedObjectInstance, DATA_ID_ELLIPSE, ellipse);
				}
			}
		}

		if (modifiedObjectInstance != null && !eraseInfo.getCollisionInfo().isCheckOnlyBoundaries()) {
			modifiedObjectInstance.eraseAtDirect(eraseInfo.getCollisionInfo(),
					(Graphics2D)eraseInfo.getObjectData(modifiedObjectInstance, DATA_ID_GRAPHICS2D),
					(Ellipse2D.Float)eraseInfo.getObjectData(modifiedObjectInstance, DATA_ID_ELLIPSE));
		}
	}

	public void eraseAtDirect(final CollisionInfo collisionInfo, final Graphics2D graphics, final Ellipse2D.Float ellipse) {
		if (collisionInfo.isCheckOnlyBoundaries()) {
			return;
		}
		if (collides(collisionInfo)) {
			ellipse.x = collisionInfo.getCenterX() - collisionInfo.getRadiusX() - _x;
			ellipse.y = collisionInfo.getCenterY() - collisionInfo.getRadiusY() - _y;
			ellipse.width = collisionInfo.getRadiusX() * 2f;
			ellipse.height = collisionInfo.getRadiusY() * 2f;
			ellipse.x *= (float)_image.getWidth() / _width;
			ellipse.y *= (float)_image.getHeight() / _height;
			ellipse.width *= (float)_image.getWidth() / _width;
			ellipse.height *= (float)_image.getHeight() / _height;
			System.out.println("Ellipse: " + ellipse.x + ", " + ellipse.y + ", " + ellipse.width + "x" + ellipse.height);
			graphics.fill(ellipse);
		}
	}
	
	@Override
	public boolean eraseEnd(final EraseInfo eraseInfo) {
		if (eraseInfo.getCollisionInfo().isCheckOnlyBoundaries()) {
			return false;
		}
		BitmapImage modifiedObjectInstance = (BitmapImage) eraseInfo
				.getModifiedObject(this);
		if (modifiedObjectInstance != null) {
			Graphics2D graphics = eraseInfo.getObjectData(modifiedObjectInstance, DATA_ID_GRAPHICS2D);
			graphics.dispose();
			Rectangle newRect = new Rectangle();
			BufferedImage reducedImage = BitmapHelper.reduceImage(modifiedObjectInstance._image, newRect);
			if (reducedImage == null) {
				System.out.println("Full Reduced");
				return false;
			}
			modifiedObjectInstance._image = reducedImage;
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(reducedImage, "PNG", bos);
				modifiedObjectInstance._fileData = bos.toByteArray();
				int origWidth = _image.getWidth();
				int origHeight = _image.getHeight();
				modifiedObjectInstance._x = _x + _width * (float)newRect.x / (float)origWidth;
				modifiedObjectInstance._y = _y + _height * (float)newRect.y / (float)origHeight;
				modifiedObjectInstance._width = _width * (float)newRect.width / (float)origWidth;
				modifiedObjectInstance._height = _height * (float)newRect.height / (float)origHeight;
				System.out.println("Stored new image data for " + modifiedObjectInstance._x + ", " + modifiedObjectInstance._y + ", " + modifiedObjectInstance._width + "x" + modifiedObjectInstance._height);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			System.out.println("End Empty");
		}
		System.out.println("End Successfull");
		return true;
	}
	
	public void setLocation(float x, float y) {
		_x = x;
		_y = y;
	}
	
	public void setSize(float width, float height) {
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
	public boolean collides(final CollisionInfo collisionInfo) {
		return collisionInfo.collides(_x, _y, _x + _width, _y + _height);
	}

	@Override
	public void render(final IPageBackRenderer renderer) {
		renderer.draw(_image, _x, _y, _width, _height);
	}

	public BitmapImage(final BinaryDeserializer reader) throws IOException {
		super(reader);
		_x = reader.readFloat();
		_y = reader.readFloat();
		_width = reader.readFloat();
		_height = reader.readFloat();
		_fileData = reader.readByteArray();
		_image = readImage(_fileData);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeFloat(_x);
		writer.writeFloat(_y);
		writer.writeFloat(_width);
		writer.writeFloat(_height);
		writer.writeByteArray(_fileData, 0, _fileData.length);
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
}
