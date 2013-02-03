package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.SocketFactory;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public abstract class ClientSync extends Sync {
	private final static Logger LOGGER = Logger.getLogger(ClientSync.class.getName());
	
	private String _authToken = "";
	private int _clientId = 0;
	
	private final int _serverMagic;
	private final int _clientMagic;
	
	private String _hostname;
	private int _port;
	
	private int _timeout = 5000;
	
	private List<ClientListener> _listeners = new ArrayList<ClientListener>();

	public ClientSync(final String hostname, final int port, final int serverMagic, final int clientMagic) {
		_hostname = hostname;
		_port = port;
		_serverMagic = serverMagic;
		_clientMagic = clientMagic;
	}
	
	public void setAuthToken(final String authToken) {
		_authToken = authToken;
	}
	
	public String getAuthToken() {
		return _authToken;
	}
	
	public int getClientId() {
		return _clientId;
	}
	
	protected void openSocket() throws IOException {
		_connection = SocketChannel.open();
		_connection.connect(new InetSocketAddress(_hostname, _port));
		onConnect(_connection);
	}
	
	@Override
	public void start() throws IOException {
		stop();
		startThread();
	}
	
	/**
	 * General thread method
	 */
	protected void onThread() throws IOException {
	}
	
	protected void performInit(final BinarySerializer writer, final BinaryDeserializer reader, final SocketChannel connection) throws IOException {
		// Write server magic
		writer.writeInt(_serverMagic);
		writer.flush();
		// Check magic
		final int magic = reader.readInt();
		if (magic != _clientMagic) {
			LOGGER.log(Level.WARNING, "Server " + connection.socket().getRemoteSocketAddress() + " has invalid magic: " + String.format("%08X", magic));
			connection.close();
			throw new IOException("Invalid server magic");
		}
		// Send own authToken
		writer.writeString(_authToken);
		// Send own name
		writer.writeString(_name);
		writer.flush();
		// Get client id
		_clientId = reader.readInt();
		// Get name
		_remoteName = reader.readString();
		LOGGER.log(Level.INFO, "Server " + connection.socket().getRemoteSocketAddress() + " (" + _clientId + ": " + _remoteName + ") connected");
	}
	
	protected void fireDisconnected() {
		for (ClientListener l : _listeners) {
			l.onDisconnected();
		}
	}
	
	protected void fireConnected() {
		for (ClientListener l : _listeners) {
			l.onConnected();
		}
	}
	
	public void addListener(final ClientListener listener) {
		_listeners.add(listener);
	}
	
	public void removeListener(final ClientListener listener) {
		_listeners.remove(listener);
	}
}
