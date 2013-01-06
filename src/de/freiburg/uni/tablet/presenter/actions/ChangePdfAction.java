/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.PdfSerializable;

/**
 * @author lukas
 * 
 */
public class ChangePdfAction extends AbstractAction implements IAction, IBinarySerializable {
	private final PdfSerializable _oldPdf;
	private final PdfSerializable _newPdf;

	/**
	 * 
	 */
	public ChangePdfAction(int clientId, final PdfSerializable oldPdf,
			final PdfSerializable newPdf) {
		super(clientId);
		_oldPdf = oldPdf;
		_newPdf = newPdf;
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ChangePdfAction(final BinaryDeserializer reader)
			throws IOException {
		super(reader);
		_oldPdf = reader.readObjectTable();
		_newPdf = reader.readObjectTable();
	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction(int clientId) {
		return new ChangePdfAction(clientId, _newPdf, _oldPdf);
	}

	@Override
	public void perform(final DocumentEditor editor) {
		// Todo: Store each pages pdf index.
		//editor.getDocument().setPdf(_oldPdf);
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		super.serialize(writer);
		writer.writeObjectTable(_oldPdf.getId(), _oldPdf);
		writer.writeObjectTable(_newPdf.getId(), _newPdf);
	}

}
