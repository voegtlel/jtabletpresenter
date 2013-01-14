/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.net;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.freiburg.uni.tablet.presenter.actions.IAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.data.PackageInputStream;
import de.freiburg.uni.tablet.presenter.data.PackageInputStream.PackageReader;
import de.freiburg.uni.tablet.presenter.data.PackageOutputStream.PackageWriter;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;

/**
 * @author lukas
 * 
 */
public class HistoryClient implements PackageReader, PackageWriter {
	private final ClientThread _client;
	private final Thread _receiveThread;
	private DocumentEditor _documentEditor;

	private final Object _threadSync = new Object();

	private ByteBuffer _recvBuffer;
	private boolean _canReceive;
	private boolean _canSend;
	
	public HistoryClient(final ClientThread client, boolean canReceive, boolean canSend) {
		_client = client;
		_canReceive = canReceive;
		_canSend = canSend;
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

		_recvBuffer = ByteBuffer.allocateDirect(1024);
	}
	
	public void setReceiveSend(boolean canReceive, boolean canSend) {
		_canReceive = canReceive;
		_canSend = canSend;
	}
	
	public void setDocumentEditor(final DocumentEditor documentEditor) {
		_documentEditor = documentEditor;
	}

	public void start() throws IOException {
		_client.start();
	}

	protected void onClientConnected() {
		if (_canReceive) {
			_receiveThread.start();
		}
	}

	protected void onClientDisconnected() {
		synchronized (_threadSync) {
			if (_receiveThread != null) {
				_receiveThread.interrupt();
				try {
					_receiveThread.join();
				} catch (final InterruptedException e) {
				}
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
		PackageInputStream packageInputStream = new PackageInputStream(this);
		BinaryDeserializer reader = new BinaryDeserializer(packageInputStream);
		while (_client.isRunning()) {
			try {
				packageInputStream.nextPackage();
				IBinarySerializable readObject = reader.readObjectTable();
				if (readObject instanceof IAction) {
					IAction action = (IAction) readObject;
					action.perform(_documentEditor);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void close() {
		if (_receiveThread != null) {
			try {
				_receiveThread.join(500);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			if (_receiveThread.isAlive()) {
				_receiveThread.stop();
			}
		}
	}

	@Override
	public void writePackage(byte[] data, int size) {
		if (_canSend) {
			_client.send(data, 0, size);
		}
	}

	@Override
	public int readPackageSize() {
		if (_canReceive) {
			_recvBuffer.position(0);
			_recvBuffer.limit(4);
			_client.receive(_recvBuffer);
			return _recvBuffer.getInt();
		}
		return 0;
	}

	@Override
	public boolean readPackage(byte[] data, int count) {
		if (_canReceive) {
			if (_recvBuffer.capacity() < data.length) {
				_recvBuffer = ByteBuffer.allocateDirect(data.length);
			}
			_recvBuffer.position(0);
			_recvBuffer.limit(count);
			if (!_client.receive(_recvBuffer)) {
				return false;
			}
			_recvBuffer.get(data, 0, count);
			return true;
		}
		return false;
	}
}
