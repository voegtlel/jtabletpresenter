/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.net;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;

/**
 * @author lukas
 * 
 */
public class HistoryClient {
	private final static int CMD_DOCUMENT = 0;
	private final static int CMD_INIT = 1;

	private final ClientThread _client;
	private final Thread _receiveThread;

	private final List<ClientListener> _listeners = new ArrayList<ClientListener>();
	private final Object _threadSync = new Object();

	public HistoryClient(final ClientThread client) {
		_client = client;
		_client.addListener(new ClientListener() {
			@Override
			public void error(final Throwable t) {
				onClientError(t);
			}

			@Override
			public void disconnected() {
				onClientDisconnected();
			}

			@Override
			public void connected() {
				onClientConnected();
			}
		});

		_receiveThread = new Thread() {
			@Override
			public void run() {
				receiveThread();
			}
		};
	}

	public void start() throws IOException {
		_client.start();
	}

	protected void onClientConnected() {
		_receiveThread.start();
	}

	protected void onClientDisconnected() {
		synchronized (_threadSync) {
			_receiveThread.interrupt();
			try {
				_receiveThread.join();
			} catch (final InterruptedException e) {
			}
		}
	}

	protected void onClientError(final Throwable t) {
		t.printStackTrace();
	}

	/**
	 * Thread for receiving data
	 */
	protected void receiveThread() {
		final ByteBuffer recvBuffer = ByteBuffer.allocateDirect(1024);
		while (_client.isRunning()) {
			recvBuffer.position(0);
			recvBuffer.limit(4);
			if (!_client.receive(recvBuffer)) {
				break;
			}
			final int cmd = recvBuffer.getInt();
		}
	}

	/**
	 * 
	 */
	private void fireDisconnected() {
		for (final ClientListener listener : _listeners) {
			listener.disconnected();
		}
	}

	private void onReceiveData(final InputStream inputStream)
			throws IOException {
		final BinaryDeserializer reader = new BinaryDeserializer(inputStream);
		final IAction action = reader.readSerializableClass();
		action.perform(_editor);
	}

	public void send(final byte[] data) {
		try {
			_outputStream.write(data);
		} catch (final IOException e) {
			e.printStackTrace();
			try {
				_socket.close();
			} catch (final IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void close() {
		try {
			_socket.shutdownInput();
			_socket.shutdownOutput();
			_outputStream.close();
			_socket.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		try {
			_thread.join(500);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		if (_thread.isAlive()) {
			_thread.stop();
		}
	}
}
