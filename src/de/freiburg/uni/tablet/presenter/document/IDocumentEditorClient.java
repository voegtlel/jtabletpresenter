package de.freiburg.uni.tablet.presenter.document;

import java.io.File;
import java.io.IOException;

import de.freiburg.uni.tablet.presenter.data.BinaryDeserializer;
import de.freiburg.uni.tablet.presenter.data.IBinarySerializableId;
import de.freiburg.uni.tablet.presenter.geometry.BitmapImage;
import de.freiburg.uni.tablet.presenter.page.IPen;

public interface IDocumentEditorClient extends IDocumentEditor {
	DocumentPage getCurrentBackPage();

	IPen getCurrentPen();

	void setCurrentPen(IPen currentPen);

	File getCurrentImageFile();

	BitmapImage getCurrentImage();

	void setCurrentImageFile(File imageFile) throws IOException;

	void setBaseDocument(IDocument baseDocument);

	IDocument getBaseDocument();
}