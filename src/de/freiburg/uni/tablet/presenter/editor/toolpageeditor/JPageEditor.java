/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.Provider;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig.KeyValue;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEntity;
import de.freiburg.uni.tablet.presenter.document.document.DocumentAdapter;
import de.freiburg.uni.tablet.presenter.document.document.IClientDocument;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.document.editor.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.document.editor.DocumentEditorClient;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditorClient;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.IPageLayerBufferBackground;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferBack;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferColor;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferComposite;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferFront;
import de.freiburg.uni.tablet.presenter.editor.rendering.RenderCanvas;
import de.freiburg.uni.tablet.presenter.editor.rendering.toolbar.IToolbarItem;
import de.freiburg.uni.tablet.presenter.editor.rendering.toolbar.ToolbarAction;
import de.freiburg.uni.tablet.presenter.editor.rendering.toolbar.ToolbarFill;
import de.freiburg.uni.tablet.presenter.editor.rendering.toolbar.ToolbarRenderer;
import de.freiburg.uni.tablet.presenter.editor.rendering.toolbar.ToolbarSpace;
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
	private JPageToolBar _panelTools;

	private int _lastExtendedState;
	private Rectangle _lastBounds;

	private PageLayerBufferBack _clientOnlyLayer;
	private PageLayerBufferBack _serverSyncLayer;
	private PageLayerBufferFront _frontLayer;

	private IDocumentEditorClient _documentEditor = new DocumentEditorClient();

	private final DocumentListener _documentListener;

	private IButtonAction[] _buttonActions = new IButtonAction[] {};

	private PageLayerBufferColor _backgroundColorLayer;
	private IPageLayerBufferBackground _backgroundDataLayer;

	private DocumentConfig _config;
	
	private DownClient _downClient;
	private UpClient _upClient;
	private boolean _isConnected;

	private JPanel _containerPanel;
	
	private IAction _lastSavedAction = null;
	
	private Provider _globalHotkeyProvider = null;

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
			public void backgroundEntityChanged(final DocumentPage documentPage,
					final IEntity lastBackgroundEntity) {
				onPdfPageChanged(documentPage, lastBackgroundEntity);
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
		
		_pageRenderer = new RenderCanvas();
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
		_containerPanel.add(_pageRenderer.getContainerComponent());
		getContentPane().add(_containerPanel,
				BorderLayout.CENTER);
		
		final PageLayerBufferComposite pageLayers = new PageLayerBufferComposite(
				_pageRenderer);
		_backgroundColorLayer = pageLayers.addColorBuffer();
		_backgroundColorLayer.setColor(_config.getColor("document.background.color", Color.white));
		if (_config.getBoolean("document.useDefaultRatio", false)) {
			_backgroundColorLayer.setDesiredRatio(_config.getFloat("document.defaultRatio", 4.0f/3.0f));
		}
		_backgroundDataLayer = pageLayers.addBackgroundBuffer(_config.getInt("pdf.renderer", PageLayerBufferComposite.PDF_LIBRARY_TYPE_JPOD));
		_backgroundDataLayer.setRatioEnabled(_config.getBoolean("pdf.useRatio", false));
		_serverSyncLayer = pageLayers.addBackBuffer();
		_clientOnlyLayer = pageLayers.addBackBuffer();
		_frontLayer = pageLayers.addFrontBuffer();
		_pageRenderer.setDisplayedPageLayerBuffer(pageLayers);

		_panelTools = new JPageToolBar();
		getContentPane().add(_panelTools, BorderLayout.WEST);
		_buttonActions = new IButtonAction[] { new ButtonTools(this), null,
				new ButtonNext(this), new ButtonPrevious(this),
				new ButtonSpinnerPage(this), null, new ButtonUndo(this),
				new ButtonRedo(this), null, new ButtonColor(this), null,
				new ButtonToggleFullscreen(this) };
		_panelTools.setToolButtonsVertical(_buttonActions);
		
		registerShortcuts();
		buildToolbar();
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
		if (_globalHotkeyProvider != null) {
			_globalHotkeyProvider.reset();
			_globalHotkeyProvider.stop();
			_globalHotkeyProvider = null;
		}
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
	
	private void buildToolbar() {
		String toolbarOrientation = _config.getString("toolbar.orientation", "NONE");
		int orientation = ToolbarRenderer.getOrientation(toolbarOrientation);
		if (orientation == ToolbarRenderer.ORIENTATION_NONE) {
			return;
		}
		
		final List<KeyValue> toolbarEntries = _config.getAll("toolbar.");
		final List<IToolbarItem> actions = new ArrayList<>(toolbarEntries.size());
		for (KeyValue item : toolbarEntries) {
			final String actionId = item.key.substring(8);
			if (actionId.equals("orientation") || actionId.equals("compactSize") || actionId.equals("compactOpacity")) {
				continue;
			}
			final String actionName = item.value.toString();
			if (actionName.equals("space")) {
				actions.add(new ToolbarSpace());
			} else if (actionName.equals("fill")) {
				actions.add(new ToolbarFill());
			} else {
				IButtonAction button = null;
				for (IButtonAction b : _buttonActions) {
					if (b != null) {
						button = b.getButton(actionName);
						if (button != null) {
							break;
						}
					}
				}
				if (button != null) {
					System.out.println("Adding toolbar action " + actionId + " for " + actionName);
					actions.add(new ToolbarAction(button.getText(), button.getImageResource(), button));
				} else {
					System.out.println("Unknown action " + actionName);
				}
			}
		}
		IToolbarItem[] toolbarItems = actions.toArray(new IToolbarItem[actions.size()]);
		_pageRenderer.setToolbar(toolbarItems, orientation, _config.getInt("toolbar.compactSize", 15), _config.getFloat("toolbar.compactOpacity", 0.25f));
	}
	
	private void registerShortcuts() {
		final List<KeyValue> shortcuts = _config.getAll("shortcut.");
		for (KeyValue item : shortcuts) {
			final String actionName = item.key.substring(9);
			int lastIndex = actionName.lastIndexOf('.');
			String destination = actionName.substring(0, lastIndex);
			final boolean isGlobal = destination.endsWith("global");
			if (isGlobal) {
				destination = destination.substring(0, destination.length() - 7);
			}
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
				if (ks == null) {
					System.err.println("Invalid shortcut: " + actionKeys + ", shortcut not registered");
					continue;
				}
				if (isGlobal) {
					if (_globalHotkeyProvider == null) {
						_globalHotkeyProvider = Provider.getCurrentProvider(true);
					}
					HotKeyListener listener = new HotKeyListener() {
						@Override
						public void onHotKey(final HotKey hotkey) {
							System.out.println("Global shortcut " + ks + " activated for " + actionName);
							final Point loc = MouseInfo.getPointerInfo().getLocation();
							refButton.perform(loc);
						}
					};
					_globalHotkeyProvider.register(ks, listener);
					System.out.println("Global shortcut " + ks + " registered for " + actionName);
				} else {
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
				}
			} else {
				System.out.println("Unknown action " + destination);
			}
		}
		
		// Paste action
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), TransferHandler.getPasteAction().getValue(Action.NAME));
		this.getRootPane().getActionMap().put(TransferHandler.getPasteAction().getValue(Action.NAME), new ActionWrapper(this.getRootPane(), TransferHandler.getPasteAction()));
		this.getRootPane().setTransferHandler(new EditorTransferHandler(this));
		_pageRenderer.setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new EditorTransferHandler(this)));
		this.getRootPane().setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new EditorTransferHandler(this)));
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
		
		_pageRenderer.updateTool();
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
		
		_pageRenderer.updateTool();
	}
	
	protected void onPdfPageChanged(final DocumentPage documentPage,
			final IEntity lastBackgroundEntity) {
		if ((documentPage == _documentEditor.getCurrentPage()) || (documentPage == _documentEditor.getCurrentBackPage())) {
			_backgroundDataLayer.setBackgroundEntity(documentPage.getBackgroundEntity());
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
		DocumentPage documentPage = _documentEditor.getCurrentBackPage();
		if (documentPage == null) {
			documentPage = _documentEditor.getCurrentPage();
		}
		if (documentPage != null) {
			_backgroundDataLayer.setBackgroundEntity(documentPage.getBackgroundEntity());
		}
		_pageRenderer.updateTool();
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
