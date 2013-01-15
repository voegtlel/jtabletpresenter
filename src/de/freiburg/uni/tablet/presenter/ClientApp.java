package de.freiburg.uni.tablet.presenter;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;

import com.sun.jna.NativeLibrary;

import de.freiburg.uni.tablet.presenter.actions.SetDocumentAction;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageEditor;
import de.freiburg.uni.tablet.presenter.net2.ClientDownSync;
import de.freiburg.uni.tablet.presenter.net2.ClientUpSync;

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
		_pageRenderer.setTitle("JTabletPresenter v1.01");
		_pageRenderer.setBounds(100, 100, 640, 480);
		_pageRenderer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		DocumentEditor doc = null;
		if (_pageRenderer.getConfig().getBoolean("client.down.enabled", false)) {
			doc = new DocumentEditor(_pageRenderer.getConfig());
			ClientDownSync clientDownSync = new ClientDownSync(doc, _pageRenderer.getPageEditor());
			
			System.out.println("Connect remote");
			clientDownSync.start(_pageRenderer.getConfig().getString("client.down.host", ""),
					_pageRenderer.getConfig().getInt("client.down.port", 8024),
					_pageRenderer.getConfig().getInt("client.down.timeout", 5000000));
			System.out.println("Wait for document...");
			try {
				while (!clientDownSync.hasDocument()) {
					Thread.sleep(100);
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.out.println("Done");
		} else {
			if (_pageRenderer.getConfig().getBoolean("autosave.loadStartup", false)) {
				final File savedSession = new File("session.dat");
				if (savedSession.exists())  {
					try {
						System.out.println("Loading session");
						FileInputStream is = new FileInputStream(savedSession);
						BinaryDeserializer bd = new BinaryDeserializer(is);
						doc = bd.readSerializableClass();
						is.close();
						System.out.println("Session loaded");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (doc == null) {
				System.out.println("Create new session");
				doc = new DocumentEditor(_pageRenderer.getConfig());
			}
		}
		_pageRenderer.setDocumentEditor(doc);
		
		if (_pageRenderer.getConfig().getBoolean("client.up.enabled", false)) {
			doc.setActiveLayerClientOnly(false);
			ClientUpSync clientSync = new ClientUpSync(doc.getHistory());
			System.out.println("Connect remote");
			clientSync.start(_pageRenderer.getConfig().getString("client.up.host", ""),
					_pageRenderer.getConfig().getInt("client.up.port", 8025),
					_pageRenderer.getConfig().getInt("client.up.timeout", 5000000));
			clientSync.onActionPerformed(new SetDocumentAction(doc.getDocument().getClientId(), doc.getDocument()));
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
