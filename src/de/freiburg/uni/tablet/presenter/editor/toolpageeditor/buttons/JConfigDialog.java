package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

public class JConfigDialog extends JDialog {
	/**
	 * Default serial version
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField _textFieldAddress;
	private JTextField _textFieldPenWidth;
	private JTextField _textFieldEraserWidth;

	private final IToolPageEditor _editor;

	/**
	 * @wbp.parser.constructor
	 */
	public JConfigDialog(Frame parent, IToolPageEditor editor) {
		super(parent, "Config", true);
		_editor = editor;
		initComponents();
		initFields();
	}

	public JConfigDialog(Dialog parent, IToolPageEditor editor) {
		super(parent, "Config", true);
		_editor = editor;
		initComponents();
		initFields();
	}

	private final void initComponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JLabel lblPenWidth = new JLabel("Pen Width:");
		GridBagConstraints gbc_lblPenWidth = new GridBagConstraints();
		gbc_lblPenWidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPenWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblPenWidth.gridx = 0;
		gbc_lblPenWidth.gridy = 0;
		contentPane.add(lblPenWidth, gbc_lblPenWidth);

		_textFieldPenWidth = new JTextField();
		GridBagConstraints gbc_textFieldPenWidth = new GridBagConstraints();
		gbc_textFieldPenWidth.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldPenWidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPenWidth.gridx = 1;
		gbc_textFieldPenWidth.gridy = 0;
		contentPane.add(_textFieldPenWidth, gbc_textFieldPenWidth);
		_textFieldPenWidth.setColumns(10);

		JLabel lblPenColor = new JLabel("Pen Color:");
		GridBagConstraints gbc_lblPenColor = new GridBagConstraints();
		gbc_lblPenColor.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPenColor.insets = new Insets(0, 0, 5, 5);
		gbc_lblPenColor.gridx = 0;
		gbc_lblPenColor.gridy = 1;
		contentPane.add(lblPenColor, gbc_lblPenColor);

		JLabel lblEraserWidth = new JLabel("Eraser Width:");
		GridBagConstraints gbc_lblEraserWidth = new GridBagConstraints();
		gbc_lblEraserWidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblEraserWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblEraserWidth.gridx = 0;
		gbc_lblEraserWidth.gridy = 2;
		contentPane.add(lblEraserWidth, gbc_lblEraserWidth);

		_textFieldEraserWidth = new JTextField();
		GridBagConstraints gbc_textFieldEraserWidth = new GridBagConstraints();
		gbc_textFieldEraserWidth.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldEraserWidth.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldEraserWidth.gridx = 1;
		gbc_textFieldEraserWidth.gridy = 2;
		contentPane.add(_textFieldEraserWidth, gbc_textFieldEraserWidth);
		_textFieldEraserWidth.setColumns(10);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Networking",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridwidth = 2;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 3;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JCheckBox chckbxNetworkingEnable = new JCheckBox("Enable");
		GridBagConstraints gbc_chckbxNetworkingEnable = new GridBagConstraints();
		gbc_chckbxNetworkingEnable.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNetworkingEnable.gridx = 0;
		gbc_chckbxNetworkingEnable.gridy = 0;
		panel.add(chckbxNetworkingEnable, gbc_chckbxNetworkingEnable);

		JLabel lblNetworkingAddress = new JLabel("Address:");
		GridBagConstraints gbc_lblNetworkingAddress = new GridBagConstraints();
		gbc_lblNetworkingAddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNetworkingAddress.insets = new Insets(0, 0, 0, 5);
		gbc_lblNetworkingAddress.gridx = 0;
		gbc_lblNetworkingAddress.gridy = 1;
		panel.add(lblNetworkingAddress, gbc_lblNetworkingAddress);

		_textFieldAddress = new JTextField();
		GridBagConstraints gbc_textFieldAddress = new GridBagConstraints();
		gbc_textFieldAddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldAddress.gridx = 1;
		gbc_textFieldAddress.gridy = 1;
		panel.add(_textFieldAddress, gbc_textFieldAddress);
		_textFieldAddress.setColumns(10);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 4;
		contentPane.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JButton btnOk = new JButton("Ok");
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.insets = new Insets(0, 0, 0, 5);
		gbc_btnOk.gridx = 0;
		gbc_btnOk.gridy = 0;
		panel_1.add(btnOk, gbc_btnOk);

		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		panel_1.add(btnCancel, gbc_btnCancel);
	}

	public void initFields() {
	}
}
