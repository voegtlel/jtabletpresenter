/**
 * Copyright Lukas Voegtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.net;

/**
 * @author lukas
 * 
 */
public interface ClientListener {

	/**
	 * 
	 */
	void disconnected();

	void connected();

	void error(Throwable t);

}
