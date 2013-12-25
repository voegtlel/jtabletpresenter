package de.freiburg.uni.tablet.presenter.document;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.cos.COSObject;
import de.intarsys.pdf.encoding.WinAnsiEncoding;
import de.intarsys.pdf.font.PDFont;
import de.intarsys.pdf.font.PDFontTools;
import de.intarsys.pdf.font.PDFontType1;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.parser.CSContentParser;
import de.intarsys.pdf.parser.PDFParser;
import de.intarsys.pdf.writer.COSWriter;
import de.intarsys.tools.randomaccess.RandomAccessByteArray;

/**
 * Class for a font
 * @author Lukas
 *
 */
public class TextFont implements IEntity {
	private final long _id;
	private final IDocument _parent;

	/**
	 * The internal font
	 */
	private PDFont _font;
	private float _size;
	
	/**
	 * A temporary buffer for faster methods
	 */
	private ByteArrayOutputStream _bosBuffer = new ByteArrayOutputStream();

	/**
	 * Creates the font by a name and size
	 * TODO by name
	 * @param parent
	 * @param name
	 * @param size
	 */
	public TextFont(final IDocument parent, final String name, final float size) {
		_id = parent.nextId();
		_parent = parent;

		_font = PDFontType1.createNew(PDFontType1.FONT_Helvetica);
		_font.setEncoding(WinAnsiEncoding.UNIQUE);

		_size = size;
	}

	/**
	 * Deserializer ctor
	 * @param reader
	 * @throws IOException
	 */
	public TextFont(final BinaryDeserializer reader) throws IOException {
		_id = reader.readLong();
		_parent = reader.readObjectTable();
		_parent.deserializeId(_id);
		final boolean fontAvailable = reader.readBoolean();
		if (fontAvailable) {
			final byte[] data = reader.readByteArray();
			final PDFParser p = new CSContentParser();
			RandomAccessByteArray dataBuffer = new RandomAccessByteArray(data);
			COSObject co;
			try {
				co = (COSObject) p.parseElement(dataBuffer);
				_font = (PDFont) PDFont.META.createFromCos(co);
			} catch (COSLoadException e) {
				e.printStackTrace();
				_font = null;
			}
		} else {
			_font = null;
		}
		_size = reader.readFloat();
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeLong(_id);
		writer.writeObjectTable(_parent);

		if (_font != null) {
			writer.writeBoolean(true);
			RandomAccessByteArray dataBuffer = new RandomAccessByteArray(null);
			COSWriter w = new COSWriter(dataBuffer, null);
			w.writeObject(_font.cosGetObject());
			byte[] data = dataBuffer.toByteArray();
			writer.writeByteArray(data, 0, data.length);
		} else {
			writer.writeBoolean(false);
		}
		writer.writeFloat(_size);
	}

	@Override
	public long getId() {
		return _id;
	}

	@Override
	public IDocument getParent() {
		return _parent;
	}
	
	/**
	 * Encodes the given text to bytes used by the internal font engine
	 * @param text
	 * @return
	 */
	public byte[] getEncodedTextData(final String text) {
		return _font.getEncoding().encode(text.toCharArray(),
				0, text.length());
	}
	
	/**
	 * Encodes the char to bytes (optimized)
	 * @param c
	 * @return
	 */
	public byte[] getEncodedTextData(final char c) {
		_bosBuffer.reset();
		try {
			_font.getEncoding().putNextDecoded(_bosBuffer, c);
		} catch (IOException e) {
			// this does not happen
		}
		return _bosBuffer.toByteArray();
	}
	
	/**
	 * Transforms from pdf-Space to local space (and verce-vice)
	 * @param x
	 * @param y
	 * @param rect
	 */
	public void measureToLocalSpace(final float x, final float y, final Rectangle2D.Float rect) {
		rect.x += x;
		rect.y = y - (rect.y + rect.height);
	}

	/**
	 * Measures the text size. x is always 0. y and height are always constant for a font.
	 * The width is calculated for the given text.
	 * Does not process newlines.
	 * @param text
	 * @return
	 */
	public Rectangle2D.Float measureText(final String text) {
		final byte[] data = getEncodedTextData(text);
		final float sWidth = PDFontTools.getGlyphWidthEncodedScaled(_font, _size, data, 0, data.length);
		CDSRectangle rect = _font.getFontDescriptor().getFontBB();
		return new Rectangle2D.Float(0, -(_size * rect.getUpperRightY()) / 1000f, sWidth, (_size * (rect.getUpperRightY() - rect.getLowerLeftY())) / 1000f);
	}
	
	/**
	 * Measures the given text range, starting at beginIndex and ending at endIndex.
	 * x is the starting of the text at beginIndex. y and height are always constant for a font.
	 * Does not process newlines.
	 * @param text
	 * @param beginIndex
	 * @param endIndex
	 * @return
	 */
	public Rectangle2D.Float measureTextRange(final String text, final int beginIndex, final int endIndex) {
		String begin = text.substring(0, beginIndex);
		final byte[] dataBegin = getEncodedTextData(begin);
		final float sWidthBegin = PDFontTools.getGlyphWidthEncodedScaled(_font, _size, dataBegin, 0, dataBegin.length);
		String str = text.substring(beginIndex, endIndex);
		final byte[] data = getEncodedTextData(str);
		final float sWidth = PDFontTools.getGlyphWidthEncodedScaled(_font, _size, data, 0, data.length);
		
		CDSRectangle rect = _font.getFontDescriptor().getFontBB();
		return new Rectangle2D.Float(sWidthBegin, -(_size * rect.getUpperRightY()) / 1000f, sWidth, (_size * (rect.getUpperRightY() - rect.getLowerLeftY())) / 1000f);
	}
	
	/**
	 * Gets the caret index for a coordinate (in local text space) within the given text. Does not process newlines.
	 * @param text
	 * @param x
	 * @return
	 */
	public int getCaretIndex(final String text, final float x) {
		float totalWidth = 0.0f;
		for (int i = 0; i < text.length(); i++) {
			byte[] charData = getEncodedTextData(text.charAt(i));
			float glyphWidth = PDFontTools.getGlyphWidthEncodedScaled(_font, _size, charData, 0, charData.length);
			if (x < totalWidth + glyphWidth * 0.5f) {
				return i;
			}
			totalWidth += glyphWidth;
		}
		if (x < totalWidth) {
			return text.length();
		}
		return -1;
	}

	public PDFont getPDFont() {
		return _font;
	}

	/**
	 * Gets the size of the font (needs not to be the size returned by measure)
	 * @return
	 */
	public float getSize() {
		return _size;
	}
	
	@Override
	public String toString() {
		return "TextFont {id: " + _id + ", font: " + _font + ", size: " + _size + "}";
	}

	/**
	 * Gets the height of a line
	 * @return
	 */
	public float getLineHeight() {
		return _size;
	}
}
