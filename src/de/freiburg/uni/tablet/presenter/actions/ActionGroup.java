/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

/**
 * @author lukas
 * 
 */
public class ActionGroup implements IAction {
	private final LinkedElementList<IAction> _actions = new LinkedElementList<IAction>();

	/**
	 * 
	 * @param clientId
	 */
	public ActionGroup() {
	}

	/**
	 * @throws IOException
	 * 
	 */
	public ActionGroup(final BinaryDeserializer reader) throws IOException {
		int count = reader.readInt();
		for (int i = 0; i < count; i++) {
			_actions.addLast((IAction) reader.readSerializableClass());
		}
	}
	
	public void addAction(IAction action) {
		_actions.addLast(action);
	}

	@Override
	public boolean hasUndoAction() {
		for (LinkedElement<IAction> e = _actions.getFirst(); e != null; e = e
				.getNext()) {
			if (!e.getData().hasUndoAction()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IAction getUndoAction() {
		ActionGroup undoAction = new ActionGroup();
		for (LinkedElement<IAction> e = _actions.getFirst(); e != null; e = e
				.getNext()) {
			undoAction.addAction(e.getData().getUndoAction());
		}
		return undoAction;
	}

	@Override
	public void perform(final DocumentEditor editor) {
		for (LinkedElement<IAction> e = _actions.getFirst(); e != null; e = e
				.getNext()) {
			e.getData().perform(editor);
		}
	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
		int count = (_actions.isEmpty()?0:_actions.getFirst().getNextCount());
		writer.writeInt(count);
		for (LinkedElement<IAction> e = _actions.getFirst(); e != null; e = e
				.getNext()) {
			writer.writeSerializableClass(e.getData());
		}
	}
}
