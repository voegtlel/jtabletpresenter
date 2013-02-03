/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import android.content.Context;
import android.graphics.Color;
import de.freiburg.uni.tablet.presenter.document.DocumentAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IDocument;
import de.freiburg.uni.tablet.presenter.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferBack;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferColor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferComposite;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferFront;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferPdf;
import de.freiburg.uni.tablet.presenter.editor.rendering.RenderCanvas;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonColor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonConnect;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonDocumentClearPdf;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonDocumentNew;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonDocumentOpenPdf;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonNext;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonOpenFrom;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPageClear;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPageClone;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPageDelete;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPageNew;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPrevious;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPrimary;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonRedo;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonSaveAs;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonSecondary;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonSpinnerPage;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonUndo;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.IButton;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

/**
 * @author lukas
 * 
 */
public class PageEditor implements IToolPageEditor {
	private RenderCanvas _pageRenderer;

	private PageLayerBufferBack _clientOnlyLayer;
	private PageLayerBufferBack _serverSyncLayer;
	private PageLayerBufferFront _frontLayer;

	private DocumentEditor _documentEditor = new DocumentEditor();

	private final DocumentListener _documentListener;

	private PageLayerBufferColor _backgroundLayer;
	private PageLayerBufferPdf _pdfLayer;

	private DocumentConfig _config;
	
	private IButton[] _buttons;

	/**
	 * Create the panel.
	 * @param renderer 
	 * @param configFilename 
	 */
	public PageEditor(final DocumentConfig config, final RenderCanvas renderer) {
		_pageRenderer = renderer;
		_documentListener = new DocumentAdapter() {
			@Override
			public void renderableRemoved(final IRenderable renderableAfter,
					final IRenderable renderable, final DocumentPage page) {
				onRenderableChanged(renderable, page);
			}

			@Override
			public void renderableAdded(final IRenderable renderableAfter,
					final IRenderable renderable, final DocumentPage page) {
				onRenderableAdded(renderable, page);
			}
			
			@Override
			public void pdfPageChanged(final DocumentPage documentPage,
					final PdfPageSerializable lastPdfPage) {
				onPdfPageChanged(documentPage, lastPdfPage);
			}
			
			@Override
			public void renderableModified(final IRenderable renderable,
					final DocumentPage page) {
				onRenderableChanged(renderable, page);
			}
			
			@Override
			public void renderableModifyEnd(final IRenderable renderable,
					final DocumentPage page) {
				// Not interesting here
			}
		};

		_documentEditor.addListener(new DocumentEditorAdapter() {
			@Override
			public void changing() {
				onDocumentChanging();
			}
			
			@Override
			public void currentPageChanged(final DocumentPage lastCurrentPage,
					final DocumentPage lastCurrentBackPage) {
				onCurrentPageChanged();
			}
			
			@Override
			public void documentChanged(final IEditableDocument lastDocument) {
				onDocumentChanged(lastDocument);
			}
			
			@Override
			public void baseDocumentChanged(final IDocument lastDocument) {
				onBaseDocumentChanged(lastDocument);
			}
		});
		_config = config;
		initialize();
	}

	/**
	 * Initializes the frame
	 */
	private void initialize() {
		final PageLayerBufferComposite pageLayers = new PageLayerBufferComposite(_pageRenderer);
		_backgroundLayer = pageLayers.addColorBuffer();
		_backgroundLayer.setColor(_config.getColor("document.background.color", Color.WHITE));
		_pdfLayer = pageLayers.addPdfBuffer();
		_serverSyncLayer = pageLayers.addBackBuffer();
		_clientOnlyLayer = pageLayers.addBackBuffer();
		_frontLayer = pageLayers.addFrontBuffer();
		_pageRenderer.setDisplayedPageLayerBuffer(pageLayers);
		
		_buttons = new IButton[]{
				new ButtonColor(this),
				new ButtonConnect(this),
				new ButtonDocumentClearPdf(this),
				new ButtonDocumentNew(this),
				new ButtonDocumentOpenPdf(this),
				new ButtonNext(this),
				new ButtonOpenFrom(this),
				new ButtonPageClear(this),
				new ButtonPageClone(this),
				new ButtonPageDelete(this),
				new ButtonPageNew(this),
				new ButtonPrevious(this),
				new ButtonPrimary(this),
				new ButtonRedo(this),
				new ButtonSaveAs(this),
				new ButtonSecondary(this),
				new ButtonSpinnerPage(this),
				new ButtonUndo(this),
		};
	}

