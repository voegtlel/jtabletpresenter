package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xsocket.connection.BlockingConnection;
import org.xsocket.connection.ConnectionUtils;
import org.xsocket.connection.IBlockingConnection;
import org.xsocket.connection.INonBlockingConnection;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

public class UpServer extends ServerSync {
	public static final int SERVER_MAGIC = 0x81235342;
	public static final int CLIENT_MAGIC = 0x32235039;
	
	private final static Logger LOGGER = Logger.getLogger(UpServer.class.getName());
	
	private final DocumentEditor _editor;
	
	public UpServer(final int port, final DocumentEditor editor) throws IOException {
		super(port, SERVER_MAGIC, CLIENT_MAGIC);
		_editor = editor;
	}
	
	@Override
	protected boolean onConnect(final INonBlockingConnection connection)
			throws IOException {
		final UpClientSync clientSync = new UpClientSync(connection);
		clientSync.start();
		return true;
	}
	
	private class UpClientSync extends ClientSync {
		private DocumentHistoryListener _documentHistoryListener;
		private LinkedElementList<IAction> _actions = new LinkedElementList<IAction>();
		
		private Object _threadSync = new Object();
		
		private IBlockingConnection _blockingConnection;
		
		public UpClientSync(final INonBlockingConnection connection) throws IOException {
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
			onConnect(_connection);
		}
		
		@Override
		protected boolean onConnect(final INonBlockingConnection connection) throws IOException {
			if (!super.onConnect(connection)) {
				return false;
			}
			// Register blocking connection
			final INonBlockingConnection nbc = ConnectionUtils.synchronizedConnection(connection);
			_blockingConnection = new BlockingConnection(nbc);
			startThread();
			return true;
		}
		
		@Override
		protected void onThread() throws IOException {
			synchronized (_editor.getHistory()) {
				_editor.getHistory().addListener(_documentHistoryListener);
			}
			
			try {
				final BinarySerializer writer = new BinarySerializer(_blockingConnection);
				final BinaryDeserializer reader = new BinaryDeserializer(_blockingConnection);
				// Exchange init
				performInit(writer, reader, _blockingConnection);
				// Send initial data
				LOGGER.log(Level.INFO, "Serialize init doc");
				writer.writeSerializableClass(new SetClientDocumentAction(_editor.getDocument(), _editor.getCurrentPageIndex()));
				LOGGER.log(Level.INFO, "Serialize init doc done");
				while (true) {
					final IAction action;
					// Get next action
					synchronized (_threadSync) {
						while (_actions.isEmpty()) {
							try {
								_threadSync.wait();
							} catch (InterruptedException e) {
							}
							_threadSync.notifyAll();
						}
						// Pop action
						action = _actions.getFirst().getData();
						_actions.removeFirst();
					}
					// Write action
					LOGGER.log(Level.INFO, "Serialize " + action.getClass().getName());
					writer.writeSerializableClass(action);
					LOGGER.log(Level.INFO, "Serialize " + action.getClass().getName() + " done");
				}
			} finally {
				synchronized (_editor.getHistory()) {
					_editor.getHistory().removeListener(_documentHistoryListener);
				}
				_blockingConnection.close();
			}
		}
	}
}
