/**
 * Copyright Lukas Vögtle
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
public class PrevAction implements IAction, IBinarySerializable {
	/**
	 * 
	 */
	public PrevAction() {

	}

	@Override
	public boolean hasUndoAction() {
		return false;
	}

	@Override
	public IAction getUndoAction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void perform() {
		// TODO Auto-generated method stub

	}

	@Override
	public void serialize(final BinarySerializer writer) throws IOException {
	}

}
