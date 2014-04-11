class CollectionViewItemKey extends MObject implements mocha.foundation.Copying<CollectionViewItemKey> {
	private CollectionViewLayout.CollectionViewItemType _type;
	private mocha.foundation.IndexPath _indexPath;
	private String _identifier;

	static Object collectionItemKeyForLayoutAttributes(CollectionViewLayout.Attributes layoutAttributes) {
		CollectionViewItemKey key = this.getClass().new();
		key.setIndexPath(layoutAttributes.getIndexPath());
		CollectionViewLayout.CollectionViewItemType const itemType = layoutAttributes.getRepresentedElementCategory();
		key.setType(itemType);
		key.setIdentifier(layoutAttributes.getRepresentedElementKind());
		return key;
	}

	static Object collectionItemKeyForCellWithIndexPath(mocha.foundation.IndexPath indexPath) {
		CollectionViewItemKey key = this.getClass().new();
		key.setIndexPath(indexPath);
		key.setType(CollectionViewLayout.CollectionViewItemType.CELL);
		key.setIdentifier(PSTCollectionElementKindCell);
		return key;
	}

	String description() {
		return String.format("<%s: %p Type = %s Identifier=%s IndexPath = %s>", StringFromClass(this.getClass()),
		                                  this, CollectionViewLayout.CollectionViewItemTypeToString(this.getType()), _identifier, this.getIndexPath());
	}

	int hash() {
		return ((_indexPath.hash() + _type) * 31) + _identifier.hash();
	}

	boolean isEqual(Object other) {
		if (other.isKindOfClass(this.getClass())) {
		    CollectionViewItemKey otherKeyItem = (CollectionViewItemKey)other;
		    // identifier might be null?
		    if (_type == otherKeyItem.getType() && _indexPath.isEqual(otherKeyItem.getIndexPath()) && (_identifier.isEqualToString(otherKeyItem.getIdentifier()) || _identifier == otherKeyItem.getIdentifier())) {
		        return true;
		    }
		}
		return false;
	}

	Object copyWithZone(mocha.foundation.Zone zone) {
		CollectionViewItemKey itemKey = this.getClass().new();
		itemKey.setIndexPath(this.getIndexPath());
		itemKey.setType(this.getType());
		itemKey.setIdentifier(this.getIdentifier());
		return itemKey;
	}

	/* Setters & Getters */
	/* ========================================== */

	public CollectionViewLayout.CollectionViewItemType getType() {
		return this._type;
	}

	public void setType(CollectionViewLayout.CollectionViewItemType type) {
		this._type = type;
	}

	public mocha.foundation.IndexPath getIndexPath() {
		return this._indexPath;
	}

	public void setIndexPath(mocha.foundation.IndexPath indexPath) {
		this._indexPath = indexPath;
	}

	public String getIdentifier() {
		return this._identifier;
	}

	public void setIdentifier(String identifier) {
		this._identifier = identifier;
	}

}

