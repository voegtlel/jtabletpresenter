package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;

public class ClientUpSync extends ClientSync {
	private static final Logger LOGGER = Logger.getLogger(ClientUpSync.class.getName());
	
	private final LinkedList<IAction> _actions = new LinkedList<IAction>();

	private DocumentHistoryListener _documentHistoryListener;

	private DocumentHistory _history;
	
	public ClientUpSync(final DocumentHistory history) {
		_history = history;
		_documentHistoryListener = new DocumentHistoryListener() {
			@Override
			public void actionPerformed(final IAction action) {
				onActionPerformed(action);
			}
			
			@Override
			public void actionAdded(final IAction action) {
				onActionPerformed(action);
			}
		};
		history.addListener(_documentHistoryListener);
	}
	
	public void onActionPerformed(final IAction action) {
		if (_running) {
			synchronized (_threadSync) {
				LOGGER.log(Level.INFO, "Enqueue " + action.getClass().getName());
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
					if (action instanceof SetClientDocumentAction) {
						LOGGER.log(Level.INFO, "Write up " + action.getClass().getName());
						SetServerDocumentAction serverAction = ((SetClientDocumentAction)action).getServerAction();
						LOGGER.log(Level.INFO, " --> " + serverAction.getClass().getName());
						_writerSync.writeSerializableClass(serverAction);
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
	protected void dataThread(final String host, final int port,
			final long timeout) {
		try {
			if (!connectThread(host, port, timeout)) {
				fireDisconnected();
				return;
			}
			if (!initData()) {
				throw new IOException("Invalid server");
			}
			// Start threads
			fireConnected();
			sendThread();
		} catch (final IOException e) {
			fireError(e);
			disconnect();
			fireDisconnected();
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		_history.removeListener(_documentHistoryListener);
	}
}
