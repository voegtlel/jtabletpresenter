package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

public class ActionWrapper implements Action {
	private Object _source;
	private Action _wrapped;

	public ActionWrapper(final Object source, final Action wrapped) {
		_source = source;
		_wrapped = wrapped;
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		System.out.println("Action: " + e);
		_wrapped.actionPerformed(new ActionEvent(_source, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers()));
	}

	@Override
	public Object getValue(final String key) {
		return _wrapped.getValue(key);
	}

	@Override
	public void putValue(final String key, final Object value) {
		_wrapped.putValue(key, value);
	}

	@Override
	public void setEnabled(final boolean b) {
		_wrapped.setEnabled(b);
	}

	@Override
	public boolean isEnabled() {
		return _wrapped.isEnabled();
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		_wrapped.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
		_wrapped.removePropertyChangeListener(listener);
	}
}
