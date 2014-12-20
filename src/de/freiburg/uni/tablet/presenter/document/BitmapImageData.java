package de.freiburg.uni.tablet.presenter.document;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;

/**
 * Class representing image data
 *
 */
public class BitmapImageData extends AbstractEntity {
	private byte[] _fileData;
	private BufferedImage _image;

	public BitmapImageData(final IDocument parent, final File file) throws IOException {
		this(parent, FileHelper.readFile(file), null);
	}
	
	public BitmapImageData(final IDocument parent, final byte[] fileData, final BufferedImage image) throws IOException {
		super(parent);
		_fileData = fileData;
		if (image == null) {
			_image = readImage(fileData);
		} else {
			_image = image;
		}
	}
	
	public BitmapImageData(final IDocument parent, final BufferedImage image) {
		super(parent);
		_fileData = null;
		_image = image;
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

	synchronized public BitmapImageData clone(final IDocument newDocument) {
		System.out.println("clone image " + getId());
		final ColorModel m = _image.getColorModel();
		final WritableRaster wr = _image.copyData(null);
		BufferedImage clone = new BufferedImage(m, wr, m.isAlphaPremultiplied(), null);
		try {
			return new BitmapImageData(newDocument, _fileData, clone);
		} catch (IOException e) {
			throw new IllegalStateException("Can't clone image", e);
		}
	}
	
	public Graphics2D createGraphics() {
		// We are going to draw -> no file storage
		_fileData = null;
		return _image.createGraphics();
	}
	
	public int getWidth() {
		return _image.getWidth();
	}
	
	public int getHeight() {
		return _image.getHeight();
	}
	
	public BufferedImage getImage() {
		return _image;
	}
	
	/**
	 * Reduces the image size by finding empty borders (with a margin of 1) and returns the new data.
	 * Returns the modified image or this image if nothing was modified.
	 * @param image
	 * @param newRect the rectangle of the filled image area in the given image
	 * @return the new reduced image or null if the image is empty
	 */
	public BitmapImageData reduceImage(final Rectangle newRect) {
		int width = _image.getWidth();
		int height = _image.getHeight();
		newRect.setBounds(0, 0, width, height);
		int[] pixelData = _image.getRGB(0, 0, width, height, null, 0, width);
		// Check left empty columns
		boolean isFirst = false;
		for (int iX = 0; iX < width; iX++) {
			boolean allEmpty = true;
			for (int iY = 0; iY < height; iY++) {
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				if (isFirst) {
					isFirst = false;
				} else {
					newRect.x++;
					newRect.width--;
				}
			} else {
				break;
			}
		}
		if (newRect.width <= 0) {
			return null;
		}
		isFirst = true;
		for (int iX = width - 1; iX >= newRect.x; iX--) {
			boolean allEmpty = true;
			for (int iY = 0; iY < height; iY++) {
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				if (isFirst) {
					isFirst = false;
				} else {
					newRect.width--;
				}
			} else {
				break;
			}
		}
		int newRight = newRect.x + newRect.width;
		isFirst = true;
		for (int iY = 0; iY < height; iY++) {
			boolean allEmpty = true;
			for (int iX = newRect.x; iX < newRight; iX++) {
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				if (isFirst) {
					isFirst = false;
				} else {
					newRect.y++;
					newRect.height--;
				}
			} else {
				break;
			}
		}
		isFirst = true;
		for (int iY = height - 1; iY >= newRect.y; iY--) {
			boolean allEmpty = true;
			for (int iX = newRect.x; iX < newRight; iX++) {
				if ((pixelData[iY * width + iX] < 0) || (pixelData[iY * width + iX] > 0x05000000)) {
					allEmpty = false;
					break;
				}
			}
			if (allEmpty) {
				if (isFirst) {
					isFirst = false;
				} else {
					newRect.height--;
				}
			} else {
				break;
			}
		}
		if (newRect.x == 0 && newRect.y == 0 && newRect.width == width && newRect.height == height) {
			System.out.println("no reduction");
			return this;
		}
		if (newRect.width <= 0 || newRect.height <= 0) {
			return null;
		}
		BufferedImage result = new BufferedImage(newRect.width, newRect.height, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = result.getGraphics();
		graphics.drawImage(_image, 0, 0, newRect.width, newRect.height, newRect.x, newRect.y, newRect.x + newRect.width, newRect.y + newRect.height, null);
		graphics.dispose();
		System.out.println("Reduced image from " + width + "x" + height + " to " + newRect);
		return new BitmapImageData(getParent(), result);
	}
	
	public BitmapImageData(final BinaryDeserializer reader) throws IOException {
		super(reader);
		_fileData = reader.readByteArray();
		_image = readImage(_fileData);
	}

	@Override
	synchronized public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		if (_fileData == null) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				if (!ImageIO.write(_image, "png", os)) {
					throw new IllegalStateException("No png writer");
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			_fileData = os.toByteArray();
		}
		writer.writeByteArray(_fileData, 0, _fileData.length);
	}
}
