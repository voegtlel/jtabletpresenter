package de.freiburg.uni.tablet.presenter.net;

public interface AcceptListener {
	/**
	 * Fired, when a client has connected. The given client thread was not
	 * started.
	 * 
	 * @param socket
	 */
	void clientConnected(ClientThread socket);
}
