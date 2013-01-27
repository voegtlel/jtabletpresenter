package de.freiburg.uni.tablet.presenter.net2;

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

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.PackageInputStream;
import de.freiburg.uni.tablet.presenter.data.PackageOutputStream;

public abstract class NetSync {
	public static final int CLIENT_HEADER_MAGIC = 0x6153556;
	public static final int SERVER_HEADER_MAGIC = 0x6429451;
	
	// 512k buffer
	private static final int BUFFER_SIZE = 1024*512;
	
	private final List<NetSyncListener> _listeners = new ArrayList<NetSyncListener>();
	
	private SocketChannel _socket;
	private Selector _connectSelector;
	
	protected final Object _threadSync = new Object();
	private Thread _thread;
	protected boolean _running;
	
	protected final PackageInputStream _packageInputStreamSync;
	protected final BinaryDeserializer _readerSync;
	
	protected final PackageOutputStream _packageOutputStreamAsync;
	protected final BinarySerializer _writerAsync;
	
	protected final PackageOutputStream _packageOutputStreamSync;
	protected final BinarySerializer _writerSync;
	
	private final LinkedList<ByteBuffer> _sendBuffersFree = new LinkedList<ByteBuffer>();
	private final LinkedList<ByteBuffer> _sendBuffersFilled = new LinkedList<ByteBuffer>();
	private final ByteBuffer _readSizeBuffer = ByteBuffer.allocateDirect(4);
	private final ByteBuffer _readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	
	/**
	 * Create sync with empty socket
	 */
	public NetSync() {
		_packageInputStreamSync = new PackageInputStream(new PackageInputStream.PackageReader() {
			@Override
			public int readPackageSize() {
				return readPackageSizeSync();
			}
			
			@Override
			public boolean readPackage(final byte[] data, final int count) {
				return readSync(data, count);
			}
		});
		_packageOutputStreamAsync = new PackageOutputStream(new PackageOutputStream.PackageWriter() {
			@Override
			public void writePackage(final byte[] data, final int size) {
				sendPacketAsync(data, 0, size, false);
			}
			
			@Override
			public void writePackageRaw(final byte[] data, final int size) {
				sendPacketAsync(data, 0, size, true);
			}
		});
		_packageOutputStreamSync = new PackageOutputStream(new PackageOutputStream.PackageWriter() {
			@Override
			public void writePackage(final byte[] data, final int size) {
				try {
					sendPacketSync(data, 0, size, false);
				} catch (IOException e) {
					fireError(e);
				}
			}
			
			@Override
			public void writePackageRaw(final byte[] data, final int size) {
				try {
					sendPacketSync(data, 0, size, true);
				} catch (IOException e) {
					fireError(e);
				}
			}
		});
		_writerAsync = new BinarySerializer(_packageOutputStreamAsync);
		_readerSync = new BinaryDeserializer(_packageInputStreamSync);
		_writerSync = new BinarySerializer(_packageOutputStreamSync);
	}
	
	/**
	 * Create sync by existing socket
	 * @param socket
	 */
	public NetSync(final SocketChannel socket) {
		this();
		_socket = socket;
	}

