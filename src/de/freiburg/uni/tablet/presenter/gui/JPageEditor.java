/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.editor.gui.JPageRenderer;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonColor;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonEraser;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonFullscreen;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonNext;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonOpenFrom;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonPen;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonPreferences;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonPrevious;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonRedo;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonSaveAs;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonSpinnerPage;
import de.freiburg.uni.tablet.presenter.gui.buttons.ButtonUndo;
import de.freiburg.uni.tablet.presenter.page.DefaultPage;
import de.freiburg.uni.tablet.presenter.page.IPage;
import de.freiburg.uni.tablet.presenter.page.IPen;
import de.freiburg.uni.tablet.presenter.page.SolidPen;
import de.freiburg.uni.tablet.presenter.tools.ITool;
import de.freiburg.uni.tablet.presenter.tools.ToolEraser;
import de.freiburg.uni.tablet.presenter.tools.ToolScribble;

/**
 * @author lukas
 * 
 */
public class JPageEditor extends JPanel implements IToolPageEditor {
	private JPageRenderer _pageRenderer;
	private JPanel _panelTools;

	private final List<IPage> _pages = new LinkedList<IPage>();
	private int _currentPage = 0;
	private IPen _currentPen = new SolidPen();

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
		setLayout(new BorderLayout(0, 0));
		_pageRenderer = new JPageRenderer();
		this.add(_pageRenderer);
		_pageRenderer.setPage(new DefaultPage());
		_pageRenderer.setNormalTool(new ToolScribble(_pageRenderer,
				_pageRenderer, this));
		_pageRenderer.setInvertedTool(new ToolEraser(_pageRenderer,
				_pageRenderer, this));

		final JPanel panel = new JPanel();
		add(panel, BorderLayout.EAST);
		final GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0 };
		gbl_panel.rowHeights = new int[] { 0 };
		gbl_panel.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		_panelTools = new JPanel();
		add(_panelTools, BorderLayout.WEST);
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
		gbl_panelTools.columnWidths = new int[] { 0, 0 };
		gbl_panelTools.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
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
		_pageRenderer.setPage(page);
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
	public Container getContainer() {
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
	public int getCurrentPage() {
		return _currentPage;
	}

	@Override
	public void setCurrentPage(final int index) {
		_currentPage = index;
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
}
