package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.connection.IHandler;
import org.xsocket.connection.INonBlockingConnection;

public abstract class Sync {
	private final static Logger LOGGER = Logger.getLogger(Sync.class.getName());
	
	protected INonBlockingConnection _connection;
	private Object _sync = new Object();
	protected String _name = "Client";
	protected String _remoteName = null;
	
	private Thread _thread;
	
	protected IHandler _defaultHandler;
	
	public Sync() {
		_defaultHandler = new ISystemHandler() {
			@Override
			public boolean onIdleTimeout(final INonBlockingConnection connection)
					throws IOException {
				return Sync.this.onIdleTimeout(connection);
			}
			
			@Override
			public boolean onConnectionTimeout(final INonBlockingConnection connection)
					throws IOException {
				return Sync.this.onConnectionTimeout(connection);
			}
			
			@Override
			public boolean onDisconnect(final INonBlockingConnection connection)
					throws IOException {
				return Sync.this.onDisconnect(connection);
			}
			
			@Override
			public boolean onData(final INonBlockingConnection connection)
					throws IOException, BufferUnderflowException,
					ClosedChannelException, MaxReadSizeExceededException {
				return Sync.this.onData(connection);
			}
			
			@Override
			public boolean onConnect(final INonBlockingConnection connection)
					throws IOException, BufferUnderflowException,
					MaxReadSizeExceededException {
				return Sync.this.onConnect(connection);
			}
		};
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
			if (_thread == null || !_thread.isAlive()) {
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
			if (_thread.isAlive()) {
				_thread.stop();
			}
			_thread = null;
		}
	}
	
	/**
	 * General thread method
	 */
	protected void onThread() throws IOException {
	}

	protected boolean onIdleTimeout(final INonBlockingConnection connection) throws IOException {
		if (_remoteName != null) {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " (" + _remoteName + ") timeout");
		} else {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " timeout");
		}
		return true;
	}

	protected boolean onConnectionTimeout(final INonBlockingConnection connection) throws IOException {
		if (_remoteName != null) {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " (" + _remoteName + ") connect timeout");
		} else {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " connect timeout");
		}
		return true;
	}

	protected boolean onDisconnect(final INonBlockingConnection connection) throws IOException {
		if (_remoteName != null) {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " (" + _remoteName + ") disconnected");
		} else {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " disconnected");
		}
		fireDisconnected();
		return true;
	}

	protected boolean onData(final INonBlockingConnection connection) throws IOException {
		/*if (_remoteName != null) {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " (" + _remoteName + ") data: " + connection.available());
		} else {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " data" + connection.available());
		}*/
		return true;
	}

	protected boolean onConnect(final INonBlockingConnection connection) throws IOException {
		if (_remoteName != null) {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " (" + _remoteName + ") connect");
		} else {
			LOGGER.log(Level.INFO, "Net " + connection.getRemoteAddress() + " connect");
		}
		fireConnected();
		return true;
	}
	
	protected void fireDisconnected() {
	}
	
	protected void fireConnected() {
	}
}
