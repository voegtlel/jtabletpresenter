/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;

/**
 * @author lukas
 * 
 */
public class ChangePageIndexAction implements IAction, IBinarySerializable {
	/**
	 * 
	 */
	public ChangePageIndexAction() {

	}

	@Override
	public boolean hasUndoAction() {
		return true;
	}

	@Override
	public IAction getUndoAction() {
		return null;
	}

	@Override
	public void perform() {

	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
	}

}
