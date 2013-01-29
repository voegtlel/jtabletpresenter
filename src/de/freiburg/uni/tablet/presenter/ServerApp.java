package de.freiburg.uni.tablet.presenter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;
import de.freiburg.uni.tablet.presenter.xsocket.DownServer;
import de.freiburg.uni.tablet.presenter.xsocket.UpServer;

public class ServerApp {
	private DocumentEditor _editor;
	
	private UpServer _upServer;
	private DownServer _downServer;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getLogger("de.freiburg.uni.tablet.presenter").setLevel(Level.ALL);
		
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
		String name = config.getString("server.name", "Server");
		String authTokenDown = config.getString("server.down.authToken", "");
		String authTokenUp = config.getString("server.up.authToken", "");
		int portDown = config.getInt("server.down.port", 8025);
		int portUp = config.getInt("server.up.port", 8025);
		
		_editor = new DocumentEditor();
		_editor.setDocument(new ServerDocument(1));
		
		try {
			_upServer = new UpServer(portUp, _editor);
			_upServer.setName(name);
			_upServer.setAuthToken(authTokenUp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			_downServer = new DownServer(portDown, _editor);
			_downServer.setName(name);
			_downServer.setAuthToken(authTokenDown);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		System.out.println("Start");
		
		try {
			_upServer.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			_downServer.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		System.out.println("Stopping");
		
		try {
			_upServer.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			_downServer.stop();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Finished");
	}
}
