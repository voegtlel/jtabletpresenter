package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.PackageOutputStream;
import de.freiburg.uni.tablet.presenter.document.DocumentHistory;
import de.freiburg.uni.tablet.presenter.document.DocumentHistoryListener;

public class ClientUpSync {
	// 512k buffer
	private static final int SEND_BUFFER_SIZE = 1024*512;
	
	private SocketChannel _socket;
	private Selector _connectSelector;
	private Selector _sendSelector;
	
	private final LinkedList<ByteBuffer> _sendBuffersFree = new LinkedList<ByteBuffer>();
	private final LinkedList<ByteBuffer> _sendBuffersFilled = new LinkedList<ByteBuffer>();

	private Object _threadSync = new Object();
	private Thread _thread;
	private boolean _running;
	
	private PackageOutputStream _outputStream;
	private BinarySerializer _serializer;
	
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
		_outputStream = new PackageOutputStream(new PackageOutputStream.PackageWriter() {
			@Override
			public void writePackage(byte[] data, int size) {
				sendPacket(data, 0, size);
			}
		});
		_serializer = new BinarySerializer(_outputStream);
	}
	
	@SuppressWarnings("deprecation")
	public void stop() {
		System.out.println("Stop up sync");
		_running = false;
		try {
			if (_socket != null) {
				_socket.shutdownInput();
				_socket.shutdownOutput();
			}
			synchronized (_threadSync) {
				_connectSelector.close();
				_connectSelector = null;
				_sendSelector.close();
				_sendSelector = null;
				_thread.join(100);
				_socket.close();
				_socket = null;
				
				_thread.join(250);
				if (_thread.isAlive()) {
					_thread.stop();
				}
				_thread = null;
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void start(final String host, final int port, final long timeout) {
		System.out.println("Connecting to " + host + ":" + port + " (timeout: " + timeout + "ms)");
		_running = true;
		_thread = new Thread() {
			@Override
			public void run() {
				dataThread(host, port, timeout);
			}
		};
		_thread.start();
	}

	public void onActionPerformed(final IAction action) {
		if (_running) {
			try {
				System.out.println("Write up " + action.getClass().getName());
				_serializer.writeSerializableClass(action);
				_serializer.flush();
				_outputStream.flush();
				_outputStream.nextPackage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected boolean connect(final String host, final int port,
			final long timeout) throws IOException {
		// Perform connect and register selector
		synchronized (_threadSync) {
			_socket = SocketChannel.open();
			_socket.configureBlocking(false);

			final InetAddress remoteAddr = InetAddress.getByName(host);
			final InetSocketAddress socketAddress = new InetSocketAddress(
					remoteAddr, port);

			_connectSelector = Selector.open(); 
			_socket.register(_connectSelector, SelectionKey.OP_CONNECT);

			_socket.connect(socketAddress);
			
			System.out.println("connect");
		}

		boolean isConnected = false;
		// Perform the select for connecting
		_connectSelector.select(timeout);
		synchronized (_threadSync) {
			// Verify connection
			for (final SelectionKey key : _connectSelector.selectedKeys()) {
				if (key.isConnectable()) {
					isConnected = true;
				}
			}
			_connectSelector.close();
			_connectSelector = null;
			if (!isConnected) {
				System.out.println("Not connected");
				_socket.close();
				_socket = null;
			} else {
				_socket.finishConnect();
				System.out.println("Connected");
			}
		}
		return isConnected;
	}
	
	private void sendThread() throws IOException {
		_socket.configureBlocking(true);
		while (_running) {
			System.out.println("Run");
			// Perform action
			synchronized (_threadSync) {
				System.out.println("Thread sync");
				// We send more data
				if (_sendBuffersFilled.isEmpty()) {
					try {
						System.out.println("Wait");
						_threadSync.wait();
						// Immediately let the calling thread continue
						System.out.println("Notify");
						_threadSync.notifyAll();
						System.out.println("Continue");
					} catch (InterruptedException e) {
						break;
					}
				} else {
					System.out.println("Client peek buffer");
					final ByteBuffer buffer = _sendBuffersFilled
							.peek();
					System.out.println("Client up write " + buffer.remaining() + " bytes");
					_socket.write(buffer);
					if (buffer.remaining() == 0) {
						_sendBuffersFilled.pop();
						System.out.println("Reuse buffer");
						if (buffer.capacity() <= SEND_BUFFER_SIZE) {
							_sendBuffersFree.addLast(buffer);
						}
					}
				}
				if (!_socket.isConnected()) {
					System.out.println("Not connected");
					break;
				}
			}
		}
	}
	
	protected void dataThread(final String host, final int port,
			final long timeout) {
		try {
			boolean isConnected = connect(host, port, timeout);

			// Start threads
			if (isConnected) {
				fireConnected();
				sendThread();
			} else {
				fireDisconnected();
			}
		} catch (final IOException e) {
			fireError(e);
			if (_socket != null) {
				try {
					_socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			fireDisconnected();
		}
	}

	private void fireError(IOException e) {
		System.out.println("error event");
		e.printStackTrace();
	}

	private void fireDisconnected() {
		System.out.println("disconnected event");
	}

	private void fireConnected() {
		System.out.println("connected event");
	}
	
	/**
	 * Puts the given data in the send-queue.
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 */
	private void sendLargePacket(final byte[] data, final int offset, final int length) {
		// We need to synchronize
		synchronized (_threadSync) {
			System.out.println("Send large packet: " + length);
			final ByteBuffer buffer = ByteBuffer.allocateDirect(length + 4);
			buffer.putInt(length);
			buffer.put(data, offset, length);
			buffer.flip();
			_sendBuffersFilled.addLast(buffer);
			_threadSync.notifyAll();
		}
	}
	
	/**
	 * Puts the given data in the send-queue.
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 */
	protected void sendPacket(final byte[] data, final int offset, final int length) {
		if (length - 4 > SEND_BUFFER_SIZE) {
			sendLargePacket(data, offset, length);
			return;
		}
		// We need to synchronize
		synchronized (_threadSync) {
			System.out.println("Send packet: " + length);
			ByteBuffer buffer;
			if (_sendBuffersFree.isEmpty()) {
				buffer = ByteBuffer.allocateDirect(SEND_BUFFER_SIZE);
			} else {
				buffer = _sendBuffersFree.pop();
				buffer.clear();
			}
			buffer.putInt(length);
			buffer.put(data, offset, length);
			buffer.flip();
			_sendBuffersFilled.addLast(buffer);
			_threadSync.notifyAll();
		}
	}
}
