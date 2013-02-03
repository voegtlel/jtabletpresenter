package de.freiburg.uni.tablet.presenter.editor.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import de.freiburg.uni.tablet.presenter.R;

public class FileDialog extends Dialog {
	private static final String ITEM_TEXT = "text";
	private static final String ITEM_IS_FOLDER = "isFolder";
	private static final String ITEM_IMAGE = "image";
	
	private ListView _fileList;
	private EditText _filename;
	private TextView _location;
	
	private String[] _filters;
	private boolean _openFile;
	
	private String _selectedFile;
	
	private ArrayList<String> _displayedPaths = new ArrayList<String>();
	private Button _buttonOk;

	public FileDialog(Context context) {
		super(context);
		
		this.setContentView(R.layout.filedialog_open);
		
		_buttonOk = (Button) this.findViewById(R.id.filedialog_button_ok);
		_buttonOk.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickOk();
			}
		});
		Button buttonCancel = (Button) this.findViewById(R.id.filedialog_button_cancel);
		buttonCancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickCancel();
			}
		});
		Button buttonNewDirectory = (Button) this.findViewById(R.id.filedialog_button_new_directory);
		buttonNewDirectory.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickNewDirectory();
			}
		});
		_filename = (EditText) this.findViewById(R.id.filedialog_filename);
		_location = (TextView) this.findViewById(R.id.filedialog_location);
		_fileList = (ListView) this.findViewById(R.id.filedialog_file_list);
		_fileList.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> adapterView, final View view, final int position,
					final long id) {
				onListItemClick(position);
			}
		});
		setDirectory("./");
	}
	
	public void setFilters(final String[] filters) {
		_filters = filters;
	}
	
	public void setOpenFileDialog(final boolean openFile) {
		_openFile = openFile;
		_filename.setEnabled(!openFile);
	}
	
	public void setDirectory(final String directory) {
		try {
			_location.setText(new File(directory).getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
			_location.setText(directory);
		}
		initializeList();
	}
	
	/**
	 * Gets the selected file or null
	 * @return
	 */
	public String getSelectedFile() {
		return _selectedFile;
	}
	
	private void addListItem(final ArrayList<HashMap<String, Object>> list, final String filename, final boolean isFolder) {
		final HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_TEXT, filename);
		item.put(ITEM_IS_FOLDER, isFolder);
		item.put(ITEM_IMAGE, isFolder?R.drawable.icon_filedialog_folder:R.drawable.icon_filedialog_file);
		list.add(item);
	}
	
	private void initializeList() {
		if (_openFile) {
			_buttonOk.setEnabled(false);
		}
		File path = new File(_location.getText().toString());
		File[] files = path.listFiles();
		if (files == null) {
			path = new File("/");
			_location.setText("/");
			files = path.listFiles();
		}
		if (files == null) {
			return;
		}
		
		final ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		
		if (!path.getAbsolutePath().equalsIgnoreCase("/")) {
			addListItem(data, "..", true);
		}
		
		for (File file : files) {
			if (!file.isHidden() && file.canRead()) {
				if (file.isDirectory()) {
					addListItem(data, file.getName(), true);
				} else if (file.isFile()) {
					final String filename = file.getName();
					boolean addFile = true;
					if (_filters != null) {
						addFile = false;
						for (String filter : _filters) {
							if (filename.toLowerCase().endsWith(filter)) {
								addFile = true;
								break;
							}
						}
					}
					if (addFile) {
						addListItem(data, file.getName(), false);
					}
				}
			}
		}
		
		Collections.sort(data, new Comparator<HashMap<String, Object>>() {
			@Override
			public int compare(final HashMap<String, Object> lhs,
					final HashMap<String, Object> rhs) {
				final boolean leftIsFolder = (Boolean) lhs.get(ITEM_IS_FOLDER);
				final boolean rightIsFolder = (Boolean) rhs.get(ITEM_IS_FOLDER);
				if (leftIsFolder && rightIsFolder) {
					return ((String)lhs.get(ITEM_TEXT)).compareTo((String) rhs.get(ITEM_TEXT));
				} else if (leftIsFolder) {
					return Integer.MIN_VALUE;
				}
				return Integer.MAX_VALUE;
			}
		});
		_displayedPaths.clear();
		_displayedPaths.ensureCapacity(data.size());
		for (HashMap<String, Object> item : data) {
			String itemText = (String) item.get(ITEM_TEXT);
			String itemPath;
			if (itemText.startsWith("/")) {
				itemPath = itemText;
			} else if (itemText.equals("..")) {
				itemPath = path.getParent();
			} else {
				itemPath = path.getPath() + "/" + (String) item.get(ITEM_TEXT);
			}
			_displayedPaths.add(itemPath);
		}
		
		final SimpleAdapter fileList = new SimpleAdapter(this.getContext(), data, R.layout.filedialog_item, new String[] {
				ITEM_TEXT, ITEM_IMAGE }, new int[] { R.id.filedialog_item_text, R.id.filedialog_item_image });
		_fileList.setAdapter(fileList);
	}

	protected void onClickOk() {
		if (!_filename.getText().toString().isEmpty()) {
			// TODO: Message instead
			// TODO: Overwrite warning if save file dialog
			// TODO: Check if exists if open file dialog
			_selectedFile = new File(_location.getText().toString()).getAbsolutePath() + "/" + _filename.getText().toString();
			this.dismiss();			
		}
	}

	protected void onClickCancel() {
		_selectedFile = null;
		this.cancel();
	}

	protected void onClickNewDirectory() {
		// TODO Create new directory
	}
	
	protected void onListItemClick(final int position) {
		final String itemPath = _displayedPaths.get(position);
		final File itemFile = new File(itemPath);
		if (itemFile.isDirectory()) {
			_location.setText(itemFile.getAbsolutePath());
			_fileList.setSelection(position);
			initializeList();
		} else if (itemFile.isFile()) {
			if (_openFile) {
				_filename.setText(itemPath);
				_buttonOk.setEnabled(true);
			}
		}
	}
}
