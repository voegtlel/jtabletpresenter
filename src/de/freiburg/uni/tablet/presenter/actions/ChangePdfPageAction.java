/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;

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
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return new ChangePdfPageAction(_page, _lastPdfPage, _pdfPage);
	}

	@Override
	public void perform(final IDocumentEditor editor) {
		_page.setPdfPage(_pdfPage);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_page);
		writer.writeObjectTable(_pdfPage);
		writer.writeObjectTable(_lastPdfPage);
	}

	@Override
	public String toString() {
		return String.format("ChangePdfPage: (Last: %X) to %X in %X", (_lastPdfPage==null?0:_lastPdfPage.getId()), _pdfPage.getId(), _page.getId());
	}
}
