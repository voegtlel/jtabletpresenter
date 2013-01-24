/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;

/**
 * @author lukas
 * 
 */
public class ChangePdfPageAction implements IAction {
	private final DocumentPage _page;
	private final PdfPageSerializable _pdfPage;
	private final PdfPageSerializable _lastPdfPage;

	/**
	 * 
	 */
	public ChangePdfPageAction(final DocumentPage page,
			final PdfPageSerializable pdfPage, final PdfPageSerializable lastPdfPage) {
		_page = page;
		_pdfPage = pdfPage;
		_lastPdfPage = lastPdfPage;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ChangePdfPageAction(final BinaryDeserializer reader)
			throws IOException {
		_page = reader.readObjectTable();
		_pdfPage = reader.readObjectTable();
		_lastPdfPage = reader.readObjectTable();
	}
	
	@Override
	public boolean mustRedraw(DocumentEditor editor) {
		return true;
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new ChangePdfPageAction(_page, _lastPdfPage, _pdfPage);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		_page.setPdfPage(_pdfPage);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_page);
		writer.writeObjectTable(_pdfPage);
		writer.writeObjectTable(_lastPdfPage);
	}

}
