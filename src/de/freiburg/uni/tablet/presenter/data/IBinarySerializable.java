package de.freiburg.uni.tablet.presenter.data;

import java.io.IOException;

/**
 * Interface for serializing
 * 
 * @author lukas
 * 
 */
public interface IBinarySerializable {
	/**
	 * Serialized the object
	 * 
	 * @throws IOException
	 */
	void serialize(BinarySerializer writer) throws IOException;
}
