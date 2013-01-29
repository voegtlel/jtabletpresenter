package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xsocket.connection.INonBlockingConnection;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

public class DownClient extends ClientSync {
	private final static Logger LOGGER = Logger.getLogger(DownClient.class.getName());
	
	private DocumentEditor _editor;
	
	private Pipe _readPipe;
	
	public DownClient(final String hostname, final int port, final DocumentEditor editor) throws IOException {
		super(hostname, port, UpServer.SERVER_MAGIC, UpServer.CLIENT_MAGIC);
		_editor = editor;
		_readPipe = Pipe.open();
		_readPipe.sink().configureBlocking(true);
		_readPipe.source().configureBlocking(true);
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
		try {
			connection.transferTo(_readPipe.sink(), connection.available());
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return true;
	}
	
	@Override
	protected void onThread() throws IOException {
		try {
			final BinaryDeserializer reader = SocketHelper.getReader(_readPipe.source());
			final BinarySerializer writer = SocketHelper.getWriter(_connection);
			performInit(writer, reader, _connection);
			while (true) {
				final IAction action = reader.readSerializableClass();
				LOGGER.log(Level.INFO, "Read action " + action.getClass().getName());
				try {
					action.perform(_editor);
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
