package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Cursor;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.PdfPageSerializable;
import de.freiburg.uni.tablet.presenter.document.document.IEditableDocument;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;
import de.intarsys.pdf.app.action.ActionException;
import de.intarsys.pdf.app.action.ActionHandlerRegistry;
import de.intarsys.pdf.app.action.ActionTools;
import de.intarsys.pdf.app.action.IActionHandler;
import de.intarsys.pdf.app.action.TriggerEvent;
import de.intarsys.pdf.cds.CDSRectangle;
import de.intarsys.pdf.cos.COSBasedObject;
import de.intarsys.pdf.cos.COSName;
import de.intarsys.pdf.cos.COSObject;
import de.intarsys.pdf.pd.PDAction;
import de.intarsys.pdf.pd.PDActionGoTo;
import de.intarsys.pdf.pd.PDActionLaunch;
import de.intarsys.pdf.pd.PDActionURI;
import de.intarsys.pdf.pd.PDAnnotation;
import de.intarsys.pdf.pd.PDDestination;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDExplicitDestination;
import de.intarsys.pdf.pd.PDPage;

public class ToolPdfCursor extends AbstractTool {
	private boolean _wasKeyDown = false;
	private DataPoint _lastDataPoint = null;
	private static ToolPdfCursor _activeEventProcessor = null;
	private PDDocument _document = null;
	private boolean _hovering = false;
	
	/**
	 * Documents
	 * @param container
	 *            used for cursor changing
	 */
	public ToolPdfCursor(final IToolPageEditor editor) {
		super(editor);
		updateCursor();
	}
	
	static {
		initPdf();
	}
	
	private static void initPdf() {
		ActionHandlerRegistry.get().registerActionHandler(new IActionHandler() {
			private COSName _actionType = COSName.constant("GoTo");
			
			@Override
			public void process(final TriggerEvent event, final COSObject obj)
					throws ActionException {
				if (_activeEventProcessor != null) {
					_activeEventProcessor.process(event, obj);
				}
				System.out.println("Process GoTo " + event);
			}
			
			@Override
			public COSName getActionType() {
				return _actionType;
			}
		});
		ActionHandlerRegistry.get().registerActionHandler(new IActionHandler() {
			private COSName _actionType = COSName.constant("GoToR");
			
			@Override
			public void process(final TriggerEvent event, final COSObject obj)
					throws ActionException {
				if (_activeEventProcessor != null) {
					_activeEventProcessor.process(event, obj);
				}
				System.out.println("Process GoToR " + event);
			}
			
			@Override
			public COSName getActionType() {
				return _actionType;
			}
		});
		ActionHandlerRegistry.get().registerActionHandler(new IActionHandler() {
			private COSName _actionType = COSName.constant("Launch");
			
			@Override
			public void process(final TriggerEvent event, final COSObject obj)
					throws ActionException {
				if (_activeEventProcessor != null) {
					_activeEventProcessor.process(event, obj);
				}
				System.out.println("Process Launch");
			}
			
			@Override
			public COSName getActionType() {
				return _actionType;
			}
		});
		ActionHandlerRegistry.get().registerActionHandler(new IActionHandler() {
			private COSName _actionType = COSName.constant("URI");
			
			@Override
			public void process(final TriggerEvent event, final COSObject obj)
					throws ActionException {
				if (_activeEventProcessor != null) {
					_activeEventProcessor.process(event, obj);
				}
				System.out.println("Process URI");
			}
			
			@Override
			public COSName getActionType() {
				return _actionType;
			}
		});
		ActionHandlerRegistry.get().registerActionHandler(new IActionHandler() {
			private COSName _actionType = COSName.constant("Link");
			
			@Override
			public void process(final TriggerEvent event, final COSObject obj)
					throws ActionException {
				if (_activeEventProcessor != null) {
					_activeEventProcessor.process(event, obj);
				}
				System.out.println("Process Link");
			}
			
			@Override
			public COSName getActionType() {
				return _actionType;
			}
		});
	}
	
