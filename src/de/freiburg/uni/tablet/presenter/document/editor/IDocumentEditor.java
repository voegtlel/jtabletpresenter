package de.freiburg.uni.tablet.presenter.document.editor;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializableId;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;

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
	 * Checks if the given page is the current page
	 * @param page
	 */
	boolean isCurrentPage(DocumentPage page);

	/**
	 * Set the current page
	 * 
	 * @param index
	 * @param createIfNotExisting
	 */
	void setCurrentPageByIndex(int index);

	/**
	 * Gets the front document
	 * @return
	 */
	IClientDocument getFrontDocument();

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

	/**
	 * Sets the base document
	 * @param document
	 */
	void setBackDocument(IClientDocument backDocument);
}