package de.freiburg.uni.tablet.presenter.list;

public class LinkedElementList<T> {
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
			_first = _last = new LinkedElement<T>(data);
		} else {
			final LinkedElement<T> newElement = new LinkedElement<T>(_last,
					_last.getNext(), data);
			if (_last.getNext() != null) {
				_last.getNext().setPrevious(newElement);
			}
			_last.setNext(newElement);
			_last = newElement;
		}
	}

	public void addFirst(final T data) {
		if (_first == null) {
			_first = _last = new LinkedElement<T>(data);
		} else {
			final LinkedElement<T> newElement = new LinkedElement<T>(
					_first.getPrevious(), _first, data);
			if (_first.getPrevious() != null) {
				_first.getPrevious().setNext(newElement);
			}
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
		if ((first == _first) && (_first == _last)) {
			newList._first = _first = null;
			newList._last = _last = null;
		} else if (first == _last) {
			newList._first = null;
			newList._last = null;
			_last = first.getPrevious();
			_last.setNext(null);
			first.setPrevious(null);
		} else if (first == _first) {
			newList._first = first.getNext();
			newList._last = _last;
			newList._first.setPrevious(null);
			_first = _last = null;
		} else {
			newList._first = first.getNext();
			newList._last = _last;
			_last = first.getPrevious();
			_last.setNext(null);
			newList._last.setPrevious(null);
		}
		return newList;
	}

	public boolean isEmpty() {
		return _first == null;
	}

	public boolean hasOne() {
		return _first == _last;
	}
}
