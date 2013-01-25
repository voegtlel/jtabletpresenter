package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.document.PdfSerializable;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.pdf.PdfRenderer;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;

public class FileHelper {
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
	
	public static final FileFilter FILTER_presenterDocumentFile = new FileFilter() {
		@Override
		public String getDescription() {
			return "JPresenter Document File (*.jpd)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith(".jpd") || f.isDirectory();
		}
	};
	public static final FileFilter FILTER_presenterPageFile = new FileFilter() {
		@Override
		public String getDescription() {
			return "JPresenter Page File (*.jpp)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith(".jpp") || f.isDirectory();
		}
	};
	public static final FileFilter FILTER_pdf = new FileFilter() {
		@Override
		public String getDescription() {
			return "PDF (*.pdf)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith(".pdf") || f.isDirectory();
		}
	};
	
	public static FileFilter stringToFilter(final String str) {
		if ("pdf".equalsIgnoreCase(str)) {
			return FILTER_pdf;
		} else if ("jpp".equalsIgnoreCase(str)) {
			return FILTER_presenterPageFile;
		} else {
			return FILTER_presenterDocumentFile;
		}
	}
	
	public static void saveDocument(final IEditableDocument document, final File file) throws IOException {
		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
				fileOutputStream);
		final BinarySerializer binarySerializer = new BinarySerializer(
				bufferedOutputStream);
		binarySerializer.writeObjectTable(document);
		binarySerializer.close();
		bufferedOutputStream.close();
		fileOutputStream.close();
	}
	
	public static void saveDocumentPage(DocumentPage page, File file) throws IOException {
		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
				fileOutputStream);
		final BinarySerializer writer = new BinarySerializer(
				bufferedOutputStream);
		page.serializeDirect(writer);
		writer.close();
		bufferedOutputStream.close();
		fileOutputStream.close();
	}
	
	public static void savePdf(Document document, Document baseDocument, DocumentConfig config, File file) throws IOException {
		Logger.getLogger(ButtonSaveAs.class.getName()).log(Level.INFO, "Render as PDF " + file.getPath());
		try {
			final int pdfDefaultWidth = config.getInt("pdf.defaultWidth", 1024);
			final int pdfDefaultHeight = config.getInt("pdf.defaultHeight", 768);
			final boolean pdfIgnoreEmptyPages = config.getBoolean("pdf.ignoreEmptyPages", false);
			final boolean pdfShowPageNumber = config.getBoolean("pdf.showPageNumber", true);
			final boolean pdfIgnoreEmptyPageNumber = config.getBoolean("pdf.ignoreEmptyPageNumber", true);
			final float pdfThicknessFactor = config.getFloat("pdf.thicknessFactor", 0.2f);
			final PdfRenderer pdfRenderer = new PdfRenderer(file,
					pdfDefaultWidth, pdfDefaultHeight,
					pdfIgnoreEmptyPages, pdfIgnoreEmptyPageNumber, pdfShowPageNumber, pdfThicknessFactor);
			LinkedElement<DocumentPage> page = document.getPages();
			LinkedElement<DocumentPage> basePage = baseDocument.getPages();
			for(; page != null; page = page.getNext()) {
				pdfRenderer.nextPage(page.getData().getPdfPage());
				basePage.getData().render(pdfRenderer);
				page.getData().render(pdfRenderer);
			}
			pdfRenderer.close();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public static boolean showSaveDialog(final Component component, final IToolPageEditor editor, final FileFilter defaultType) {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(FILTER_pdf);
		fileChooser.addChoosableFileFilter(FILTER_presenterDocumentFile);
		fileChooser.addChoosableFileFilter(FILTER_presenterPageFile);
		fileChooser.setFileFilter(defaultType);
		if (fileChooser.showSaveDialog(component) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				if ((fileChooser.getFileFilter() == FILTER_pdf)
						&& !fileChooser.getSelectedFile().getPath().toLowerCase().endsWith(".pdf")) {
					f = new File(f.getPath() + ".pdf");
				} else if ((fileChooser.getFileFilter() == FILTER_presenterDocumentFile)
						&& !fileChooser.getSelectedFile().getPath().toLowerCase().endsWith(".jpd")) {
					f = new File(f.getPath() + ".jpd");
				} else if ((fileChooser.getFileFilter() == FILTER_presenterPageFile)
						&& !fileChooser.getSelectedFile().getPath().toLowerCase().endsWith(".jpp")) {
					f = new File(f.getPath() + ".jpp");
				}
				if (f.getPath().toLowerCase().endsWith(".jpd")) {
					saveDocument(editor.getDocumentEditor().getDocument(), f);
					return true;
				} else if (f.getPath().toLowerCase().endsWith(".jpp")) {
					saveDocumentPage(editor.getDocumentEditor().getCurrentPage(), f);
					return true;
				} else if (f.getPath().toLowerCase().endsWith(".pdf")) {
					savePdf(editor.getDocumentEditor().getDocument(), editor.getDocumentEditor().getBaseDocument(), editor.getConfig(), f);
					return true;
				} else {
					JOptionPane.showMessageDialog(component, "Can't save. Unrecognized file type.");
				}
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(component, "Couldn't open file",
						"Error", JOptionPane.ERROR_MESSAGE);
			} catch (final IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(component, "Couldn't write file: "
						+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return false;
	}
	
	/**
	 * Opens a document file
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static IEditableDocument openDocument(File file) throws IOException {
		final FileInputStream fileInputStream = new FileInputStream(file);
		final BufferedInputStream bufferedInputStream = new BufferedInputStream(
				fileInputStream);
		final BinaryDeserializer binaryDeserializer = new BinaryDeserializer(
				bufferedInputStream);
		final IEditableDocument document = binaryDeserializer.readObjectTable();
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
		fileChooser.addChoosableFileFilter(FILTER_presenterDocumentFile);
		fileChooser.addChoosableFileFilter(FILTER_presenterPageFile);
		fileChooser.addChoosableFileFilter(FILTER_pdf);
		fileChooser.setFileFilter(defaultFilter);
		if (fileChooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
			try {
				final File f = fileChooser.getSelectedFile();
				if (f.getPath().toLowerCase().endsWith(".jpd")) {
					editor.getDocumentEditor().setDocument(openDocument(f));
					return true;
				} else if (f.getPath().toLowerCase().endsWith(".jpp")) {
					final DocumentPage page = editor.getDocumentEditor().getDocument().clonePage(openPage(f));
					editor.getDocumentEditor().getDocument().insertPage(
							editor.getDocumentEditor().getCurrentPage(), page);
					editor.getDocumentEditor().setCurrentPage(page);
					return true;
				} else if (f.getPath().toLowerCase().endsWith(".pdf")) {
					final PdfModeOption defOpt = new PdfModeOption(IEditableDocument.PDF_MODE_CLEAR, "Clear all");
					final Object dialogResult = JOptionPane.showInputDialog(component, "Select PDF Mode", "PDF Loading...", JOptionPane.QUESTION_MESSAGE, null, new Object[] {
							defOpt,
						new PdfModeOption(IEditableDocument.PDF_MODE_REINDEX, "Reindex Pages"),
						new PdfModeOption(IEditableDocument.PDF_MODE_KEEP_INDEX, "Keep Page Indices"),
						new PdfModeOption(IEditableDocument.PDF_MODE_APPEND, "Append Pdf Pages"),
						defOpt
					}, defOpt);
					if (dialogResult != null) {
						final PdfModeOption res = (PdfModeOption)dialogResult;
						final PdfSerializable pdf = new PdfSerializable(editor.getDocumentEditor().getDocument(), f);
						editor.getDocumentEditor().getDocument().setPdfPages(pdf, res.getKey());
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
	
	public static void autosave(DocumentEditor editor) {
		try {
			int index = editor.getCurrentPageIndex();
			DocumentPage page = editor.getCurrentPage();
			File autosaveDir = new File("autosave");
			if (!autosaveDir.exists()) {
				autosaveDir.mkdirs();
			}
			FileHelper.saveDocumentPage(page, new File("autosave/page_" + index + "-" + String.format("%X", page.getId()) + ".jpp"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
