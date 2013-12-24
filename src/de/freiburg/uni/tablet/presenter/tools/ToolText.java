package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.document.TextFont;
import de.freiburg.uni.tablet.presenter.document.editor.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.geometry.CollisionInfo;
import de.freiburg.uni.tablet.presenter.geometry.CollisionListener;
import de.freiburg.uni.tablet.presenter.geometry.DataPoint;
import de.freiburg.uni.tablet.presenter.geometry.IRenderable;
import de.freiburg.uni.tablet.presenter.geometry.Text;
import de.freiburg.uni.tablet.presenter.page.IPageBackRenderer;

public class ToolText extends AbstractTool implements CollisionListener {
	private TextFont _font = null;
	private Text _currentText = null;
	private DataPoint _startData = null;
	private DataPoint _lastData = null;
	
	private JTextField _textField;
	private boolean _hasFocus = false;
	private float _caretSize = 0;
	private int _caretDot = 0;
	private int _caretMark = 0;
	private DocumentPage _selectPage;
	
	int _collisionCounter = 0;
	private DataPoint _lastCollisionStartData = null;
	int _lastCollisionCounter = -1;
	
	/**
	 * Documents
	 * @param container
	 *            used for cursor changing
	 */
	public ToolText(final IToolPageEditor editor) {
		super(editor);
		editor.getDocumentEditor().addListener(new DocumentEditorAdapter() {
			@Override
			public void changing() {
				endEditingText();
			}
		});
		updateCursor();
		_textField = new JTextField();
		_textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(final FocusEvent e) {
				ToolText.this.onFocusChanged(false);
			}
			
			@Override
			public void focusGained(final FocusEvent e) {
				ToolText.this.onFocusChanged(true);
			}
		});
		_textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(final DocumentEvent e) {
				ToolText.this.onChangedDocument();
			}
			
			@Override
			public void insertUpdate(final DocumentEvent e) {
				ToolText.this.onChangedDocument();
			}
			
			@Override
			public void changedUpdate(final DocumentEvent e) {
				ToolText.this.onChangedDocument();
			}
		});
		_textField.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(final CaretEvent e) {
				ToolText.this.onCaretUpdate(e);
			}
		});
		_textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "unfocus-action");
		_textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "unfocus-action");
		_textField.getActionMap().put("unfocus-action", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				unfocusEditor();
			}
		});
		editor.addDummyComponent(_textField);
	}
	
	private void unfocusEditor() {
		System.out.println("Unfocus editor");
		_editor.getMainComponent().requestFocus();
	}
	
	synchronized private void onFocusChanged(final boolean hasFocus) {
		if (_startData == null) {
			System.out.println("Focus changed to " + hasFocus);
			if (!hasFocus) {
				endEditingText();
			}
			_hasFocus = hasFocus;
			_editor.getFrontRenderer().requireRepaint();
		}
	}
	
	synchronized private void onCaretUpdate(final CaretEvent e) {
		_caretDot = e.getDot();
		_caretMark = e.getMark();
		_editor.getFrontRenderer().requireRepaint();
	}
	
	synchronized private void onChangedDocument() {
		if (_currentText != null) {
			_editor.getFrontRenderer().requireRepaint();
			_currentText.setText(_textField.getText());
		}
	}
	
	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
		if (_currentText != null) {
			int indexStart = Math.min(Math.min(_caretMark, _caretDot), _currentText.getText().length());
			int indexEnd = Math.min(Math.max(_caretMark, _caretDot), _currentText.getText().length());
			Rectangle2D.Float measureText = _font.measureTextRange(_currentText.getText(), indexStart, indexEnd);
			renderer.drawDebugRect(measureText.x + _currentText.getX(), measureText.y + _currentText.getY(), measureText.x + measureText.width + _currentText.getX(), measureText.y + measureText.height + _currentText.getY());
			_currentText.render(renderer);
			if (_hasFocus) {
				if (_caretDot < _caretMark) {
					renderer.draw(_currentText.getX() + measureText.x - _caretSize * 0.5f, _currentText.getY(), "|", _font);
				} else {
					renderer.draw(_currentText.getX() + measureText.width + measureText.x - _caretSize * 0.5f, _currentText.getY(), "|", _font);
				}
			}
		}
	}
	
	synchronized private void endEditingText() {
		if (_currentText != null) {
			System.out.println("End editing");
			_currentText.bake();
			_editor.getDocumentEditor().getCurrentPage().addRenderable(_currentText);
			_editor.getFrontRenderer().requireRepaint();
			_currentText = null;
			_caretDot = _caretMark = 0;
			_textField.setText("");
			if (_textField.isFocusOwner()) {
				unfocusEditor();
			}
		}
	}

	@Override
	synchronized public void begin() {
		endEditingText();
		_startData = null;
		_lastData = null;
		_selectPage = _editor.getDocumentEditor().getCurrentPage();
	}

	@Override
	synchronized public void draw(final DataPoint data) {
		if (_startData == null) {
			if (_lastCollisionStartData != null) {
				float diffX = _lastCollisionStartData.getXOrig() - data.getXOrig();
				float diffY = _lastCollisionStartData.getXOrig() - data.getXOrig();
				if (diffX * diffX + diffY * diffY > 5.0f) {
					_lastCollisionCounter = -1;
				}
			}
			_lastCollisionStartData = data;
			_collisionCounter = 0;
			_selectPage.collideWith(new CollisionInfo(data.getX(), data.getY(), data.getXOrig(), data.getYOrig(), 0, 0, 0, 0, true, data, true), this);
			if (_currentText == null) {
				// None found -> create new
				_currentText = new Text(_editor.getDocumentEditor().getCurrentPage(), data, "", _font);
			} else {
				_editor.getDocumentEditor().getCurrentPage().removeRenderable(_currentText);
				_currentText = _currentText.cloneRenderable(_editor.getDocumentEditor().getCurrentPage());
				int caretIndex = _currentText.getFont().getCaretIndex(_currentText.getText(), data.getX() - _currentText.getX());
				_textField.setText(_currentText.getText());
				_textField.setCaretPosition(caretIndex);
			}
			_startData = data;
		} else {
			float diffX = _startData.getXOrig() - data.getXOrig();
			float diffY = _startData.getXOrig() - data.getXOrig();
			if (diffX * diffX + diffY * diffY > 5.0f) {
				// Offset
				float offsetX = data.getX() - _lastData.getX();
				float offsetY = data.getY() - _lastData.getY();
				_currentText.setLocation(_currentText.getLocation().clone(offsetX, offsetY));
			}
		}
		_lastData = data;
		_editor.getFrontRenderer().requireRepaint();
	}
	
	@Override
	public void collides(final IRenderable data) {
		if (data instanceof Text) {
			if (_currentText == null) {
				_currentText = (Text) data;
			} else if (_collisionCounter > _lastCollisionCounter) {
				_currentText = (Text) data;
			}
			_collisionCounter++;
		}
	}

	@Override
	synchronized public void end() {
		_startData = null;
		
		//Text newElement = new Text(_editor.getDocumentEditor().getCurrentPage(), _startData.getX(), _startData.getY(), "Blablaäöügpq+~'#´`<>|!°^", _font);

		/*_editor.getPageEditor().suspendRepaint();
		_editor.getDocumentEditor().getCurrentPage().addRenderable(newElement);
		
		_editor.getFrontRenderer().setRepaintListener(null);
		
		_editor.getPageEditor().resumeRepaint();*/
		//_textDialog.requestFocus();
		_textField.requestFocus();
	}
	
	@Override
	public void over() {
		super.over();
		//_textDialog.setVisible(true);
		if (_font == null || _font.getParent() != _editor.getDocumentEditor().getFrontDocument()) {
			_font = new TextFont(_editor.getDocumentEditor().getFrontDocument(), "Font1", 0.05f);
			_caretSize = _font.measureText("|").width;
		}
		_editor.getFrontRenderer().setRepaintListener(this);
		System.out.println("Tool text renderer active");
		_editor.getFrontRenderer().requireRepaint();
	}
	
	@Override
	public void out() {
		super.out();
		
		_editor.getPageEditor().suspendRepaint();
		
		endEditingText();
		
		_editor.getFrontRenderer().setRepaintListener(null);
		System.out.println("Tool text renderer inactive");
		
		_editor.getPageEditor().resumeRepaint();
	}

	@Override
	protected Cursor generateCursor() {
		return Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
	}
}
