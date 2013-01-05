package de.freiburg.uni.tablet.presenter.document;

public abstract class DocumentEditorAdapter implements DocumentEditorListener {

	@Override
	public void currentPageChanged(final DocumentPage lastCurrentPage) {
	}

	@Override
	public void activeLayerChanged(final boolean lastActiveLayerClientOnly) {
	}

	@Override
	public void documentChanged(final Document lastDocument) {
	}
	
	@Override
	public void changing() {
	}
}
