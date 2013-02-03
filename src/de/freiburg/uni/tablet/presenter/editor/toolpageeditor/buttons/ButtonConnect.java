/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;
import de.freiburg.uni.tablet.presenter.xsocket.DownClient;
import de.freiburg.uni.tablet.presenter.xsocket.UpClient;

/**
 * @author lukas
 * 
 */
public class ButtonConnect extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonConnect(final IToolPageEditor editor) {
		super(editor, R.id.connect);
	}
	
	private DownClient _downClient;
	private UpClient _upClient;

	@Override
	public void perform(final Context context) {
		final LayoutInflater factory = LayoutInflater.from(context);
	    final View textEntryView = factory.inflate(R.layout.dialog_connect, null);
		final Builder builder = new AlertDialog.Builder(context).setTitle(R.string.msg_title_info)
				.setView(textEntryView)
				.setNegativeButton(R.string.msg_button_cancel, null)
				.setPositiveButton(R.string.msg_button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						final EditText hostEdit = (EditText) textEntryView.findViewById(R.id.connect_host);
						final EditText portEdit = (EditText) textEntryView.findViewById(R.id.connect_port);
						final CheckBox upCheckBox = (CheckBox) textEntryView.findViewById(R.id.connect_up);
						final EditText authEdit = (EditText) textEntryView.findViewById(R.id.connect_auth);
						final CheckBox initDownCheckBox = (CheckBox) textEntryView.findViewById(R.id.connect_init_down);
						final String host = hostEdit.getText().toString();
						final int port = Integer.parseInt(portEdit.getText().toString());
						final boolean up = upCheckBox.isChecked();
						final String auth = authEdit.getText().toString();
						final boolean initDown = initDownCheckBox.isChecked();
						try {
							if (_upClient != null) {
								_upClient.stop();
								_upClient = null;
							}
							if (_downClient != null) {
								_downClient.stop();
								_downClient = null;
							}
							if (up) {
								_upClient = new UpClient(host, port, _editor.getDocumentEditor());
								_upClient.start();
								_upClient.setName("AndroidClient");
								_upClient.setSyncDownInit(initDown);
								_upClient.setAuthToken(auth);
								_upClient.start();
							} else {
								_downClient = new DownClient(host, port, _editor.getDocumentEditor());
								_downClient.setName("AndroidClient");
								_downClient.setAuthToken(auth);
								_downClient.start();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
		if (_upClient != null || _downClient != null) {
			builder.setNegativeButton(R.string.msg_button_disconnect, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					if (_upClient != null) {
						_upClient.stop();
						_upClient = null;
					}
					if (_downClient != null) {
						_downClient.stop();
						_downClient = null;
					}
				}
			});
		}
		final AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
}
