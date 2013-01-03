package de.freiburg.uni.tablet.presenter.actions;

import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.BinarySerializer;

public abstract class AbstractAction implements IAction {
	private final int _clientId;
	
	public AbstractAction(int clientId) {
		_clientId = clientId;
	}
	
	public AbstractAction(BinaryDeserializer reader) throws IOException {
		_clientId = reader.readInt();
	}
	
	@Override
	public void serialize(BinarySerializer writer) throws IOException {
		writer.writeInt(_clientId);
	}

	@Override
	public int getClientId() {
		return _clientId;
	}
}
