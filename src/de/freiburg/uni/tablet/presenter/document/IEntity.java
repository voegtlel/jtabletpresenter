/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.data.IBinarySerializable;

/**
 * @author lukas
 * 
 */
public interface IEntity extends IBinarySerializable {
	/**
	 * Gets the unique id for the entity
	 * 
	 * @return
	 */
	long getId();

	/**
	 * Gets the parent entity
	 * 
	 * @return
	 */
	IEntity getParent();
}
