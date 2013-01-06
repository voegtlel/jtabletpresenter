/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.pdf.PdfRenderer;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;

/**
 * @author lukas
 * 
 */
public class ButtonSaveAs extends AbstractButtonAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final FileFilter FILTER_presenterDocumentFile = new FileFilter() {
		@Override
		public String getDescription() {
			return "JPresenter Document File (*.jpd)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith(".jpd");
		}
	};
	public static final FileFilter FILTER_presenterPageFile = new FileFilter() {
		@Override
		public String getDescription() {
			return "JPresenter Page File (*.jpp)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith(".jpp");
		}
	};
	public static final FileFilter FILTER_pdf = new FileFilter() {
		@Override
		public String getDescription() {
			return "PDF (*.pdf)";
		}

		@Override
		public boolean accept(final File f) {
			return f.getPath().toLowerCase().endsWith(".pdf");
		}
	};

	/**
	 * Creates the action with an editor.
	 */
	public ButtonSaveAs(final IToolPageEditor editor) {
		super("save", editor, "Save", "/buttons/document-save-as.png");
	}
	
	public static void saveDocument(Document document, File file) throws IOException {
		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
				fileOutputStream);
		final BinarySerializer binarySerializer = new BinarySerializer(
				bufferedOutputStream);
		binarySerializer.writeObjectTable(document.getId(), document);
		bufferedOutputStream.close();
		fileOutputStream.close();
	}
	
	public static void saveDocumentPage(DocumentPage page, File file) throws IOException {
		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		
		final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
				fileOutputStream);
		final BinarySerializer binarySerializer = new BinarySerializer(
				bufferedOutputStream);
		page.serializeDirect(binarySerializer);
		bufferedOutputStream.close();
		fileOutputStream.close();
	}
	
	public static void savePdf(Document document, DocumentConfig config, File file) throws IOException {
		Logger.getLogger(ButtonSaveAs.class.getName()).log(Level.INFO, "Render as PDF " + file.getPath());
		try {
			int pdfDefaultWidth = config.getInt("pdf.defaultWidth", 1024);
			int pdfDefaultHeight = config.getInt("pdf.defaultHeight", 768);
			boolean pdfIgnoreEmptyPages = config.getBoolean("pdf.ignoreEmptyPages", false);
			boolean pdfShowPageNumber = config.getBoolean("pdf.showPageNumber", true);
			boolean pdfIgnoreEmptyPageNumber = config.getBoolean("pdf.ignoreEmptyPageNumber", true);
			float pdfThicknessFactor = config.getFloat("pdf.thicknessFactor", 0.2f);
			PdfRenderer pdfRenderer = new PdfRenderer(file,
					pdfDefaultWidth, pdfDefaultHeight,
					document.getPdf().getDocument(),
					pdfIgnoreEmptyPages, pdfIgnoreEmptyPageNumber, pdfShowPageNumber, pdfThicknessFactor);
			LinkedElement<DocumentPage> pages = document.getPages();
			for(; pages != null; pages = pages.getNext()) {
				pdfRenderer.nextPage(pages.getData().getPdfPageIndex());
				pages.getData().getClientOnlyLayer().render(pdfRenderer);
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
					savePdf(editor.getDocumentEditor().getDocument(), editor.getConfig(), f);
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

	@Override
	public void perform(final Component button) {
		showSaveDialog(button, _editor, FILTER_pdf);
	}
}
