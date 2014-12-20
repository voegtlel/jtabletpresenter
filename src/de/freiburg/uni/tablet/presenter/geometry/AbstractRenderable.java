package de.freiburg.uni.tablet.presenter.geometry;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.document.AbstractPageEntity;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;

public abstract class AbstractRenderable extends AbstractPageEntity implements IRenderable {
	/**
	 * Creates the renderable
	 */
	protected AbstractRenderable(final DocumentPage parent) {
		super(parent);
	}


	/**
	 * Creates the renderable from an input stream
	 * 
	 * @throws IOException
	 */
	protected AbstractRenderable(final BinaryDeserializer reader)
			throws IOException {
		super(reader);
	}

	@Override
	public String toString() {
		return String.format("Renderable %s (%X)", this.getClass().getSimpleName(), System.identityHashCode(this));
	}
}