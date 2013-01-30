package de.freiburg.uni.tablet.presenter;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.sun.jna.NativeLibrary;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageEditor;
import de.freiburg.uni.tablet.presenter.page.SolidPen;
import de.freiburg.uni.tablet.presenter.xsocket.ClientListener;
import de.freiburg.uni.tablet.presenter.xsocket.DownClient;
import de.freiburg.uni.tablet.presenter.xsocket.UpClient;

public class ClientApp {

	private JPageEditor _pageRenderer;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		Logger.getLogger("de.freiburg.uni.tablet.presenter").setLevel(Level.ALL);
		
		String configFile = "config.ini";
		if (args.length > 0) {
			configFile = args[0];
		}
		
		final DocumentConfig config = new DocumentConfig(configFile);
		if (config.getBoolean("server.enabled", false)) {
			ServerApp serverApp = new ServerApp(config);
			serverApp.start();
			try {
				Object o = new Object();
				while (true) {
					synchronized (o) {
						o.wait();
					}
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
		_pageRenderer.setSize(800, 600);
		_pageRenderer.setLocationByPlatform(true);
		//_pageRenderer.setBounds(100, 100, 640, 480);
		_pageRenderer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		_pageRenderer.getDocumentEditor().setDocument(new ServerDocument(1));
		
		if (_pageRenderer.getConfig().getBoolean("client.down.enabled", false)) {
			String hostname = _pageRenderer.getConfig().getString("client.down.host", "");
			int port = _pageRenderer.getConfig().getInt("client.down.port", 8024);
			int timeout = _pageRenderer.getConfig().getInt("client.down.timeout", 5000);
			String clientName = _pageRenderer.getConfig().getString("client.down.name", "Client");
			String authToken = _pageRenderer.getConfig().getString("client.down.authToken", "");
			
			try {
				final DownClient clientDownSync = new DownClient(hostname, port, _pageRenderer.getDocumentEditor());
				clientDownSync.setAuthToken(authToken);
				clientDownSync.setName(clientName);
				
				clientDownSync.addListener(new ClientListener() {
					@Override
					public void onError(final Exception e) {
						JOptionPane.showMessageDialog(_pageRenderer, "Error: " + e.toString());
					}
					
					@Override
					public void onDisconnected() {
						int res = JOptionPane.showConfirmDialog(_pageRenderer, "Disconnected. Reconnect?");
						if (res == JOptionPane.OK_OPTION) {
							clientDownSync.stop();
							try {
								clientDownSync.start();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
					
					@Override
					public void onConnected() {
						System.out.println("Connected");
					}
				});
				
				System.out.println("Connect remote");
				try {
					clientDownSync.start();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		} else {
			if (_pageRenderer.getConfig().getBoolean("autosave.loadStartup", false)) {
				final File savedSession = new File("session.dat");
				if (savedSession.exists())  {
					try {
						System.out.println("Loading session");
						//FileInputStream is = new FileInputStream(savedSession);
						SeekableByteChannel bs = Files.newByteChannel(savedSession.toPath(), StandardOpenOption.READ);
						BinaryDeserializer bd = new BinaryDeserializer(bs);
						_pageRenderer.getDocumentEditor().deserialize(bd);
						bs.close();
						//is.close();
						System.out.println("Session loaded");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		_pageRenderer.getDocumentEditor().setCurrentPen(new SolidPen(_pageRenderer.getConfig().getFloat("editor.defaultPen.thickness", 1f), _pageRenderer.getConfig().getColor("editor.defaultPen.color", Color.black)));
		
		if (_pageRenderer.getConfig().getBoolean("client.up.enabled", false)) {
			String hostname = _pageRenderer.getConfig().getString("client.up.host", "");
			int port = _pageRenderer.getConfig().getInt("client.up.port", 8024);
			int timeout = _pageRenderer.getConfig().getInt("client.up.timeout", 5000);
			String clientName = _pageRenderer.getConfig().getString("client.up.name", "Client");
			String authToken = _pageRenderer.getConfig().getString("client.up.authToken", "");
			
			try {
				final UpClient clientSync = new UpClient(hostname, port, _pageRenderer.getDocumentEditor());
				clientSync.setAuthToken(authToken);
				clientSync.setName(clientName);
				clientSync.addListener(new ClientListener() {
					@Override
					public void onError(final Exception e) {
						JOptionPane.showMessageDialog(_pageRenderer, "Error: " + e.toString());
					}
					
					@Override
					public void onDisconnected() {
						int res = JOptionPane.showConfirmDialog(_pageRenderer, "Disconnected. Reconnect?");
						if (res == JOptionPane.OK_OPTION) {
							clientSync.stop();
							try {
								clientSync.start();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					@Override
					public void onConnected() {
					}
				});
				System.out.println("Connect remote");
				clientSync.start();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		_pageRenderer.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (_pageRenderer.getConfig().getBoolean("autosave.saveExit", false)) {
					final File savedSession = new File("session.dat");
					try {
						System.out.println("Saving session");
						//FileOutputStream os = new FileOutputStream(savedSession);
						//BufferedOutputStream bos = new BufferedOutputStream(os);
						SeekableByteChannel bc = Files.newByteChannel(savedSession.toPath(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
						BinarySerializer bs = new BinarySerializer(bc);
						_pageRenderer.getDocumentEditor().serialize(bs);
						bs.flush();
						bc.close();
						//bs.close();
						//bos.close();
						//os.close();
						System.out.println("Session autosaved");
					} catch (Exception e1) {
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
