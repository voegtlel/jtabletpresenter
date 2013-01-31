package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xsocket.connection.IConnection;
import org.xsocket.connection.NonBlockingConnection;

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
	
	private List<ClientListener> _listeners = new ArrayList<ClientListener>();

	private long _idleTimeout = IConnection.MAX_TIMEOUT_MILLIS;
	
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
	
	/**
	 * Sets the idle timeout
	 * @param idleTimeout
	 */
	public void setIdleTimeout(final long idleTimeout) {
		_idleTimeout = idleTimeout;
		if (_connection != null) {
			_connection.setIdleTimeoutMillis(idleTimeout);
		}
	}
	
	@Override
	public void start() throws IOException {
		stop();
		_connection = new NonBlockingConnection(_hostname, _port, _defaultHandler);
		_connection.setIdleTimeoutMillis(_idleTimeout);
	}
	
	/**
	 * General thread method
	 */
	protected void onThread() throws IOException {
	}
	
	protected void performInit(final BinarySerializer writer, final BinaryDeserializer reader, final IConnection connection) throws IOException {
		// Write server magic
		writer.writeInt(_serverMagic);
		writer.flush();
		// Check magic
		final int magic = reader.readInt();
		if (magic != _clientMagic) {
			LOGGER.log(Level.WARNING, "Server " + connection.getRemoteAddress() + " has invalid magic: " + String.format("%08X", magic));
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
		LOGGER.log(Level.INFO, "Server " + connection.getRemoteAddress() + " (" + _clientId + ": " + _remoteName + ") connected");
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
