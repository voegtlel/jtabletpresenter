package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

public class UpClient extends ClientSync {
	private final static Logger LOGGER = Logger.getLogger(UpClient.class.getName());
	
	private final IDocumentEditor _editor;
	
	private DocumentHistoryListener _documentHistoryListener;
	private LinkedElementList<IAction> _actions = new LinkedElementList<IAction>();
	
	private Object _threadSync = new Object();
	
	private boolean _syncDownInit = false;
	
	public UpClient(final String hostname, final int port, final IDocumentEditor editor) throws IOException {
		super(hostname, port, DownServer.SERVER_MAGIC, DownServer.CLIENT_MAGIC);
		_editor = editor;
		
		_documentHistoryListener = new DocumentHistoryListener() {
			@Override
			public void actionPerformed(final IAction action) {
				onActionPerformed(action);
			}
			
			@Override
			public void actionAdded(final IAction action) {
				onActionPerformed(action);
			}
		};
	}
	
	/**
	 * If enabled, on connect the page is synched down before sending
	 * @param syncDownInit
	 */
	public void setSyncDownInit(final boolean syncDownInit) {
		_syncDownInit = syncDownInit;
	}
	
	protected void onActionPerformed(final IAction action) {
		synchronized (_threadSync) {
			_actions.addLast(action);
			_threadSync.notify();
		}
	}
	
	@Override
	protected void stopThread() {
		synchronized (_threadSync) {
			_running = false;
			_threadSync.notifyAll();
		}
		super.stopThread();
	}
	
	@Override
	protected void onThread() throws IOException {
		synchronized (_editor.getHistory()) {
			_editor.getHistory().addListener(_documentHistoryListener);
		}
		
		DummyReadThread readThread = null;
		try {
			LOGGER.log(Level.INFO, "Open socket");
			openSocket();
			LOGGER.log(Level.INFO, "Initialize connection");
			
			final BinarySerializer writer = new BinarySerializer(_connection);
			final BinaryDeserializer reader = new BinaryDeserializer(_connection);
			// Exchange init
			performInit(writer, reader, _connection);
			writer.writeBoolean(_syncDownInit);
			writer.flush();
			LOGGER.log(Level.INFO, "Initialize connection done");
			if (_syncDownInit) {
				LOGGER.log(Level.INFO, "Deserialize init doc");
				try {
					final IEditableDocument document = reader.readObjectTable();
					final DocumentPage currentPage = reader.readObjectTable();
					_editor.setDocument(document);
					_editor.setCurrentPage(currentPage);
				} catch (Exception e) {
					e.printStackTrace();
					throw new IOException(e);
				}
				LOGGER.log(Level.INFO, "Deserialize init doc done");
			} else {
				// Send initial data
				LOGGER.log(Level.INFO, "Serialize init doc");
				writer.writeSerializableClass(new SetServerDocumentAction(_editor.getFrontDocument(), _editor.getCurrentPage()));
				writer.flush();
				LOGGER.log(Level.INFO, "Serialize init doc done");
			}
			fireConnected();
			readThread = new DummyReadThread(_connection, new DummyReadThread.IDisconnectListener() {
				@Override
				public void onDisconnect() {
					try {
						LOGGER.log(Level.INFO, "onDisconnect");
						synchronized (_threadSync) {
							_connection.close();
							_running = false;
							LOGGER.log(Level.INFO, "sync");
							_threadSync.notifyAll();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			readThread.start();
			while (_running) {
				final IAction action;
				// Get next action
				synchronized (_threadSync) {
					while (_running && _actions.isEmpty()) {
						try {
							_threadSync.wait();
						} catch (InterruptedException e) {
						}
						_threadSync.notifyAll();
					}
					if (!_running) {
						LOGGER.log(Level.INFO, "not running any more");
						break;
					}
					// Pop action
					action = _actions.getFirst().getData();
					_actions.removeFirst();
				}
				// Write action
				LOGGER.log(Level.INFO, "Serialize " + action.getClass().getName());
				try {
					writer.writeSerializableClass(action);
					writer.flush();
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
				LOGGER.log(Level.INFO, "Serialize " + action.getClass().getName() + " done");
			}
		} finally {
			synchronized (_editor.getHistory()) {
				_editor.getHistory().removeListener(_documentHistoryListener);
			}
			LOGGER.log(Level.INFO, "Close connection");
			if (_connection != null) {
				_connection.close();
			}
			if (readThread != null) {
				readThread.stop();
			}
			onDisconnect();
		}
	}
}
