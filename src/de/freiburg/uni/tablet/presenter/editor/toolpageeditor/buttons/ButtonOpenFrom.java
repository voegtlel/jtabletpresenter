/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonOpenFrom extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the action with an editor.
	 */
	public ButtonOpenFrom(final IToolPageEditor editor) {
		super("open", editor, "Open", "/buttons/document-open.png");
	}
	
	/**
	 * Opens a document file
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Document openDocument(File file) throws IOException {
		final FileInputStream fileInputStream = new FileInputStream(file);
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(
				fileInputStream);
		final BinaryDeserializer binaryDeserializer = new BinaryDeserializer(
				bufferedInputStream);
		final Document document = binaryDeserializer.readObjectTable();
		bufferedInputStream.close();
		fileInputStream.close();
		return document;
	}
	
	/**
	 * Opens a document file
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static DocumentPage openPage(File file) throws IOException {
		final FileInputStream fileInputStream = new FileInputStream(file);
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(
				fileInputStream);
		final BinaryDeserializer binaryDeserializer = new BinaryDeserializer(
				bufferedInputStream);
		final DocumentPage page = new DocumentPage(binaryDeserializer, null);
		bufferedInputStream.close();
		fileInputStream.close();
		return page;
	}

	@Override
	public void perform(final Component button) {
		final JFileChooser fileChooser = new JFileChooser();
		final FileFilter presenterDocumentFile = new FileFilter() {
			@Override
			public String getDescription() {
				return "JPresenter Document File (*.jpd)";
			}

			@Override
			public boolean accept(final File f) {
				return f.getPath().toLowerCase().endsWith(".jpd");
			}
		};
		final FileFilter presenterPageFile = new FileFilter() {
			@Override
			public String getDescription() {
				return "JPresenter Page File (*.jpp)";
			}

			@Override
			public boolean accept(final File f) {
				return f.getPath().toLowerCase().endsWith(".jpp");
			}
		};
		final FileFilter pdf = new FileFilter() {
			@Override
			public String getDescription() {
				return "PDF (*.pdf)";
			}

			@Override
			public boolean accept(final File f) {
				return f.getPath().toLowerCase().endsWith(".pdf");
			}
		};
		fileChooser.addChoosableFileFilter(presenterDocumentFile);
		fileChooser.addChoosableFileFilter(presenterPageFile);
		fileChooser.addChoosableFileFilter(pdf);
		fileChooser.setFileFilter(presenterDocumentFile);
		if (fileChooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
			try {
				final File f = fileChooser.getSelectedFile();
				if (f.getPath().toLowerCase().endsWith(".jpd")) {
					_editor.getDocumentEditor().setDocument(openDocument(f));
				} else if (f.getPath().toLowerCase().endsWith(".jpp")) {
					DocumentPage page = openPage(f).clone(_editor.getDocumentEditor().getDocument());
					_editor.getDocumentEditor().getDocument().insertPage(
							_editor.getDocumentEditor().getCurrentPage(), page);
					_editor.getDocumentEditor().setCurrentPage(page);
				} else if (f.getPath().toLowerCase().endsWith(".pdf")) {
					_editor.getDocumentEditor().getDocument().setPdf(f);
				}
			} catch (final FileNotFoundException e) {
				JOptionPane.showMessageDialog(button, "Couldn't open file",
						"Error", JOptionPane.ERROR_MESSAGE);
			} catch (final IOException e) {
				JOptionPane.showMessageDialog(button, "Couldn't read file: "
						+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			
		}
	}
}
