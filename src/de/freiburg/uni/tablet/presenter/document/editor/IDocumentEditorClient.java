package de.freiburg.uni.tablet.presenter.document.editor;

import java.io.File;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.page.IPen;

public interface IDocumentEditorClient extends IDocumentEditor {
	/**
	 * Gets the current back page
	 * @return
	 */
	DocumentPage getCurrentBackPage();

	IPen getCurrentPen();

	void setCurrentPen(IPen currentPen);

	File getCurrentImageFile();

	BitmapImage getCurrentImage();

	void setCurrentImageFile(File imageFile) throws IOException;

	/**
	 * Gets the base document
	 * @return
	 */
	IClientDocument getBackDocument();
	
	/**
	 * Gets the front document
	 * @return
	 */
	@Override
	IEditableDocument getFrontDocument();
	
	/**
	 * @return
	 */
	IEditableDocument getDocument();
	
	/**
	 * Set current page by index with option to create missing pages
	 * @param index
	 * @param createIfNotExisting
	 */
	void setCurrentPageByIndex(int index, boolean createIfNotExisting);
}