package de.freiburg.uni.tablet.presenter.list;

public class LinkedElement<T> {
	private LinkedElement<T> _previous;
	private LinkedElement<T> _next;
	private T _data;

	public LinkedElement(final LinkedElement<T> previous,
			final LinkedElement<T> next, final T data) {
		_previous = previous;
		_next = next;
		_data = data;
	}

	public LinkedElement<T> getPrevious() {
		return _previous;
	}

	public void setPrevious(final LinkedElement<T> previous) {
		_previous = previous;
	}

	public LinkedElement<T> getNext() {
		return _next;
	}

	public void setNext(final LinkedElement<T> next) {
		_next = next;
	}

	public T getData() {
		return _data;
	}

	public void setData(final T data) {
		_data = data;
	}

	public int getNextCount() {
		int i = 0;
		for (LinkedElement<T> j = this; j != null; j = j.getNext()) {
			i++;
		}
		return i;
	}
}
