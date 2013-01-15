package de.freiburg.uni.tablet.presenter.net2;

import java.nio.channels.SocketChannel;

public interface AcceptListener {
	/**
	 * Fired, when a client has connected. The given client thread was not
	 * started.
	 * 
	 * @param socket
	 */
	void clientConnected(SocketChannel socket);
}
