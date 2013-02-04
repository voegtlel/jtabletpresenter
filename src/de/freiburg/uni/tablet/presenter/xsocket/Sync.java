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
	protected boolean _running;
	
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
			if (_thread == null || !_thread.isAlive()) {
				stopThread();
				_running = true;
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
	protected void stopThread() {
		if (_thread != null) {
			_running = false;
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
	protected abstract void onThread() throws IOException;
	
	

	protected boolean onDisconnect() throws IOException {
		if (_remoteName != null) {
			LOGGER.log(Level.INFO, "Net (" + _remoteName + ") disconnected");
		} else {
			LOGGER.log(Level.INFO, "Net disconnected");
		}
		fireDisconnected();
		return true;
	}
	
	protected void fireDisconnected() {
	}
	
	protected void fireConnected() {
	}
}
