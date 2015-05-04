package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.tools.ToolImage;

/**
 * Transfer handler for the IToolPageEditor
 * @author Lukas
 *
 */
public class EditorTransferHandler extends TransferHandler implements DropTargetListener {
	private static final long serialVersionUID = 1L;
	private IToolPageEditor _editor;
	
	public EditorTransferHandler(final IToolPageEditor editor) {
		_editor = editor;
	}
	
	private void acceptImage(final Transferable t) throws UnsupportedFlavorException, IOException {
		Image imageData = (Image)t.getTransferData(DataFlavor.imageFlavor);
		if (imageData instanceof BufferedImage) {
			_editor.getDocumentEditor().setCurrentImage((BufferedImage)imageData);
		} else {
			System.out.println("Set image to copied image");
			BufferedImage bi = new BufferedImage(imageData.getWidth(null), imageData.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = bi.createGraphics();
			graphics.drawImage(imageData, 0, 0, null);
			graphics.dispose();
			_editor.getDocumentEditor().setCurrentImage(bi);
		}
	}
	
	private void acceptFile(final Transferable t) throws UnsupportedFlavorException, IOException {
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
		if (files.size() == 1) {
			System.out.println("Set image to " + files.get(0));
			_editor.getDocumentEditor().setCurrentImageFile(files.get(0));
		} else {
			throw new UnsupportedFlavorException(DataFlavor.javaFileListFlavor);
		}
	}
	
	private boolean hasFileFlavor(final DataFlavor[] supported) {
		for (DataFlavor f1 : supported) {
			if (DataFlavor.javaFileListFlavor.equals(f1)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasImageFlavor(final DataFlavor[] supported) {
		for (DataFlavor f1 : supported) {
			if (DataFlavor.imageFlavor.equals(f1)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isDragSupported(final DataFlavor[] supported) {
		if (hasImageFlavor(supported)) {
			return true;
		} else if (hasFileFlavor(supported)) {
			return true;
		}
		return false;
	}
	
	@Override
	public int getSourceActions(final JComponent c) {
		return TransferHandler.NONE;
	}
	
	@Override
	public boolean canImport(final TransferSupport support) {
		return isDragSupported(support.getDataFlavors());
	}
	
	@Override
	public boolean importData(final TransferSupport support) {
		try {
			if (hasImageFlavor(support.getDataFlavors())) {
				acceptImage(support.getTransferable());
				_editor.getPageEditor().setNormalToolOnce(new ToolImage(_editor));
				return true;
			} else if (hasFileFlavor(support.getDataFlavors())) {
				acceptFile(support.getTransferable());
				_editor.getPageEditor().setNormalToolOnce(new ToolImage(_editor));
				return true;
			}
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
		if (isDragSupported(dtde.getCurrentDataFlavors())) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			dtde.rejectDrag();
		}
	}
	
	@Override
	public void dragExit(final DropTargetEvent dte) {
	}
	
	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
		if (isDragSupported(dtde.getCurrentDataFlavors())) {
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		} else {
			dtde.rejectDrag();
		}
	}
	
	@Override
	public void drop(final DropTargetDropEvent dtde) {
		try {
			if (hasImageFlavor(dtde.getCurrentDataFlavors())) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				acceptImage(dtde.getTransferable());
				dtde.dropComplete(true);
				_editor.getPageEditor().setNormalToolOnce(new ToolImage(_editor));
				return;
			} else if (hasFileFlavor(dtde.getCurrentDataFlavors())) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				acceptFile(dtde.getTransferable());
				dtde.dropComplete(true);
				_editor.getPageEditor().setNormalToolOnce(new ToolImage(_editor));
				return;
			}
			dtde.rejectDrop();
		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
			dtde.dropComplete(false);
		}
	}
	
	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde) {
	}
}
