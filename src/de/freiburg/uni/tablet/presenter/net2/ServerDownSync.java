package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

public class ServerDownSync extends ServerSync {
	private DocumentEditor _editor;
	
	public ServerDownSync(final DocumentEditor editor, final SocketChannel socket, final String requiredAuthToken) {
		super(socket, requiredAuthToken);
		_editor = editor;
	}
	
	private void receiveThread() throws IOException {
		while (_running) {
			if (!_packageInputStreamSync.nextPackage()) {
				System.out.println("Down cancel package");
				break;
			}
			try {
				final IAction action = _readerSync.readSerializableClass();
				System.out.println("Read action " + action.getClass().getName());
				action.perform(_editor);
			} catch (IOException e) {
				throw e;
			}
		}
	}
	
	@Override
	protected void dataThread() {
		try {
			connectThread();
			initData();
			if (!_authToken.equals(_requiredAuthToken)) {
				throw new IOException("Auth tokens do not match (got " + _authToken + ")");
			}
			fireConnected();
			// Start threads
			receiveThread();
		} catch (final IOException e) {
			fireError(e);
			disconnect();
			fireDisconnected();
		}
	}
}
