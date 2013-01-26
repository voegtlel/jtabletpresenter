package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class ServerSync extends NetSync {
	protected String _clientName = "client";
	protected String _authToken = "";
	protected String _requiredAuthToken = "";
	protected int _clientId = 0;
	
	public ServerSync(final SocketChannel socket, final String requiredAuthToken) {
		super(socket);
		_requiredAuthToken = requiredAuthToken;
	}
	
	@Override
	public void start() {
		super.start();
	}
	
	public String getClientName() {
		return _clientName;
	}
	
	public String getAuthToken() {
		return _authToken;
	}
	
	public int getClientId() {
		return _clientId;
	}
	
	public void setClientId(int clientId) {
		_clientId = clientId;
	}
	
	protected boolean initData() throws IOException {
		_writerSync.writeInt(SERVER_HEADER_MAGIC);
		_packageOutputStreamSync.nextPackageRaw();
		if (!_packageInputStreamSync.nextPackageRaw(4)) {
			System.out.println("Cancel init client");
			return false;
		}
		final int serverMagic = _readerSync.readInt();
		if (serverMagic != CLIENT_HEADER_MAGIC) {
			throw new IOException("Invalid Client");
		}
		
		_writerSync.writeInt(_clientId);
		_packageOutputStreamSync.nextPackage();
		
		if (!_packageInputStreamSync.nextPackage()) {
			System.out.println("Cancel init handshake");
			return false;
		}
		
		_authToken = _readerSync.readString();
		_clientName = _readerSync.readString();
		
		return true;
	}
}
