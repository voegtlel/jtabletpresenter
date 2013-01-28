package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;

public class ServerUpSync extends ServerSync {
	private static final Logger LOGGER = Logger.getLogger(ServerUpSync.class.getName());
	
	private final LinkedList<IAction> _actions = new LinkedList<IAction>();

	private DocumentHistoryListener _documentHistoryListener;

	private DocumentHistory _history;
	
	public ServerUpSync(final DocumentHistory history, final SocketChannel socket, final String requiredAuthToken) {
		super(socket, requiredAuthToken);
		_history = history;
		_documentHistoryListener = new DocumentHistoryListener() {
			@Override
			public void actionPerformed(final IAction action) {
				synchronized (_history) {
					onActionPerformed(action);
				}
			}
			
			@Override
			public void actionAdded(final IAction action) {
				synchronized (_history) {
					onActionPerformed(action);
				}
			}
		};
		synchronized (_history) {
			_history.addListener(_documentHistoryListener);
		}
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
					final IAction action = _actions.removeFirst();
					if (action instanceof SetServerDocumentAction) {
						LOGGER.log(Level.INFO, "Write up " + action.getClass().getName());
						SetClientDocumentAction clientAction = ((SetServerDocumentAction)action).getClientAction();
						System.out.println(" --> " + clientAction.getClass().getName());
						_writerSync.writeSerializableClass(clientAction);
					} else {
						LOGGER.log(Level.INFO, "Write up " + action.getClass().getName());
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
			if (!initData()) {
				throw new IOException("Invalid server");
			}
			if (!_authToken.equals(_requiredAuthToken)) {
				throw new IOException("Auth tokens do not match (got " + _authToken + ")");
			}
			fireConnected();
			// Start threads
			sendThread();
		} catch (final IOException e) {
			fireError(e);
			disconnect();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		synchronized (_history) {
			_history.removeListener(_documentHistoryListener);
		}
	}
}
