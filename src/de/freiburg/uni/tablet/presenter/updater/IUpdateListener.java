package de.freiburg.uni.tablet.presenter.updater;

import java.io.File;

public interface IUpdateListener {
	void onVersionInfo(String fromVersion, String toVersion);
	
	void onStartUpdater();

	void onStartDownloadArchive(String downloadUrl);

	void onStartWriteArchive(File targetFile);

	void onStartWriteUpdater(File updaterFile);
}
