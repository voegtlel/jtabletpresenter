/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig.KeyValue;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.document.document.DocumentAdapter;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.document.editor.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.document.editor.DocumentEditorClient;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditorClient;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageLayerBufferPdf;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferBack;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferColor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferComposite;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferFront;
import de.freiburg.uni.tablet.presenter.editor.rendering.RenderCanvas;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonColor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonNext;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPrevious;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonRedo;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonSpinnerPage;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonToggleFullscreen;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonTools;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonUndo;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.freiburg.uni.tablet.presenter.xsocket.ClientListener;
import de.freiburg.uni.tablet.presenter.xsocket.DownClient;
import de.freiburg.uni.tablet.presenter.xsocket.UpClient;

/**
 * @author lukas
 * 
 */
public class JPageEditor extends JFrame implements IToolPageEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private RenderCanvas _pageRenderer;
	private JPanel _panelTools;

	private int _lastExtendedState;
	private Rectangle _lastBounds;

	private PageLayerBufferBack _clientOnlyLayer;
	private PageLayerBufferBack _serverSyncLayer;
	private PageLayerBufferFront _frontLayer;

	private IDocumentEditorClient _documentEditor = new DocumentEditorClient();

	private final DocumentListener _documentListener;

	private IButtonAction[] _buttonActions = new IButtonAction[] {};

	private PageLayerBufferColor _backgroundLayer;
	private IPageLayerBufferPdf _pdfLayer;

	private DocumentConfig _config;
	
	private DownClient _downClient;
	private UpClient _upClient;
	private boolean _isConnected;

	private JPanel _containerPanel;
	
	private IAction _lastSavedAction = null;

	/**
	 * Create the panel.
	 * @param configFilename
	 */
	public JPageEditor(final DocumentConfig config) {
		_documentListener = new DocumentAdapter() {
			@Override
			public void renderableRemoving(final IRenderable renderableAfter,
					final IRenderable renderable, final DocumentPage page) {
				onRenderableChanged(renderable, page);
			}
			
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
			public void renderableModifying(final IRenderable renderable,
					final DocumentPage page) {
				onRenderableChanged(renderable, page);
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
			public void documentChanged(final IClientDocument lastDocument) {
				onDocumentChanged(lastDocument);
			}
			
			@Override
			public void backDocumentChanged(final IDocument lastDocument) {
				onBackDocumentChanged(lastDocument);
			}
		});
		_config = config;
		initialize();
	}

	/**
	 * Initializes the frame
	 */
	private void initialize() {
		getContentPane().setLayout(new BorderLayout(0, 0));

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(final WindowEvent e) {
				onWindowOpened();
			}
			
			@Override
			public void windowClosing(final WindowEvent e) {
				onWindowClosing();
			}
		});
		
		addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(final WindowEvent e) {
			}
			
			@Override
			public void windowGainedFocus(final WindowEvent e) {
				onWindowFocus();
			}
		});
		
		final RenderCanvas pageRenderer = new RenderCanvas();
		_pageRenderer = pageRenderer;
		_pageRenderer.setLockPressure(!_config.getBoolean("editor.variableThickness", false));
		_pageRenderer.setVoidColor(_config.getColor("editor.voidColor", Color.BLACK));
		_containerPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e,
					final int condition, final boolean pressed) {
				if (e.getSource() != null && e.getSource() instanceof Component) {
					Component src = (Component) e.getSource();
					if (SwingUtilities.isDescendingFrom(src, _containerPanel) && src != _pageRenderer) {
						System.out.println("event " + e + " ignored");
						return true;
					}
				}
				return super.processKeyBinding(ks, e, condition, pressed);
			}
		};
		_containerPanel.setLayout(new OverlayLayout(_containerPanel));
		_containerPanel.add(pageRenderer.getContainerComponent());
		getContentPane().add(_containerPanel,
				BorderLayout.CENTER);

		final PageLayerBufferComposite pageLayers = new PageLayerBufferComposite(
				pageRenderer);
		_backgroundLayer = pageLayers.addColorBuffer();
		_backgroundLayer.setColor(_config.getColor("document.background.color", Color.white));
		if (_config.getBoolean("document.useDefaultRatio", false)) {
			_backgroundLayer.setDesiredRatio(_config.getFloat("document.defaultRatio", 4.0f/3.0f));
		}
		_pdfLayer = pageLayers.addPdfBuffer(_config.getInt("pdf.renderer", PageLayerBufferComposite.PDF_LIBRARY_TYPE_JPOD));
		_pdfLayer.setRatioEnabled(_config.getBoolean("pdf.useRatio", false));
		_serverSyncLayer = pageLayers.addBackBuffer();
		_clientOnlyLayer = pageLayers.addBackBuffer();
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
	}
	
	@Override
	public void wasSaved() {
		_lastSavedAction = _documentEditor.getHistory().getLastAction();
	}

	protected void onWindowClosing() {
		if (_documentEditor.getHistory().getLastAction() != _lastSavedAction) {
			int res = JOptionPane.showConfirmDialog(_pageRenderer.getContainerComponent(), "Save before closing?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION);
			if (res == JOptionPane.CANCEL_OPTION) {
				return;
			}
			if (res == JOptionPane.YES_OPTION) {
				if (!FileHelper.showSaveDialog(_pageRenderer.getContainerComponent(), this, FileHelper.stringToFilter(_config.getString("save.defaultExt", "jpd")))) {
					return;
				}
			}
		}
		
		_pageRenderer.setNormalTool(null);
		_pageRenderer.setInvertedTool(null);
		_pageRenderer.stop();
		System.out.println("Dispose");
		this.dispose();
		System.gc();
	}

	protected void onWindowOpened() {
		_pageRenderer.start();
		System.out.println("onWindowOpened");
	}
	
	protected void onWindowFocus() {
		_pageRenderer.requestFocusInWindow();
		System.out.println("onWindowFocus");
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
		final List<KeyValue> shortcuts = _config.getAll("shortcut.");
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
					public void actionPerformed(final ActionEvent e) {
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
	public void setToolbarVisible(final boolean visible) {
		_panelTools.setVisible(visible);
	}
	
	@Override
	public boolean isToolbarVisible() {
		return _panelTools.isVisible();
	}

	@Override
	public boolean isFullscreen() {
		return this.isUndecorated();
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
	
	@Override
	public JFrame getMainComponent() {
		return this;
	}
	
	@Override
	public void addDummyComponent(final Component c) {
		_containerPanel.add(c);
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
			_serverSyncLayer.requireRepaint(renderable, false);
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
		} else if (_documentEditor.getCurrentPage() != null) {
			_pdfLayer.setPdfPage(_documentEditor.getCurrentPage().getPdfPage());
		}
	}

	protected void onDocumentChanged(final IClientDocument lastDocument) {
		if (lastDocument != null) {
			lastDocument.removeListener(_documentListener);
		}
		if (_documentEditor.getDocument() != null) {
			_documentEditor.getDocument().addListener(_documentListener);
		}
	}
	
	protected void onBackDocumentChanged(final IDocument lastDocument) {
		if (lastDocument != null) {
			lastDocument.removeListener(_documentListener);
		}
		if (_documentEditor.getBackDocument() != null) {
			_documentEditor.getBackDocument().addListener(_documentListener);
		}
	}
	
	@Override
	public IPageBackRenderer getFrontRenderer() {
		return _frontLayer;
	}

	@Override
	public IDocumentEditorClient getDocumentEditor() {
		return _documentEditor;
	}

	@Override
	public IPageEditor getPageEditor() {
		return _pageRenderer;
	}
	
	@Override
	public DocumentConfig getConfig() {
		return _config;
	}
	
	@Override
	public boolean isConnectedUp() {
		return _isConnected && (_upClient != null);
	}
	
	@Override
	public boolean isConnectedDown() {
		return _isConnected && (_downClient != null);
	}
	
	@Override
	public void disconnect() {
		if (_upClient != null) {
			_upClient.stop();
			_upClient = null;
		}
		if (_downClient != null) {
			_downClient.stop();
			_downClient = null;
		}
		_isConnected = false;
	}
	
	@Override
	public void connectUpClient(final String hostname, final int port, final boolean initDown, final String name, final String authToken) throws IOException {
		disconnect();
		_upClient = new UpClient(hostname, port, _documentEditor);
		_upClient.setName(name);
		_upClient.setSyncDownInit(initDown);
		_upClient.setAuthToken(authToken);
		_upClient.addListener(new ClientListener() {
			@Override
			public void onError(final Exception e) {
			}
			
			@Override
			public void onDisconnected() {
				_isConnected = false;
			}
			
			@Override
			public void onConnected() {
				_isConnected = true;
			}
		});
		_upClient.start();
		_isConnected = true;
	}
	
	@Override
	public void connectDownClient(final String hostname, final int port, final String name, final String authToken) throws IOException {
		disconnect();
		
		_downClient = new DownClient(hostname, port, _documentEditor);
		_downClient.setName(name);
		_downClient.setAuthToken(authToken);
		_downClient.addListener(new ClientListener() {
			@Override
			public void onError(final Exception e) {
			}
			
			@Override
			public void onDisconnected() {
				_isConnected = false;
			}
			
			@Override
			public void onConnected() {
				_isConnected = true;
			}
		});
		_downClient.start();
		_isConnected = true;
	}
	
	@Override
	public void dispose() {
		for (IButtonAction ba : _buttonActions) {
			if (ba != null) {
				ba.dispose();
			}
		}
		super.dispose();
	}
}
