package de.freiburg.uni.tablet.presenter.data;


/**
 * Interface for serializing
 * 
 * @author lukas
 * 
 */
public interface IBinarySerializableId extends IBinarySerializable {
	/**
	 * Gets the unique id for the entity
	 * 
	 * @return
	 */
	long getId();
}
