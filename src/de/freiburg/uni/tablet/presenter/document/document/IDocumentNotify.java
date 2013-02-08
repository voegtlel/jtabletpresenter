package de.freiburg.uni.tablet.presenter.document.document;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface IDocumentNotify extends IDocument {

	void fireRenderableAdded(IRenderable afterRenderable, IRenderable renderable, DocumentPage page);

	void fireRenderableRemoved(IRenderable afterRenderable, IRenderable renderable, DocumentPage page);

	void firePdfPageChanged(DocumentPage documentPage,
			PdfPageSerializable lastPdfPage);

	void fireRenderableModified(IRenderable renderable, DocumentPage documentPage);
	
	void fireRenderableModifyEnd(IRenderable renderable, DocumentPage documentPage);
}
