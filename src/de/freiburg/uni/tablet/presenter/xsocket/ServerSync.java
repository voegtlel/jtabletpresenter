package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public abstract class ServerSync {
	private final static Logger LOGGER = Logger.getLogger(ServerSync.class.getName());
	
	private int _port;
	private ServerSocketChannel _server;
	private String _name = "Server";
	private String _authToken = null;
	protected int _clientId = 0;
	
	private final int _serverMagic;
	private final int _clientMagic;

	private boolean _running;

	private Thread _thread;
	
	public ServerSync(final int port, final int serverMagic, final int clientMagic) throws IOException {
		_port = port;
		_server = ServerSocketChannel.open();
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
	
	public void start() {
		if (_thread == null || !_thread.isAlive()) {
			stop();
			_running = true;
			_thread = new Thread() {
				@Override
				public void run() {
					try {
						ServerSync.this.onThread();
					} catch (IOException e) {
						e.printStackTrace();
						// TODO: Application response
					}
				}
			};
			_thread.start();
		}
	}
	
	/**
	 * Stops the internal thread
	 */
	@SuppressWarnings("deprecation")
	public void stop() {
		if (_thread != null) {
			_running = false;
			try {
				_server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				_thread.join(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (_thread.isAlive()) {
				_thread.stop();
			}
			_thread = null;
		}
	}
	
	private void onThread() throws IOException { 
		{
			final InetSocketAddress bound = new InetSocketAddress(_port);
			_server.bind(bound);
			LOGGER.log(Level.INFO, "Bound socket to " + bound);
		}
		while (_running) {
			final SocketChannel socket = _server.accept();
			try {
				socket.finishConnect();
				onConnect(socket);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected abstract void onConnect(final SocketChannel connection) throws IOException;
	
	protected abstract class ClientSync extends Sync {
		private int _clientId;

		public ClientSync(final SocketChannel connection, int clientId) throws IOException {
			_connection = connection;
			_clientId = clientId;
		}
		
		protected void performInit(final BinarySerializer writer, final BinaryDeserializer reader, final SocketChannel connection) throws IOException {
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
