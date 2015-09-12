package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.PdfSerializable;
import de.freiburg.uni.tablet.presenter.document.document.IDocument;
import de.freiburg.uni.tablet.presenter.document.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.document.editor.IDocumentEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.pdf.PdfRenderer;
import de.freiburg.uni.tablet.presenter.tools.ToolImage;

public class FileHelper {
	public static final int FILE_HEADER_VERSION = 0x00010001;
	public static final int FILE_HEADER_DOCUMENT = 0x6a747064; //jtpd
	public static final int FILE_HEADER_PAGE = 0x6a747070; //jtpp
	public static final int FILE_HEADER_SESSION = 0x6a747073; //jtps
	
	private static class PdfModeOption {
		private final int _key;
		private final String _text;

		public PdfModeOption(final int key, final String text) {
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
			return "Session Data (session.jps)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith("session.jps") || f.isDirectory();
		}
	};
	public static final FileFilter FILTER_image = new FileFilter() {
		@Override
		public String getDescription() {
			return "Image (*.png, *.jpg, *.jpeg, *.gif, *.bmp)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith(".png") || f.getPath().toLowerCase().endsWith(".jpg") ||
					f.getPath().toLowerCase().endsWith(".jpeg") || f.getPath().toLowerCase().endsWith(".gif") ||
					f.getPath().toLowerCase().endsWith(".bmp") || f.isDirectory();
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
	
	public static void saveDocument(final IDocument document, final File file) throws IOException {
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		
		final BinarySerializer writer = new BinarySerializer(
				fileChannel);
		writer.writeInt(FILE_HEADER_DOCUMENT);
		writer.writeInt(FILE_HEADER_VERSION);
		writer.writeObjectTable(document);
		writer.flush();
		fileChannel.close();
	}
	
	public static void saveDocumentPage(final DocumentPage page, final File file) throws IOException {
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		
		final BinarySerializer writer = new BinarySerializer(
				fileChannel);
		writer.writeInt(FILE_HEADER_PAGE);
		writer.writeInt(FILE_HEADER_VERSION);
		page.serializeDirect(writer);
		writer.flush();
		fileChannel.close();
	}
	
	public static void saveSession(final IDocumentEditor editor, final File file) throws IOException {
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		final BinarySerializer writer = new BinarySerializer(fileChannel);
		writer.writeInt(FILE_HEADER_SESSION);
		writer.writeInt(FILE_HEADER_VERSION);
		editor.serialize(writer);
		writer.flush();
		fileChannel.close();
	}
	
	public static void savePdf(final IDocument document, final IDocument baseDocument, final DocumentConfig config, final File file) throws IOException {
		Logger.getLogger(ButtonSaveAs.class.getName()).log(Level.INFO, "Render as PDF " + file.getPath());
		try {
			final int pdfDefaultWidth = config.getInt("pdf.defaultWidth", 1024);
			final int pdfDefaultHeight = config.getInt("pdf.defaultHeight", 768);
			final boolean pdfIgnoreEmptyPages = config.getBoolean("pdf.ignoreEmptyPages", false);
			final boolean pdfShowPageNumber = config.getBoolean("pdf.showPageNumber", true);
			final boolean pdfIgnoreEmptyPageNumber = config.getBoolean("pdf.ignoreEmptyPageNumber", true);
			final boolean pdfIgnorePdfPageNumber = config.getBoolean("pdf.ignorePdfPageNumber", true);
			final float pdfThicknessFactor = config.getFloat("pdf.thicknessFactor", 0.2f);
			final PdfRenderer pdfRenderer = new PdfRenderer(file,
					pdfDefaultWidth, pdfDefaultHeight,
					pdfIgnoreEmptyPages, pdfIgnoreEmptyPageNumber, pdfIgnorePdfPageNumber, pdfShowPageNumber, pdfThicknessFactor);
			final int count = document.getPageCount();
			for (int i = 0; i < count; i++) {
				final DocumentPage page = document.getPageByIndex(i);
				final DocumentPage basePage = ((baseDocument != null)?baseDocument.getPageByIndex(i):null);
				pdfRenderer.nextPage((basePage != null)?basePage.getBackgroundEntity():page.getBackgroundEntity());
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
	
	public static boolean showSaveDialog(final Component component, final IToolPageEditor editor, final FileFilter defaultType) {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(FILTER_pdf);
		fileChooser.addChoosableFileFilter(FILTER_presenterDocumentFile);
		fileChooser.addChoosableFileFilter(FILTER_presenterPageFile);
		fileChooser.setFileFilter(defaultType);
		final String defaultDir = editor.getConfig().getString("file.dialog.location", "");
		if (!defaultDir.isEmpty()) {
			fileChooser.setCurrentDirectory(new File(defaultDir));
		}
		if (fileChooser.showSaveDialog(component) == JFileChooser.APPROVE_OPTION) {
			try {
				if (editor.getConfig().getBoolean("file.dialog.saveLocation", false)) {
					File location = fileChooser.getCurrentDirectory();
					if (location == null) {
						editor.getConfig().setString("file.dialog.location", "");
					} else {
						editor.getConfig().setString("file.dialog.location", location.getPath());
					}
					editor.getConfig().write(false);
				}
				File f = fileChooser.getSelectedFile();
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
					savePdf(editor.getDocumentEditor().getFrontDocument(), editor.getDocumentEditor().getBackDocument(), editor.getConfig(), f);
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
	public static IEditableDocument openDocument(final File file) throws IOException {
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
		
		final BinaryDeserializer reader = new BinaryDeserializer(fileChannel);
		if (FILE_HEADER_DOCUMENT != reader.readInt()) {
			throw new IOException("Invalid header for document");
		}
		if (FILE_HEADER_VERSION != reader.readInt()) {
			throw new IOException("Invalid version for document");
		}
		final IEditableDocument document = reader.readObjectTable();
		fileChannel.close();
		
		return document;
	}
	
	/**
	 * Opens a page file
	 * 
	 * @param file
	 * @param dstPage
	 * @throws IOException
	 */
	public static void openPage(final File file, final DocumentPage dstPage) throws IOException {
		final SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
		
		final BinaryDeserializer reader = new BinaryDeserializer(fileChannel);
		if (FILE_HEADER_PAGE != reader.readInt()) {
			throw new IOException("Invalid header for document");
		}
		if (FILE_HEADER_VERSION != reader.readInt()) {
			throw new IOException("Invalid version for document");
		}
		dstPage.deserializeDirect(reader);
		fileChannel.close();
	}
	
	/**
	 * Opens a session.jps file
	 * @param file
	 * @param editor
	 * @throws IOException
	 */
	public static void openSession(final File file, final IDocumentEditor editor) throws IOException {
		System.out.println("Loading session");
		SeekableByteChannel fileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
		BinaryDeserializer reader = new BinaryDeserializer(fileChannel);
		if (FILE_HEADER_SESSION != reader.readInt()) {
			throw new IOException("Invalid header for session");
		}
		if (FILE_HEADER_VERSION != reader.readInt()) {
			throw new IOException("Invalid version for document");
		}
		editor.deserialize(reader);
		fileChannel.close();
		System.out.println("Session loaded");
	}
	
	/**
	 * Opens a pdf file
	 * @param component
	 * @param editor
	 * @param pdfFile
	 * @return true if accepted
	 * @throws IOException
	 */
	public static boolean openPdf(final Component component, final IToolPageEditor editor, final File pdfFile) throws IOException {
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
			final PdfSerializable pdf = new PdfSerializable(editor.getDocumentEditor().getFrontDocument(), pdfFile);
			editor.getDocumentEditor().getFrontDocument().setPdfPages(pdf, res.getKey());
			return true;
		}
		return false;
	}
	
	/**
	 * Tries to open the given file by its mime type
	 * @param component
	 * @param editor
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static boolean openFile(final Component component, final IToolPageEditor editor, final File file) throws IOException {
		MediaType mediaType;
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		try {
			AutoDetectParser parser = new AutoDetectParser();
			Detector detector = parser.getDetector();
			Metadata md = new Metadata();
			md.add(Metadata.RESOURCE_NAME_KEY, file.getAbsolutePath());
			mediaType = detector.detect(bis, md);
		} finally {
			bis.close();
		}
		
		if ("application".equals(mediaType.getType())) {
			if ("jtabletpresenter".equals(mediaType.getSubtype())) {
				String type = mediaType.getParameters().get("type");
				System.out.println("jtabletpresenter subtype: " + type);
				if ("document".equals(type)) {
					editor.getDocumentEditor().setBackDocument(null);
					editor.getDocumentEditor().setDocument(openDocument(file));
					return true;
				} else if ("page".equals(type)) {
					DocumentPage currentPage = editor.getDocumentEditor().getCurrentPage();
					openPage(file, currentPage);
					return true;
				} else if ("sessiondat".equals(type)) {
					openSession(file, editor.getDocumentEditor());
					return true;
				}
			} else if ("pdf".equals(mediaType.getSubtype())) {
				return openPdf(component, editor, file);
			}
		} else if ("image".equals(mediaType.getType())) {
			editor.getDocumentEditor().setCurrentImageFile(file);
			editor.getPageEditor().setNormalToolOnce(new ToolImage(editor));
			return true;
		}
		return false;
	}
	
	public static boolean showOpenDialog(final Component component, final IToolPageEditor editor, final FileFilter defaultFilter) {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(FILTER_presenterDocumentFile);
		fileChooser.addChoosableFileFilter(FILTER_presenterPageFile);
		fileChooser.addChoosableFileFilter(FILTER_pdf);
		fileChooser.addChoosableFileFilter(FILTER_session);
		fileChooser.addChoosableFileFilter(FILTER_image);
		fileChooser.setFileFilter(defaultFilter);
		final String defaultDir = editor.getConfig().getString("file.dialog.location", "");
		if (!defaultDir.isEmpty()) {
			fileChooser.setCurrentDirectory(new File(defaultDir));
		}
		if (fileChooser.showOpenDialog(component) == JFileChooser.APPROVE_OPTION) {
			try {
				if (editor.getConfig().getBoolean("file.dialog.saveLocation", false)) {
					File location = fileChooser.getCurrentDirectory();
					if (location == null) {
						editor.getConfig().setString("file.dialog.location", "");
					} else {
						editor.getConfig().setString("file.dialog.location", location.getPath());
					}
					editor.getConfig().write(false);
				}
				openFile(component, editor, fileChooser.getSelectedFile());
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
	
	public static void autosave(final IDocumentEditor editor) {
		try {
			final int index = editor.getCurrentPageIndex();
			final DocumentPage page = editor.getCurrentPage();
			if (page != null) {
				File autosaveDir = new File("autosave");
				if (!autosaveDir.exists()) {
					autosaveDir.mkdirs();
				}
				FileHelper.saveDocumentPage(page, new File("autosave/page_" + index + "-" + String.format("%X", page.getId()) + ".jpp"));
			}
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
