package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class DocumentEditor implements IBinarySerializable {
	private IPen _currentPen = new SolidPen();

	private Document _document = new Document(1);
	private final DocumentHistory _history;
	private DocumentPage _currentPage;
	private boolean _activeLayerClientOnly = true;

	private final List<DocumentEditorListener> _listeners = new LinkedList<DocumentEditorListener>();

	public DocumentEditor() {
		_currentPage = _document.getPageByIndex(0, true);
		_history = new DocumentHistory(this);
	}

	public void addListener(final DocumentEditorListener listener) {
		_listeners.add(listener);
	}

	public void removeListener(final DocumentEditorListener listener) {
		_listeners.remove(listener);
	}

	void fireCurrentPageChanged(final DocumentPage lastCurrentPage) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.currentPageChanged(lastCurrentPage);
		}
	}

	void fireActiveLayerChanged(final boolean lastActiveLayerClientOnly) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.activeLayerChanged(lastActiveLayerClientOnly);
		}
	}

	void fireDocumentChanged(final Document lastDocument) {
		for (final DocumentEditorListener listener : _listeners) {
			listener.documentChanged(lastDocument);
		}
	}

	/**
	 * Returns the current page.
	 * 
	 * @return
	 */
	public DocumentPage getCurrentPage() {
		return _currentPage;
	}

	/**
	 * Set the current page
	 * 
	 * @param page
	 */
	public void setCurrentPage(final DocumentPage page) {
		final DocumentPage lastPage = _currentPage;
		_currentPage = page;
		fireCurrentPageChanged(lastPage);
	}

	/**
	 * Returns the current page index.
	 * 
	 * @return
	 */
	public int getCurrentPageIndex() {
		return _document.getPageIndex(_currentPage);
	}

	/**
	 * Set the current page
	 * 
	 * @param index
	 * @param createIfNotExisting
	 */
	public void setCurrentPageByIndex(final int index,
			final boolean createIfNotExisting) {
		final DocumentPage page = _document.getPageByIndex(index,
				createIfNotExisting);
		if (page == null) {
			throw new IllegalArgumentException("index out of range");
		}
		setCurrentPage(page);
	}

	/**
	 * @return
	 */
	public DocumentPageLayer getActiveLayer() {
		if (_currentPage == null) {
			return null;
		}
		return _activeLayerClientOnly ? _currentPage.getClientOnlyLayer()
				: _currentPage.getServerSyncLayer();
	}

	/**
	 * Gets if the active layer is the client only layer
	 * 
	 * @return
	 */
	public boolean isActiveLayerClientOnly() {
		return _activeLayerClientOnly;
	}

	/**
	 * Sets if the active layer is the client only layer
	 * 
	 * @return
	 */
	public void setActiveLayerClientOnly(final boolean activeLayerClientOnly) {
		final boolean lastActiveLayerClientOnly = _activeLayerClientOnly;
		_activeLayerClientOnly = activeLayerClientOnly;
		fireActiveLayerChanged(lastActiveLayerClientOnly);
	}

	/**
	 * @return
	 */
	public IPen getCurrentPen() {
		return _currentPen;
	}

	/**
	 * @param currentPen
	 */
	public void setCurrentPen(final IPen currentPen) {
		_currentPen = currentPen;
	}

	public Document getDocument() {
		return _document;
	}

	public void setDocument(final Document document) {
		final Document lastDocument = _document;
		_document = document;
		fireDocumentChanged(lastDocument);
		setCurrentPage(document.getPageByIndex(0, true));
	}

	public DocumentHistory getHistory() {
		return _history;
	}

	public DocumentEditor(final BinaryDeserializer reader) throws IOException {
		_document = reader.readObjectTable();
		_currentPen = reader.readSerializableClass();
		_currentPage = reader.readObjectTable();
		_activeLayerClientOnly = reader.readBoolean();
		_history = new DocumentHistory(this);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeObjectTable(_document.getId(), _document);
		writer.writeSerializableClass(_currentPen);
		writer.writeObjectTable(_currentPage.getId(), _currentPage);
		writer.writeBoolean(_activeLayerClientOnly);
	}
}
