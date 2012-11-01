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

import javax.print.attribute.standard.JobMessageFromOperator;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

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
		JFileChooser fileChooser = new JFileChooser();
		FileFilter presenterDocumentFile = new FileFilter() {
			@Override
			public String getDescription() {
				return "JPresenter Document File (*.jpd)";
			}
			
			@Override
			public boolean accept(File f) {
				return f.getPath().toLowerCase().endsWith(".jpd");
			}
		};
		FileFilter presenterPageFile = new FileFilter() {
			@Override
			public String getDescription() {
				return "JPresenter Page File (*.jpp)";
			}
			
			@Override
			public boolean accept(File f) {
				return f.getPath().toLowerCase().endsWith(".jpp");
			}
		};
		FileFilter pdf = new FileFilter() {
			@Override
			public String getDescription() {
				return "PDF (*.pdf)";
			}
			
			@Override
			public boolean accept(File f) {
				return f.getPath().toLowerCase().endsWith(".pdf");
			}
		};
		fileChooser.addChoosableFileFilter(presenterDocumentFile);
		fileChooser.addChoosableFileFilter(presenterPageFile);
		fileChooser.addChoosableFileFilter(pdf);
		fileChooser.setFileFilter(presenterDocumentFile);
		if (fileChooser.showSaveDialog(button) == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(f);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				BinarySerializer binarySerializer = new BinarySerializer(bufferedOutputStream);
				_editor.getDocumentEditor().getDocument().serialize(binarySerializer);
				bufferedOutputStream.close();
				fileOutputStream.close();
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(button, "Couldn't open file", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(button, "Couldn't write file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
