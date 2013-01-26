package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;

public class ServerUpSync extends ServerSync {
	public ServerUpSync(final DocumentHistory history, final SocketChannel socket, final String requiredAuthToken) {
		super(socket, requiredAuthToken);
		history.addListener(new DocumentHistoryListener() {
			@Override
			public void actionPerformed(final IAction action) {
				onActionPerformed(action);
			}
			
			@Override
			public void actionAdded(final IAction action) {
				onActionPerformed(action);
			}
		});
	}
	
	public void onActionPerformed(final IAction action) {
		if (_running) {
			try {
				if (action instanceof SetServerDocumentAction) {
					System.out.println("Write up " + action.getClass().getName());
					SetClientDocumentAction clientAction = ((SetServerDocumentAction)action).getClientAction();
					System.out.println(" --> " + clientAction.getClass().getName());
					_writerAsync.writeSerializableClass(clientAction);
				} else {
					System.out.println("Write up " + action.getClass().getName());
					_writerAsync.writeSerializableClass(action);
				}
				_writerAsync.flush();
				_packageOutputStreamAsync.flush();
				_packageOutputStreamAsync.nextPackage();
			} catch (IOException e) {
				e.printStackTrace();
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
			sendThread();
		} catch (final IOException e) {
			fireError(e);
			disconnect();
			fireDisconnected();
		}
	}

}
