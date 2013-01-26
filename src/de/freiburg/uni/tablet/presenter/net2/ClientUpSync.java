package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;

public class ClientUpSync extends ClientSync {
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
			try {
				if (action instanceof SetClientDocumentAction) {
					System.out.println("Write up " + action.getClass().getName());
					SetServerDocumentAction serverAction = ((SetClientDocumentAction)action).getServerAction();
					System.out.println(" --> " + serverAction.getClass().getName());
					_writerAsync.writeSerializableClass(serverAction);
				} else {
					System.out.println("Write up " + action.getClass().getName());
					_writerAsync.writeSerializableClass(action);
				}
				_writerAsync.flush();
				_packageOutputStreamAsync.flush();
				_packageOutputStreamAsync.nextPackage();
			} catch (IOException e) {
				e.printStackTrace();
				fireError(e);
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
