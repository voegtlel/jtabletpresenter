package de.freiburg.uni.tablet.presenter.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentEditor;
import de.freiburg.uni.tablet.presenter.document.DocumentEditorAdapter;
import de.freiburg.uni.tablet.presenter.net.AcceptListener;
import de.freiburg.uni.tablet.presenter.net.AcceptThread;
import de.freiburg.uni.tablet.presenter.net.ClientThread;
import de.freiburg.uni.tablet.presenter.net.HistoryClient;

public class NetworkPageEditor {
	private DocumentEditor _documentEditor;
	private AcceptThread _acceptThread;
	private List<HistoryClient> _clients = new ArrayList<HistoryClient>();
	private HistoryClient _client;

	private String _address;
	private boolean _enabled;

	public NetworkPageEditor(DocumentEditor documentEditor) {
		_documentEditor = documentEditor;
		_documentEditor.addListener(new DocumentEditorAdapter() {
			@Override
			public void documentChanged(Document lastDocument) {
				onDocumentChanged(lastDocument);
			}
		});
	}

	private void onDocumentChanged(Document lastDocument) {

	}

	public void setListening(boolean listening) {
		if (listening) {
			if (_acceptThread != null) {
				_acceptThread.close();
			}
			_acceptThread = new AcceptThread(9135);
			_acceptThread.addListener(new AcceptListener() {
				@Override
				public void clientConnected(ClientThread socket) {
					onConnect(socket);
				}
			});
			_acceptThread.start();
		} else {
			if (_acceptThread != null) {
				_acceptThread.close();
				_acceptThread = null;
			}
			for (HistoryClient client : _clients) {
				client.close();
			}
			_clients.clear();
		}
	}

	private void connect() {
		if(_enabled) {
			try {
				if (_client != null) {
					_client.close();
				}
				_client = new HistoryClient(new ClientThread(_address, 9135, 1000),
						_documentEditor);
				_client.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void onConnect(ClientThread thread) {
		HistoryClient historyClient = new HistoryClient(thread, _documentEditor);
		try {
			historyClient.start();
			_clients.add(historyClient);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setAddress(String address) {
		_address = address;
		connect();
	}

	public void setEnabled(boolean enabled) {
		_enabled = enabled;
		connect();
	}
}
