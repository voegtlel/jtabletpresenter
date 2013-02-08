package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

public class UpServer extends ServerSync {
	public static final int SERVER_MAGIC = 0x81235342;
	public static final int CLIENT_MAGIC = 0x32235039;
	
	private final static Logger LOGGER = Logger.getLogger(UpServer.class.getName());
	
	private final IDocumentEditor _editor;
	
	public UpServer(final int port, final IDocumentEditor editor) throws IOException {
		super(port, SERVER_MAGIC, CLIENT_MAGIC);
		_editor = editor;
	}
	
	@Override
	protected void onConnect(final SocketChannel connection)
			throws IOException {
		final UpClientSync clientSync = new UpClientSync(connection);
		clientSync.start();
	}
	
	private class UpClientSync extends ClientSync {
		private DocumentHistoryListener _documentHistoryListener;
		private LinkedElementList<IAction> _actions = new LinkedElementList<IAction>();
		
		private Object _threadSync = new Object();
		
		public UpClientSync(final SocketChannel connection) throws IOException {
			super(connection, _clientId++);
			
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
		
		protected void onActionPerformed(final IAction action) {
			synchronized (_threadSync) {
				_actions.addLast(action);
				_threadSync.notify();
			}
		}
		
		@Override
		public void start() throws IOException {
			startThread();
		}
		
		@Override
		protected void onThread() throws IOException {
			synchronized (_editor.getHistory()) {
				_editor.getHistory().addListener(_documentHistoryListener);
			}
			
			DummyReadThread readThread = null;
			try {
				LOGGER.log(Level.INFO, "Init client");
				final BinarySerializer writer = new BinarySerializer(_connection);
				final BinaryDeserializer reader = new BinaryDeserializer(_connection);
				// Exchange init
				performInit(writer, reader, _connection);
				// Send initial data
				LOGGER.log(Level.INFO, "Serialize init doc");
				writer.writeSerializableClass(new SetServerDocumentAction(_editor.getFrontDocument(), _editor.getCurrentPage()));
				writer.flush();
				LOGGER.log(Level.INFO, "Serialize init doc done");
				fireConnected();
				readThread = new DummyReadThread(_connection, new DummyReadThread.IDisconnectListener() {
					@Override
					public void onDisconnect() {
						try {
							synchronized (_threadSync) {
								_connection.close();
								_running = false;
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
					writer.writeSerializableClass(action);
					writer.flush();
					LOGGER.log(Level.INFO, "Serialize " + action.getClass().getName() + " done");
				}
			} finally {
				synchronized (_editor.getHistory()) {
					_editor.getHistory().removeListener(_documentHistoryListener);
				}
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
}
