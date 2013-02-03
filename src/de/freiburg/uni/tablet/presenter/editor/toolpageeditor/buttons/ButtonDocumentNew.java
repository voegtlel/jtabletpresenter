/**
 * Copyright Lukas Vögtle
 * Albert Ludwigs University of Freiburg
 */
package de.freiburg.uni.tablet.presenter.editor.toolpageeditor.buttons;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import de.freiburg.uni.tablet.presenter.R;
import de.freiburg.uni.tablet.presenter.document.ServerDocument;
import de.freiburg.uni.tablet.presenter.editor.IToolPageEditor;

/**
 * @author lukas
 * 
 */
public class ButtonDocumentNew extends AbstractButtonAction {
	/**
	 * Creates the action with an editor.
	 */
	public ButtonDocumentNew(final IToolPageEditor editor) {
		super(editor, R.id.document_new);
	}

	@Override
	public void perform(final Context context) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle(R.string.msg_title_info)
				.setMessage(R.string.msg_new_save)
				.setNegativeButton(R.string.msg_button_cancel, null)
				.setPositiveButton(R.string.msg_button_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						FileHelper.showSaveDialog(context, _editor, FileHelper.stringToFilter(_editor.getConfig().getString("save.defaultExt", "jpd")), new FileHelper.OnSuccessListener() {
							@Override
							public void onSuccess() {
								_editor.getDocumentEditor().setDocument(new ServerDocument(1));
							}
						});
					}
				})
				.setPositiveButton(R.string.msg_button_no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						_editor.getDocumentEditor().setDocument(new ServerDocument(1));
					}
				})
				.create();
		alertDialog.show();
	}
}
