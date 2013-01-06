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
	
	private static class PdfModeOption {
		private final int _key;
		private final String _text;

		public PdfModeOption(int key, String text) {
			_key = key;
			_text = text;
		}
		
		public int getKey() {
			return _key;
		}
		
		@Override
		public String toString() {
			return _text;
		}
	}

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
	
	public static boolean showOpenDialog(final Component component, IToolPageEditor editor, FileFilter defaultFilter) {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(ButtonSaveAs.FILTER_presenterDocumentFile);
		fileChooser.addChoosableFileFilter(ButtonSaveAs.FILTER_presenterPageFile);
		fileChooser.addChoosableFileFilter(ButtonSaveAs.FILTER_pdf);
		fileChooser.setFileFilter(defaultFilter);
		if (fileChooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
			try {
				final File f = fileChooser.getSelectedFile();
				if (f.getPath().toLowerCase().endsWith(".jpd")) {
					editor.getDocumentEditor().setDocument(openDocument(f));
					return true;
				} else if (f.getPath().toLowerCase().endsWith(".jpp")) {
					final DocumentPage page = openPage(f).clone(editor.getDocumentEditor().getDocument());
					editor.getDocumentEditor().getDocument().insertPage(
							editor.getDocumentEditor().getCurrentPage(), page);
					editor.getDocumentEditor().setCurrentPage(page);
					return true;
				} else if (f.getPath().toLowerCase().endsWith(".pdf")) {
					final PdfModeOption defOpt = new PdfModeOption(Document.PDF_MODE_REINDEX, "Reindex Pages");
					final Object dialogResult = JOptionPane.showInputDialog(component, "Select PDF Mode", "PDF Loading...", JOptionPane.QUESTION_MESSAGE, null, new Object[] {
							defOpt,
						new PdfModeOption(Document.PDF_MODE_KEEP_INDEX, "Keep Page Indices"),
						new PdfModeOption(Document.PDF_MODE_APPEND, "Append Pdf Pages")
					}, defOpt);
					if (dialogResult != null) {
						final PdfModeOption res = (PdfModeOption)dialogResult;
						editor.getDocumentEditor().getDocument().setPdf(f, res.getKey());
						return true;
					}
				}
			} catch (final FileNotFoundException e) {
				JOptionPane.showMessageDialog(component, "Couldn't open file",
						"Error", JOptionPane.ERROR_MESSAGE);
			} catch (final IOException e) {
				JOptionPane.showMessageDialog(component, "Couldn't read file: "
						+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return false;
	}

	@Override
	public void perform(final Component button) {
		showOpenDialog(button, _editor, ButtonSaveAs.FILTER_presenterDocumentFile);
	}
}
