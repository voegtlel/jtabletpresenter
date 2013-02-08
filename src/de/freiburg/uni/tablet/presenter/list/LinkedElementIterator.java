package de.freiburg.uni.tablet.presenter.list;

import java.util.Iterator;

public class LinkedElementIterator<E> implements Iterator<E> {
	private LinkedElement<E> _nextElement;

	public LinkedElementIterator(final LinkedElement<E> element) {
		_nextElement = element;
	}
	
	@Override
	public boolean hasNext() {
		return _nextElement != null;
	}

	@Override
	public E next() {
		final LinkedElement<E> element = _nextElement;
		_nextElement = element.getNext();
		return element.getData();
	}

	@Override
	public void remove() {
		throw new IllegalStateException("Can't remove");
	}

}
