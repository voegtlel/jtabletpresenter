package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Sync {
	private final static Logger LOGGER = Logger.getLogger(Sync.class.getName());
	
	protected SocketChannel _connection;
	private Object _sync = new Object();
	protected String _name = "Client";
	protected String _remoteName = null;
	
	private Thread _thread;
	
	public Sync() {
	}
	
	/**
	 * Set the name
	 * @param name
	 */
	public void setName(final String name) {
		_name = name;
	}
	
	/**
	 * Gets the name
	 * @return
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Gets the remote name
	 * @return
	 */
	public String getRemoteName() {
		return _remoteName;
	}
	
	/**
	 * Starts networking
	 * @throws IOException
	 */
	public abstract void start() throws IOException;
	
	/**
	 * Stops networking
	 */
	public void stop() {
		synchronized (_sync) {
			if (_connection != null) {
				try {
					_connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			stopThread();
			_connection = null;
			_thread = null;
		}
	}
	
	/**
	 * Starts the internal thread
	 */
	protected void startThread() {
		synchronized (_sync) {
			stopThread();
			_thread = new Thread() {
				@Override
				public void run() {
					try {
						Sync.this.onThread();
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
	protected void stopThread() {
		if (_thread != null) {
			try {
				_thread.join(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			_thread = null;
		}
	}
	
	/**
	 * General thread method
	 */
	protected void onThread() throws IOException {
	}

	protected boolean onDisconnect(final SocketChannel connection) throws IOException {
		if (connection != null) {
			if (_remoteName != null) {
				LOGGER.log(Level.INFO, "Net " + connection.socket().getRemoteSocketAddress() + " (" + _remoteName + ") disconnected");
			} else {
				LOGGER.log(Level.INFO, "Net " + connection.socket().getRemoteSocketAddress() + " disconnected");
			}
		} else {
			if (_remoteName != null) {
				LOGGER.log(Level.INFO, "Net (" + _remoteName + ") disconnected");
			} else {
				LOGGER.log(Level.INFO, "Net disconnected");
			}
		}
		fireDisconnected();
		return true;
	}

	protected boolean onConnect(final SocketChannel connection) throws IOException {
		if (_remoteName != null) {
			LOGGER.log(Level.INFO, "Net " + connection.socket().getRemoteSocketAddress() + " (" + _remoteName + ") connect");
		} else {
			LOGGER.log(Level.INFO, "Net " + connection.socket().getRemoteSocketAddress() + " connect");
		}
		return true;
	}
	
	protected void fireDisconnected() {
	}
	
	protected void fireConnected() {
	}
}
