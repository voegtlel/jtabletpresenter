package de.freiburg.uni.tablet.presenter;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.jna.NativeLibrary;

import de.freiburg.uni.tablet.presenter.actions.SetServerDocumentAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageEditor;
import de.freiburg.uni.tablet.presenter.net2.ClientDownSync;
import de.freiburg.uni.tablet.presenter.net2.ClientUpSync;
import de.freiburg.uni.tablet.presenter.net2.NetSyncListener;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class ClientApp {

	private JPageEditor _pageRenderer;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		String configFile = "config.ini";
		if (args.length > 0) {
			configFile = args[0];
		}
		
		final DocumentConfig config = new DocumentConfig(configFile);
		if (config.getBoolean("server.enabled", false)) {
			ServerApp serverApp = new ServerApp(config);
			serverApp.start();
			try {
				while(true) {
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			System.setProperty("sun.awt.noerasebackground", "true");
			
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						final ClientApp window = new ClientApp(config);
						window._pageRenderer.setVisible(true);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Create the application.
	 * @param args 
	 */
	public ClientApp(final DocumentConfig config) {
		NativeLibrary.addSearchPath("freetype", ".");
		
		_pageRenderer = new JPageEditor(config);
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		_pageRenderer.setTitle("JTabletPresenter v1.02");
		_pageRenderer.setBounds(100, 100, 640, 480);
		_pageRenderer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		_pageRenderer.getDocumentEditor().setDocument(new ServerDocument(1));
		
		if (_pageRenderer.getConfig().getBoolean("client.down.enabled", false)) {
			final ClientDownSync clientDownSync = new ClientDownSync(_pageRenderer.getDocumentEditor(), _pageRenderer.getPageEditor());
			clientDownSync.setAuthToken(_pageRenderer.getConfig().getString("client.down.authToken", ""));
			clientDownSync.setClientName(_pageRenderer.getConfig().getString("client.down.name", "Client"));
			
			clientDownSync.addListener(new NetSyncListener() {
				@Override
				public void onError(final Exception e) {
					JOptionPane.showMessageDialog(_pageRenderer, "Error: " + e.toString());
				}
				
				@Override
				public void onDisconnected() {
					int res = JOptionPane.showConfirmDialog(_pageRenderer, "Disconnected. Reconnect?");
					if (res == JOptionPane.OK_OPTION) {
						clientDownSync.start(_pageRenderer.getConfig().getString("client.down.host", ""),
								_pageRenderer.getConfig().getInt("client.down.port", 8024),
								_pageRenderer.getConfig().getInt("client.down.timeout", 5000));
					}
				}
				
				@Override
				public void onConnected() {
					System.out.println("Connected");
				}
			});
			
			System.out.println("Connect remote");
			clientDownSync.start(_pageRenderer.getConfig().getString("client.down.host", ""),
					_pageRenderer.getConfig().getInt("client.down.port", 8024),
					_pageRenderer.getConfig().getInt("client.down.timeout", 5000));
		} else {
			if (_pageRenderer.getConfig().getBoolean("autosave.loadStartup", false)) {
				final File savedSession = new File("session.dat");
				if (savedSession.exists())  {
					try {
						System.out.println("Loading session");
						FileInputStream is = new FileInputStream(savedSession);
						BinaryDeserializer bd = new BinaryDeserializer(is);
						_pageRenderer.getDocumentEditor().deserialize(bd);
						is.close();
						System.out.println("Session loaded");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		_pageRenderer.getDocumentEditor().setCurrentPen(new SolidPen(_pageRenderer.getConfig().getFloat("editor.defaultPen.thickness", 1f), _pageRenderer.getConfig().getColor("editor.defaultPen.color", Color.black)));
		
		if (_pageRenderer.getConfig().getBoolean("client.up.enabled", false)) {
			final ClientUpSync clientSync = new ClientUpSync(_pageRenderer.getDocumentEditor().getHistory());
			clientSync.setAuthToken(_pageRenderer.getConfig().getString("client.up.authToken", ""));
			clientSync.setClientName(_pageRenderer.getConfig().getString("client.up.name", "Client"));
			clientSync.addListener(new NetSyncListener() {
				@Override
				public void onError(final Exception e) {
					JOptionPane.showMessageDialog(_pageRenderer, "Error: " + e.toString());
				}
				
				@Override
				public void onDisconnected() {
					int res = JOptionPane.showConfirmDialog(_pageRenderer, "Disconnected. Reconnect?");
					if (res == JOptionPane.OK_OPTION) {
						clientSync.start(_pageRenderer.getConfig().getString("client.down.host", ""),
								_pageRenderer.getConfig().getInt("client.down.port", 8025),
								_pageRenderer.getConfig().getInt("client.down.timeout", 5000));
					}
				}
				
				@Override
				public void onConnected() {
				}
			});
			System.out.println("Connect remote");
			clientSync.start(_pageRenderer.getConfig().getString("client.up.host", ""),
					_pageRenderer.getConfig().getInt("client.up.port", 8025),
					_pageRenderer.getConfig().getInt("client.up.timeout", 5000));
			clientSync.onActionPerformed(new SetServerDocumentAction(_pageRenderer.getDocumentEditor().getDocument()));
		}
		
		_pageRenderer.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (_pageRenderer.getConfig().getBoolean("autosave.saveExit", false)) {
					final File savedSession = new File("session.dat");
					try {
						System.out.println("Saving session");
						FileOutputStream os = new FileOutputStream(savedSession);
						BufferedOutputStream bos = new BufferedOutputStream(os);
						BinarySerializer bs = new BinarySerializer(bos);
						bs.writeSerializableClass(_pageRenderer.getDocumentEditor());
						bs.close();
						bos.close();
						os.close();
						System.out.println("Session autosaved");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		/*
		 * _pageRenderer.setPage(new DefaultPage());
		 * _pageRenderer.setNormalTool(new ToolScribble(_pageRenderer,
		 * _pageRenderer)); _pageRenderer.setInvertedTool(new
		 * ToolEraser(_pageRenderer, _pageRenderer, _pageRenderer));
		 */
	}
}
