package de.freiburg.uni.tablet.presenter.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextArea;
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
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;

public class ToolText extends AbstractTool implements CollisionListener {
	private TextFont _font = null;
	private Text _currentText = null;
	private String _currentTextStr = null;
	private DataPoint _startData = null;
	private DataPoint _lastData = null;
	
	private JTextArea _textArea;
	private boolean _hasFocus = false;
	private float _caretSize = 0;
	private int _caretDot = 0;
	private int _caretMark = 0;
	private DocumentPage _selectPage;
	
	int _collisionCounter = 0;
	private DataPoint _lastCollisionStartData = null;
	int _lastCollisionCounter = -1;
	
	IPen _selectPen = new SolidPen(1.0f, Color.BLUE);
	
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
		_textArea = new JTextArea();
		_textArea.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(final FocusEvent e) {
				ToolText.this.onFocusChanged(false);
			}
			
			@Override
			public void focusGained(final FocusEvent e) {
				ToolText.this.onFocusChanged(true);
			}
		});
		_textArea.getDocument().addDocumentListener(new DocumentListener() {
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
		_textArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(final CaretEvent e) {
				ToolText.this.onCaretUpdate(e);
			}
		});
		_textArea.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "unfocus-action");
		_textArea.getActionMap().put("unfocus-action", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				unfocusEditor();
			}
		});
		editor.addDummyComponent(_textArea);
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
			_currentText.setText(_textArea.getText());
			_currentTextStr = _textArea.getText();
		}
	}
	
	@Override
	synchronized public void render(final IPageBackRenderer renderer) {
		if (_currentText != null) {
			int indexStart = Math.min(Math.min(_caretMark, _caretDot), _currentTextStr.length());
			int indexEnd = Math.min(Math.max(_caretMark, _caretDot), _currentTextStr.length());
			final String[] startLines = _currentTextStr.substring(0, indexStart).split("\\r?\\n|\\r", -1);
			final String[] selectLines = _currentTextStr.substring(indexStart, indexEnd).split("\\r?\\n|\\r", -1);
			String lastStartLine = (startLines.length > 0?startLines[startLines.length - 1]:"");
			String firstSelectLine = (selectLines.length > 0?selectLines[0]:"");
			int selectStartLine = startLines.length - 1;
			int selectEndLine = selectStartLine + selectLines.length - 1;
			Path2D selectBorder = new Path2D.Float();
			Rectangle2D.Float measure0 = _font.measureTextRange(lastStartLine + firstSelectLine, lastStartLine.length(), lastStartLine.length() + firstSelectLine.length());
			Rectangle2D.Float measure = measure0;
			float h = _font.getLineHeight();
			float x = _currentText.getX();
			float y = _currentText.getY() + h * selectStartLine;
			selectBorder.moveTo(x + measure0.x, y + measure0.y);
			selectBorder.lineTo(x + measure0.x + measure0.width, y + measure0.y);
			selectBorder.lineTo(x + measure0.x + measure0.width, y + measure0.y + h);
			for (int i = 1; i < selectLines.length; i++) {
				measure = _font.measureText(selectLines[i]);
				selectBorder.lineTo(x + measure.width, y + h * i + measure.y);
				selectBorder.lineTo(x + measure.width, y + h * i + measure.y + h);
			}
			if (selectLines.length > 1) {
				selectBorder.lineTo(x, y + h * selectLines.length + measure.y);
				selectBorder.lineTo(x, y + measure0.y + h);
			}
			selectBorder.lineTo(x + measure0.x, y + measure0.y + h);
			selectBorder.closePath();
			renderer.draw(_selectPen, selectBorder);
			_currentText.render(renderer);
			if (_hasFocus) {
				if (_caretDot < _caretMark) {
					renderer.draw(x + measure0.x - _caretSize * 0.5f, y, new String[] {"|"}, _font);
				} else {
					renderer.draw(x + measure.width + measure.x - _caretSize * 0.5f, y + (selectEndLine - selectStartLine) * _font.getLineHeight(), new String[] {"|"}, _font);
				}
			}
		}
	}
	
	synchronized private void endEditingText() {
		if (_currentText != null) {
			System.out.println("End editing");
			_currentText.bake();
			if (_currentText.getTextLines().length > 0 && (_currentText.getTextLines().length != 1 || !_currentText.getTextLines()[0].isEmpty())) {
				_editor.getDocumentEditor().getCurrentPage().addRenderable(_currentText);
			}
			_editor.getFrontRenderer().requireRepaint();
			_currentText = null;
			_caretDot = _caretMark = 0;
			_textArea.setText("");
			if (_textArea.isFocusOwner()) {
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
			_selectPage.collideWith(new CollisionInfo(data.getX(), data.getY(), data.getXOrig(), data.getYOrig(), 0, 0, 0, 0, true, data, false), this);
			if (_currentText == null) {
				// None found -> create new
				_currentText = new Text(_editor.getDocumentEditor().getCurrentPage(), data, new String[]{""}, _font);
				_currentTextStr = "";
			} else {
				_editor.getDocumentEditor().getCurrentPage().removeRenderable(_currentText);
				_currentText = _currentText.cloneRenderable(_editor.getDocumentEditor().getCurrentPage());
				_currentTextStr = _currentText.getText();
				_textArea.setText(_currentTextStr);
				
				int lineIndex = _currentText.getCaretLine(data.getX(), data.getY());
				if (lineIndex != -1) {
					int columnIndex = _currentText.getFont().getCaretIndex(_currentText.getTextLines()[lineIndex], data.getX() - _currentText.getX());
					if (columnIndex != -1) {
						int offset = 0;
						for (int i = 0; i < lineIndex; i++) {
							offset += _currentText.getTextLines()[i].length() + 1; //1 for \n
						}
						offset += columnIndex;
						_textArea.setCaretPosition(offset);
					}
				}
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
		_textArea.requestFocus();
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
