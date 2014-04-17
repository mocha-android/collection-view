package mocha.ui.collectionview;

import mocha.foundation.*;

class CollectionViewItemKey extends MObject implements mocha.foundation.Copying<CollectionViewItemKey>, mocha.foundation.Comparable<CollectionViewItemKey> {
	static final String ELEMENT_KIND_CELL = "ELEMENT_KIND_CELL";

	private CollectionViewLayout.CollectionViewItemType type;
	private mocha.foundation.IndexPath indexPath;
	private String identifier;

	public static CollectionViewItemKey collectionItemKeyForLayoutAttributes(CollectionViewLayout.Attributes layoutAttributes) {
		CollectionViewItemKey key = new CollectionViewItemKey();
		key.indexPath = layoutAttributes.getIndexPath();
		key.type = layoutAttributes.getRepresentedElementCategory();
		key.identifier = layoutAttributes.getRepresentedElementKind();
		return key;
	}

	public static CollectionViewItemKey collectionItemKeyForCellWithIndexPath(mocha.foundation.IndexPath indexPath) {
		CollectionViewItemKey key = new CollectionViewItemKey();
		key.indexPath = indexPath;
		key.type = CollectionViewLayout.CollectionViewItemType.CELL;
		key.identifier = ELEMENT_KIND_CELL;
		return key;
	}

	protected String toStringExtra() {
		return String.format("Type = %s; Identifier = %s; IndexPath = %s", this.type, this.identifier, this.indexPath);
	}

	public int hashCode() {
		return ((this.indexPath.hashCode() + this.type.hashCode()) * 31) + this.identifier.hashCode();
	}

	public boolean equals(Object other) {
		boolean result = false;

		if(other instanceof CollectionViewItemKey) {
		    CollectionViewItemKey otherItemKey = (CollectionViewItemKey)other;

		    if (this.type == otherItemKey.type && this.indexPath.equals(otherItemKey.indexPath)) {
				if(this.identifier != null && otherItemKey.identifier != null) {
					result = this.identifier.equals(otherItemKey.identifier);
				} else {
					result = this.identifier == null && otherItemKey.identifier == null;
				}
		    }
		}

		// MLog("CV_TEST CollectionViewItemKey equals %s, %s = %s", this, other, result);

		return result;
	}

	public ComparisonResult compareTo(CollectionViewItemKey other) {
		return this.indexPath.compareTo(other.indexPath);
	}

	public CollectionViewItemKey copy() {
		CollectionViewItemKey itemKey = new CollectionViewItemKey();

		itemKey.indexPath = this.indexPath;
		itemKey.type = this.type;
		itemKey.identifier = this.identifier;

		return itemKey;
	}

	public CollectionViewItemKey copy(IndexPath newIndexPath) {
		CollectionViewItemKey itemKey = new CollectionViewItemKey();

		itemKey.indexPath = newIndexPath;
		itemKey.type = this.type;
		itemKey.identifier = this.identifier;

		return itemKey;
	}

	/* Setters & Getters */
	/* ========================================== */

	public CollectionViewLayout.CollectionViewItemType getType() {
		return this.type;
	}

	public IndexPath getIndexPath() {
		return this.indexPath;
	}

	public String getIdentifier() {
		return this.identifier;
	}

}

