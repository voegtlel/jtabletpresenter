package de.freiburg.uni.tablet.presenter.updater;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class UpdateDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private JProgressBar _progressBarTotal;
	private JProgressBar _progressBarCurrent;

	private Thread _updateThread;

	private String _mainClassName;

	private String[] _mainClassArgs;

	private Object _threadSync = new Object();
	private int _step = 0;
	private boolean _cancelRequested = false;

	private JButton _cancelButton;

	private JTextArea _consoleOutput;
	
	public UpdateDialog(final Frame frame, final String mainClassName, final String[] mainClassArgs) {
		super(frame);
		_mainClassName = mainClassName;
		_mainClassArgs = mainClassArgs;
		initialize();
	}
	
	public UpdateDialog() {
		super();
		initialize();
	}
	
	/**
	 * Create the dialog.
	 */
	private void initialize() {
		setModal(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("Updater");
		setBounds(100, 100, 450, 300);
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(final WindowEvent e) {
		        onClose(e);
		    }
		});
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 1.0};
		contentPanel.setLayout(gbl_contentPanel);
		{
			_progressBarTotal = new JProgressBar();
			_progressBarTotal.setMaximum(10000);
			_progressBarTotal.setStringPainted(true);
			GridBagConstraints gbc_progressBarTotal = new GridBagConstraints();
			gbc_progressBarTotal.insets = new Insets(0, 0, 5, 0);
			gbc_progressBarTotal.fill = GridBagConstraints.BOTH;
			gbc_progressBarTotal.gridx = 0;
			gbc_progressBarTotal.gridy = 0;
			contentPanel.add(_progressBarTotal, gbc_progressBarTotal);
		}
		{
			_progressBarCurrent = new JProgressBar();
			_progressBarCurrent.setMaximum(10000);
			_progressBarCurrent.setStringPainted(true);
			GridBagConstraints gbc_progressBarCurrent = new GridBagConstraints();
			gbc_progressBarCurrent.fill = GridBagConstraints.BOTH;
			gbc_progressBarCurrent.insets = new Insets(0, 0, 5, 0);
			gbc_progressBarCurrent.gridx = 0;
			gbc_progressBarCurrent.gridy = 1;
			contentPanel.add(_progressBarCurrent, gbc_progressBarCurrent);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 0;
			gbc_scrollPane.gridy = 2;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				_consoleOutput = new JTextArea();
				scrollPane.setViewportView(_consoleOutput);
				_consoleOutput.setEditable(false);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				_cancelButton = new JButton("Cancel");
				_cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						onCancel();
					}
				});
				_cancelButton.setActionCommand("Cancel");
				buttonPane.add(_cancelButton);
			}
		}
		
		startUpdateThread();
	}

	private void startUpdateThread() {
		_updateThread = new Thread() {
			@Override
			public void run() {
				performUpdate();
			}
		};
		_updateThread.start();
	}
	
	private void log(final String message) {
		System.out.println("Log: " + message);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_consoleOutput.append(message + "\n");
			}
		});
	}
	
	private void setProgress(final int substep, final int substepTotal) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				float progress = _step / 8.0f + (float)substep / (float)substepTotal / 8.0f;
				_progressBarTotal.setValue((int)(10000 * progress));
				_progressBarCurrent.setValue((int)(10000 * ((float)substep / substepTotal)));
			}
		});
	}

	protected void performUpdate() {
		try {
			log("Reading version information");
			ClientUpdater.beginInstallUpdate(new IUpdateListener() {
				@Override
				public void onVersionInfo(final String fromVersion, final String toVersion) {
					log("Starting update from " + fromVersion + " to " + toVersion);
					synchronized (_threadSync) {
						_step = 1;
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
				}

				@Override
				public void onStartDownloadArchive(final String downloadUrl) {
					log("Downloading archive");
					synchronized (_threadSync) {
						_step = 2;
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
				}

				@Override
				public void onStartWriteArchive(final File targetFile) {
					log("Creating archive");
					synchronized (_threadSync) {
						_step = 3;
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
				}

				@Override
				public void onStartWriteUpdater(final File updaterFile) {
					log("Extracting updater");
					synchronized (_threadSync) {
						_step = 4;
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
				}

				@Override
				public void onStartUpdater() {
					synchronized (_threadSync) {
						_step = 5;
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
					log("Exiting in 1 sec");
					log("Starting updater");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (_threadSync) {
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
				}
			}, new IGetListener() {
				private int _expectedSize;

				@Override
				public void onProgress(final int bytesLoaded) {
					synchronized (_threadSync) {
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
					if (_expectedSize > 0) {
						setProgress(bytesLoaded, _expectedSize);
					} else {
						setProgress(1, 2);
					}
				}
				
				@Override
				public void onDone() {
					synchronized (_threadSync) {
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
					log("Done");
				}
				
				@Override
				public void onBegin(final String urlStr, final int expectedSize) {
					synchronized (_threadSync) {
						if (_cancelRequested) {
							throw new RuntimeException("Cancel requested");
						}
					}
					_expectedSize = expectedSize;
					log("Downloading " + urlStr + (expectedSize >= 0?"(" + expectedSize + " bytes)":""));
				}
			}, _mainClassName, _mainClassArgs);
		} catch (IOException e) {
			log("Error: " + e.toString());
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_cancelButton.setEnabled(false);
			}
		});
	}

	protected void onCancel() {
		synchronized (_threadSync) {
			_cancelRequested = true;
		}
		_cancelButton.setEnabled(false);
		log("Cancelling...");
	}

	protected void onClose(final WindowEvent e) {
		if (_updateThread.isAlive()) {
			onCancel();
		} else {
			this.setVisible(false);
			this.dispose();
		}
	}
}
