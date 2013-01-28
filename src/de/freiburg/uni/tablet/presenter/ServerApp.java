package de.freiburg.uni.tablet.presenter;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.actions.SetClientDocumentAction;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;
import de.freiburg.uni.tablet.presenter.net2.AcceptListener;
import de.freiburg.uni.tablet.presenter.net2.AcceptThread;
import de.freiburg.uni.tablet.presenter.net2.NetSyncListener;
import de.freiburg.uni.tablet.presenter.net2.ServerDownSync;
import de.freiburg.uni.tablet.presenter.net2.ServerUpSync;

public class ServerApp {
	private String _name;
	private String _authTokenDown;
	private String _authTokenUp;
	
	private AcceptThread _acceptDownThread;
	private List<ServerDownSync> _downSyncs = new ArrayList<ServerDownSync>();
	private List<ServerUpSync> _upSyncs = new ArrayList<ServerUpSync>();
	private DocumentEditor _editor;
	private AcceptThread _acceptUpThread;
	
	private int _nextClientId = 2;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getLogger("de.freiburg.uni.tablet.presenter.net2").setLevel(Level.ALL);
		
		String configFile = "config.ini";
		if (args.length > 0) {
			configFile = args[0];
		}
		
		ServerApp serverApp = new ServerApp(new DocumentConfig(configFile));
		
		serverApp.start();
		try {
			System.out.println("Ready");
			System.out.println("Press any key to exit");
			// Wait for exit
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverApp.stop();
	}
	
	public ServerApp(final DocumentConfig config) {
		_name = config.getString("server.name", "Server");
		_authTokenDown = config.getString("server.down.authToken", "");
		_authTokenUp = config.getString("server.up.authToken", "");
		
		_acceptUpThread = new AcceptThread(config.getInt("server.up.port", 8024));
		_acceptDownThread = new AcceptThread(config.getInt("server.down.port", 8025));
		_editor = new DocumentEditor();
		_editor.setDocument(new ServerDocument(1));
		
		_acceptDownThread.addListener(new AcceptListener() {
			@Override
			public void clientConnected(final SocketChannel socket) {
				onDownConnected(socket);
			}
		});
		
		_acceptUpThread.addListener(new AcceptListener() {
			@Override
			public void clientConnected(final SocketChannel socket) {
				onUpClientConnected(socket);
			}
		});
	}
	
	private void onDownConnected(final SocketChannel socket) {
		try {
			System.out.println("Down client connected from " + socket.getRemoteAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		final ServerDownSync client = new ServerDownSync(_editor, socket, _authTokenDown);
		client.setClientId(_nextClientId++);
		client.addListener(new NetSyncListener() {
			@Override
			public void onError(Exception e) {
			}
			
			@Override
			public void onDisconnected() {
				System.out.println("Remove client " + client.getClientId() + " -> " + client.getClientName());
				synchronized (_downSyncs) {
					client.dispose();
					_downSyncs.remove(client);
				}
			}
			
			@Override
			public void onConnected() {
				System.out.println("Client " + client.getClientId() + " -> " + client.getClientName() + " connected");
			}
		});
		client.start();
		synchronized (_downSyncs) {
			_downSyncs.add(client);
		}
	}
	
	private void onUpClientConnected(final SocketChannel socket) {
		try {
			System.out.println("Up client connected from " + socket.getRemoteAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		final ServerUpSync client = new ServerUpSync(_editor.getHistory(), socket, _authTokenUp);
		client.setClientId(_nextClientId++);
		client.addListener(new NetSyncListener() {
			@Override
			public void onError(Exception e) {
			}
			
			@Override
			public void onDisconnected() {
				System.out.println("Remove client " + client.getClientId() + " -> " + client.getClientName());
				synchronized (_upSyncs) {
					client.dispose();
					_upSyncs.remove(client);
				}
			}
			
			@Override
			public void onConnected() {
				System.out.println("Client " + client.getClientId() + " -> " + client.getClientName() + " connected");
			}
		});
		client.start();
		client.onActionPerformed(new SetClientDocumentAction(_editor.getDocument(), _editor.getCurrentPageIndex()));
		synchronized (_upSyncs) {
			_upSyncs.add(client);
		}
	}
	
	public void start() {
		System.out.println("Start");
		
		_acceptDownThread.start();
		_acceptUpThread.start();
	}

	public void stop() {
		System.out.println("Stopping");
		
		for (ServerDownSync sync : _downSyncs) {
			System.out.println("Stopping Thread");
			sync.stop();
		}
		
		for (ServerUpSync sync : _upSyncs) {
			System.out.println("Stopping Thread");
			sync.stop();
		}
		
		_acceptDownThread.close();
		_acceptUpThread.close();
		
		System.out.println("Finished");
	}
}
