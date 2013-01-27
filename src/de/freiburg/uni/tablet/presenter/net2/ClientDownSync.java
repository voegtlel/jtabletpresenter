package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;

public class ClientDownSync extends ClientSync {
	protected final DocumentEditor _editor;
	protected final IPageEditor _pageEditor;
	
	public ClientDownSync(final DocumentEditor editor, final IPageEditor pageEditor) {
		_editor = editor;
		_pageEditor = pageEditor;
	}
	
	private void receiveThread() throws IOException {
		while (_running) {
			if (!_packageInputStreamSync.nextPackage()) {
				System.out.println("Down cancel package");
				break;
			}
			final IAction action = _readerSync.readSerializableClass();
			_pageEditor.suspendRepaint();
			System.out.println("Read action " + action.getClass().getName());
			action.perform(_editor);
			_pageEditor.resumeRepaint();
		}
	}
	
	@Override
	protected void dataThread(final String host, final int port, final long timeout) {
		try {
			if (!connectThread(host, port, timeout)) {
				fireDisconnected();
				return;
			}
			// Perform initial data
			if (!initData()) {
				throw new IOException("Handshake error");
			}
			// Receive initial document
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
