package de.freiburg.uni.tablet.presenter.net;

import java.net.Socket;

public interface AcceptListener {
	void clientConnected(Socket socket);
}
