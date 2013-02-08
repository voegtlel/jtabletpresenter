package de.freiburg.uni.tablet.presenter.document.document;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;

public class DocumentClient extends Document implements IClientDocument {
	/**
	 * Create a new client document.
	 */
	public DocumentClient(final int docId) {
		super(docId);
	}
	
	/**
	 * Clone ctor
	 * @param docId
	 * @param base
	 */
	protected DocumentClient(final int docId, final DocumentClient base) {
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
	
	public DocumentClient(final BinaryDeserializer reader) throws IOException {
		super(reader);
	}
	
	@Override
	public IClientDocument clone(final int docId) {
		return new DocumentClient(docId, this);
	}
}
