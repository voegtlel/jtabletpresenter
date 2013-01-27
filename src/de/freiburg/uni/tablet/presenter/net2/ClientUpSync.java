package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.util.LinkedList;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;

public class ClientUpSync extends ClientSync {
	private final LinkedList<IAction> _actions = new LinkedList<IAction>();
	
	public ClientUpSync(final DocumentHistory history) {
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
				System.out.println("Enqueue " + action.getClass().getName());
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
					if (action instanceof SetClientDocumentAction) {
						System.out.println("Write up " + action.getClass().getName());
						SetServerDocumentAction serverAction = ((SetClientDocumentAction)action).getServerAction();
						System.out.println(" --> " + serverAction.getClass().getName());
						_writerSync.writeSerializableClass(serverAction);
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
}
