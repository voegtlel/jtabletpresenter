package de.freiburg.uni.tablet.presenter.net2;

import java.io.IOException;

public abstract class ClientSync extends NetSync {
	protected String _clientName = "client";
	protected String _authToken = "";
	protected int _clientId = 0;
	
	@Override
	public void start(String host, int port, long timeout) {
		super.start(host, port, timeout);
	}
	
	public void setClientName(final String clientName) {
		_clientName = clientName;
	}
	
	public String getClientName() {
		return _clientName;
	}
	
	public void setAuthToken(final String authToken) {
		_authToken = authToken;
	}
	
	public String getAuthToken() {
		return _authToken;
	}
	
	public int getClientId() {
		return _clientId;
	}
	
	protected boolean initData() throws IOException {
		_writerSync.writeInt(CLIENT_HEADER_MAGIC);
		_packageOutputStreamSync.nextPackageRaw();
		if (!_packageInputStreamSync.nextPackageRaw(4)) {
			System.out.println("Cancel init server");
			return false;
		}
		final int serverMagic = _readerSync.readInt();
		if (serverMagic != SERVER_HEADER_MAGIC) {
			throw new IOException("Invalid Server");
		}
		
		_writerSync.writeString(_authToken);
		_writerSync.writeString(_clientName);
		_packageOutputStreamSync.nextPackage();
		
		if (!_packageInputStreamSync.nextPackage()) {
			System.out.println("Cancel init handshake");
			return false;
		}
		
		_clientId = _readerSync.readInt();
		
		return true;
	}
}
