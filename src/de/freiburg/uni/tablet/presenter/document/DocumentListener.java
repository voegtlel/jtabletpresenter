package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface DocumentListener {
	/**
	 * Called, when a page was inserted
	 * 
	 * @param prevPage
	 * @param page
	 */
	void pageInserted(IClientDocument document, DocumentPage prevPage, DocumentPage page);

	/**
	 * Called, when a page was removed
	 * 
	 * @param index
	 * @param page
	 */
	void pageRemoved(IClientDocument document, DocumentPage prevPage, DocumentPage page);

	/**
	 * Called, when a renderable was added
	 * 
	 * @param renderableAfter
	 * @param renderable
	 * @param page
	 */
	void renderableAdded(IRenderable renderableAfter, IRenderable renderable, DocumentPage page);
	
	/**
	 * Called, when a renderable is to be added
	 * 
	 * @param renderableAfter
	 * @param renderable
	 * @param page
	 */
	void renderableAdding(IRenderable renderableAfter, IRenderable renderable, DocumentPage page);

	/**
	 * Called, when a renderable was removed
	 * 
	 * @param renderableAfter
	 * @param renderable
	 * @param page
	 */
	void renderableRemoved(IRenderable renderableAfter, IRenderable renderable, DocumentPage page);
	
	/**
	 * Called, when a renderable is to be removed
	 * 
	 * @param renderableAfter
	 * @param renderable
	 * @param page
	 */
	void renderableRemoving(IRenderable renderableAfter, IRenderable renderable, DocumentPage page);
	
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
	
	/**
	 * Called, when a renderable is to be modified
	 * @param page
	 * @param renderable
	 */
	void renderableModifying(IRenderable renderable, DocumentPage page);
	
	/**
	 * Called, when a modifying a renderable ends
	 * @param page
	 * @param renderable
	 */
	void renderableModifyEnd(IRenderable renderable, DocumentPage page);
}