	protected void process(final TriggerEvent event, final COSObject obj) {
		COSBasedObject subObj = PDAction.META.createFromCos(obj);
		if (subObj == null) {
			System.out.println("Unsupported action: " + obj + " (unrecognized type)");
			return;
		}
		if (subObj instanceof PDActionGoTo) {
			boolean success = false;
			PDActionGoTo pdActionGoTo = (PDActionGoTo) subObj;
			PDDestination destination = pdActionGoTo.getDestination();
			PDExplicitDestination resolvedDestination = destination.getResolvedDestination(_document);
			String message = pdActionGoTo.toString();
			if (resolvedDestination != null) {
				message = resolvedDestination.toString();
				PDPage page = resolvedDestination.getPage(_document);
				if (page != null) {
					IEditableDocument document = _editor.getDocumentEditor().getDocument();
					for (DocumentPage p : document.getPages()) {
						if (p.getPdfPage() != null && p.getPdfPage().getPage() != null) {
							PDPage testPage = p.getPdfPage().getPage();
							if (testPage.getDoc() == _document && testPage == page) {
								final DocumentPage newActivePage = p;
								success = true;
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										_editor.getDocumentEditor().setCurrentPage(newActivePage);
									}
								});
								break;
							}
						}
					}
				}
			}
			if (!success) {
				final String fMessage = message;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, "Link " + fMessage + " invalid");
					}
				});
			}
		} else if (subObj instanceof PDActionURI) {
			PDActionURI pdActionURI = (PDActionURI) subObj;
			final String uri = pdActionURI.getURI();
			try {
				Desktop.getDesktop().browse(new URI(uri));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
				final String errorMessage = e.toString();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(null, "Can't browse uri " + uri + ": " + errorMessage);
					}
				});
			}
		} else if (subObj instanceof PDActionLaunch) {
			PDActionLaunch pdActionLaunch = (PDActionLaunch) subObj;
			final File file = pdActionLaunch.getFile();
			if (file != null && file.exists()) {
				if (file.exists()) {
					try {
						Desktop.getDesktop().open(file);
					} catch (final IOException e) {
						e.printStackTrace();
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								JOptionPane.showMessageDialog(null, "Can't open file " + file + ": " + e.toString());
							}
						});
					}
				} else {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, "File " + file + " not found");
						}
					});
				}
			}
		} else {
			System.out.println("Unsupported action: " + subObj.getClass() + " -> " + subObj);
		}
	}

	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
	}

	@Override
	synchronized public void begin() {
		_wasKeyDown = true;
	}
	
	private PdfPageSerializable getPage() {
		DocumentPage currentPage = _editor.getDocumentEditor().getCurrentPage();
		if (currentPage != null && currentPage.getPdfPage() != null) {
			PdfPageSerializable pdfPage = currentPage.getPdfPage();
			if (pdfPage.getPage() != null) {
				return pdfPage;
			}
		}
		return null;
	}
	
	private void checkHover(final DataPoint data) {
		PdfPageSerializable pdfPage = getPage();
		if (pdfPage == null)
			return;
		boolean hoveringNew = false;
		PDPage page = pdfPage.getPage();
		List<PDAnnotation> annotations = page.getAnnotations();
		if (annotations != null) {
			for (PDAnnotation annot : annotations) {
				CDSRectangle mediaBox = page.getMediaBox();
				// origin is bottom left
				float x = data.getX() * mediaBox.getWidth() - mediaBox.getLowerLeftX();
				float y = mediaBox.getUpperRightY() - data.getY() * mediaBox.getHeight();
				hoveringNew = hoveringNew || annot.getRectangle().contains(x, y);
				if (hoveringNew) {
					break;
				}
			}
		}
		if (hoveringNew != _hovering) {
			_hovering = hoveringNew;
			invalidateCursor();
		}
	}
	
	@Override
	synchronized public void drawAlways(final DataPoint data) {
		checkHover(data);
	}
	
	@Override
	synchronized public void draw(final DataPoint data) {
		_lastDataPoint = data;
		if (!_wasKeyDown)
			return;
		_wasKeyDown = false;
		
		PdfPageSerializable pdfPage = getPage();
		if (pdfPage == null)
			return;
		PDPage page = pdfPage.getPage();
		PDDocument document = pdfPage.getParentPdf().getDocument();
		_document = document;
		_activeEventProcessor = this;
		List<PDAnnotation> annotations = page.getAnnotations();
		if (annotations != null) {
			for (PDAnnotation annot : annotations) {
				CDSRectangle mediaBox = page.getMediaBox();
				// origin is bottom left
				float x = data.getX() * mediaBox.getWidth() - mediaBox.getLowerLeftX();
				float y = mediaBox.getUpperRightY() - data.getY() * mediaBox.getHeight();
				if (annot.getRectangle().contains(x, y)) {
					ActionTools.annotationTriggerMouseDown(annot);
				}
			}
		}
		_document = null;
		_activeEventProcessor = null;
	}

	@Override
	synchronized public void end() {
		if (_lastDataPoint == null) {
			return;
		}
		
		PdfPageSerializable pdfPage = getPage();
		if (pdfPage == null)
			return;
		PDPage page = pdfPage.getPage();
		PDDocument document = pdfPage.getParentPdf().getDocument();
		_document = document;
		_activeEventProcessor = this;
		List<PDAnnotation> annotations = page.getAnnotations();
		if (annotations != null) {
			for (PDAnnotation annot : annotations) {
				final CDSRectangle mediaBox = page.getMediaBox();
				final float x = _lastDataPoint.getX() * mediaBox.getWidth() - mediaBox.getLowerLeftX();
				final float y = mediaBox.getUpperRightY() - _lastDataPoint.getY() * mediaBox.getHeight();
				if (annot.getRectangle().contains(x, y)) {
					ActionTools.annotationTriggerMouseUp(annot);
				}
			}
		}
		_document = null;
		_activeEventProcessor = null;
	}

	@Override
	protected Cursor generateCursor() {
		if (_hovering) {
			return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		}
		return Cursor.getDefaultCursor();
	}
}
