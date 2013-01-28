package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.PackageInputStream;
import de.freiburg.uni.tablet.presenter.data.PackageOutputStream;

public abstract class NetSync {
	private static final Logger LOGGER = Logger.getLogger(NetSync.class.getName());
	
	public static final int CLIENT_HEADER_MAGIC = 0x6153556;
	public static final int SERVER_HEADER_MAGIC = 0x6429451;
	
	// 512k buffer
	private static final int BUFFER_SIZE = 1024*32;
	
	public static final boolean SPLIT_PACKAGES = true;
	
	private final List<NetSyncListener> _listeners = new ArrayList<NetSyncListener>();
	
	private SocketChannel _socket;
	private Selector _connectSelector;
	
	protected final Object _threadSync = new Object();
	private Thread _thread;
	protected boolean _running;
	
	protected final PackageInputStream _packageInputStreamSync;
	protected final BinaryDeserializer _readerSync;
	
	protected final PackageOutputStream _packageOutputStreamSync;
	protected final BinarySerializer _writerSync;
	
	private final ByteBuffer _readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	
	private final ByteBuffer _sendBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	
	/**
	 * Create sync with empty socket
	 */
	public NetSync() {
		_packageInputStreamSync = new PackageInputStream(new PackageInputStream.PackageReader() {
			@Override
			public int readPackageSize() {
				try {
					return readPackageSizeSync();
				} catch (IOException e) {
					e.printStackTrace();
					fireError(e);
					disconnect();
					return 0;
				}
			}
			
			@Override
			public boolean readPackage(final byte[] data, final int count) {
				try {
					return readSync(data, count);
				} catch (IOException e) {
					e.printStackTrace();
					fireError(e);
					disconnect();
					return false;
				}
			}
		});
		_packageOutputStreamSync = new PackageOutputStream(new PackageOutputStream.PackageWriter() {
			@Override
			public void writePackage(final byte[] data, final int size) {
				try {
					sendPacketSync(data, 0, size, false);
				} catch (IOException e) {
					fireError(e);
					disconnect();
				}
			}
			
			@Override
			public void writePackageRaw(final byte[] data, final int size) {
				try {
					sendPacketSync(data, 0, size, true);
				} catch (IOException e) {
					fireError(e);
					disconnect();
				}
			}
		});
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
		LOGGER.log(Level.INFO, "Stop client");
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
				if (_socket != null && _socket.isOpen()) {
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
		LOGGER.log(Level.INFO, "Connecting to " + host + ":" + port + " (timeout: " + timeout + "ms)");
		
		_readerSync.resetState();
		_writerSync.resetState();
		
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
		LOGGER.log(Level.INFO, "Start client");
		
		_readerSync.resetState();
		_writerSync.resetState();
		
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
			
			LOGGER.log(Level.INFO, "connect");
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
				LOGGER.log(Level.WARNING, "Not connected");
				_socket.close();
				_socket = null;
			} else {
				_socket.finishConnect();
				LOGGER.log(Level.INFO, "Connected");
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
			
			LOGGER.log(Level.INFO, "connected");
		}
	}
	
	/**
	 * Reads data
	 * @param data
	 * @param count
	 * @return
	 * @throws IOException 
	 */
	protected boolean readSync(final byte[] data, final int count) throws IOException {
		// We need to synchronize
		synchronized (_threadSync) {
			if (_socket == null || !_socket.isConnected()) {
				throw new IOException("Not connected");
			}
			int iOffset = 0;
			int iLength = count;
			LOGGER.log(Level.INFO, "Recv sync: " + count);
			
			while (iLength > 0) {
				_readBuffer.clear();
				_readBuffer.limit(Math.min(_readBuffer.capacity(), iLength));
				int read = _socket.read(_readBuffer);
				if (read == -1) {
					disconnect();
					throw new IOException("Disconnected");
				}
				_readBuffer.flip();
				_readBuffer.get(data, iOffset, read);
				iOffset += read;
				iLength -= read;
				LOGGER.log(Level.INFO, "Channel read " + read + " (" + (count - iLength) + "/" + count + " bytes)");
			}
		}
		return true;
	}

	/**
	 * Reads a packet size
	 * @return
	 * @throws IOException 
	 */
	private int readPackageSizeSync() throws IOException {
		synchronized (_threadSync) {
			if (_socket == null || !_socket.isConnected()) {
				throw new IOException("End of stream");
			}
			_readBuffer.clear();
			_readBuffer.limit(4);
			LOGGER.log(Level.INFO, "Try get package size");
			while (_readBuffer.hasRemaining()) {
				int read = _socket.read(_readBuffer);
				if (read == -1) {
					disconnect();
					throw new IOException("End of stream");
				}
			}
			_readBuffer.flip();
			int size = _readBuffer.getInt();
			LOGGER.log(Level.INFO, "Read package size " + size + " bytes");
			return size;
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
		synchronized (_threadSync) {
			if (_socket == null || !_socket.isConnected()) {
				throw new IOException("Not connected");
			}
			int iOffset = offset;
			int iLength = length;
			_sendBuffer.clear();
			if (!raw) {
				_sendBuffer.putInt(length);
			}
			LOGGER.log(Level.INFO, "Send packet sync: " + length);
			
			while (iLength > 0) {
				int write = Math.min(_sendBuffer.remaining(), iLength);
				_sendBuffer.put(data, iOffset, write);
				iOffset += write;
				iLength -= write;
				LOGGER.log(Level.INFO, "Buffer written " + write + " (" + (length - iLength) + "/" + length + " bytes)");
				_sendBuffer.flip();
				while (_sendBuffer.hasRemaining()) {
					int written = _socket.write(_sendBuffer);
					if (written == -1) {
						disconnect();
						throw new IOException("End of stream");
					}
					LOGGER.log(Level.INFO, "Channel written " + written + " bytes");
				}
				_sendBuffer.clear();
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
		synchronized (_threadSync) {
			if (_socket != null && _socket.isOpen()) {
				_running = false;
				try {
					if (_socket.isConnected()) {
						_socket.shutdownInput();
						_socket.shutdownOutput();
					}
				} catch (IOException e) {
				}
				try {
					_socket.close();
					fireDisconnected();
				} catch (IOException e) {
					_socket = null;
					fireError(e);
				}
			}
		}
	}
	
	protected void fireError(final IOException e) {
		LOGGER.log(Level.INFO, "error event");
		e.printStackTrace();
		for (NetSyncListener l : _listeners) {
			l.onError(e);
		}
	}

	protected void fireDisconnected() {
		LOGGER.log(Level.INFO, "disconnected event");
		for (NetSyncListener l : _listeners) {
			l.onDisconnected();
		}
	}
	
	protected void fireConnected() {
		LOGGER.log(Level.INFO, "connected event");
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
	
	public void dispose() {
		synchronized (_threadSync) {
			if (_socket.isOpen()) {
				disconnect();
				fireDisconnected();
			}
		}
	}
}
