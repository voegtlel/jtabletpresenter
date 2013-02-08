package de.freiburg.uni.tablet.presenter.document.editor;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.page.IPen;

public abstract class DocumentEditorAdapter implements DocumentEditorListener {

	@Override
	public void currentPageChanged(final DocumentPage lastCurrentPage, final DocumentPage lastCurrentBackPage) {
	}

	@Override
	public void documentChanged(final IClientDocument lastDocument) {
	}
	
	@Override
	public void backDocumentChanged(final IDocument lastDocument) {
	}
	
	@Override
	public void changing() {
	}
	
	@Override
	public void currentPenChanged(final IPen lastPen) {
	}
}
