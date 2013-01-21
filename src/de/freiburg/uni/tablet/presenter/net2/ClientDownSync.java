package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import de.freiburg.uni.tablet.presenter.actions.ChangePageIndexAction;
import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.actions.SetDocumentAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.PackageInputStream;
import de.freiburg.uni.tablet.presenter.data.PackageInputStream.PackageReader;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;

public class ClientDownSync {
	// 512k buffer
	private static final int RECV_BUFFER_SIZE = 1024*512;
	
	private SocketChannel _socket;
	private Selector _connectSelector;
	
	private final Object _threadSync = new Object();
	private Thread _thread;
	private boolean _running;
	
	private DocumentEditor _editor;

	private boolean _hasDocument = false;

	private IPageEditor _pageEditor;
	
	public ClientDownSync(final DocumentEditor editor, IPageEditor pageEditor) {
		_editor = editor;
		_pageEditor = pageEditor;
	}
	
	@SuppressWarnings("deprecation")
	public void stop() {
		System.out.println("Stop down client");
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
	
	public void start(final String host, final int port,
			final long timeout) {
		System.out.println("Start down client");
		
		_running = true;
		_thread = new Thread() {
			@Override
			public void run() {
				dataThread(host, port, timeout);
			}
		};
		_thread.start();
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
	
	/**
	 * Read bytes from the network. If there cannot be read as many bytes as
	 * there is space in buffer, false is returned.
	 * 
	 * @param buffer
	 * @return success
	 */
	protected boolean receive(final ByteBuffer buffer) {
		try {
			// Infinite loop
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
	
	private void receiveThread() throws IOException {
		_socket.configureBlocking(true);
		final ByteBuffer sizeBuffer = ByteBuffer.allocateDirect(4);
		final ByteBuffer readBuffer = ByteBuffer.allocateDirect(RECV_BUFFER_SIZE);
		final PackageInputStream pis = new PackageInputStream(new PackageReader() {
			@Override
			public int readPackageSize() {
				System.out.println("Down reading package size...");
				sizeBuffer.clear();
				receive(sizeBuffer);
				int size = sizeBuffer.getInt();
				System.out.println("Down read package size " + size + " bytes");
				return size;
			}
			
			@Override
			public boolean readPackage(byte[] data, int count) {
				System.out.println("Down read package (" + count + " bytes)");
				if (count > readBuffer.capacity()) {
					// Extra large packet
					ByteBuffer tempBuffer = ByteBuffer.allocateDirect(count);
					if (!receive(tempBuffer)) {
						System.out.println("Down cancel reading");
						return false;
					}
					tempBuffer.get(data, 0, count);
				} else {
					readBuffer.clear();
					readBuffer.limit(count);
					if (!receive(readBuffer)) {
						System.out.println("Down cancel reading");
						return false;
					}
					readBuffer.get(data, 0, count);
				}
				return true;
			}
		});
		final BinaryDeserializer reader = new BinaryDeserializer(pis);
		while (_running) {
			System.out.println("Down wait for package");
			if (!pis.nextPackage()) {
				System.out.println("Down cancel package");
				break;
			}
			System.out.println("Down package read");
			IAction action = reader.readSerializableClass();
			System.out.println("Read action " + action.getClass().getName());
			if (action instanceof SetDocumentAction) {
				_hasDocument = true;
				action.perform(_editor);
				if (action.mustRedraw(_editor)) {
					_pageEditor.clear();
				}
			} else if (!(action instanceof ChangePageIndexAction)) {
				action.perform(_editor);
				if (action.mustRedraw(_editor)) {
					_pageEditor.clear();
				}
			}
		}
	}
	
	protected void dataThread(final String host, final int port, final long timeout) {
		try {
			connect(host, port, timeout);
			// Start threads
			receiveThread();
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
	
	public boolean hasDocument() {
		return _hasDocument;
	}

	private void fireError(IOException e) {
		System.out.println("Down client exception");
		e.printStackTrace();
	}

	private void fireDisconnected() {
		System.out.println("Down client disconnected");
	}
}
