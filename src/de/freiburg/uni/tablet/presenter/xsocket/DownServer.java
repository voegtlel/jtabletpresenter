package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	protected void onConnect(final SocketChannel connection)
			throws IOException {
		final DownClientSync sync = new DownClientSync(connection);
		sync.start();
	}
	
	private class DownClientSync extends ClientSync {
		public DownClientSync(final SocketChannel connection) throws IOException {
			super(connection, _clientId++);
		}
		
		@Override
		public void start() throws IOException {
			startThread();
		}
		
		@Override
		protected void onThread() throws IOException {
			try {
				LOGGER.log(Level.INFO, "Init client");
				final BinaryDeserializer reader = new BinaryDeserializer(_connection);
				final BinarySerializer writer = new BinarySerializer(_connection);
				// Initial data
				performInit(writer, reader, _connection);
				// Check exchange
				if (reader.readBoolean()) {
					LOGGER.log(Level.INFO, "Serialize init doc");
					// Here the client requests to receive the server document first
					writer.writeSerializableClass(new SetServerDocumentAction(_editor.getDocument(), _editor.getCurrentPageIndex()));
					writer.flush();
					LOGGER.log(Level.INFO, "Serialize init doc done");
				}
				fireConnected();
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
				if (_connection != null) {
					_connection.close();
				}
				onDisconnect();
			}
		}
	}
}
