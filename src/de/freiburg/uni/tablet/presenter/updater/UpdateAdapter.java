package de.freiburg.uni.tablet.presenter.updater;

import java.io.File;

public class UpdateAdapter implements IUpdateListener {
	@Override
	public void onVersionInfo(final String fromVersion, final String toVersion) {}
	
	@Override
	public void onStartUpdater() {}

	@Override
	public void onStartDownloadArchive(final String downloadUrl) {}

	@Override
	public void onStartWriteArchive(final File targetFile) {}

	@Override
	public void onStartWriteUpdater(final File updaterFile) {}
}
