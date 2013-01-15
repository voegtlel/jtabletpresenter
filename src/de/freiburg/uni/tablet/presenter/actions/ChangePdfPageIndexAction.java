/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;

/**
 * @author lukas
 * 
 */
public class ChangePdfPageIndexAction extends AbstractAction implements IAction, IBinarySerializable {
	private final DocumentPage _page;
	private final int _pdfPageIndex;
	private final int _lastPdfPageIndex;

	/**
	 * 
	 */
	public ChangePdfPageIndexAction(int clientId, final DocumentPage page,
			final int pdfPageIndex, final int lastPdfPageIndex) {
		super(clientId);
		_page = page;
		_pdfPageIndex = pdfPageIndex;
		_lastPdfPageIndex = lastPdfPageIndex;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ChangePdfPageIndexAction(final BinaryDeserializer reader)
			throws IOException {
		super(reader);
		_page = reader.readObjectTable();
		_pdfPageIndex = reader.readInt();
		_lastPdfPageIndex = reader.readInt();
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
	public IAction getUndoAction(int clientId) {
		return new ChangePdfPageIndexAction(clientId, _page, _lastPdfPageIndex, _pdfPageIndex);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		_page.setPdfPageIndex(_pdfPageIndex);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeObjectTable(_page.getId(), _page);
		writer.writeInt(_pdfPageIndex);
		writer.writeInt(_lastPdfPageIndex);
	}

}
