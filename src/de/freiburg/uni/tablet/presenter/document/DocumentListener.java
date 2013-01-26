package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface DocumentListener {
	/**
	 * Called, when a page was inserted
	 * 
	 * @param prevPage
	 * @param page
	 */
	void pageInserted(DocumentPage prevPage, DocumentPage page);

	/**
	 * Called, when a page was removed
	 * 
	 * @param index
	 * @param page
	 */
	void pageRemoved(DocumentPage prevPage, DocumentPage page);

	/**
	 * Called, when a renderable was added
	 * 
	 * @param renderableAfter
	 * @param renderable 
	 * @param page
	 */
	void renderableAdded(IRenderable renderableAfter, IRenderable renderable, DocumentPage page);

	/**
	 * Called, when a renderable was removed
	 * 
	 * @param renderableAfter
	 * @param renderable
	 * @param page
	 */
	void renderableRemoved(IRenderable renderableAfter, IRenderable renderable, DocumentPage page);
	
	/**
	 * Called, when a pdf page assignment was changed
	 * @param documentPage
	 * @param lastPdfPage
	 */
	void pdfPageChanged(DocumentPage documentPage,
			PdfPageSerializable lastPdfPage);

	/**
	 * Called, when a renderable was modified
	 * @param page
	 * @param renderable
	 */
	void renderableModified(IRenderable renderable, DocumentPage page);
}
