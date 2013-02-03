package de.freiburg.uni.tablet.presenter.geometry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public class BitmapImage extends AbstractRenderable {
	private final static int DATA_ID_GRAPHICS2D = 0;
	private final static int DATA_ID_PAINT = 1;
	
	private float _x;
	private float _y;
	
	private float _width;
	private float _height;
	
	private byte[] _fileData;
	private Bitmap _image;

	public BitmapImage(final DocumentPage parent, final File file, float x, float y, float width, float height) throws IOException {
		this(parent, readFile(file), null, x, y, width, height);
	}
	
	public BitmapImage(final DocumentPage parent, final byte[] fileData, Bitmap image, float x, float y, float width, float height) {
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
	
	public static byte[] readFile(final File file) throws IOException {
		final RandomAccessFile raf = new RandomAccessFile(file, "r");
		final byte[] data = new byte[(int) raf.getChannel().size()];
		raf.readFully(data);
		raf.close();
		System.out.println("Read file " + file);
		return data;
	}
	
	public static Bitmap readImage(final byte[] data) throws IOException {
		final Bitmap input = BitmapFactory.decodeByteArray(data, 0, data.length);
		int w = input.getWidth();
		int h = input.getHeight();
		System.out.println("Read image " + w + "x" + h);
		final Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		final Canvas g = new Canvas(result);
		g.drawBitmap(input, 0, 0, null);
		return result;
	}

	@Override
	synchronized public BitmapImage cloneRenderable(final DocumentPage page) {
		System.out.println("clone image " + getId());
		final Bitmap clone = _image.copy(Bitmap.Config.ARGB_8888, true);
		return new BitmapImage(page, _fileData, clone, _x, _y, _width, _height);
	}
	
	@Override
	synchronized public boolean eraseStart(final EraseInfo eraseInfo) {
		if (eraseInfo.getCollisionInfo().isCheckOnlyBoundaries()) {
			return false;
		}
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		paint.setColor(0);
		paint.setStyle(Paint.Style.FILL);
		final Canvas canvas = new Canvas(_image);
		eraseInfo.addObjectData(this, DATA_ID_GRAPHICS2D, canvas);
		eraseInfo.addObjectData(this, DATA_ID_PAINT, paint);
		return true;
	}

	@Override
	synchronized public boolean eraseAt(final EraseInfo eraseInfo) {
		final Canvas g = (Canvas)eraseInfo.getObjectData(this, DATA_ID_GRAPHICS2D);
		final Paint paint = (Paint)eraseInfo.getObjectData(this, DATA_ID_PAINT);
		final CollisionInfo collisionInfo = eraseInfo.getCollisionInfo();
		
		if (collides(collisionInfo)) {
			_parent.fireRenderableModified(this);
			RectF rect = new RectF();
			rect.left = collisionInfo.getCenterX() - collisionInfo.getRadiusX() - _x;
			rect.top = collisionInfo.getCenterY() - collisionInfo.getRadiusY() - _y;
			rect.right = rect.left + collisionInfo.getRadiusX() * 2f;
			rect.bottom = rect.top + collisionInfo.getRadiusY() * 2f;
			rect.left *= (float)_image.getWidth() / _width;
			rect.top *= (float)_image.getHeight() / _height;
			rect.right *= (float)_image.getWidth() / _width;
			rect.bottom *= (float)_image.getHeight() / _height;
			g.drawOval(rect, paint);
			_parent.fireRenderableModified(this);
		}
		return true;
	}
	
	@Override
	synchronized public boolean eraseEnd(final EraseInfo eraseInfo) {
		final Rect newRect = new Rect();
		final Bitmap reducedImage = BitmapHelper.reduceImage(_image, newRect);
		if (reducedImage == null) {
			System.out.println("Full Reduced");
			return false;
		}
		if (_image != reducedImage) {
			_parent.fireRenderableModified(this);
			_image = reducedImage;
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			reducedImage.compress(CompressFormat.PNG, 90, bos);
			_fileData = bos.toByteArray();
			final int origWidth = _image.getWidth();
			final int origHeight = _image.getHeight();
			_x = _x + _width * (float)newRect.left / (float)origWidth;
			_y = _y + _height * (float)newRect.top / (float)origHeight;
			_width = _width * (float)(newRect.right - newRect.left) / (float)origWidth;
			_height = _height * (float)(newRect.bottom - newRect.top) / (float)origHeight;
			System.out.println("Stored new image data for " + _x + ", " + _y + ", " + _width + "x" + _height);
			_parent.fireRenderableModified(this);
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
	
	public void setLocation(float x, float y) {
		_x = x;
		_y = y;
	}
	
	public void setSize(float width, float height) {
		_width = width;
		_height = height;
	}
	
	public Bitmap getImage() {
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
