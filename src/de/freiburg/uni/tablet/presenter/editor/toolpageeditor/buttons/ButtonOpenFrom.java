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
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonOpenFrom extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonOpenFrom(final IToolPageEditor editor) {
		super(editor, "Open", "/buttons/document-open.png");
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
			final File f = fileChooser.getSelectedFile();
			try {
				final FileInputStream fileInputStream = new FileInputStream(f);
				final BufferedInputStream bufferedInputStream = new BufferedInputStream(
						fileInputStream);
				final BinaryDeserializer binaryDeserializer = new BinaryDeserializer(
						bufferedInputStream);
				final Document document = binaryDeserializer.readObjectTable();
				_editor.getDocumentEditor().setDocument(document);
				bufferedInputStream.close();
				fileInputStream.close();
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
