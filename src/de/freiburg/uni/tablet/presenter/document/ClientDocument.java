package de.freiburg.uni.tablet.presenter.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;

public class ClientDocument extends Document implements IClientDocument {
	/**
	 * Create a new client document.
	 */
	public ClientDocument(final int docId) {
		super(docId);
	}
	
	/**
	 * Clone ctor
	 * @param docId
	 * @param base
	 */
	protected ClientDocument(final int docId, final ClientDocument base) {
		super(docId, base);
	}

	@Override
	public DocumentPage insertPage(final DocumentPage afterPage,
			final DocumentPage page) {
		if (page.getParent() != this) {
			throw new IllegalStateException("Page has invalid parent");
		}
		if (afterPage != null && afterPage.getParent() != this) {
			throw new IllegalStateException("Page has invalid parent");
		}
		if (afterPage != null) {
			final LinkedElement<DocumentPage> element = _pages
					.getElementByInstance(afterPage);
			if (element == null) {
				throw new IllegalArgumentException("afterPage not in document");
			}
			_pages.insertAfter(element, page);
			firePageInserted(this, element.getData(), page);
		} else {
			_pages.addFirst(page);
			firePageInserted(this, null, page);
		}
		return page;
	}

	@Override
	public void removePage(final DocumentPage page) {
		if (page.getParent() != this) {
			throw new IllegalStateException("Page has invalid parent");
		}
		final LinkedElement<DocumentPage> element = _pages
				.getElementByInstance(page);
		if (element == null) {
			throw new IllegalArgumentException("afterPage not in document");
		}
		final LinkedElement<DocumentPage> prevPage = element.getPrevious();
		_pages.remove(element);
		firePageRemoved(this, (prevPage == null?null:prevPage.getData()), element.getData());
	}
	
	public ClientDocument(final BinaryDeserializer reader) throws IOException {
		super(reader);
	}
	
	@Override
	public IClientDocument clone(final int docId) {
		return new ClientDocument(docId, this);
	}
}
