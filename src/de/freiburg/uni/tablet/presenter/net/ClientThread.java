/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lukas
 * 
 */
public class ClientThread {
	private final static int SEND_BUFFER_SIZE = 1024;

	private final Thread _connectThread;
	private final Thread _sendThread;

	private final List<ClientListener> _listeners = new ArrayList<ClientListener>();
	private final SocketChannel _socket;

	private final Selector _connectSelector;
	private final Selector _receiveSelector;
	private final Selector _sendSelector;

	private final Object _threadSync = new Object();
	private final Object _sendSync = new Object();

	private final LinkedList<ByteBuffer> _sendBuffersFree = new LinkedList<ByteBuffer>();
	private final LinkedList<ByteBuffer> _sendBuffersFilled = new LinkedList<ByteBuffer>();

	private boolean _running = false;
	private boolean _isDisconnected = false;

	/**
	 * @throws IOException
	 * 
	 */
	public ClientThread(final String host, final int port, final long timeout)
			throws IOException {
		_socket = SocketChannel.open();
		_connectSelector = Selector.open();
		_receiveSelector = Selector.open();
		_sendSelector = Selector.open();

		_connectThread = new Thread() {
			@Override
			public void run() {
				connectThread(host, port, timeout);
			}
		};

		_sendThread = new Thread() {
			@Override
			public void run() {
				sendThread();
			}
		};
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ClientThread(final SocketChannel socket) throws IOException {
		_socket = socket;
		_connectSelector = Selector.open();
		_receiveSelector = Selector.open();
		_sendSelector = Selector.open();

		_connectThread = new Thread() {
			@Override
			public void run() {
				connectThread();
			}
		};

		_sendThread = new Thread() {
			@Override
			public void run() {
				sendThread();
			}
		};
	}

	/**
	 * Starts the connection
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		synchronized (_threadSync) {
			if (!_running) {
				_running = true;
				_connectThread.start();
			}
		}
	}

	/**
	 * Checks the thread state
	 * 
	 * @return thread state
	 */
	public boolean isRunning() {
		synchronized (_threadSync) {
			return _running;
		}
	}

	/**
	 * Stops all threads and closes the connection
	 */
	@SuppressWarnings("deprecation")
	public void stop() {
		synchronized (_threadSync) {
			try {
				_running = false;
				_socket.shutdownInput();
				_socket.shutdownOutput();
				_connectSelector.close();
				_receiveSelector.close();
				_sendSelector.close();
				_connectThread.join(100);
				_sendThread.join(100);
				_socket.close();
				_connectThread.join(250);
				_sendThread.join(250);
				if (_connectThread.isAlive()) {
					_connectThread.stop();
				}
				if (_sendThread.isAlive()) {
					_sendThread.stop();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Thread for connecting using an existing socket
	 */
	protected void connectThread() {
		try {
			// Configure blocking
			synchronized (_threadSync) {
				_socket.configureBlocking(false);
			}
			// Event
			fireConnected();

			// Start sub-threads
			synchronized (_threadSync) {
				_sendThread.start();
			}
		} catch (final IOException e) {
			fireError(e, false);
			fireDisconnected(false);
			try {
				_socket.close();
			} catch (final IOException e1) {
				fireError(e1, false);
			}
		}
	}

	/**
	 * Performs the connect action
	 * 
	 * @param host
	 *            destination host
	 * @param port
	 *            destination port
	 * @param timeout
	 *            timeout value in msec
	 */
	protected void connectThread(final String host, final int port,
			final long timeout) {
		try {
			// Perform connect and register selector
			synchronized (_threadSync) {
				_socket.configureBlocking(false);

				final InetAddress remoteAddr = InetAddress.getByName(host);
				final InetSocketAddress socketAddress = new InetSocketAddress(
						remoteAddr, port);

				_socket.register(_connectSelector, SelectionKey.OP_CONNECT);

				_socket.connect(socketAddress);
			}

			// Perform the select for connecting
			_connectSelector.select(timeout);

			// Verify connection
			boolean isConnected = false;
			for (final SelectionKey key : _connectSelector.selectedKeys()) {
				if (key.isConnectable()) {
					isConnected = true;
				}
			}
			_connectSelector.close();
			// Register selector for receiving and sending
			synchronized (_threadSync) {
				_socket.register(_sendSelector, SelectionKey.OP_WRITE);
				_socket.register(_receiveSelector, SelectionKey.OP_READ);
			}

			// Start threads
			if (isConnected) {
				_sendThread.start();
				fireConnected();
			} else {
				_socket.close();
				fireDisconnected(false);
			}
		} catch (final IOException e) {
			fireError(e, false);
			try {
				_socket.close();
			} catch (final IOException e1) {
				fireError(e1, false);
			}
			fireDisconnected(false);
		}
	}

	/**
	 * Read bytes from the network. If the bytes cannot be read as many bytes as
	 * there is space in buffer, false is returned.
	 * 
	 * @param buffer
	 * @return success
	 */
	protected boolean receive(final ByteBuffer buffer) {
		try {
			// Infinite loop
			while (_running && buffer.remaining() > 0) {
				// Perform select
				_receiveSelector.select();
				// Perform action
				synchronized (_threadSync) {
					for (final SelectionKey key : _receiveSelector
							.selectedKeys()) {
						if (key.isReadable()) {
							// We receive more data
							if (_socket.read(buffer) == -1) {
								// EOS
								break;
							}
						}
					}
					if (!_socket.isConnected()) {
						fireDisconnected(true);
						return false;
					}
				}
			}
		} catch (final IOException e) {
			fireError(e, true);
			try {
				_socket.close();
			} catch (final IOException e1) {
				fireError(e1, true);
			}
			return false;
		}
		// Flip buffer
		buffer.flip();
		return true;
	}

	/**
	 * Thread for sending
	 */
	protected void sendThread() {
		try {
			// Infinite loop
			while (_running) {
				// Select
				_sendSelector.select();
				// Perform action
				synchronized (_threadSync) {
					for (final SelectionKey key : _receiveSelector
							.selectedKeys()) {
						if (key.isWritable()) {
							// We send more data
							if (_sendBuffersFilled.isEmpty()) {
								try {
									_sendSync.wait();
								} catch (final InterruptedException e) {
									break;
								}
							} else {
								final ByteBuffer buffer = _sendBuffersFilled
										.peek();
								_socket.write(buffer);
								if (buffer.remaining() == 0) {
									_sendBuffersFilled.pop();
									_sendBuffersFree.addLast(buffer);
								}
							}
						}
					}
					if (!_socket.isConnected()) {
						break;
					}
				}
			}
		} catch (final IOException e) {
			fireError(e, false);
			try {
				_socket.close();
			} catch (final IOException e1) {
				fireError(e1, false);
			}
		}
		fireDisconnected(false);
	}

	/**
	 * Puts the given data in the send-queue.
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 */
	public void send(final byte[] data, final int offset, final int length) {
		// We need to synchronize
		synchronized (_threadSync) {
			ByteBuffer buffer;
			if (_sendBuffersFree.isEmpty()) {
				buffer = ByteBuffer.allocateDirect(SEND_BUFFER_SIZE);
			} else {
				buffer = _sendBuffersFree.pop();
				buffer.position(0);
				buffer.limit(SEND_BUFFER_SIZE);
			}
			buffer.put(data, offset, length);
			_sendBuffersFilled.addLast(buffer);
			_sendSync.notifyAll();
		}
	}

	/**
	 * Fire disconnected event
	 */
	private void fireDisconnected(final boolean threaded) {
		if (threaded) {
			final Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					fireDisconnected(false);
				}
			});
			t.start();
		} else if (!_isDisconnected) {
			_isDisconnected = true;
			for (final ClientListener listener : _listeners) {
				listener.disconnected();
			}
		}
	}

	/**
	 * Fire connected event
	 */
	private void fireConnected() {
		for (final ClientListener listener : _listeners) {
			listener.connected();
		}
	}

	/**
	 * Fire error event
	 */
	private void fireError(final Throwable t, final boolean threaded) {
		if (threaded) {
			final Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					fireError(t, false);
				}
			});
			th.start();
		} else {
			for (final ClientListener listener : _listeners) {
				listener.error(t);
			}
		}
	}

	/**
	 * Add a listener
	 * 
	 * @param listener
	 */
	public void addListener(final ClientListener listener) {
		_listeners.add(listener);
	}
}
