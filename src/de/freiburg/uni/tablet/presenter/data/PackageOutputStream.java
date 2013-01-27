package de.freiburg.uni.tablet.presenter.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PackageOutputStream extends OutputStream {
	private final ByteArrayOutputStream _outputStream;
	
	private final PackageWriter _packageWriter;

	/**
	 * Create a package input stream with a package source
	 * @param packageSource
	 */
	public PackageOutputStream(final PackageWriter packageWriter) {
		_packageWriter = packageWriter;
		_outputStream = new ByteArrayOutputStream();
	}
	
	@Override
	public void write(final int b) throws IOException {
		_outputStream.write(b);
	}
	
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		_outputStream.write(b, off, len);
	}
	
	@Override
	public void flush() throws IOException {
		_outputStream.flush();
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		_outputStream.write(b);
	}
	
	/*public void debugPrintData() {
		final StringBuilder data = new StringBuilder();
		byte[] byteArray = _outputStream.toByteArray();
		for (int i = 0; i < byteArray.length; i++) {
			data.append(String.format("%02x ", (int)byteArray[i] & 0xff));
		}
		System.out.println(data.toString());
	}*/
	
	/**
	 * Sends the next package to the stream
	 */
	public void nextPackage() {
		final byte[] data = _outputStream.toByteArray();
		_packageWriter.writePackage(data, data.length);
		_outputStream.reset();
	}
	
	/**
	 * Sends the next package to the stream without the size header and returns the sent size
	 * @return sent data size
	 */
	public int nextPackageRaw() {
		final byte[] data = _outputStream.toByteArray();
		_packageWriter.writePackageRaw(data, data.length);
		_outputStream.reset();
		return data.length;
	}

	/**
	 * Interface for input source
	 * @author lukas
	 *
	 */
	public interface PackageWriter {
		/**
		 * writes the next package
		 */
		void writePackage(byte[] data, int size);
		
		/**
		 * writes the next package
		 */
		void writePackageRaw(byte[] data, int size);
	}
}
