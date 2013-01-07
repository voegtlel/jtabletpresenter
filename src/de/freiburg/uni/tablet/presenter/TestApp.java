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

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageEditor;

public class TestApp {

	private JPageEditor _pageRenderer;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		System.setProperty("sun.awt.noerasebackground", "true");
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final TestApp window = new TestApp();
					window._pageRenderer.setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TestApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		NativeLibrary.addSearchPath("freetype", ".");
		_pageRenderer = new JPageEditor();
		_pageRenderer.setTitle("JTabletPresenter v1.01");
		_pageRenderer.setBounds(100, 100, 640, 480);
		_pageRenderer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DocumentEditor doc = null;
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
		_pageRenderer.setDocumentEditor(doc);
		
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
