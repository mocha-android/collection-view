class CollectionViewUpdateItem extends MObject {
	private mocha.foundation.IndexPath _initialIndexPath;
	private mocha.foundation.IndexPath _finalIndexPath;
	private Object _gap;
	private CollectionViewUpdateItem.CollectionUpdateAction _updateAction;

	public enum CollectionUpdateAction {
		INSERT,
		DELETE,
		RELOAD,
		MOVE,
		NONE
	}

	public CollectionViewUpdateItem(mocha.foundation.IndexPath initialIndexPath, mocha.foundation.IndexPath finalIndexPath, CollectionViewUpdateItem.CollectionUpdateAction updateAction) {
		super.init();

		_initialIndexPath = initialIndexPath;
		_finalIndexPath = finalIndexPath;
		_updateAction = updateAction;
	}

	public CollectionViewUpdateItem(CollectionViewUpdateItem.CollectionUpdateAction updateAction, mocha.foundation.IndexPath indexPath) {
		if (updateAction == CollectionViewUpdateItem.CollectionUpdateAction.INSERT)
		    return this.initWithInitialIndexPathFinalIndexPathUpdateAction(null, indexPath, updateAction);
		else if (updateAction == CollectionViewUpdateItem.CollectionUpdateAction.DELETE)
		    return this.initWithInitialIndexPathFinalIndexPathUpdateAction(indexPath, null, updateAction);
		else if (updateAction == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD)
		    return this.initWithInitialIndexPathFinalIndexPathUpdateAction(indexPath, indexPath, updateAction);

		return null;
	}

	public CollectionViewUpdateItem(mocha.foundation.IndexPath oldIndexPath, mocha.foundation.IndexPath newIndexPath) {
		return this.initWithInitialIndexPathFinalIndexPathUpdateAction(oldIndexPath, newIndexPath, CollectionViewUpdateItem.CollectionUpdateAction.MOVE);
	}

	CollectionViewUpdateItem.CollectionUpdateAction updateAction() {

	}

	mocha.foundation.ComparisonResult compareIndexPaths(CollectionViewUpdateItem otherItem) {
		mocha.foundation.ComparisonResult result = mocha.foundation.OrderedSame;
		mocha.foundation.IndexPath thisIndexPath = null;
		mocha.foundation.IndexPath otherIndexPath = null;

		switch (_updateAction) {
		    case CollectionViewUpdateItem.CollectionUpdateAction.INSERT:
		        thisIndexPath = _finalIndexPath;
		        otherIndexPath = otherItem.newIndexPath();
		        break;
		    case CollectionViewUpdateItem.CollectionUpdateAction.DELETE:
		        thisIndexPath = _initialIndexPath;
		        otherIndexPath = otherItem.indexPath();
		    default: break;
		}

		if (this.getIsSectionOperation()) result = thisIndexPath.section.compare(otherIndexPath.section);
		else result = thisIndexPath.compare(otherIndexPath);
		return result;
	}

	mocha.foundation.ComparisonResult inverseCompareIndexPaths(CollectionViewUpdateItem otherItem) {
		return (mocha.foundation.ComparisonResult)(this.compareIndexPaths(otherItem) * -1);
	}

	Object indexPath() {
		//TODO: check this
		return _initialIndexPath;
	}

	boolean isSectionOperation() {
		return (_initialIndexPath.item == mocha.foundation.NotFound || _finalIndexPath.item == mocha.foundation.NotFound);
	}

	String description() {
		String action = null;
		switch (_updateAction) {
		    case CollectionViewUpdateItem.CollectionUpdateAction.INSERT: action = "insert"; break;
		    case CollectionViewUpdateItem.CollectionUpdateAction.DELETE: action = "delete"; break;
		    case CollectionViewUpdateItem.CollectionUpdateAction.MOVE:   action = "move";   break;
		    case CollectionViewUpdateItem.CollectionUpdateAction.RELOAD: action = "reload"; break;
		    default: break;
		}

		return String.format("Index path before update (%s) index path after update (%s) action (%s).",  _initialIndexPath, _finalIndexPath, action);
	}

	void setNewIndexPath(mocha.foundation.IndexPath indexPath) {
		_finalIndexPath = indexPath;
	}

	void setGap(Object gap) {
		_gap = gap;
	}

	mocha.foundation.IndexPath newIndexPath() {
		return _finalIndexPath;
	}

	Object gap() {
		return _gap;
	}

	CollectionViewUpdateItem.CollectionUpdateAction action() {
		return _updateAction;
	}

	/* Setters & Getters */
	/* ========================================== */

}

