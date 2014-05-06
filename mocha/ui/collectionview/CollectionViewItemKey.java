package mocha.ui.collectionview;

import mocha.foundation.*;
import mocha.foundation.Comparable;

class CollectionViewItemKey extends MObject implements Copying<CollectionViewItemKey>, Comparable<CollectionViewItemKey> {
	static final String ELEMENT_KIND_CELL = "ELEMENT_KIND_CELL";

	private final CollectionElementCategory type;
	private final mocha.foundation.IndexPath indexPath;
	private final String identifier;
	private int hashCode;

	public static CollectionViewItemKey collectionItemKeyForLayoutAttributes(CollectionViewLayoutAttributes layoutAttributes) {
		return new CollectionViewItemKey(layoutAttributes.getRepresentedElementCategory(), layoutAttributes.getIndexPath(), layoutAttributes.getRepresentedElementKind());
	}

	public static CollectionViewItemKey collectionItemKeyForCellWithIndexPath(mocha.foundation.IndexPath indexPath) {
		return new CollectionViewItemKey(CollectionElementCategory.CELL, indexPath, ELEMENT_KIND_CELL);
	}

	private CollectionViewItemKey(CollectionElementCategory type, IndexPath indexPath, String identifier) {
		this.type = type;
		this.indexPath = indexPath;
		this.identifier = identifier;
		this.hashCode = ((this.indexPath.hashCode() + this.type.hashCode()) * 31) + this.identifier.hashCode();
	}

	protected String toStringExtra() {
		return String.format("Type = %s; Identifier = %s; IndexPath = %s", this.type, this.identifier, this.indexPath);
	}

	public int hashCode() {
		return this.hashCode;
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
		return new CollectionViewItemKey(this.type, this.indexPath, this.identifier);
	}

	public CollectionViewItemKey copy(IndexPath newIndexPath) {
		return new CollectionViewItemKey(this.type, newIndexPath, this.identifier);
	}

	public CollectionElementCategory getType() {
		return this.type;
	}

	public IndexPath getIndexPath() {
		return this.indexPath;
	}

	public String getIdentifier() {
		return this.identifier;
	}

}

