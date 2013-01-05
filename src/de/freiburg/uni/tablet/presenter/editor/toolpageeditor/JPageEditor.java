/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentEditorListener;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.DocumentPageLayer;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.PageRepaintListener;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageRenderer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.JPageRendererBufferStrategy;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferBack;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferColor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferComposite;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferFront;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferPdf;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonColor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonFullscreen;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonNext;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPreferences;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPrevious;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonRedo;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonSpinnerPage;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonUndo;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPageFrontRenderer;
import de.intarsys.pdf.pd.PDDocument;

/**
 * @author lukas
 * 
 */
public class JPageEditor extends JFrame implements IToolPageEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IPageEditor _pageRenderer;
	private JPanel _panelTools;

	private int _lastExtendedState;
	private Rectangle _lastBounds;

	private PageLayerBufferBack _clientOnlyLayer;
	private PageLayerBufferBack _serverSyncLayer;
	private PageLayerBufferFront _frontLayer;

	private DocumentEditor _documentEditor;

	private final DocumentListener _documentListener;

	private final DocumentEditorListener _documentEditorListener;

	private IButtonAction[] _buttonActions = new IButtonAction[] {};

	private PageLayerBufferColor _backgroundLayer;
	private PageLayerBufferPdf _pdfLayer;

	/**
	 * Create the panel.
	 */
	public JPageEditor() {
		setDocumentEditor(new DocumentEditor());
		initialize();
		_documentListener = new DocumentAdapter() {
			@Override
			public void renderableRemoved(final IRenderable renderable,
					final DocumentPageLayer layer) {
				onRenderableRemoved(renderable, layer);
			}

			@Override
			public void renderableAdded(final IRenderable renderable,
					final DocumentPageLayer layer) {
				onRenderableAdded(renderable, layer);
			}
		};

		_documentEditorListener = new DocumentEditorAdapter() {
			@Override
			public void currentPageChanged(final DocumentPage lastCurrentPage) {
				onCurrentPageChanged();
			}

			@Override
			public void activeLayerChanged(
					final boolean lastActiveLayerClientOnly) {
				onActiveLayerChanged();
			}

			@Override
			public void documentChanged(final Document lastDocument) {
				onDocumentChanged(lastDocument);
			}
		};
	}

	/**
	 * Initializes the frame
	 */
	private void initialize() {
		getContentPane().setLayout(new BorderLayout(0, 0));
		final IPageRenderer pageRenderer = new JPageRendererBufferStrategy();
		_pageRenderer = pageRenderer;
		getContentPane().add(pageRenderer.getContainerComponent(),
				BorderLayout.CENTER);

		final PageLayerBufferComposite pageLayers = new PageLayerBufferComposite(
				pageRenderer);
		_backgroundLayer = pageLayers.addColorBuffer();
		_pdfLayer = pageLayers.addPdfBuffer();
		_serverSyncLayer = pageLayers.addBackBuffer();
		_serverSyncLayer.setRepaintListener(new PageRepaintListener() {
			@Override
			public void render(final IPageBackRenderer renderer) {
				onRenderServerSync();
			}
		});
		_clientOnlyLayer = pageLayers.addBackBuffer();
		_clientOnlyLayer.setRepaintListener(new PageRepaintListener() {
			@Override
			public void render(final IPageBackRenderer renderer) {
				onRenderClientOnly();
			}
		});
		_frontLayer = pageLayers.addFrontBuffer();
		_pageRenderer.setDisplayedPageLayerBuffer(pageLayers);

		_panelTools = new JPanel();
		getContentPane().add(_panelTools, BorderLayout.WEST);
		setToolButtons(new IButtonAction[] { new ButtonPreferences(this), null,
				new ButtonNext(this), new ButtonPrevious(this),
				new ButtonSpinnerPage(this), null, new ButtonUndo(this),
				new ButtonRedo(this), null, new ButtonColor(this), null,
				new ButtonFullscreen(this) });
	}

	public void setToolButtons(final IButtonAction[] buttons) {
		// Build layout
		final GridBagLayout gbl_panelTools = new GridBagLayout();
		gbl_panelTools.columnWidths = new int[] { 0 };
		gbl_panelTools.columnWeights = new double[] { 1.0 };
		gbl_panelTools.rowHeights = new int[buttons.length + 1];
		gbl_panelTools.rowWeights = new double[buttons.length + 1];

		for (int i = 0; i < buttons.length; i++) {
			gbl_panelTools.rowWeights[i] = 0.0;
			if (buttons[i] == null) {
				gbl_panelTools.rowHeights[i] = 10;
			} else {
				gbl_panelTools.rowHeights[i] = 0;
			}
		}
		gbl_panelTools.rowWeights[buttons.length] = 1.0;
		gbl_panelTools.rowHeights[buttons.length] = 0;

		_panelTools.setLayout(gbl_panelTools);

		// Add controls
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i] != null) {
				final GridBagConstraints gbc_control = new GridBagConstraints();
				gbc_control.insets = new Insets(0, 0, 5, 0);
				gbc_control.fill = GridBagConstraints.BOTH;
				gbc_control.gridx = 0;
				gbc_control.gridy = i;
				if (buttons[i].getControl() != null) {
					_panelTools.add(buttons[i].getControl(), gbc_control);
				} else {
					final IButtonAction action = buttons[i];
					final JButton button = new JPageToolButton(
							buttons[i].getText(),
							buttons[i].getImageResource(), false);
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							action.perform(button);
						}
					});
					_panelTools.add(button, gbc_control);
				}
			}
		}
		_buttonActions = buttons;
	}
	
	@Override
	public void setPdfDocument(File pdfFile) throws IOException {
		_pdfLayer.setDocument(pdfFile);
		_pageRenderer.clear();
	}
	
	@Override
	public PDDocument getPdfDocument() {
		return _pdfLayer.getDocument();
	}

	@Override
	public boolean isFullscreen() {
		return isUndecorated();
	}

	@Override
	public void setFullscreen(final boolean fullscreen) {
		if (fullscreen != isFullscreen()) {
			if (fullscreen) {
				_lastExtendedState = this.getExtendedState();
				_lastBounds = this.getBounds();
				this.setVisible(false);
				this.dispose();
				this.setUndecorated(true);
				this.setExtendedState(Frame.MAXIMIZED_BOTH);
				this.setVisible(true);
			} else {
				this.setVisible(false);
				this.dispose();
				this.setBounds(_lastBounds);
				this.setExtendedState(_lastExtendedState);
				this.setUndecorated(false);
				this.setVisible(true);
			}
		}
	}

	protected void onRenderableRemoved(final IRenderable renderable,
			final DocumentPageLayer layer) {
		// Redraw all objects on page
		if (layer == _documentEditor.getCurrentPage().getClientOnlyLayer()) {
			_clientOnlyLayer.clear();
			layer.render(_clientOnlyLayer);
		} else if (layer == _documentEditor.getCurrentPage()
				.getServerSyncLayer()) {
			_serverSyncLayer.clear();
			layer.render(_serverSyncLayer);
		}
		// Else the object was not on a visible layer
	}

	protected void onRenderableAdded(final IRenderable renderable,
			final DocumentPageLayer layer) {
		// Draw new object
		if (layer == _documentEditor.getCurrentPage().getClientOnlyLayer()) {
			renderable.render(_clientOnlyLayer);
		} else if (layer == _documentEditor.getCurrentPage()
				.getServerSyncLayer()) {
			renderable.render(_serverSyncLayer);
		}
		// Else the object is not on a visible layer
	}

	private void resetEditing() {

	}

	protected void onCurrentPageChanged() {
		// Restart editing
		resetEditing();
		// Update layers
		final DocumentPage currentPage = _documentEditor.getCurrentPage();
		_clientOnlyLayer.clear();
		currentPage.getClientOnlyLayer().render(_clientOnlyLayer);
		_serverSyncLayer.clear();
		currentPage.getServerSyncLayer().render(_serverSyncLayer);
		// Next PDF Page
		_pdfLayer.clear();
		_pdfLayer.setPageIndex(_documentEditor.getCurrentPageIndex());
		// Render all
		_pageRenderer.clear();
	}

	protected void onActiveLayerChanged() {
		// Restart editing
		resetEditing();
	}

	protected void onDocumentChanged(final Document lastDocument) {
		lastDocument.removeListener(_documentListener);
		_documentEditor.getDocument().addListener(_documentListener);
	}

	protected void onRenderClientOnly() {
		_documentEditor.getCurrentPage().getClientOnlyLayer()
				.render(_clientOnlyLayer);
	}

	protected void onRenderServerSync() {
		_documentEditor.getCurrentPage().getServerSyncLayer()
				.render(_serverSyncLayer);
	}

	@Override
	public IPageFrontRenderer getFrontRenderer() {
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

	public void setDocumentEditor(final DocumentEditor documentEditor) {
		if (_documentEditor != documentEditor) {
			if (_documentEditor != null) {
				_documentEditor.getDocument().removeListener(_documentListener);
				_documentEditor.removeListener(_documentEditorListener);
			}
			final DocumentEditor lastEditor = documentEditor;
			_documentEditor = documentEditor;
			if (_documentEditor != null) {
				_documentEditor.addListener(_documentEditorListener);
				_documentEditor.getDocument().addListener(_documentListener);
			}
			for (final IButtonAction button : _buttonActions) {
				if (button != null) {
					button.onUpdateEditor(lastEditor);
				}
			}
		}
	}
}
