package de.freiburg.uni.tablet.presenter;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;

import de.freiburg.uni.tablet.presenter.gui.JPageRenderer;

public class TestApp {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final TestApp window = new TestApp();
					window.frame.setVisible(true);
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
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JPageRenderer _pageRenderer = new JPageRenderer();
		frame.getContentPane().add(_pageRenderer, BorderLayout.CENTER);
	}

}
