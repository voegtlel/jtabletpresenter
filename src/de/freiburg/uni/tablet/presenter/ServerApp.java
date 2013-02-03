package de.freiburg.uni.tablet.presenter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.document.DocumentListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.FileHelper;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.xsocket.DownServer;
import de.freiburg.uni.tablet.presenter.xsocket.UpServer;

public class ServerApp {
	private static final Logger LOGGER = Logger.getLogger(ServerApp.class.getName());
	
	private DocumentEditor _editor;
	
	private UpServer _upServer;
	private DownServer _downServer;
	
	private Thread _autosaveThread;
	private boolean _autosaveRunning;
	private long _autosaveIntervall;
	private long _autosaveLastChange;
	private String _autosavePath;
	private Object _autosaveThreadSync = new Object();

	protected DocumentListener _documentListener;
	
	private final SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getLogger("de.freiburg.uni.tablet.presenter").setLevel(Level.ALL);
		
		String configFile = "config.ini";
		if (args.length > 0) {
			configFile = args[0];
		}
		
		ServerApp serverApp = new ServerApp(new DocumentConfig(configFile));
		
		serverApp.start();
		try {
			System.out.println("Ready");
			System.out.println("Press any key to exit");
			// Wait for exit
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverApp.stop();
	}
	
	public ServerApp(final DocumentConfig config) {
		String name = config.getString("server.name", "Server");
		String authTokenDown = config.getString("server.down.authToken", "");
		String authTokenUp = config.getString("server.up.authToken", "");
		int portDown = config.getInt("server.down.port", 8025);
		int portUp = config.getInt("server.up.port", 8025);
		
		boolean timedAutosave = config.getBoolean("server.timedAutosave.enabled", false);
		_autosaveIntervall = config.getLong("server.timedAutosave.intervall", 600000);
		_autosavePath = config.getString("server.timedAutosave.path", "autosave-{time}.jpd");
		
		final boolean autosaveDocChange = config.getBoolean("server.changeDocumentAutosave.enabled", true);
		final String autosaveDocPath = config.getString("server.changeDocumentAutosave.path", "autosave-{time}.jpd");
		
		_editor = new DocumentEditor();
		// If required, additional events can be placed here
		_documentListener = new DocumentListener() {
			@Override
			public void pageInserted(IEditableDocument document,
					DocumentPage prevPage, DocumentPage page) {
				_autosaveLastChange = System.currentTimeMillis();
			}

			@Override
			public void pageRemoved(IEditableDocument document,
					DocumentPage prevPage, DocumentPage page) {
				_autosaveLastChange = System.currentTimeMillis();
			}

			@Override
			public void renderableAdded(IRenderable renderableAfter,
					IRenderable renderable, DocumentPage page) {
				_autosaveLastChange = System.currentTimeMillis();
			}

			@Override
			public void renderableRemoved(IRenderable renderableAfter,
					IRenderable renderable, DocumentPage page) {
				_autosaveLastChange = System.currentTimeMillis();
			}

			@Override
			public void pdfPageChanged(DocumentPage documentPage,
					PdfPageSerializable lastPdfPage) {
				_autosaveLastChange = System.currentTimeMillis();
			}

			@Override
			public void renderableModified(IRenderable renderable,
					DocumentPage page) {
				_autosaveLastChange = System.currentTimeMillis();
			}

			@Override
			public void renderableModifyEnd(IRenderable renderable,
					DocumentPage page) {
				_autosaveLastChange = System.currentTimeMillis();
			}
		};
		_editor.addListener(new DocumentEditorAdapter() {
			@Override
			public void documentChanged(final IEditableDocument lastDocument) {
				if (lastDocument != null) {
					lastDocument.removeListener(_documentListener);
				}
				if (_editor.getDocument() != null) {
					_editor.getDocument().addListener(_documentListener);
					if (autosaveDocChange) {
						try {
							saveDocument(lastDocument, autosaveDocPath);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		_editor.setDocument(new ServerDocument(1));
		
		try {
			_upServer = new UpServer(portUp, _editor);
			_upServer.setName(name);
			_upServer.setAuthToken(authTokenUp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			_downServer = new DownServer(portDown, _editor);
			_downServer.setName(name);
			_downServer.setAuthToken(authTokenDown);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (timedAutosave) {
			_autosaveThread = new Thread() {
				@Override
				public void run() {
					autosaveThread();
				}
			};
		}
	}

	public void start() {
		LOGGER.log(Level.INFO, "Start");
		
		_upServer.start();
		_downServer.start();
		
		if (_autosaveThread != null) {
			_autosaveRunning = true;
			_autosaveThread.start();
		}
	}

	public void stop() {
		LOGGER.log(Level.INFO, "Stopping");
		
		_upServer.stop();
		_downServer.stop();
		
		if (_autosaveThread != null) {
			try {
				synchronized (_autosaveThreadSync) {
					_autosaveRunning = false;
					_autosaveThreadSync.notifyAll();
				}
				_autosaveThread.join(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		LOGGER.log(Level.INFO, "Finished");
	}
	
	protected void saveDocument(final IEditableDocument document, final String basePath) throws IOException {
		final String dateString = _dateFormat.format(new Date());
		final File autosavePath = new File(basePath.replaceAll("\\{time\\}", dateString));
		LOGGER.log(Level.INFO, "Saving to " + autosavePath);
		FileHelper.saveDocument(document, autosavePath);
		LOGGER.log(Level.INFO, "Saving done");
	}
	
	protected void autosaveThread() {
		synchronized (_autosaveThreadSync) {
			while (true) {
				long waitEnd = System.currentTimeMillis() + _autosaveIntervall;
				while ((_autosaveLastChange + _autosaveIntervall < System.currentTimeMillis()) || (waitEnd > System.currentTimeMillis())) {
					waitEnd = System.currentTimeMillis() + _autosaveIntervall;
					while (waitEnd > System.currentTimeMillis()) {
						try {
							_autosaveThreadSync.wait(waitEnd - System.currentTimeMillis());
						} catch (InterruptedException e) {
						}
					}
				}
				if (!_autosaveRunning) {
					return;
				}
				try {
					saveDocument(_editor.getDocument(), _autosavePath);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, "Autosaving canceled: " + e.toString());
					return;
				}
			}
		}
	}
}
