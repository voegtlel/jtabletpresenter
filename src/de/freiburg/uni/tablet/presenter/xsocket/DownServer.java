package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xsocket.connection.INonBlockingConnection;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

public class DownServer extends ServerSync {
	public static final int CLIENT_MAGIC = 0x63263533;
	public static final int SERVER_MAGIC = 0x15434623;
	
	private final static Logger LOGGER = Logger.getLogger(DownServer.class.getName());
	
	private DocumentEditor _editor;
	
	public DownServer(final int port, final DocumentEditor editor) throws IOException {
		super(port, SERVER_MAGIC, CLIENT_MAGIC);
		_editor = editor;
	}
	
	@Override
	protected boolean onConnect(final INonBlockingConnection connection)
			throws IOException {
		final DownClientSync sync = new DownClientSync(connection);
		sync.start();
		return true;
	}
	
	private class DownClientSync extends ClientSync {
		private final Pipe _readPipe;
		
		public DownClientSync(final INonBlockingConnection connection) throws IOException {
			super(connection, _clientId++);
			_readPipe = Pipe.open();
			_readPipe.sink().configureBlocking(true);
			_readPipe.source().configureBlocking(true);
		}
		
		@Override
		public void start() throws IOException {
			onConnect(_connection);
		}
		
		@Override
		protected boolean onConnect(final INonBlockingConnection connection)
				throws IOException {
			if (!super.onConnect(connection)) {
				return false;
			}
			startThread();
			return true;
		}
		
		@Override
		protected boolean onData(final INonBlockingConnection connection)
				throws IOException {
			if (!super.onData(connection)) {
				return false;
			}
			connection.transferTo(_readPipe.sink(), connection.available());
			return true;
		}
		
		@Override
		protected void onThread() throws IOException {
			try {
				LOGGER.log(Level.INFO, "Init client");
				final BinaryDeserializer reader = new BinaryDeserializer(_readPipe.source());
				final BinarySerializer writer = new BinarySerializer(_connection);
				performInit(writer, reader, _connection);
				while (true) {
					final IAction action = reader.readSerializableClass();
					LOGGER.log(Level.INFO, "Read action " + action.getClass().getName());
					try {
						if (action instanceof SetClientDocumentAction) {
							SetServerDocumentAction serverAction = ((SetClientDocumentAction) action).getServerAction();
							serverAction.perform(_editor);
						} else {
							action.perform(_editor);
						}
					} catch (Exception e) {
						throw new IOException(e);
					}
					LOGGER.log(Level.INFO, "Action " + action.getClass().getName() + " performed");
				}
			} finally {
				_connection.close();
			}
		}
	}
}
