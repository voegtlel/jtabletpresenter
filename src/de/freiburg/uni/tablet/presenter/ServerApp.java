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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = "config.ini";
		if (args.length > 0) {
			configFile = args[0];
		}
		
		final AcceptThread acceptDownThread = new AcceptThread(8025);
		final List<ServerDownSync> _downSyncs = new ArrayList<ServerDownSync>();
		final List<ServerUpSync> _upSyncs = new ArrayList<ServerUpSync>();
		final DocumentEditor editor = new DocumentEditor(new DocumentConfig(configFile));
		acceptDownThread.addListener(new AcceptListener() {
			@Override
			public void clientConnected(final SocketChannel socket) {
				try {
					System.out.println("Down client connected from " + socket.getRemoteAddress());
				} catch (IOException e) {
					e.printStackTrace();
				}
				final ServerDownSync client = new ServerDownSync(editor, socket);
				client.start();
				_downSyncs.add(client);
			}
		});
		
		final AcceptThread acceptUpThread = new AcceptThread(8024);
		acceptUpThread.addListener(new AcceptListener() {
			@Override
			public void clientConnected(final SocketChannel socket) {
				try {
					System.out.println("Up client connected from " + socket.getRemoteAddress());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final ServerUpSync client = new ServerUpSync(editor.getHistory(), socket);
				client.start();
				client.onActionPerformed(new SetDocumentAction(editor.getDocument().getClientId(), editor.getDocument()));
				_upSyncs.add(client);
			}
		});
		
		System.out.println("Start");
		
		acceptDownThread.start();
		acceptUpThread.start();
		
		try {
			System.out.println("Ready");
			System.out.println("Press any key to exit");
			// Wait for exit
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Stopping");
		
		for (ServerDownSync sync : _downSyncs) {
			System.out.println("Stopping Thread");
			sync.stop();
		}
		
		for (ServerUpSync sync : _upSyncs) {
			System.out.println("Stopping Thread");
			sync.stop();
		}
		
		acceptDownThread.close();
		
		System.out.println("Finished");
	}

}
