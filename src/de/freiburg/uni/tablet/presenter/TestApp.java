package de.freiburg.uni.tablet.presenter;

import java.awt.EventQueue;

import javax.swing.JFrame;

import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.JPageEditor;

public class TestApp {

	private JPageEditor _pageRenderer;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
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
		_pageRenderer = new JPageEditor();
		_pageRenderer.setBounds(100, 100, 450, 300);
		_pageRenderer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_pageRenderer.setDocumentEditor(new DocumentEditor());

		/*
		 * _pageRenderer.setPage(new DefaultPage());
		 * _pageRenderer.setNormalTool(new ToolScribble(_pageRenderer,
		 * _pageRenderer)); _pageRenderer.setInvertedTool(new
		 * ToolEraser(_pageRenderer, _pageRenderer, _pageRenderer));
		 */
	}
}
