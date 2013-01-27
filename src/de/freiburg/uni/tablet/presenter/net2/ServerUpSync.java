package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;

public class ServerUpSync extends ServerSync {
	private final LinkedList<IAction> _actions = new LinkedList<IAction>();
	
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
			synchronized (_threadSync) {
				_actions.addLast(action);
				_threadSync.notifyAll();
			}
		}
	}

	/**
	 * Writes the actions
	 * @throws IOException
	 */
	private void sendThread() throws IOException {
		while (_running) {
			// Perform action
			synchronized (_threadSync) {
				// We send more data
				if (_actions.isEmpty()) {
					try {
						_threadSync.wait();
						// Immediately let the calling thread continue
						_threadSync.notifyAll();
					} catch (InterruptedException e) {
						continue;
					}
				} else {
					System.out.println("Client pop buffer");
					final IAction action = _actions.removeFirst();
					if (action instanceof SetServerDocumentAction) {
						System.out.println("Write up " + action.getClass().getName());
						SetClientDocumentAction clientAction = ((SetServerDocumentAction)action).getClientAction();
						System.out.println(" --> " + clientAction.getClass().getName());
						_writerSync.writeSerializableClass(clientAction);
					} else {
						System.out.println("Write up " + action.getClass().getName());
						_writerSync.writeSerializableClass(action);
					}
					_writerSync.flush();
					_packageOutputStreamSync.flush();
					_packageOutputStreamSync.nextPackage();
				}
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
