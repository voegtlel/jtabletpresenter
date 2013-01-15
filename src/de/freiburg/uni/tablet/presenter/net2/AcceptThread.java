package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class AcceptThread {
	private final int _port;
	private final ServerSocketChannel _socket;
	private final Thread _thread;

	private final Object _threadSync = new Object();

	private boolean _running = false;

	public AcceptThread(final int port) {
		_port = port;
		try {
			_socket = ServerSocketChannel.open();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		_thread = new Thread() {
			@Override
			public void run() {
				runThread();
			}
		};
	}

	private final List<AcceptListener> _listeners = new ArrayList<AcceptListener>();

	public void addListener(final AcceptListener l) {
		_listeners.add(l);
	}

	private void fireListener(final SocketChannel socket) {
		for (final AcceptListener l : _listeners) {
			l.clientConnected(socket);
		}
	}

	public void start() {
		synchronized (_threadSync) {
			if (!_running) {
				_running = true;
				_thread.start();
			}
		}
	}

	private void runThread() {
		try {
			System.out.println("Bind accept socket to " + _port);
			_socket.bind(new InetSocketAddress(_port));
			while (_running) {
				System.out.println("accepting");
				final SocketChannel socket = _socket.accept();
				System.out.println("accepted");
				fireListener(socket);
			}
			System.out.println("Close accept socket");
			_socket.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
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
