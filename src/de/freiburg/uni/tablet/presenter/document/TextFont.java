package de.freiburg.uni.tablet.presenter.document;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
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

public class TextFont implements IEntity {
	private final long _id;
	private final IDocument _parent;

	private PDFont _font;
	private float _size;

	public TextFont(final IDocument parent, final String name, final float size) {
		_id = parent.nextId();
		_parent = parent;

		_font = PDFontType1.createNew(PDFontType1.FONT_Helvetica);
		_font.setEncoding(WinAnsiEncoding.UNIQUE);

		_size = size;
	}

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
	
	public byte[] getEncodedTextData(final String text) {
		return _font.getEncoding().encode(text.toCharArray(),
				0, text.length());
	}

	public Float measureText2(final String text) {
		final byte[] data = getEncodedTextData(text);
		final float sWidth = PDFontTools.getGlyphWidthEncodedScaled(_font, _size, data, 0, data.length);
		final float sHeight = PDFontTools.getGlyphHeightScaled(_font, _size);
		return new Point2D.Float(sWidth, sHeight);
	}
	
	public Rectangle2D.Float measureText(final String text) {
		final byte[] data = getEncodedTextData(text);
		final float sWidth = PDFontTools.getGlyphWidthEncodedScaled(_font, _size, data, 0, data.length);
		CDSRectangle rect = _font.getFontDescriptor().getFontBB();
		return new Rectangle2D.Float(0, (_size * rect.getLowerLeftY()) / 1000f, sWidth, (_size * (rect.getUpperRightY() - rect.getLowerLeftY())) / 1000f);
	}

	public PDFont getPDFont() {
		return _font;
	}

	public float getSize() {
		return _size;
	}
	
	@Override
	public String toString() {
		return "TextFont {id: " + _id + ", font: " + _font + ", size: " + _size + "}";
	}
}
