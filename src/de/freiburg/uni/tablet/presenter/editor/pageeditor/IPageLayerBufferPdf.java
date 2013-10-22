package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;

public interface IPageLayerBufferPdf extends IPageLayerBuffer {

	/**
	 * Sets the document
	 * @param document
	 */
	void setPdfPage(PdfPageSerializable pdfPage);

}