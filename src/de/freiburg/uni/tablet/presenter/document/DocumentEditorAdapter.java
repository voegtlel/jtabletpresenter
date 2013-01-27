package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.page.IPen;

public abstract class DocumentEditorAdapter implements DocumentEditorListener {

	@Override
	public void currentPageChanged(final DocumentPage lastCurrentPage, final DocumentPage lastCurrentBackPage) {
	}

	@Override
	public void documentChanged(final IEditableDocument lastDocument) {
	}
	
	@Override
	public void baseDocumentChanged(final IDocument lastDocument) {
	}
	
	@Override
	public void changing() {
	}
	
	@Override
	public void currentPenChanged(final IPen lastPen) {
	}
}