	protected void onRenderableChanged(final IRenderable renderable,
			final DocumentPage page) {
		// Redraw the object on page
		if (page == _documentEditor.getCurrentPage()) {
			_clientOnlyLayer.requireRepaint(renderable, true);
		} else if (page == _documentEditor.getCurrentBackPage()) {
			_serverSyncLayer.requireRepaint(renderable, true);
		}
		// Else the object was not on a visible layer
	}

	protected void onRenderableAdded(final IRenderable renderable,
			final DocumentPage page) {
		// Draw new object
		if (page == _documentEditor.getCurrentPage()) {
			// renderable.render(_clientOnlyLayer);
			_clientOnlyLayer.requireRepaint(renderable, false);
			// _pageRenderer.requireRepaint();
		} else if (page == _documentEditor.getCurrentBackPage()) {
			// renderable.render(_serverSyncLayer);
			_clientOnlyLayer.requireRepaint(renderable, false);
			// _pageRenderer.requireRepaint();
		}
		// Else the object is not on a visible layer
	}
	
	protected void onPdfPageChanged(final DocumentPage documentPage,
			final PdfPageSerializable lastPdfPage) {
		if ((documentPage == _documentEditor.getCurrentPage()) || (documentPage == _documentEditor.getCurrentBackPage())) {
			_pdfLayer.setPdfPage(documentPage.getPdfPage());
		}
	}
	
	protected void onDocumentChanging() {
		_pageRenderer.stopTool();
	}

	protected void onCurrentPageChanged() {
		// Update layers
		_clientOnlyLayer.setRepaintListener(_documentEditor.getCurrentPage());
		_serverSyncLayer.setRepaintListener(_documentEditor.getCurrentBackPage());
		// Next PDF Page
		if (_documentEditor.getCurrentBackPage() != null) {
			_pdfLayer.setPdfPage(_documentEditor.getCurrentBackPage().getPdfPage());
		} else {
			_pdfLayer.setPdfPage(_documentEditor.getCurrentPage().getPdfPage());
		}
	}

	protected void onDocumentChanged(final IEditableDocument lastDocument) {
		if (lastDocument != null) {
			lastDocument.removeListener(_documentListener);
		}
		if (_documentEditor.getDocument() != null) {
			_documentEditor.getDocument().addListener(_documentListener);
		}
	}
	
	protected void onBaseDocumentChanged(final IDocument lastDocument) {
		if (lastDocument != null) {
			lastDocument.removeListener(_documentListener);
		}
		if (_documentEditor.getBaseDocument() != null) {
			_documentEditor.getBaseDocument().addListener(_documentListener);
		}
	}
	
	/**
	 * Processes the action
	 * @param context
	 * @param actionId
	 * @param groupActionId
	 */
	public void processAction(final Context context, final int actionId, final int groupActionId) {
		for (IButton button : _buttons) {
			if (button.perform(context, actionId, groupActionId)) {
				break;
			}
		}
	}
	
	@Override
	public IPageBackRenderer getFrontRenderer() {
		return _frontLayer;
	}

	@Override
	public DocumentEditor getDocumentEditor() {
		return _documentEditor;
	}

	@Override
	public IPageEditor getPageEditor() {
		return _pageRenderer;
	}
	
	public DocumentConfig getConfig() {
		return _config;
	}
}
