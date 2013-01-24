/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.document;

import de.freiburg.uni.tablet.presenter.data.IBinarySerializableId;

/**
 * @author lukas
 * 
 */
public interface IEntity extends IBinarySerializableId {
	/**
	 * Gets the parent entity
	 * 
	 * @return
	 */
	IEntity getParent();
}
