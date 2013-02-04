package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IDocument;
import de.freiburg.uni.tablet.presenter.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.document.PdfSerializable;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.pdf.PdfRenderer;

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
	public static final FileFilter FILTER_session = new FileFilter() {
		@Override
		public String getDescription() {
			return "Session Data (session.dat)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith("session.dat") || f.isDirectory();
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
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		
		final BinarySerializer writer = new BinarySerializer(
				fileChannel);
		writer.writeObjectTable(document);
		writer.flush();
		fileChannel.close();
	}
	
	public static void saveDocumentPage(DocumentPage page, File file) throws IOException {
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		
		final BinarySerializer writer = new BinarySerializer(
				fileChannel);
		page.serializeDirect(writer);
		writer.flush();
		fileChannel.close();
	}
	
	public static void savePdf(IDocument document, IDocument baseDocument, DocumentConfig config, File file) throws IOException {
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
			final int count = document.getPageCount();
			for(int i = 0; i < count; i++) {
				final DocumentPage page = document.getPageByIndex(i);
				final DocumentPage basePage = ((baseDocument != null)?baseDocument.getPageByIndex(i):null);
				pdfRenderer.nextPage((basePage != null)?basePage.getPdfPage():page.getPdfPage());
				if (basePage != null) {
					basePage.render(pdfRenderer);
				}
				page.render(pdfRenderer);
			}
			pdfRenderer.close();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public static void saveSession(final DocumentEditor editor, final File file) throws IOException {
		SeekableByteChannel bc = Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		BinarySerializer bs = new BinarySerializer(bc);
		editor.serialize(bs);
		bs.flush();
		bc.close();
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
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
		
		final BinaryDeserializer reader = new BinaryDeserializer(fileChannel);
		final IEditableDocument document = reader.readObjectTable();
		fileChannel.close();
		
		return document;
	}
	
	/**
	 * Opens a document file
	 * 
	 * @param file
	 * @param dstPage
	 * @throws IOException
	 */
	public static void openPage(final File file, final DocumentPage dstPage) throws IOException {
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
		
		final BinaryDeserializer reader = new BinaryDeserializer(fileChannel);
		dstPage.deserializeDirect(reader);
		fileChannel.close();
	}
	
	/**
	 * Opens a session.dat file
	 * @param file
	 * @param editor
	 * @throws IOException
	 */
	public static void openSession(final File file, final DocumentEditor editor) throws IOException {
		System.out.println("Loading session");
		SeekableByteChannel bs = Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
		BinaryDeserializer bd = new BinaryDeserializer(bs);
		editor.deserialize(bd);
		bs.close();
		System.out.println("Session loaded");
	}
	
	public static boolean showOpenDialog(final Component component, IToolPageEditor editor, FileFilter defaultFilter) {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(FILTER_presenterDocumentFile);
		fileChooser.addChoosableFileFilter(FILTER_presenterPageFile);
		fileChooser.addChoosableFileFilter(FILTER_pdf);
		fileChooser.addChoosableFileFilter(FILTER_session);
		fileChooser.setFileFilter(defaultFilter);
		if (fileChooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
			try {
				final File f = fileChooser.getSelectedFile();
				if (f.getPath().toLowerCase().endsWith(".jpd")) {
					editor.getDocumentEditor().setDocument(openDocument(f));
					return true;
				} else if (f.getPath().toLowerCase().endsWith(".jpp")) {
					DocumentPage currentPage = editor.getDocumentEditor().getCurrentPage();
					openPage(f, currentPage);
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
				} else if (f.getPath().toLowerCase().endsWith("session.dat")) {
					openSession(f, editor.getDocumentEditor());
					return true;
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
	
	public static byte[] readFile(final File file) throws IOException {
		final RandomAccessFile raf = new RandomAccessFile(file, "r");
		final byte[] data = new byte[(int) raf.getChannel().size()];
		raf.readFully(data);
		raf.close();
		System.out.println("Read file " + file);
		return data;
	}
}
