package de.freiburg.uni.tablet.presenter.list;

import java.util.Iterator;

public class LinkedElementList<T> implements Iterable<T> {
	private LinkedElement<T> _first;
	private LinkedElement<T> _last;

	public void insertAfter(final LinkedElement<T> element, final T data) {
		if (element == _last) {
			addLast(data);
		} else {
			final LinkedElement<T> newElement = new LinkedElement<T>(element,
					element.getNext(), data);
			if (element.getNext() != null) {
				element.getNext().setPrevious(newElement);
			}
			element.setNext(newElement);
		}
	}

	public void insertBefore(final LinkedElement<T> element, final T data) {
		if (element == _first) {
			addFirst(data);
		} else {
			final LinkedElement<T> newElement = new LinkedElement<T>(
					element.getPrevious(), element, data);
			if (element.getPrevious() != null) {
				element.getPrevious().setNext(newElement);
			}
			element.setPrevious(newElement);
		}
	}

	public void addLast(final T data) {
		if (_last == null) {
			_first = _last = new LinkedElement<T>(null, null, data);
		} else {
			final LinkedElement<T> newElement = new LinkedElement<T>(_last,
					null, data);
			_last.setNext(newElement);
			_last = newElement;
		}
	}

	public void addFirst(final T data) {
		if (_first == null) {
			_first = _last = new LinkedElement<T>(null, null, data);
		} else {
			final LinkedElement<T> newElement = new LinkedElement<T>(null,
					_first, data);
			_first.setPrevious(newElement);
			_first = newElement;
		}
	}

	public void remove(final LinkedElement<T> element) {
		if (element == _first) {
			removeFirst();
		} else if (element == _last) {
			removeLast();
		} else {
			element.getNext().setPrevious(element.getPrevious());
			element.getPrevious().setNext(element.getNext());
			element.setNext(null);
			element.setPrevious(null);
		}
	}

	public void removeLast() {
		if (_last == _first) {
			_last = _first = null;
		} else {
			_last = _last.getPrevious();
			_last.getNext().setPrevious(null);
			_last.setNext(null);
		}
	}

	public void removeFirst() {
		if (_last == _first) {
			_last = _first = null;
		} else {
			_first = _first.getNext();
			_first.getPrevious().setNext(null);
			_first.setPrevious(null);
		}
	}

	public LinkedElement<T> getFirst() {
		return _first;
	}

	public LinkedElement<T> getLast() {
		return _last;
	}
	
	public int getCount() {
		if (_first == null) {
			return 0;
		}
		return _first.getNextCount();
	}
	
	public void clear() {
		LinkedElement<T> next = null;
		for (LinkedElement<T> j = _first; j != null; j = next) {
			next = j.getNext();
			j.setPrevious(null);
			j.setNext(null);
		}
		_first = _last = null;
	}

	public LinkedElementList<T> splitAt(final LinkedElement<T> first) {
		final LinkedElementList<T> newList = new LinkedElementList<T>();
		if (first == _first) {
			newList._first = _first;
			newList._last = _last;
			_first = _last = null;
		} else {
			newList._first = first;
			newList._last = _last;
			_last = first.getPrevious();
			_last.setNext(null);
			newList._last.setPrevious(null);
		}
		return newList;
	}

	public LinkedElementList<T> splitAtRemove(final LinkedElement<T> first) {
		final LinkedElementList<T> newList = new LinkedElementList<T>();
		if (first == _first && _first == _last) {
			newList._first = _first = null;
			newList._last = _last = null;
		} else if (first == _last) {
			newList._first = null;
			newList._last = null;
			_last = _last.getPrevious();
			_last.getNext().setPrevious(null);
			_last.setNext(null);
		} else if (first == _first) {
			newList._first = _first.getNext();
			newList._first.getPrevious().setNext(null);
			newList._first.setPrevious(null);
			newList._last = _last;
			_first = _last = null;
		} else {
			newList._first = first.getNext();
			newList._last = _last;
			_last = first.getPrevious();
			newList._first.setPrevious(null);
			_last.setNext(null);
			first.setNext(null);
			first.setPrevious(null);
		}
		return newList;
	}

	/**
	 * Gets an element of the list by comparing the instance
	 * 
	 * @param object
	 *            object to find
	 * @return
	 */
	public LinkedElement<T> getElementByInstance(final T object) {
		for (LinkedElement<T> j = _first; j != null; j = j.getNext()) {
			if (j.getData() == object) {
				return j;
			}
		}
		return null;
	}

	/**
	 * Finds the object by .equals.
	 * 
	 * @param object
	 * @return
	 */
	public LinkedElement<T> getElement(final T object) {
		for (LinkedElement<T> j = _first; j != null; j = j.getNext()) {
			if (j.getData().equals(object)) {
				return j;
			}
		}
		return null;
	}

	/**
	 * Finds the object by its index or returns null if out of range.
	 * 
	 * @param object
	 * @return
	 */
	public LinkedElement<T> getElementByIndex(final int index) {
		int i = index;
		for (LinkedElement<T> j = _first; j != null; j = j.getNext()) {
			if (i == 0) {
				return j;
			}
			i--;
		}
		return null;
	}

	/**
	 * Returns the elements index or -1 if not in list
	 * 
	 * @param data
	 * @return
	 */
	public int getElementIndex(final T data) {
		int i = 0;
		for (LinkedElement<T> j = _first; j != null; j = j.getNext()) {
			if (j.getData() == data) {
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Deletes all following elements and sets the next element
	 * 
	 * @param element
	 * @param nextData
	 * @return
	 */
	public void setNext(final LinkedElement<T> element, final T nextData) {
		_last = new LinkedElement<T>(element, null, nextData);
		element.setNext(_last);
	}

	public boolean isEmpty() {
		return _first == null;
	}

	public boolean hasOne() {
		return _first == _last;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new LinkedElementIterator<T>(_first);
	}
}
