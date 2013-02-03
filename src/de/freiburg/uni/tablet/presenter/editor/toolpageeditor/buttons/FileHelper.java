package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentConfig;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.IDocument;
import de.freiburg.uni.tablet.presenter.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.document.PdfSerializable;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.file.FileDialog;

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
	
	public static final String FILTER_presenterDocumentFile = ".jpd";
	public static final String FILTER_presenterPageFile = ".jpp";
	public static final String FILTER_pdf = ".pdf";
	public static final String FILTER_session = "session.dat";
	
	public static String stringToFilter(final String str) {
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
		
		final BinarySerializer writer = new BinarySerializer(
				fileOutputStream.getChannel());
		writer.writeObjectTable(document);
		writer.flush();
		fileOutputStream.close();
	}
	
	public static void saveDocumentPage(DocumentPage page, File file) throws IOException {
		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		
		final BinarySerializer writer = new BinarySerializer(
				fileOutputStream.getChannel());
		page.serializeDirect(writer);
		writer.flush();
		fileOutputStream.close();
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
			// TODO: Pdf renderer
			/*final PdfRenderer pdfRenderer = new PdfRenderer(file,
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
			pdfRenderer.close();*/
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	public static void saveSession(final DocumentEditor editor, final File file) throws IOException {
		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		BinarySerializer bs = new BinarySerializer(fileOutputStream.getChannel());
		editor.serialize(bs);
		bs.flush();
		fileOutputStream.close();
	}
	
	public static void showErrorDialog(final Context context, int message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(context).setMessage(message).setTitle(R.string.msg_title_error).setNeutralButton(R.string.msg_button_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).create();
		alertDialog.show();
	}
	
	public static void showSaveDialog(final Context context, final IToolPageEditor editor, final String defaultExtension, final OnSuccessListener onSuccessListener) {
		final FileDialog fileDialog = new FileDialog(context);
		fileDialog.setOpenFileDialog(false);
		fileDialog.setFilters(new String[] {
			FILTER_pdf,
			FILTER_presenterDocumentFile,
			FILTER_presenterPageFile
		});
		
		fileDialog.setOnDismissListener(new FileDialog.OnDismissListener() {
			@Override
			public void onDismiss(final DialogInterface dialog) {
				if (fileDialog.getSelectedFile() != null) {
					try {
						final File f = new File(fileDialog.getSelectedFile());
						if (f.getPath().toLowerCase().endsWith(".jpd")) {
							saveDocument(editor.getDocumentEditor().getDocument(), f);
							if (onSuccessListener != null) {
								onSuccessListener.onSuccess();
							}
						} else if (f.getPath().toLowerCase().endsWith(".jpp")) {
							saveDocumentPage(editor.getDocumentEditor().getCurrentPage(), f);
							if (onSuccessListener != null) {
								onSuccessListener.onSuccess();
							}
						} else if (f.getPath().toLowerCase().endsWith(".pdf")) {
							savePdf(editor.getDocumentEditor().getDocument(), editor.getDocumentEditor().getBaseDocument(), editor.getConfig(), f);
							if (onSuccessListener != null) {
								onSuccessListener.onSuccess();
							}
						} else {
							showErrorDialog(context, R.string.msg_save_unrecognized_type);
						}
					} catch (final FileNotFoundException e) {
						e.printStackTrace();
						showErrorDialog(context, R.string.msg_save_cant_open);
					} catch (final IOException e) {
						e.printStackTrace();
						showErrorDialog(context, R.string.msg_save_cant_write);
					}
				}
			}
		});
		fileDialog.show();
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
		
		final BinaryDeserializer reader = new BinaryDeserializer(fileInputStream.getChannel());
		final IEditableDocument document = reader.readObjectTable();
		fileInputStream.close();
		
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
		final FileInputStream fileInputStream = new FileInputStream(file);
		
		final BinaryDeserializer reader = new BinaryDeserializer(fileInputStream.getChannel());
		dstPage.deserializeDirect(reader);
		fileInputStream.close();
	}
	
	/**
	 * Opens a session.dat file
	 * @param file
	 * @param editor
	 * @throws IOException
	 */
	public static void openSession(final File file, final DocumentEditor editor) throws IOException {
		System.out.println("Loading session");
		final FileInputStream fileInputStream = new FileInputStream(file);
		BinaryDeserializer bd = new BinaryDeserializer(fileInputStream.getChannel());
		editor.deserialize(bd);
		fileInputStream.close();
		System.out.println("Session loaded");
	}
	
	public static void showOpenDialog(final Context context, final IToolPageEditor editor, final String defaultFilter, final OnSuccessListener onSuccessListener) {
		final FileDialog fileDialog = new FileDialog(context);
		fileDialog.setOpenFileDialog(false);
		fileDialog.setFilters(new String[] {
			FILTER_pdf,
			FILTER_presenterDocumentFile,
			FILTER_presenterPageFile,
			FILTER_session
		});
		
		fileDialog.setOnDismissListener(new FileDialog.OnDismissListener() {
			@Override
			public void onDismiss(final DialogInterface dialog) {
				if (fileDialog.getSelectedFile() != null) {
					try {
						final File f = new File(fileDialog.getSelectedFile());
						if (f.getPath().toLowerCase().endsWith(".jpd")) {
							editor.getDocumentEditor().setDocument(openDocument(f));
							if (onSuccessListener != null) {
								onSuccessListener.onSuccess();
							}
						} else if (f.getPath().toLowerCase().endsWith(".jpp")) {
							DocumentPage currentPage = editor.getDocumentEditor().getCurrentPage();
							openPage(f, currentPage);
							if (onSuccessListener != null) {
								onSuccessListener.onSuccess();
							}
						} else if (f.getPath().toLowerCase().endsWith(".pdf")) {
							/*final PdfModeOption defOpt = new PdfModeOption(IEditableDocument.PDF_MODE_CLEAR, "Clear all");
							final Object dialogResult = JOptionPane.showInputDialog(component, "Select PDF Mode", "PDF Loading...", JOptionPane.QUESTION_MESSAGE, null, new Object[] {
									defOpt,
								new PdfModeOption(IEditableDocument.PDF_MODE_REINDEX, "Reindex Pages"),
								new PdfModeOption(IEditableDocument.PDF_MODE_KEEP_INDEX, "Keep Page Indices"),
								new PdfModeOption(IEditableDocument.PDF_MODE_APPEND, "Append Pdf Pages"),
								defOpt
							}, defOpt);
							AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle(R.string.msg_title_info)
							.setMessage(R.string.msg_new_save)
							.setNegativeButton(R.string.msg_button_cancel, null)
							.setPositiveButton(R.string.msg_button_yes, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (!FileHelper.showSaveDialog(context, _editor, FileHelper.stringToFilter(_editor.getConfig().getString("save.defaultExt", "jpd")))) {
										return;
									}
									_editor.getDocumentEditor().setDocument(new ServerDocument(1));
								}
							})
							.setPositiveButton(R.string.msg_button_no, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									_editor.getDocumentEditor().setDocument(new ServerDocument(1));
								}
							})
							.create();
							alertDialog.show();
							*/
							// TODO: Temporary
							final PdfModeOption dialogResult = new PdfModeOption(IEditableDocument.PDF_MODE_CLEAR, "");
							if (dialogResult != null) {
								final PdfModeOption res = (PdfModeOption)dialogResult;
								final PdfSerializable pdf = new PdfSerializable(editor.getDocumentEditor().getDocument(), f);
								editor.getDocumentEditor().getDocument().setPdfPages(pdf, res.getKey());
								if (onSuccessListener != null) {
									onSuccessListener.onSuccess();
								}
							}
						} else if (f.getPath().toLowerCase().endsWith("session.dat")) {
							openSession(f, editor.getDocumentEditor());
							if (onSuccessListener != null) {
								onSuccessListener.onSuccess();
							}
						}
					} catch (final FileNotFoundException e) {
						showErrorDialog(context, R.string.msg_save_cant_open);
					} catch (final IOException e) {
						showErrorDialog(context, R.string.msg_save_cant_read);
					}
				}
			}
		});
		
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
	
	public static interface OnSuccessListener {
		void onSuccess();
	}
}
