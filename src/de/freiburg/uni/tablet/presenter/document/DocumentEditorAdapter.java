package de.freiburg.uni.tablet.presenter.document;

public abstract class DocumentEditorAdapter implements DocumentEditorListener {

	@Override
	public void currentPageChanged(final DocumentPage lastCurrentPage, final DocumentPage lastCurrentBackPage) {
	}

	@Override
	public void documentChanged(final IEditableDocument lastDocument) {
	}
	
	@Override
	public void changing() {
	}
}