	/**
	 * Stop the thread
	 */
	@SuppressWarnings("deprecation")
	public void stop() {
		System.out.println("Stop client");
		_running = false;
		try {
			if (_socket != null) {
				_socket.shutdownInput();
				_socket.shutdownOutput();
			}
			synchronized (_threadSync) {
				if (_connectSelector != null) {
					_connectSelector.close();
					_connectSelector = null;
				}
				if (_thread != null) {
					_thread.join(100);
				}
				if (_socket != null) {
					_socket.close();
					_socket = null;
				}
				
				if (_thread != null) {
					_thread.join(250);
					if (_thread.isAlive()) {
						_thread.stop();
					}
					_thread = null;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Start the thread
	 * @param host
	 * @param port
	 * @param timeout
	 */
	protected void start(final String host, final int port,
			final long timeout) {
		System.out.println("Connecting to " + host + ":" + port + " (timeout: " + timeout + "ms)");
		
		_readerSync.resetState();
		_writerSync.resetState();
		_writerAsync.resetState();
		
		_running = true;
		_thread = new Thread() {
			@Override
			public void run() {
				dataThread(host, port, timeout);
			}
		};
		_thread.start();
	}
	
	/**
	 * Start the thread
	 * @param host
	 * @param port
	 * @param timeout
	 */
	protected void start() {
		System.out.println("Start client");
		
		_readerSync.resetState();
		_writerSync.resetState();
		_writerAsync.resetState();
		
		_running = true;
		_thread = new Thread() {
			@Override
			public void run() {
				dataThread();
			}
		};
		_thread.start();
	}
	
	/**
	 * Connects to the given host at port using timeout
	 * @param host
	 * @param port
	 * @param timeout
	 * @return true if connected
	 * @throws IOException
	 */
	protected boolean connectThread(final String host, final int port,
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
				_socket.configureBlocking(true);
			}
		}
		return isConnected;
	}
	
	/**
	 * Connects to the given host at port using timeout
	 * @param host
	 * @param port
	 * @param timeout
	 * @return true if connected
	 * @throws IOException
	 */
	protected void connectThread() throws IOException {
		// Perform connect and register selector
		synchronized (_threadSync) {
			_socket.configureBlocking(true);
			_socket.finishConnect();
			
			System.out.println("connected");
		}
	}
	
	/**
	 * Method for continous sending data from internal buffers
	 * @throws IOException
	 */
	protected void sendThread() throws IOException {
		while (_running) {
			// Perform action
			synchronized (_threadSync) {
				// We send more data
				if (_sendBuffersFilled.isEmpty()) {
					try {
						_threadSync.wait();
						// Immediately let the calling thread continue
						_threadSync.notifyAll();
					} catch (InterruptedException e) {
						continue;
					}
				} else {
					System.out.println("Client peek buffer");
					final ByteBuffer buffer = _sendBuffersFilled
							.getFirst();
					int writeSize = buffer.remaining();
					_socket.write(buffer);
					System.out.println("Wrote " + (writeSize - buffer.remaining()) + "/" + writeSize + " bytes");
					if (buffer.remaining() == 0) {
						_sendBuffersFilled.removeFirst();
						System.out.println("Reuse buffer");
						if (buffer.capacity() <= BUFFER_SIZE) {
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
	
	/**
	 * Read bytes from the network. If there cannot be read as many bytes as
	 * there is space in buffer, false is returned.
	 * 
	 * @param buffer
	 * @return success
	 */
	private boolean receive(final ByteBuffer buffer) {
		try {
			while (_running && buffer.remaining() > 0) {
				// Perform action
				synchronized (_threadSync) {
					// We receive more data
					if (_socket.read(buffer) == -1) {
						// EOS
						System.out.println("End of stream");
						break;
					}
					if (!_socket.isConnected()) {
						System.out.println("Not connected");
						return false;
					}
				}
			}
		} catch (final IOException e) {
			fireError(e);
			try {
				_socket.close();
			} catch (final IOException e1) {
			}
			return false;
		}
		// Flip buffer
		buffer.flip();
		return true;
	}
	
	/**
	 * Reads data
	 * @param data
	 * @param count
	 * @return
	 */
	protected boolean readSync(final byte[] data, final int count) {
		System.out.println("Read package (" + count + " bytes)");
		if (count > _readBuffer.capacity()) {
			// Extra large packet
			ByteBuffer tempBuffer = ByteBuffer.allocateDirect(count);
			if (!receive(tempBuffer)) {
				System.out.println("Cancel reading");
				return false;
			}
			tempBuffer.get(data, 0, count);
		} else {
			_readBuffer.clear();
			_readBuffer.limit(count);
			if (!receive(_readBuffer)) {
				System.out.println("Cancel reading");
				return false;
			}
			_readBuffer.get(data, 0, count);
		}
		return true;
	}

	/**
	 * Reads a packet size
	 * @return
	 */
	private int readPackageSizeSync() {
		_readSizeBuffer.clear();
		receive(_readSizeBuffer);
		int size = _readSizeBuffer.getInt();
		System.out.println("Read package size " + size + " bytes");
		return size;
	}
	
	/**
	 * Puts the given data in the send-queue.
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 * @param raw
	 */
	private void sendLargePacketAsync(final byte[] data, final int offset, final int length, final boolean raw) {
		// We need to synchronize
		synchronized (_threadSync) {
			System.out.println("Send large packet: " + length);
			final ByteBuffer buffer = ByteBuffer.allocateDirect(length + 4);
			if (!raw) {
				buffer.putInt(length);
			}
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
	 * @param raw
	 */
	private void sendPacketAsync(final byte[] data, final int offset, final int length, final boolean raw) {
		if (length - 4 > BUFFER_SIZE) {
			sendLargePacketAsync(data, offset, length, raw);
			return;
		}
		// We need to synchronize
		synchronized (_threadSync) {
			System.out.println("Send packet: " + length);
			ByteBuffer buffer;
			if (_sendBuffersFree.isEmpty()) {
				buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
			} else {
				buffer = _sendBuffersFree.removeFirst();
				buffer.clear();
			}
			if (!raw) {
				buffer.putInt(length);
			}
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
	 * @param raw
	 * @throws IOException 
	 */
	private void sendLargePacketSync(final byte[] data, final int offset, final int length, final boolean raw) throws IOException {
		// We need to synchronize
		synchronized (_threadSync) {
			System.out.println("Send large packet sync: " + length);
			final ByteBuffer buffer = ByteBuffer.allocateDirect(length + 4);
			if (!raw) {
				buffer.putInt(length);
			}
			buffer.put(data, offset, length);
			buffer.flip();
			
			while (buffer.hasRemaining()) {
				_socket.write(buffer);
			}
		}
	}
	
	/**
	 * Sends the given data
	 * 
	 * @param data
	 * @param offset
	 * @param length
	 * @param raw
	 * @throws IOException 
	 */
	protected void sendPacketSync(final byte[] data, final int offset, final int length, final boolean raw) throws IOException {
		if (length - 4 > BUFFER_SIZE) {
			sendLargePacketSync(data, offset, length, raw);
			return;
		}
		// We need to synchronize
		synchronized (_threadSync) {
			System.out.println("Send packet sync: " + length);
			if (_sendBuffersFree.isEmpty()) {
				final ByteBuffer newBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
				_sendBuffersFree.addLast(newBuffer);
			}
			final ByteBuffer buffer = _sendBuffersFree.getFirst();
			buffer.clear();
			if (!raw) {
				buffer.putInt(length);
			}
			buffer.put(data, offset, length);
			buffer.flip();
			
			while (buffer.hasRemaining()) {
				_socket.write(buffer);
			}
		}
	}
	
	/**
	 * Main function for thread. Connects and either calls the send thread or performs receive actions
	 * @param host
	 * @param port
	 * @param timeout
	 */
	protected void dataThread(final String host, final int port, final long timeout) {
	}
	
	/**
	 * Main function for thread. Connects and either calls the send thread or performs receive actions
	 */
	protected void dataThread() {
	}
	
	/**
	 * Disconnect socket
	 */
	protected void disconnect() {
		if (_socket != null) {
			try {
				//_socket.shutdownInput();
				//_socket.shutdownOutput();
				_socket.close();
				_socket = null;
			} catch (IOException e) {
				fireError(e);
			}
		}
	}
	
	protected void fireError(final IOException e) {
		System.out.println("error event");
		e.printStackTrace();
		for (NetSyncListener l : _listeners) {
			l.onError(e);
		}
	}

	protected void fireDisconnected() {
		System.out.println("disconnected event");
		for (NetSyncListener l : _listeners) {
			l.onDisconnected();
		}
	}
	
	protected void fireConnected() {
		System.out.println("connected event");
		for (NetSyncListener l : _listeners) {
			l.onConnected();
		}
	}
	
	public void addListener(final NetSyncListener listener) {
		_listeners.add(listener);
	}
	
	public void removeListener(final NetSyncListener listener) {
		_listeners.remove(listener);
	}
}
