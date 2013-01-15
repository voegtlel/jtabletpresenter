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
	public void write(int b) throws IOException {
		_outputStream.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
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
	
	/**
	 * Sends the next package to the stream
	 */
	public void nextPackage() {
		byte[] data = _outputStream.toByteArray();
		_packageWriter.writePackage(data, data.length);
		_outputStream.reset();
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
	}
}
