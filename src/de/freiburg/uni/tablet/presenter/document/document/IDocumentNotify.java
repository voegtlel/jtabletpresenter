package de.freiburg.uni.tablet.presenter.document.document;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;

public interface IDocumentNotify extends IDocument {

	void fireRenderableAdded(IRenderable afterRenderable, IRenderable renderable, DocumentPage page);
	
	void fireRenderableAdding(IRenderable afterRenderable, IRenderable renderable, DocumentPage page);

	void fireRenderableRemoved(IRenderable afterRenderable, IRenderable renderable, DocumentPage page);
	
	void fireRenderableRemoving(IRenderable afterRenderable, IRenderable renderable, DocumentPage page);

	void fireBackgroundEntityChanged(DocumentPage documentPage,
			IEntity backgroundObject);

	void fireRenderableModified(IRenderable renderable, DocumentPage documentPage);
	
	void fireRenderableModifying(IRenderable renderable, DocumentPage documentPage);
	
	void fireRenderableModifyEnd(IRenderable renderable, DocumentPage documentPage);
}
