package de.freiburg.uni.tablet.presenter;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.actions.SetDocumentAction;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.net2.AcceptListener;
import de.freiburg.uni.tablet.presenter.net2.AcceptThread;
import de.freiburg.uni.tablet.presenter.net2.ServerDownSync;
import de.freiburg.uni.tablet.presenter.net2.ServerUpSync;

public class ServerApp {

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
		_acceptUpThread = new AcceptThread(8024);
		_acceptDownThread = new AcceptThread(8025);
		_editor = new DocumentEditor(config);
		
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
		final ServerDownSync client = new ServerDownSync(_editor, socket);
		client.start();
		_downSyncs.add(client);
	}
	
	private void onUpClientConnected(final SocketChannel socket) {
		try {
			System.out.println("Up client connected from " + socket.getRemoteAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
		final ServerUpSync client = new ServerUpSync(_editor.getHistory(), socket);
		client.start();
		client.onActionPerformed(new SetDocumentAction(_editor.getDocument().getClientId(), _nextClientId++, _editor.getDocument()));
		_upSyncs.add(client);
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
