package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IConnection;
import org.xsocket.connection.INonBlockingConnection;
import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public abstract class ServerSync {
	private final static Logger LOGGER = Logger.getLogger(ServerSync.class.getName());
	
	private IServer _server;
	private String _name = "Server";
	private String _authToken = null;
	protected int _clientId = 0;
	
	private final int _serverMagic;
	private final int _clientMagic;
	
	public ServerSync(final int port, final int serverMagic, final int clientMagic) throws IOException {
		_server = new Server(port, new IConnectHandler() {
			@Override
			public boolean onConnect(final INonBlockingConnection connection)
					throws IOException, BufferUnderflowException,
					MaxReadSizeExceededException {
				return ServerSync.this.onConnect(connection);
			}
		});
		_serverMagic = serverMagic;
		_clientMagic = clientMagic;
	}
	
	public void setAuthToken(final String authToken) {
		_authToken = authToken;
	}
	
	public void setName(final String name) {
		_name = name;
	}
	
	public String getAuthToken() {
		return _authToken;
	}
	
	public String getName() {
		return _name;
	}
	
	public void start() throws IOException {
		_server.start();
	}
	
	public void stop() throws IOException {
		_server.close();
	}
	
	public void setIdleTimeout(final long idleTimeout) {
		_server.setIdleTimeoutMillis(idleTimeout);
	}

	protected abstract boolean onConnect(final INonBlockingConnection connection) throws IOException;
	
	protected abstract class ClientSync extends Sync {
		private int _clientId;

		public ClientSync(final INonBlockingConnection connection, int clientId) throws IOException {
			_connection = connection;
			_clientId = clientId;
			connection.setHandler(_defaultHandler);
		}
		
		protected void performInit(final BinarySerializer writer, final BinaryDeserializer reader, final IConnection connection) throws IOException {
			// Check magic
			final int magic = reader.readInt();
			if (magic != _serverMagic) {
				LOGGER.log(Level.WARNING, "Client " + connection.getRemoteAddress() + " has invalid magic: " + String.format("%08X", magic));
				connection.close();
				throw new IOException("Invalid client magic");
			}
			// Write server magic
			writer.writeInt(_clientMagic);
			writer.flush();
			// Check auth token
			final String authToken = reader.readString();
			if ((_authToken != null) && !_authToken.equals(authToken)) {
				LOGGER.log(Level.WARNING, "Client " + connection.getRemoteAddress() + " has invalid authToken: " + authToken);
				connection.close();
				throw new IOException("Invalid client auth token");
			}
			// Get name
			_remoteName = reader.readString();
			LOGGER.log(Level.INFO, "Client " + connection.getRemoteAddress() + " (" + _remoteName + ") connected");
			// Send client id
			writer.writeInt(_clientId);
			// Send own name
			writer.writeString(ServerSync.this._name);
			writer.flush();
		}
	}
}
