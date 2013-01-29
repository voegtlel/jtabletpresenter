package de.freiburg.uni.tablet.presenter.xsocket;

public interface ClientListener {
	/**
	 * Fired when connection has been established
	 */
	void onConnected();
	
	/**
	 * Fired when connection has been closed
	 */
	void onDisconnected();
	
	/**
	 * Fired when an error occured
	 * @param e
	 */
	void onError(Exception e);
}
