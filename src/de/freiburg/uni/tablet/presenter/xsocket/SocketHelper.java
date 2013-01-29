package de.freiburg.uni.tablet.presenter.xsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe.SourceChannel;

import org.xsocket.connection.IBlockingConnection;
import org.xsocket.connection.INonBlockingConnection;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public class SocketHelper {
	private final static int BUFFER_SIZE = 1024;
	
	public static BinarySerializer getWriter(final INonBlockingConnection connection) throws IOException {
		return new BinarySerializer(new OutputStream() {
			@Override
			public void flush() throws IOException {
				System.out.println("write flush");
				connection.flush();
			}
			
			@Override
			public void write(final int b) throws IOException {
				try {
					System.out.println("write byte " + b);
					connection.write((byte)b);
					System.out.println("write byte done");
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
			
			@Override
			public void write(final byte[] b) throws IOException {
				this.write(b, 0, b.length);
			}
			
			@Override
			public void write(final byte[] b, final int off, final int len)
					throws IOException {
				try {
					System.out.println("write " + len + " bytes");
					for (int i = off; i < off+len; i++) {
						System.out.print(" " + b[i]);
					}
					System.out.println();
					connection.write(b, off, len);
					System.out.println("write " + len + " bytes done");
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
		});
	}
	
	public static BinarySerializer getWriter(final IBlockingConnection connection) throws IOException {
		return new BinarySerializer(new OutputStream() {
			@Override
			public void flush() throws IOException {
				System.out.println("write flush");
				connection.flush();
			}
			
			@Override
			public void write(final int b) throws IOException {
				try {
					System.out.println("write byte " + b);
					connection.write((byte)b);
					System.out.println("write byte done");
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
			
			@Override
			public void write(final byte[] b) throws IOException {
				this.write(b, 0, b.length);
			}
			
			@Override
			public void write(final byte[] b, final int off, final int len)
					throws IOException {
				try {
					System.out.println("write " + len + " bytes");
					for (int i = off; i < off+len; i++) {
						System.out.print(" " + b[i]);
					}
					System.out.println();
					connection.write(b, off, len);
					System.out.println("write " + len + " bytes done");
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
		});
	}
	
	public static BinaryDeserializer getReader(final InputStream source) throws IOException {
		return new BinaryDeserializer(new InputStream() {
			
			@Override
			public int read(final byte[] b, final int off, final int len) throws IOException {
				try {
					System.out.println("read " + len + " bytes");
					int restLen = len;
					int restOff = off;
					while (restLen > 0) {
						final int read = source.read(b, restOff, restLen);
						restOff += read;
						restLen -= read;
						System.out.println("read " + (len - restLen) + "/" + len + " bytes");
					}
					System.out.println("read " + len + " bytes done");
					return len;
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
			
			@Override
			public int read(final byte[] b) throws IOException {
				return this.read(b, 0, b.length);
			}
			
			@Override
			public int read() throws IOException {
				System.out.println("read byte");
				try {
					int byteRes = source.read();
					System.out.println("read byte " + byteRes);
					return byteRes;
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
		});
	}
	
	public static BinaryDeserializer getReader(final IBlockingConnection connection) throws IOException {
		return new BinaryDeserializer(new InputStream() {
			private ByteBuffer _buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
			
			@Override
			public int read(final byte[] b, final int off, final int len) throws IOException {
				try {
					System.out.println("read " + len + " bytes");
					int restLen = len;
					int restOff = off;
					while (restLen > 0) {
						_buffer.clear();
						_buffer.limit(Math.min(restLen, _buffer.capacity()));
						final int read = connection.read(_buffer);
						_buffer.flip();
						_buffer.get(b, restOff, read);
						restOff += read;
						restLen -= read;
						System.out.println("read " + (len - restLen) + "/" + len + " bytes");
					}
					System.out.println("read " + len + " bytes done");
					return len;
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
			
			@Override
			public int read(final byte[] b) throws IOException {
				return this.read(b, 0, b.length);
			}
			
			@Override
			public int read() throws IOException {
				System.out.println("read byte");
				try {
					int byteRes = connection.readByte();
					System.out.println("read byte " + byteRes);
					return byteRes;
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
		});
	}
	
	public static BinaryDeserializer getReader(final SourceChannel source) throws IOException {
		return new BinaryDeserializer(new InputStream() {
			private ByteBuffer _buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
			private byte[] _singleByteBuffer = new byte[1];
			
			@Override
			public int read(final byte[] b, final int off, final int len) throws IOException {
				try {
					System.out.println(">read " + len + " bytes");
					int restLen = len;
					int restOff = off;
					while (restLen > 0) {
						_buffer.clear();
						_buffer.limit(Math.min(restLen, _buffer.capacity()));
						final int read;
						synchronized (source.blockingLock()) {
							read = source.read(_buffer);
						}
						_buffer.flip();
						_buffer.get(b, restOff, read);
						restOff += read;
						restLen -= read;
						System.out.println("read " + (len - restLen) + "/" + len + " bytes");
					}
					for (int i = off; i < off+len; i++) {
						System.out.print(" " + b[i]);
					}
					System.out.println();
					System.out.println("<read " + len + " bytes done");
					return len;
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}
			
			@Override
			public int read(final byte[] b) throws IOException {
				return this.read(b, 0, b.length);
			}
			
			@Override
			public int read() throws IOException {
				if (this.read(_singleByteBuffer) != 1) {
					System.out.println("read EOF");
					return -1;
				}
				return (int)_singleByteBuffer[0] & 0xff;
			}
		});
	}
}
