package de.freiburg.uni.tablet.presenter.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PackageInputStream extends InputStream {
	private byte[] _buffer;
	private ByteArrayInputStream _inputStream;
	
	private final PackageReader _packageReader;

	/**
	 * Create a package input stream with a package source
	 * @param packageSource
	 */
	public PackageInputStream(final PackageReader packageSource) {
		_packageReader = packageSource;
		_buffer = new byte[1024];
		_inputStream = new ByteArrayInputStream(_buffer, 0, 0);
	}
	
	@Override
	public int read() throws IOException {
		return _inputStream.read();
	}
	
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return _inputStream.read(b, off, len);
	}
	
	@Override
	public int available() throws IOException {
		return _inputStream.available();
	}
	
	@Override
	public long skip(final long n) throws IOException {
		return _inputStream.skip(n);
	}
	
	@Override
	public int read(final byte[] b) throws IOException {
		return _inputStream.read(b);
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	/**
	 * Gets the next package from the source
	 */
	public boolean nextPackage() {
		final int packageSize = _packageReader.readPackageSize();
		return nextPackageRaw(packageSize);
	}
	
	/**
	 * Gets the next package from the source
	 */
	public boolean nextPackageRaw(final int packageSize) {
		if (_buffer.length < packageSize) {
			int n = packageSize - 1;
			n = n | (n >> 1);
			n = n | (n >> 2);
			n = n | (n >> 4);
			n = n | (n >> 8);
			n = n | (n >> 16);
			n = n + 1;
			_buffer = new byte[n];
		}
		if(!_packageReader.readPackage(_buffer, packageSize)) {
			return false;
		}
		_inputStream = new ByteArrayInputStream(_buffer, 0, packageSize);
		return true;
	}

	/**
	 * Interface for input source
	 * @author lukas
	 *
	 */
	public interface PackageReader {
		/**
		 * Gets the size of the next package
		 * @return size of next package
		 */
		int readPackageSize();
		
		/**
		 * Reads the next package
		 * 
		 * @param data
		 * @param count
		 * @return true on success
		 */
		boolean readPackage(byte[] data, int count);
	}
}
