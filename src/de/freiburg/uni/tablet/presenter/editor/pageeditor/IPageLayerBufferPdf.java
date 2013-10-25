package de.freiburg.uni.tablet.presenter.editor.pageeditor;

import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;

/**
 * Interface for pdf layer buffer
 * @author Luke
 *
 */
public interface IPageLayerBufferPdf extends IPageLayerBuffer {

	/**
	 * Sets the document
	 * @param document
	 */
	void setPdfPage(PdfPageSerializable pdfPage);

	/**
	 * Enable or disable automatic page ratio (used for getDesiredRatio())
	 * @param enabled
	 */
	void setRatioEnabled(boolean enabled);

	/**
	 * Checks if the automatic ratio is enabled (used for getDesiredRatio())
	 * @return
	 */
	boolean isRatioEnabled();

}