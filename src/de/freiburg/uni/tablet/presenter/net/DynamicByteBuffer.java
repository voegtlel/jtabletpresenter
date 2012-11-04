package de.freiburg.uni.tablet.presenter.net;

import java.nio.ByteBuffer;

import de.freiburg.uni.tablet.presenter.list.LinkedElement;
import de.freiburg.uni.tablet.presenter.list.LinkedElementList;

public class DynamicByteBuffer {
	private final LinkedElementList<ByteBuffer> _buffers = new LinkedElementList<ByteBuffer>();
	private final LinkedElementList<ByteBuffer> _freeBuffers = new LinkedElementList<ByteBuffer>();

	public DynamicByteBuffer() {
		_buffers.addFirst(ByteBuffer.allocate(1024));
	}

	public void push(final ByteBuffer src) {
		final LinkedElement<ByteBuffer> last = _buffers.getLast();
		final int space = last.getData().capacity() - last.getData().limit();
		if (space > 0) {
			final int lastLimit = src.limit();
			src.limit(src.position() + Math.min(space, src.remaining()));
			last.getData().put(src);
			src.limit(lastLimit);
		}
		if (!src.hasRemaining()) {
			return;
		}
		if (!_freeBuffers.isEmpty()) {

		}
	}
}
