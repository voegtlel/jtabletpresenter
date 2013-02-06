package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.IDocumentEditor;

public class DownClient extends ClientSync {
	private final static Logger LOGGER = Logger.getLogger(DownClient.class.getName());
	
	private IDocumentEditor _editor;
	
	public DownClient(final String hostname, final int port, final IDocumentEditor editor) throws IOException {
		super(hostname, port, UpServer.SERVER_MAGIC, UpServer.CLIENT_MAGIC);
		_editor = editor;
	}
	
	@Override
	protected void onThread() throws IOException {
		try {
			openSocket();
			final BinaryDeserializer reader = new BinaryDeserializer(_connection);
			final BinarySerializer writer = new BinarySerializer(_connection);
			performInit(writer, reader, _connection);
			fireConnected();
			while (_running) {
				try {
					final IAction action = reader.readSerializableClass();
					LOGGER.log(Level.INFO, "Read action " + action.getClass().getName());
					if (action instanceof SetServerDocumentAction) {
						final SetClientDocumentAction clientAction = ((SetServerDocumentAction) action).getClientAction();
						clientAction.perform(_editor);
					} else {
						action.perform(_editor);
					}
					LOGGER.log(Level.INFO, "Action " + action.getClass().getName() + " performed");
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
				catch (Exception e) {
					e.printStackTrace();
					throw new IOException(e);
				}
			}
		} finally {
			_connection.close();
			onDisconnect();
		}
	}
}
