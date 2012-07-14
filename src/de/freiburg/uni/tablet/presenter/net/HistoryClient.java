/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

/**
 * @author lukas
 * 
 */
public class HistoryClient {
	private final Socket _socket;
	private final DocumentEditor _editor;
	private final Thread _thread;

	private final List<ClientListener> _listeners = new ArrayList<ClientListener>();

	/**
	 * @throws IOException
	 * @throws UnknownHostException
	 * 
	 */
	public HistoryClient(final String host, final int port, final int timeout,
			final DocumentEditor editor) throws UnknownHostException,
			IOException {
		_editor = editor;
		_socket = new Socket();
		final InetAddress remoteAddr = InetAddress.getByName(host);
		_socket.connect(new InetSocketAddress(remoteAddr, port), timeout);
		_thread = new Thread() {
			@Override
			public void run() {
				onReceiveThread();
			}
		};
		_thread.start();
	}

	/**
	 * 
	 */
	protected void onReceiveThread() {
		try {
			final InputStream inputStream = _socket.getInputStream();

			while (_socket.isConnected()) {
				onReceiveData(inputStream);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		System.out.println("Disconnected");
		fireDisconnected();
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
