package de.freiburg.uni.tablet.presenter.updater;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.freiburg.uni.tablet.presenter.ClientApp;
import de.freiburg.uni.tablet.presenter.Updater;

public class ClientUpdater {
	/**
	 * Internally gets an url.
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static ByteBuffer readUrl(final String urlStr, final int expectedSize, final IGetListener callback) throws IOException {
		if (callback != null) {
			callback.onBegin(urlStr, expectedSize);
		}
		URL url = new URL(urlStr);
		InputStream stream = url.openStream();
		ByteBuffer buf = ByteBuffer.allocate(expectedSize > 0?expectedSize:1024);
		try {
			while (true) {
				if (buf.remaining() < buf.capacity() / 2) {
					ByteBuffer orig = buf;
					orig.flip();
					buf = ByteBuffer.allocate(buf.capacity() * 2);
					buf.put(orig);
				}
				int read = stream.read(buf.array(), buf.position(), Math.min(buf.remaining(), Math.max(stream.available(), 1024*1024)));
				if (read < 0) {
					break;
				}
				buf.position(buf.position() + read);
				if (callback != null) {
					callback.onProgress(buf.position());
				}
			}
		} finally {
			stream.close();
		}
		buf.flip();
		if (callback != null) {
			callback.onDone();
		}
		return buf;
	}
	
	/**
	 * Ready a buffer into a string
	 * @param buf
	 * @return
	 */
	public static String bufferToString(final ByteBuffer buf) {
		return new String(buf.array(), buf.position(), buf.remaining(), Charset.forName("UTF-8"));
	}
	
	/**
	 * Checks if an update is available.
	 * @return true if an update is available
	 * @throws IOException
	 */
	public static boolean checkForUpdate(final IUpdateListener listener) throws IOException {
		JsonObject releasesLatest = Json.parse(bufferToString(readUrl("https://api.github.com/repos/voegtlel/jtabletpresenter/releases/latest", -1, null))).asObject();
		String fromVersion = "v" + ClientApp.VersionString;
		String toVersion = releasesLatest.getString("tag_name", null);
		listener.onVersionInfo(fromVersion, toVersion);
		return !fromVersion.equals(toVersion);
	}
	
	/**
	 * Installs the update
	 * @param listener
	 * @param downloadListener
	 * @throws IOException
	 */
	public static void beginInstallUpdate(final IUpdateListener listener, final IGetListener downloadListener, final String mainClassName, final String[] args) throws IOException {
		JsonObject releasesLatest = Json.parse(bufferToString(readUrl("https://api.github.com/repos/voegtlel/jtabletpresenter/releases/latest", -1, downloadListener))).asObject();
		String fromVersion = "v" + ClientApp.VersionString;
		String toVersion = releasesLatest.getString("tag_name", null);
		listener.onVersionInfo(fromVersion, toVersion);
		
		String archiveName = "JTB_" + releasesLatest.getString("tag_name", null) + "_JTabletPresenter.zip";
		
		String downloadUrl = null;
		int downloadSize = -1;
		
		JsonArray assets = releasesLatest.get("assets").asArray();
		for (JsonValue asset : assets) {
			String assetName = asset.asObject().getString("name", null);
			if (archiveName.equals(assetName)) {
				downloadUrl = asset.asObject().getString("browser_download_url", null);
				downloadSize = asset.asObject().getInt("size", -1);
			}
		}
		
		if (downloadUrl == null) {
			throw new IOException("Missing asset " + archiveName);
		}
		
		listener.onStartDownloadArchive(downloadUrl);
		
		ByteBuffer zipContent = readUrl(downloadUrl, downloadSize, downloadListener);
		
		File targetFile = File.createTempFile("JTabletPresenterUpdater", "package.zip");
		
		listener.onStartWriteArchive(targetFile);
		
		FileOutputStream packageStream = new FileOutputStream(targetFile);
		try {
			packageStream.write(zipContent.array(), zipContent.position(), zipContent.remaining());
		} finally {
			packageStream.close();
		}
		
		File updaterFile = File.createTempFile("JTabletPresenter", "Updater.jar");
		
		listener.onStartWriteUpdater(updaterFile);
		
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent.array()));
		ZipEntry entry = zis.getNextEntry();
		while (entry != null) {
			if (entry.getName().endsWith("/Updater.jar")) {
				System.out.println("Extracting " + entry.getName() + " (" + entry.getSize() + "B)");
				FileOutputStream bos = new FileOutputStream(updaterFile);
				try {
					byte[] buffer = new byte[1024];
					while (true) {
						int read = zis.read(buffer);
						if (read < 0) {
							break;
						}
						bos.write(buffer, 0, read);
					}
				} finally {
					bos.close();
				}
			}
			entry = zis.getNextEntry();
		}
		zis.close();
		
		listener.onStartUpdater();
		
		String[] updArgs = new String[9];
		updArgs[0] = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		updArgs[1] = "-cp";
		updArgs[2] = updaterFile.getAbsolutePath();
		updArgs[3] = Updater.class.getName();
		updArgs[4] = targetFile.getAbsolutePath();
		updArgs[5] = new File(".").getAbsolutePath();
		updArgs[6] = mainClassName;
		updArgs[7] = System.getProperty("java.class.path");
		updArgs[8] = updaterFile.getAbsolutePath();
		ProcessBuilder pb = new ProcessBuilder(updArgs);
		Process process = pb.start();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (process.isAlive()) {
			System.exit(0);
		} else {
			StringBuilder output = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				output.append(line);
				output.append("\n");
			}
			reader.close();
			reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = reader.readLine()) != null) {
				output.append(line);
				output.append("\n");
			}
			reader.close();
			throw new IOException("Can't start updater:" + output.toString());
		}
	}
}
