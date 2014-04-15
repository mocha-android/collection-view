package mocha.ui.collectionview;

import mocha.foundation.MObject;

class CollectionViewItemKey extends MObject implements mocha.foundation.Copying<CollectionViewItemKey> {
	static final String ELEMENT_KIND_CELL = "ELEMENT_KIND_CELL";

	private CollectionViewLayout.CollectionViewItemType type;
	private mocha.foundation.IndexPath indexPath;
	private String identifier;

	public static CollectionViewItemKey collectionItemKeyForLayoutAttributes(CollectionViewLayout.Attributes layoutAttributes) {
		CollectionViewItemKey key = new CollectionViewItemKey();
		key.setIndexPath(layoutAttributes.getIndexPath());
		key.setType(layoutAttributes.getRepresentedElementCategory());
		key.setIdentifier(layoutAttributes.getRepresentedElementKind());
		return key;
	}

	public static CollectionViewItemKey collectionItemKeyForCellWithIndexPath(mocha.foundation.IndexPath indexPath) {
		CollectionViewItemKey key = new CollectionViewItemKey();
		key.setIndexPath(indexPath);
		key.setType(CollectionViewLayout.CollectionViewItemType.CELL);
		key.setIdentifier(ELEMENT_KIND_CELL);
		return key;
	}

	protected String toStringExtra() {
		return String.format("Type = %s; Identifier = %s; IndexPath = %s", this.type, this.identifier, this.indexPath);
	}

	public int hashCode() {
		return ((this.indexPath.hashCode() + this.type.hashCode()) * 31) + this.identifier.hashCode();
	}

	public boolean equals(Object other) {
		if(other instanceof CollectionViewItemKey) {
		    CollectionViewItemKey otherItemKey = (CollectionViewItemKey)other;

		    if (this.type == otherItemKey.type && this.indexPath.equals(otherItemKey.indexPath)) {
				if(this.identifier != null && otherItemKey.identifier != null) {
					return this.identifier.equals(otherItemKey.identifier);
				} else {
					return this.identifier == null && otherItemKey.identifier == null;
				}
		    }
		}

		return false;
	}

	public CollectionViewItemKey copy() {
		CollectionViewItemKey itemKey = new CollectionViewItemKey();

		itemKey.setIndexPath(this.indexPath);
		itemKey.setType(this.type);
		itemKey.setIdentifier(this.identifier);

		return itemKey;
	}

	/* Setters & Getters */
	/* ========================================== */

	public CollectionViewLayout.CollectionViewItemType getType() {
		return this.type;
	}

	public void setType(CollectionViewLayout.CollectionViewItemType type) {
		this.type = type;
	}

	public mocha.foundation.IndexPath getIndexPath() {
		return this.indexPath;
	}

	public void setIndexPath(mocha.foundation.IndexPath indexPath) {
		this.indexPath = indexPath;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}

