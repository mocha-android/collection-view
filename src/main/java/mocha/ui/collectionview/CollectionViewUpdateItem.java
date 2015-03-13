package mocha.ui.collectionview;

import mocha.foundation.*;

class CollectionViewUpdateItem extends MObject implements mocha.foundation.Comparable <CollectionViewUpdateItem> {
	private mocha.foundation.IndexPath initialIndexPath;
	private mocha.foundation.IndexPath finalIndexPath;
	private Object gap;
	private CollectionViewUpdateItem.CollectionUpdateAction updateAction;

	public enum CollectionUpdateAction {
		INSERT,
		DELETE,
		RELOAD,
		MOVE,
		NONE
	}

	public CollectionViewUpdateItem(mocha.foundation.IndexPath initialIndexPath, mocha.foundation.IndexPath finalIndexPath, CollectionViewUpdateItem.CollectionUpdateAction updateAction) {
		this.initialIndexPath = initialIndexPath;
		this.finalIndexPath = finalIndexPath;
		this.updateAction = updateAction;
	}

	public CollectionViewUpdateItem(CollectionViewUpdateItem.CollectionUpdateAction updateAction, mocha.foundation.IndexPath indexPath) {
		this.updateAction = updateAction;

		if (updateAction == CollectionViewUpdateItem.CollectionUpdateAction.INSERT) {
			this.initialIndexPath = null;
			this.finalIndexPath = indexPath;
		} else if (updateAction == CollectionViewUpdateItem.CollectionUpdateAction.DELETE) {
			this.initialIndexPath = indexPath;
			this.finalIndexPath = null;
		} else if (updateAction == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD) {
			this.initialIndexPath = indexPath;
			this.finalIndexPath = indexPath;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public CollectionViewUpdateItem(mocha.foundation.IndexPath oldIndexPath, mocha.foundation.IndexPath newIndexPath) {
		this(oldIndexPath, newIndexPath, CollectionViewUpdateItem.CollectionUpdateAction.MOVE);
	}

	public CollectionViewUpdateItem.CollectionUpdateAction updateAction() {
		return this.updateAction;
	}

	public ComparisonResult compareTo(CollectionViewUpdateItem other) {
		mocha.foundation.IndexPath thisIndexPath = null;
		mocha.foundation.IndexPath otherIndexPath = null;

		switch (updateAction) {
			case INSERT:
				thisIndexPath = this.finalIndexPath;
				otherIndexPath = other.newIndexPath();
				break;
			case DELETE:
				thisIndexPath = this.initialIndexPath;
				otherIndexPath = other.indexPath();
			default: break;
		}

		if(thisIndexPath == null && otherIndexPath == null) {
			return ComparisonResult.SAME;
		} else if(thisIndexPath == null) {
			return ComparisonResult.DESCENDING;
		} else if(otherIndexPath == null) {
			return ComparisonResult.ASCENDING;
		}

		if(this.isSectionOperation()) {
			return ComparisonResult.compare(thisIndexPath.section, otherIndexPath.section);
		} else {
			return thisIndexPath.compareTo(otherIndexPath);
		}
	}

	public IndexPath indexPath() {
		//TODO: check this
		return this.initialIndexPath;
	}

	boolean isSectionOperation() {
		return this.initialIndexPath == null || this.initialIndexPath.item == -1 || this.finalIndexPath == null || this.finalIndexPath.item == -1;
	}

	protected String toStringExtra() {

		String action = null;
		switch (updateAction) {
		    case INSERT: action = "insert"; break;
		    case DELETE: action = "delete"; break;
		    case MOVE:   action = "move";   break;
		    case RELOAD: action = "reload"; break;
		    default: break;
		}

		return String.format("Index path before update (%s) index path after update (%s) action (%s).", initialIndexPath, finalIndexPath, action);
	}

	void setNewIndexPath(mocha.foundation.IndexPath indexPath) {
		this.finalIndexPath = indexPath;
	}

	void setGap(Object gap) {
		this.gap = gap;
	}

	mocha.foundation.IndexPath newIndexPath() {
		return this.finalIndexPath;
	}

	Object gap() {
		return this.gap;
	}

	public CollectionViewUpdateItem.CollectionUpdateAction action() {
		return this.updateAction;
	}

	public IndexPath indexPathBeforeUpdate() {
		return this.initialIndexPath;
	}

	public IndexPath indexPathAfterUpdate() {
		return this.finalIndexPath;
	}

}

