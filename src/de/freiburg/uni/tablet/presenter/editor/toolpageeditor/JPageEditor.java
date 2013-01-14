/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig.KeyValue;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentEditorListener;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.DocumentPageLayer;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;
import de.freiburg.uni.tablet.presenter.editor.IPageRepaintListener;
import de.freiburg.uni.tablet.presenter.editor.IRepaintListener;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageRenderer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.JPageRendererBufferStrategy;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferBack;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferColor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferComposite;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferFront;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferPdf;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonColor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonNext;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPrevious;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonRedo;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonSpinnerPage;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonToggleFullscreen;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonTools;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonUndo;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.page.IPageFrontRenderer;

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

	private DocumentConfig _config;

	/**
	 * Create the panel.
	 */
	public JPageEditor() {
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
			public void changing() {
				onDocumentChanging();
			}
			
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
		_config = new DocumentConfig();
		// Initialize dummy editor
		_documentEditor = new DocumentEditor(_config);
		_documentEditor.addListener(_documentEditorListener);
		_documentEditor.getDocument().addListener(_documentListener);
		initialize();
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
		_backgroundLayer.setColor(_config.getColor("document.background.color", Color.white));
		_pdfLayer = pageLayers.addPdfBuffer();
		_pdfLayer.setRepaintListener(new IRepaintListener() {
			@Override
			public void render() {
				onRenderPdfLayer();
			}
		});
		_pdfLayer.setDocument(_documentEditor.getDocument());
		_serverSyncLayer = pageLayers.addBackBuffer();
		_serverSyncLayer.setRepaintListener(new IPageRepaintListener() {
			@Override
			public void render(final IPageBackRenderer renderer) {
				onRenderServerSync();
			}
		});
		_clientOnlyLayer = pageLayers.addBackBuffer();
		_clientOnlyLayer.setRepaintListener(new IPageRepaintListener() {
			@Override
			public void render(final IPageBackRenderer renderer) {
				onRenderClientOnly();
			}
		});
		_frontLayer = pageLayers.addFrontBuffer();
		_pageRenderer.setDisplayedPageLayerBuffer(pageLayers);

		_panelTools = new JPanel();
		getContentPane().add(_panelTools, BorderLayout.WEST);
		setToolButtons(new IButtonAction[] { new ButtonTools(this), null,
				new ButtonNext(this), new ButtonPrevious(this),
				new ButtonSpinnerPage(this), null, new ButtonUndo(this),
				new ButtonRedo(this), null, new ButtonColor(this), null,
				new ButtonToggleFullscreen(this) });
		registerShortcuts();
		_config.write(false);
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
							final Point loc = button.getLocationOnScreen();
							loc.x += button.getWidth();
							action.perform(loc);
						}
					});
					_panelTools.add(button, gbc_control);
				}
			}
		}
		_buttonActions = buttons;
	}
	
	private void registerShortcuts() {
		List<KeyValue> shortcuts = _config.getAll("shortcut.");
		for (KeyValue item : shortcuts) {
			final String actionName = item.key.substring(9);
			int lastIndex = actionName.lastIndexOf('.');
			final String destination = actionName.substring(0, lastIndex);
			final String actionKeys = item.value.toString();
			IButtonAction button = null;
			for (IButtonAction b : _buttonActions) {
				if (b != null) {
					button = b.getButton(destination);
					if (button != null) {
						break;
					}
				}
			}
			if (button != null) {
				final IButtonAction refButton = button;
				final KeyStroke ks = KeyStroke.getKeyStroke(actionKeys);
				final Action action = new AbstractAction(actionName) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("Shortcut " + ks + " activated for " + actionName);
						final Point loc = MouseInfo.getPointerInfo().getLocation();
						refButton.perform(loc);
					}
				}; 
				this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, actionName);
				this.getRootPane().getActionMap().put(actionName, action);
				System.out.println("Shortcut " + ks + " registered for " + actionName);
			} else {
				System.out.println("Unknown Button " + destination);
			}
		}
	}
	
	@Override
	public void setToolbarVisible(boolean visible) {
		_panelTools.setVisible(visible);
	}
	
	@Override
	public boolean isToolbarVisible() {
		return _panelTools.isVisible();
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
		// _pageRenderer.clear();?
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
		// _pageRenderer.clear();?
	}
	
	protected void onDocumentChanging() {
		_pageRenderer.stopTool();
	}

	protected void onCurrentPageChanged() {
		// Update layers
		final DocumentPage currentPage = _documentEditor.getCurrentPage();
		_clientOnlyLayer.clear();
		currentPage.getClientOnlyLayer().render(_clientOnlyLayer);
		_serverSyncLayer.clear();
		currentPage.getServerSyncLayer().render(_serverSyncLayer);
		// Next PDF Page
		_pdfLayer.clear();
		_pdfLayer.setPageIndex(_documentEditor.getCurrentPage().getPdfPageIndex());
		// Render all
		_pageRenderer.clear();
	}

	protected void onActiveLayerChanged() {
	}

	protected void onDocumentChanged(final Document lastDocument) {
		if (lastDocument != null) {
			lastDocument.removeListener(_documentListener);
		}
		_documentEditor.getDocument().addListener(_documentListener);
		_pdfLayer.setDocument(_documentEditor.getDocument());
	}
	
	protected void onRenderPdfLayer() {
		_pageRenderer.clear();
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
	
	public DocumentConfig getConfig() {
		return _config;
	}

	public void setDocumentEditor(final DocumentEditor documentEditor) {
		if (_documentEditor != documentEditor) {
			if (_documentEditor != null) {
				_documentEditor.getDocument().removeListener(_documentListener);
				_documentEditor.removeListener(_documentEditorListener);
			}
			final DocumentEditor lastEditor = _documentEditor;
			_documentEditor = documentEditor;
			if (_documentEditor != null) {
				_documentEditor.addListener(_documentEditorListener);
				_documentEditor.getDocument().addListener(_documentListener);
				if (_pdfLayer != null) {
					_pdfLayer.setDocument(_documentEditor.getDocument());
				}
				// Fire listeners
				_documentEditor.setActiveLayerClientOnly(_documentEditor.isActiveLayerClientOnly());
				_documentEditor.setCurrentPage(_documentEditor.getCurrentPage());
				_documentEditorListener.documentChanged(null);
			}
			for (final IButtonAction button : _buttonActions) {
				if (button != null) {
					button.onUpdateEditor(lastEditor);
				}
			}
		}
	}
}
