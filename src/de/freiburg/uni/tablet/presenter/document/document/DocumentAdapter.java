package de.freiburg.uni.tablet.presenter.document.document;

import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
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
	
	@Override
	public void renderableAdding(final IRenderable renderableAfter,
			final IRenderable renderable, final DocumentPage page) {
	}
	
	@Override
	public void renderableModifying(final IRenderable renderable, final DocumentPage page) {
	}
	
	@Override
	public void renderableRemoving(final IRenderable renderableAfter,
			final IRenderable renderable, final DocumentPage page) {
	}
}
