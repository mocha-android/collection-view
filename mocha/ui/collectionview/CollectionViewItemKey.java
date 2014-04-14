package mocha.ui.collectionview;

import mocha.foundation.MObject;

class CollectionViewItemKey extends MObject implements mocha.foundation.Copying<CollectionViewItemKey> {
	static final String ELEMENT_KIND_CELL = "UICollectionElementKindCell";

	private CollectionViewLayout.CollectionViewItemType _type;
	private mocha.foundation.IndexPath _indexPath;
	private String _identifier;

	static CollectionViewItemKey collectionItemKeyForLayoutAttributes(CollectionViewLayout.Attributes layoutAttributes) {
		CollectionViewItemKey key = new CollectionViewItemKey();
		key.setIndexPath(layoutAttributes.getIndexPath());
		key.setType(layoutAttributes.getRepresentedElementCategory());
		key.setIdentifier(layoutAttributes.getRepresentedElementKind());
		return key;
	}

	static CollectionViewItemKey collectionItemKeyForCellWithIndexPath(mocha.foundation.IndexPath indexPath) {
		CollectionViewItemKey key = new CollectionViewItemKey();
		key.setIndexPath(indexPath);
		key.setType(CollectionViewLayout.CollectionViewItemType.CELL);
		key.setIdentifier(ELEMENT_KIND_CELL);
		return key;
	}

	protected String toStringExtra() {
		return String.format("Type = %s Identifier=%s IndexPath = %s", CollectionViewLayout.CollectionViewItemTypeToString(this.getType()), _identifier, this.getIndexPath());
	}

	public int hashCode() {
		return ((_indexPath.hashCode() + _type) * 31) + _identifier.hashCode();
	}

	public boolean equals(Object other) {
		if(other instanceof CollectionViewItemKey) {
		    CollectionViewItemKey otherKeyItem = (CollectionViewItemKey)other;
		    // identifier might be null?
		    if (_type == otherKeyItem.getType() && _indexPath.equals(otherKeyItem.getIndexPath()) && (_identifier.equals(otherKeyItem.getIdentifier()) || _identifier.equals(otherKeyItem.getIdentifier()))) {
		        return true;
		    }
		}
		return false;
	}

	public CollectionViewItemKey copy() {
		CollectionViewItemKey itemKey;

		try {
			itemKey = this.getClass().newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

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

