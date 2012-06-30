/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;
import de.freiburg.uni.tablet.presenter.editor.IPageEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditorListener;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.JPageRenderer;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferComposite;
import de.freiburg.uni.tablet.presenter.editor.pageeditor.PageLayerBufferEditor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonColor;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonEraser;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonFullscreen;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonNext;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonOpenFrom;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPen;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPreferences;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonPrevious;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonRedo;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonSaveAs;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonSpinnerPage;
import de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons.ButtonUndo;
import de.freiburg.uni.tablet.presenter.page.IPageRenderer;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.IToolContainer;

/**
 * @author lukas
 * 
 */
public class JPageEditor extends JFrame implements IToolPageEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IPageEditor _pageRenderer;
	private JPanel _panelTools;

	private final List<IToolPageEditorListener> _listeners = new ArrayList<IToolPageEditorListener>();

	private int _nextObjectId = 0;

	private int _lastExtendedState;
	private Rectangle _lastBounds;

	private PageLayerBufferEditor _clientOnlyLayer;
	private PageLayerBufferEditor _serverSyncLayer;

	/**
	 * Create the panel.
	 */
	public JPageEditor() {
		initialize();
	}

	/**
	 * Initializes the frame
	 */
	private void initialize() {
		getContentPane().setLayout(new BorderLayout(0, 0));
		final JPageRenderer pageRenderer = new JPageRenderer();
		_pageRenderer = pageRenderer;
		getContentPane().add(pageRenderer, BorderLayout.CENTER);

		final PageLayerBufferComposite pageLayers = new PageLayerBufferComposite(
				pageRenderer);
		_serverSyncLayer = pageLayers.addEditorBuffer();
		_clientOnlyLayer = pageLayers.addEditorBuffer();
		_pageRenderer.setDisplayedPageLayerBuffer(pageLayers);

		_panelTools = new JPanel();
		getContentPane().add(_panelTools, BorderLayout.WEST);
		setToolButtons(new IButtonAction[] { new ButtonOpenFrom(this),
				new ButtonSaveAs(this), new ButtonPreferences(this), null,
				new ButtonPen(this), new ButtonEraser(this), null,
				new ButtonNext(this), new ButtonPrevious(this),
				new ButtonSpinnerPage(this), null, new ButtonUndo(this),
				new ButtonRedo(this), null, new ButtonColor(this), null,
				new ButtonFullscreen(this) });
	}

	public void setToolButtons(final IButtonAction[] buttons) {
		// Build layout
		final GridBagLayout gbl_panelTools = new GridBagLayout();
		gbl_panelTools.columnWidths = new int[] { 0 };
		gbl_panelTools.columnWeights = new double[] { 1.0 };
		gbl_panelTools.rowHeights = new int[buttons.length + 1];
		gbl_panelTools.rowWeights = new double[buttons.length + 1];

		for (int i = 0; i < buttons.length; i++) {
			gbl_panelTools.rowWeights[i] = 0.0;
			if (buttons[i] == null) {
				gbl_panelTools.rowHeights[i] = 10;
			} else {
				gbl_panelTools.rowHeights[i] = 0;
			}
		}
		gbl_panelTools.rowWeights[buttons.length] = 1.0;
		gbl_panelTools.rowHeights[buttons.length] = 0;

		_panelTools.setLayout(gbl_panelTools);

		// Add controls
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i] != null) {
				final GridBagConstraints gbc_control = new GridBagConstraints();
				gbc_control.insets = new Insets(0, 0, 5, 0);
				gbc_control.fill = GridBagConstraints.BOTH;
				gbc_control.gridx = 0;
				gbc_control.gridy = i;
				if (buttons[i].getControl() != null) {
					_panelTools.add(buttons[i].getControl(), gbc_control);
				} else {
					final IButtonAction action = buttons[i];
					final JButton button = new JPageToolButton(
							buttons[i].getText(),
							buttons[i].getImageResource(), false);
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							action.perform(button);
						}
					});
					_panelTools.add(button, gbc_control);
				}
			}
		}
	}

	@Override
	public IPage getPage() {
		return _pageRenderer.getPage();
	}

	@Override
	public void setPage(final IPage page) {
		final int index = _pages.indexOf(page);
		if (index != -1) {
			setPageIndex(index);
		}
	}

	@Override
	public ITool getNormalTool() {
		return _pageRenderer.getNormalTool();
	}

	@Override
	public void setNormalTool(final ITool normalTool) {
		_pageRenderer.setNormalTool(normalTool);
	}

	@Override
	public ITool getInvertedTool() {
		return _pageRenderer.getInvertedTool();
	}

	@Override
	public void setInvertedTool(final ITool invertedTool) {
		_pageRenderer.setInvertedTool(invertedTool);
	}

	@Override
	public Container getContainerComponent() {
		return this;
	}

	@Override
	public IPage addPage() {
		final IPage newPage = new DefaultPage();
		_pages.add(newPage);
		return newPage;
	}

	@Override
	public IPage addPage(final int index) {
		final IPage newPage = new DefaultPage();
		_pages.add(index, newPage);
		return newPage;
	}

	@Override
	public int getPageIndex() {
		return _currentPageIndex;
	}

	@Override
	public void setPageIndex(final int index) {
		if (_currentPageIndex != index) {
			_currentPageIndex = index;
			while (_pages.size() <= _currentPageIndex) {
				_pages.add(new DefaultPage());
			}
			_pageRenderer.setPage(_pages.get(index));
			_pageRenderer.getPage().render(_pageRenderer);
			firePageNumberChanged();
		}
	}

	@Override
	public IPen getCurrentPen() {
		return _currentPen;
	}

	@Override
	public void setCurrentPen(final IPen pen) {
		_currentPen = pen;
	}

	@Override
	public int getPageCount() {
		return _pages.size();
	}

	@Override
	public Iterable<IPage> getPages() {
		return _pages;
	}

	@Override
	public IPageRenderer getRenderer() {
		return _pageRenderer;
	}

	@Override
	public IToolContainer getToolContainer() {
		return _pageRenderer;
	}

	private void firePageNumberChanged() {
		for (final IToolPageEditorListener listener : _listeners) {
			listener.pageNumberChanged();
		}
	}

	@Override
	public void addListener(final IToolPageEditorListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(final IToolPageEditorListener listener) {
		_listeners.remove(listener);
	}

	public void serialize(final BinarySerializer writer) throws IOException {
		writer.writeInt(_nextObjectId);
		writer.writeInt(_currentPageIndex);
		writer.writeInt(_pages.size());
		for (final IPage page : _pages) {
			writer.writeSerializableClass(page);
		}
	}

	public void deserialize(final BinaryDeserializer reader) throws IOException {
		_nextObjectId = reader.readInt();
		_currentPageIndex = reader.readInt();
		final int pages = reader.readInt();
		for (int i = 0; i < pages; i++) {
			_pages.add(reader.<IPage> readSerializableClass());
		}
	}

	@Override
	public int getNextObjectId() {
		return _nextObjectId++;
	}

	@Override
	public boolean isFullscreen() {
		return isUndecorated();
	}

	@Override
	public void setFullscreen(final boolean fullscreen) {
		if (fullscreen != isFullscreen()) {
			if (fullscreen) {
				_lastExtendedState = this.getExtendedState();
				_lastBounds = this.getBounds();
				this.setVisible(false);
				this.dispose();
				this.setUndecorated(true);
				this.setExtendedState(Frame.MAXIMIZED_BOTH);
				this.setVisible(true);
			} else {
				this.setVisible(false);
				this.dispose();
				this.setBounds(_lastBounds);
				this.setExtendedState(_lastExtendedState);
				this.setUndecorated(false);
				this.setVisible(true);
			}
		}
	}
}
