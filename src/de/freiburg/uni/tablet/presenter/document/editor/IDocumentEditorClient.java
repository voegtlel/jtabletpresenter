package de.freiburg.uni.tablet.presenter.document.editor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.document.BitmapImageData;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.page.IPen;

public interface IDocumentEditorClient extends IDocumentEditor {
	/**
	 * Gets the current back page
	 * @return
	 */
	DocumentPage getCurrentBackPage();

	/**
	 * Gets the current pen
	 * @return
	 */
	IPen getCurrentPen();

	/**
	 * Sets the current pen
	 * @param currentPen
	 */
	void setCurrentPen(IPen currentPen);

	/**
	 * Gets the current image file (might be null if not from file)
	 * @return
	 */
	File getCurrentImageFile();

	/**
	 * Gets the current image
	 * @return
	 */
	BitmapImageData getCurrentImage();

	/**
	 * Sets the current image from file
	 * @param imageFile
	 * @throws IOException
	 */
	void setCurrentImageFile(File imageFile) throws IOException;
	
	/**
	 * Sets the current image from bitmap
	 * @param imageData
	 * @throws IOException
	 */
	void setCurrentImage(BufferedImage imageData);

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
	 * Gets the document
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