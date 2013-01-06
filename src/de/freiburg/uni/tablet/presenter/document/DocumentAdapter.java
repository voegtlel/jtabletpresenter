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
			final DocumentPageLayer layer) {
	}

	@Override
	public void renderableRemoved(final IRenderable renderable,
			final DocumentPageLayer layer) {
	}

	@Override
	public void pdfChanged(PdfSerializable lastPdf) {
	}
}
