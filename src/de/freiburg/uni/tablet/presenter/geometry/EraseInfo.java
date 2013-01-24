package de.freiburg.uni.tablet.presenter.geometry;

import java.util.HashMap;

import de.freiburg.uni.tablet.presenter.document.Document;
import de.freiburg.uni.tablet.presenter.document.DocumentPage;
import de.freiburg.uni.tablet.presenter.list.LinkedElement;

public class EraseInfo {
	private final HashMap<Long, IRenderable> _modifiedObjects = new HashMap<Long, IRenderable>();
	private final HashMap<Long, HashMap<Integer, Object>> _objectsData = new HashMap<Long, HashMap<Integer, Object>>();
	private final Document _document;
	private CollisionInfo _collisionInfo;
	private final DocumentPage _layer;

	public EraseInfo(final Document document, final DocumentPage layer) {
		_document = document;
		_layer = layer;
	}

	/**
	 * Creates the collision info
	 * 
	 * @param x
	 * @param y
	 * @param xOrig
	 * @param yOrig
	 * @param radiusX
	 * @param radiusY
	 * @param radiusXOrig
	 * @param radiusYOrig
	 * @param checkOnlyBoundaries
	 */
	public void createCollisionInfo(final float x, final float y,
			final float xOrig, final float yOrig, final float radiusX,
			final float radiusY, final float radiusXOrig,
			final float radiusYOrig, final boolean checkOnlyBoundaries) {
		_collisionInfo = new CollisionInfo(x, y, xOrig, yOrig, radiusX,
				radiusY, radiusXOrig, radiusYOrig, checkOnlyBoundaries);
	}

	/**
	 * Gets the assigned collision info
	 * 
	 * @return
	 */
	public CollisionInfo getCollisionInfo() {
		return _collisionInfo;
	}
	
	/**
	 * Add a property for a renderable
	 * @param object the renderable
	 * @param dataId the id for the property
	 * @param data the actual data
	 */
	public <T> void addObjectData(final IRenderable object, final int dataId, T data) {
		HashMap<Integer, Object> objectData = _objectsData.get(object.getId());
		if (objectData == null) {
			objectData = new HashMap<Integer, Object>();
			_objectsData.put(object.getId(), objectData);
		}
		objectData.put(dataId, data);
	}
	
	/**
	 * Get a objects property
	 * @param object
	 * @param dataId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getObjectData(final IRenderable object, final int dataId) {
		HashMap<Integer, Object> objectData = _objectsData.get(object.getId());
		if (objectData == null) {
			return null;
		}
		return (T)objectData.get(dataId);
	}

	/**
	 * Adds a to be modified object to the list
	 * 
	 * @param origObject
	 *            original object
	 * @return copy of original to be modified
	 */
	public IRenderable addModifiedObject(final IRenderable origObject) {
		final IRenderable result = origObject.cloneRenderable(_document
				.nextId());
		_modifiedObjects.put(origObject.getId(), result);
		return result;
	}

	/**
	 * Gets the copy of the original to be modified
	 * 
	 * @param origObject
	 *            original object
	 * @return copy of original to be modified
	 */
	public IRenderable getModifiedObject(final IRenderable origObject) {
		return _modifiedObjects.get(origObject.getId());
	}

	/**
	 * Applies all replace actions.
	 */
	public void applyModifications() {
		for (LinkedElement<IRenderable> renderable = _layer.getRenderables().getFirst(); renderable != null;) {
			LinkedElement<IRenderable> nextRenderable = renderable.getNext();
			IRenderable replacement = _modifiedObjects.get(renderable.getData().getId());
			if (replacement != null) {
				System.out.println("Replace " + renderable.getData().getId() + " by " + replacement.getId());
				_layer.removeRenderable(renderable.getData());
				if (renderable.getData().eraseEnd(this)) {
					_layer.addRenderable(replacement);
				}
			}
			renderable = nextRenderable;
		}
	}
}
