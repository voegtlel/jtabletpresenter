package de.freiburg.uni.tablet.presenter.updater;

public interface IGetListener {
	void onProgress(int bytesLoaded);

	void onBegin(String urlStr, int expectedSize);

	void onDone();
}
