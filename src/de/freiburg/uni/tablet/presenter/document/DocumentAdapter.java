package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public abstract class DocumentAdapter implements DocumentListener {

	@Override
	public void pageInserted(final DocumentPage prevPage,
			final DocumentPage page) {
	}

	@Override
	public void pageRemoved(final DocumentPage prevPage, final DocumentPage page) {
	}

	@Override
	public void renderableAdded(final IRenderable renderable,
			final DocumentPage layer) {
	}

	@Override
	public void renderableRemoved(final IRenderable renderable,
			final DocumentPage layer) {
	}
	
	@Override
	public void pdfPageChanged(final DocumentPage documentPage,
			final PdfPageSerializable lastPdfPage) {
	}
}
