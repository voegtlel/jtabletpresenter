package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializableId;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.page.IPen;

public interface IDocumentEditor extends IBinarySerializableId {

	/**
	 * Returns the current page.
	 * 
	 * @return
	 */
	DocumentPage getCurrentPage();

	/**
	 * Set the current page
	 * 
	 * @param page
	 */
	void setCurrentPage(DocumentPage page);

	/**
	 * Returns the current page index.
	 * 
	 * @return
	 */
	int getCurrentPageIndex();

	/**
	 * Set the current page
	 * 
	 * @param index
	 * @param createIfNotExisting
	 */
	void setCurrentPageByIndex(int index);

	/**
	 * @return
	 */
	IClientDocument getDocument();

	/**
	 * @param document
	 */
	void setDocument(IClientDocument document);

	/**
	 * Add a listener
	 * @param listener
	 */
	void addListener(DocumentEditorListener listener);

	/**
	 * Remove a listener
	 * @param listener
	 */
	void removeListener(DocumentEditorListener listener);

	/**
	 * @return
	 */
	DocumentHistory getHistory();

	/**
	 * Override this by deserializing an editor
	 * @param reader
	 * @throws IOException
	 */
	void deserialize(BinaryDeserializer reader) throws IOException;
}