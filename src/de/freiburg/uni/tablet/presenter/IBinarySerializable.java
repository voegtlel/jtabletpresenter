package de.freiburg.uni.tablet.presenter;

import java.io.DataOutputStream;
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
	void serialize(DataOutputStream writer) throws IOException;
}
