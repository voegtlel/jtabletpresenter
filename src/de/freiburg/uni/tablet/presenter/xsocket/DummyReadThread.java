package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DummyReadThread {
	private SocketChannel _socket;
	
	private Thread _thread;

	private Object _sync = new Object();

	private boolean _running;

	private IDisconnectListener _disconnectListener;
	
	public DummyReadThread(final SocketChannel socket, final IDisconnectListener disconnectListener) {
		_socket = socket;
		_disconnectListener = disconnectListener;
	}
	
	public void start() {
		synchronized (_sync) {
			if (_thread == null || !_thread.isAlive()) {
				stop();
				_running = true;
				_thread = new Thread() {
					@Override
					public void run() {
						try {
							onThread();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				_thread.start();
			}
		}
	}
	
	/**
	 * Stops the internal thread
	 */
	@SuppressWarnings("deprecation")
	public void stop() {
		if (_thread != null) {
			_running = false;
			try {
				_thread.join(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (_thread.isAlive()) {
				_thread.stop();
			}
			_thread = null;
		}
	}

	protected void onThread() throws IOException {
		try {
			final ByteBuffer buffer = ByteBuffer.allocateDirect(1);
			while (_running) {
				_socket.read(buffer);
			}
		} finally {
			System.out.println("ReadThread disconnected");
			if (_disconnectListener != null) {
				_disconnectListener.onDisconnect();
			}
		}
	}
	
	public static interface IDisconnectListener {
		void onDisconnect();
	}
}
