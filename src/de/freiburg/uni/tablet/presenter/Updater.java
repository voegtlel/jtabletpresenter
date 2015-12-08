package de.freiburg.uni.tablet.presenter;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class Updater extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private JProgressBar _progressBarTotal;
	private JProgressBar _progressBarCurrent;

	private Thread _updateThread;

	private Object _threadSync = new Object();
	private int _step = 6;
	private boolean _cancelRequested = false;

	private JButton _cancelButton;

	private JTextArea _consoleOutput;

	private File _archiveFile;
	private File _targetDirectory;

	private File _jarFile;

	private String[] _mainClassArgs;

	public Updater(final File archiveFile, final File targetDirectory, final File jarFile, final String[] mainClassArgs) {
		_archiveFile = archiveFile;
		_targetDirectory = targetDirectory;
		_jarFile = jarFile;
		_mainClassArgs = mainClassArgs;
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
			log("Extracting update archive");
			ZipFile zf = new ZipFile(_archiveFile, ZipFile.OPEN_READ);
			try {
				ArrayList<? extends ZipEntry> entries = Collections.list(zf.entries());
				int index = 0;
				for (ZipEntry entry : entries) {
					synchronized (_threadSync) {
						if (_cancelRequested) {
							throw new IOException("Cancel requested");
						}
					}
					setProgress(index + 1, entries.size() + 1);
					int splitFirst = entry.getName().indexOf('/', 1);
					if (splitFirst != -1) {
						String subName = entry.getName().substring(splitFirst+1);
						File file = new File(_targetDirectory, subName);
						if (entry.isDirectory()) {
							log("Extracting " + subName + " (Directory)");
							file.mkdirs();
							if (!file.exists() || !file.isDirectory()) {
								throw new IOException("Can't create directory " + file);
							}
						} else {
							log("Extracting " + subName + " (" + entry.getSize() + "B)");
							if (file.exists()) {
								log("Overwriting file " + file);
							}
							InputStream zis = zf.getInputStream(entry);
							FileOutputStream bos = new FileOutputStream(file);
							try {
								byte[] buffer = new byte[1024];
								while (true) {
									int read = zis.read(buffer);
									if (read < 0) {
										break;
									}
									bos.write(buffer, 0, read);
								}
							} finally {
								bos.close();
							}
						}
					}
					index++;
				}
			} finally {
				zf.close();
			}
			synchronized (_threadSync) {
				_step = 7;
				if (_cancelRequested) {
					throw new IOException("Cancel requested");
				}
			}
			setProgress(0, 2);
			log("Extraction successful");
			
			log("Starting JTabletPresenter");
			String javaPath =  System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			String[] subArgs = new String[3 + _mainClassArgs.length];
			subArgs[0] = javaPath;
			subArgs[1] = "-jar";
			subArgs[2] = _jarFile.getAbsolutePath();
			for (int i = 0; i < _mainClassArgs.length; i++) {
				subArgs[i + 3] = _mainClassArgs[i];
			}
			ProcessBuilder pb = new ProcessBuilder(subArgs);
			pb.directory(_targetDirectory);
			Process process = pb.start();
			
			log("Waiting for startup");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			setProgress(1, 2);
			if (!process.isAlive()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					log(line);
				}
				reader.close();
				reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				while ((line = reader.readLine()) != null) {
					log(line);
				}
				reader.close();
				log("Error: Process crashed");
			} else {
				log("Exiting now");
				System.exit(0);
			}
		} catch (Exception e) {
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
	
	public static void main(final String[] args) {
		if (args.length >= 4) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String inputFile = args[0];
			String outputFolder = args[1];
			String selfFile = args[2];
			//String classPath = args[3];
			//String mainClassName = args[4];
			File jarFile = new File(outputFolder, "JTabletPresenter.jar");
			String[] mainClassArgs = new String[args.length - 5];
			for (int i = 0; i < mainClassArgs.length; i++) {
				mainClassArgs[i] = args[i + 5];
			}
			File self = new File(selfFile);
			self.deleteOnExit();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Updater updater = new Updater(new File(inputFile), new File(outputFolder), jarFile, mainClassArgs);
					updater.setVisible(true);
				}
			});
		} else {
			System.out.println("This file must be called from the update process");
		}
	}
}
