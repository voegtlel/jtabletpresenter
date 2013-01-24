package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface IDocumentNotify extends IDocument {

	void fireRenderableAdded(IRenderable renderable, DocumentPage page);

	void fireRenderableRemoved(IRenderable renderable, DocumentPage page);

	void firePdfPageChanged(DocumentPage documentPage,
			PdfPageSerializable lastPdfPage);
}
