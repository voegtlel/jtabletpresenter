package de.freiburg.uni.tablet.presenter.geometry;

import java.util.HashMap;

public class EraseInfo {
	private final HashMap<Long, HashMap<Integer, Object>> _objectsData = new HashMap<Long, HashMap<Integer, Object>>();
	private CollisionInfo _collisionInfo;

	public EraseInfo() {
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
	 * @param sourceDataPoint
	 * @param fastCollide
	 */
	public void createCollisionInfo(final float x, final float y,
			final float xOrig, final float yOrig, final float radiusX,
			final float radiusY, final float radiusXOrig,
			final float radiusYOrig, final boolean checkOnlyBoundaries,
			final DataPoint sourceDataPoint, final boolean fastCollide) {
		_collisionInfo = new CollisionInfo(x, y, xOrig, yOrig, radiusX,
				radiusY, radiusXOrig, radiusYOrig, checkOnlyBoundaries,
				sourceDataPoint, fastCollide);
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
	public <T> void addObjectData(final IRenderable object, final int dataId, final T data) {
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
}
