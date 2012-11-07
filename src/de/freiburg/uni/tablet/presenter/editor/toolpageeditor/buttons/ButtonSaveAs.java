/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import gnu.jpdf.PDFJob;

import java.awt.Component;
import java.awt.Graphics;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.Document;
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
	 * Creates the action with an editor.
	 */
	public ButtonSaveAs(final IToolPageEditor editor) {
		super(editor, "Save", "/buttons/document-save-as.png");
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
		if (fileChooser.showSaveDialog(button) == JFileChooser.APPROVE_OPTION) {
			final File f = fileChooser.getSelectedFile();
			try {
				final FileOutputStream fileOutputStream = new FileOutputStream(
						f);
				final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
						fileOutputStream);
				if (f.getPath().toLowerCase().endsWith(".jpd")) {
					final BinarySerializer binarySerializer = new BinarySerializer(
							bufferedOutputStream);
					final Document document = _editor.getDocumentEditor()
							.getDocument();
					binarySerializer.writeObjectTable(document.getId(), document);
				} else if (f.getPath().toLowerCase().endsWith(".pdf")) {
					PdfRenderer pdfRenderer = new PdfRenderer(bufferedOutputStream);
					LinkedElement<DocumentPage> pages = _editor.getDocumentEditor().getDocument().getPages();
					for(; pages != null; pages = pages.getNext()) {
						if (!pages.getData().isEmpty()) {
							pdfRenderer.nextPage();
							pages.getData().getClientOnlyLayer().render(pdfRenderer);
						}
					}
					pdfRenderer.close();
				}
				bufferedOutputStream.close();
				fileOutputStream.close();
			} catch (final FileNotFoundException e) {
				JOptionPane.showMessageDialog(button, "Couldn't open file",
						"Error", JOptionPane.ERROR_MESSAGE);
			} catch (final IOException e) {
				JOptionPane.showMessageDialog(button, "Couldn't write file: "
						+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
