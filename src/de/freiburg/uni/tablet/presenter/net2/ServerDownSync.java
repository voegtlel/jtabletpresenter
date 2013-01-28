package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

public class ServerDownSync extends ServerSync {
	private static final Logger LOGGER = Logger.getLogger(ServerDownSync.class.getName());
	
	private DocumentEditor _editor;
	
	public ServerDownSync(final DocumentEditor editor, final SocketChannel socket, final String requiredAuthToken) {
		super(socket, requiredAuthToken);
		_editor = editor;
	}
	
	private void receiveThread() throws IOException {
		while (_running) {
			LOGGER.log(Level.INFO, "Receive next package");
			if (!_packageInputStreamSync.nextPackage()) {
				LOGGER.log(Level.WARNING, "Down cancel package");
				break;
			}
			try {
				final IAction action = _readerSync.readSerializableClass();
				LOGGER.log(Level.INFO, "Read action " + action.getClass().getName());
				action.perform(_editor);
				LOGGER.log(Level.INFO, "Action " + action.getClass().getName() + " performed");
			} catch (IOException e) {
				throw e;
			}
		}
	}
	
	@Override
	protected void dataThread() {
		try {
			connectThread();
			if (!initData()) {
				throw new IOException("Invalid server");
			}
			if (!_authToken.equals(_requiredAuthToken)) {
				throw new IOException("Auth tokens do not match (got " + _authToken + ")");
			}
			fireConnected();
			// Start threads
			receiveThread();
		} catch (final IOException e) {
			fireError(e);
			disconnect();
		}
	}
}
