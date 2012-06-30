/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

/**
 * @author lukas
 * 
 */
public class AbstractEntity implements IEntity {
	private int _id;

	protected void setId(final int id) {
		_id = id;
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return _id;
	}

}
