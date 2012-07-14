package de.freiburg.uni.tablet.presenter.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AcceptThread {
	private final ServerSocket _socket;
	private final Thread _thread;

	public AcceptThread(final ServerSocket socket) {
		_socket = socket;
		_thread = new Thread() {
			@Override
			public void run() {
				runThread();
			}
		};

		_thread.start();
	}

	private final List<AcceptListener> _listeners = new ArrayList<AcceptListener>();

	public void addListener(final AcceptListener l) {
		_listeners.add(l);
	}

	private void fireListener(final Socket socket) {
		for (final AcceptListener l : _listeners) {
			l.clientConnected(socket);
		}
	}

	private void runThread() {
		try {
			final Socket socket = _socket.accept();
			fireListener(socket);
			_socket.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
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
