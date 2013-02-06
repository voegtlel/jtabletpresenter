package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public abstract class DocumentAdapter implements DocumentListener {

	@Override
	public void pageInserted(final IClientDocument document, final DocumentPage prevPage,
			final DocumentPage page) {
	}

	@Override
	public void pageRemoved(final IClientDocument document, final DocumentPage prevPage, final DocumentPage page) {
	}

	@Override
	public void renderableAdded(final IRenderable renderableAfter,
			final IRenderable renderable, final DocumentPage page) {
	}

	@Override
	public void renderableRemoved(final IRenderable renderableAfter,
			final IRenderable renderable, final DocumentPage page) {
	}

	@Override
	public void pdfPageChanged(final DocumentPage documentPage,
			final PdfPageSerializable lastPdfPage) {
	}
	
	@Override
	public void renderableModified(final IRenderable renderable, final DocumentPage page) {
	}
	
	@Override
	public void renderableModifyEnd(final IRenderable renderable, final DocumentPage page) {
	}
}
