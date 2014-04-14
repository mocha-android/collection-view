package mocha.ui.collectionview;

import com.android.internal.util.Predicate;
import mocha.foundation.MObject;
import mocha.ui.ScrollView;
import mocha.ui.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CollectionView extends mocha.ui.ScrollView implements ScrollView.Listener {
	private CollectionView.Delegate _delegate;
	private CollectionView.DataSource _dataSource;
	private mocha.ui.View _backgroundView;
	private boolean _allowsSelection;
	private boolean _allowsMultipleSelection;
	private CollectionViewLayout _layout;
	private HashSet _indexPathsForSelectedItems;
	private HashMap _cellReuseQueues;
	private HashMap _supplementaryViewReuseQueues;
	private HashMap _decorationViewReuseQueues;
	private HashSet _indexPathsForHighlightedItems;
	private int _reloadingSuspendedCount;
	private CollectionReusableView _firstResponderView;
	private mocha.ui.View _newContentView;
	private int _firstResponderViewType;
	private String _firstResponderViewKind;
	private mocha.foundation.IndexPath _firstResponderIndexPath;
	private HashMap _allVisibleViewsDict;
	private mocha.foundation.IndexPath _pendingSelectionIndexPath;
	private HashSet _pendingDeselectionIndexPaths;
	private CollectionViewData _collectionViewData;
	private Object _update;
	private mocha.graphics.Rect _visibleBoundRects;
	private mocha.graphics.Rect _preRotationBounds;
	private mocha.graphics.Point _rotationBoundsOffset;
	private int _rotationAnimationCount;
	private int _updateCount;
	private ArrayList _insertItems;
	private ArrayList _deleteItems;
	private ArrayList _reloadItems;
	private ArrayList _moveItems;
	private ArrayList _originalInsertItems;
	private ArrayList _originalDeleteItems;
	private mocha.ui.Touch _currentTouch;
	private boolean finished;
	private HashMap _cellClassDict;
	private HashMap _cellNibDict;
	private HashMap _supplementaryViewClassDict;
	private HashMap _supplementaryViewNibDict;
	private HashMap _cellNibExternalObjectsTables;
	private HashMap _supplementaryViewNibExternalObjectsTables;
	private mocha.graphics.Point _lastLayoutOffset;
	private Character filler;
	private CollectionViewFlagsStruct _collectionViewFlags = new CollectionViewFlagsStruct();
	private CollectionView.Ext _extVars;
	private static final Character P_ST_COLLETION_VIEW_EXT;

	interface FinishedBlock {

		void execute(boolean finished);

	}

	private class CollectionViewFlagsStruct {
		boolean delegateShouldHighlightItemAtIndexPath;
		boolean delegateDidHighlightItemAtIndexPath;
		boolean delegateDidUnhighlightItemAtIndexPath;
		boolean delegateShouldSelectItemAtIndexPath;
		boolean delegateShouldDeselectItemAtIndexPath;
		boolean delegateDidSelectItemAtIndexPath;
		boolean delegateDidDeselectItemAtIndexPath;
		boolean delegateSupportsMenus;
		boolean delegateDidEndDisplayingCell;
		boolean delegateDidEndDisplayingSupplementaryView;
		boolean dataSourceNumberOfSections;
		boolean dataSourceViewForSupplementaryElement;
		boolean reloadSkippedDuringSuspension;
		boolean scheduledUpdateVisibleCells;
		boolean scheduledUpdateVisibleCellLayoutAttributes;
		boolean allowsSelection;
		boolean allowsMultipleSelection;
		boolean updating;
		boolean fadeCellsForBoundsChange;
		boolean updatingLayout;
		boolean needsReload;
		boolean reloading;
		boolean skipLayoutDuringSnapshotting;
		boolean layoutInvalidatedSinceLastCellUpdate;
		boolean doneFirstLayout;

	}

	private static class Ext extends MObject {
		private CollectionView.Delegate _collectionViewDelegate;
		private CollectionViewLayout _nibLayout;
		private HashMap _nibCellsExternalObjects;
		private HashMap _supplementaryViewsExternalObjects;
		private mocha.foundation.IndexPath _touchingIndexPath;
		private mocha.foundation.IndexPath _currentIndexPath;

		/* Setters & Getters */
		/* ========================================== */

		private CollectionView.Delegate getCollectionViewDelegate() {
			return this._collectionViewDelegate;
		}

		private void setCollectionViewDelegate(CollectionView.Delegate collectionViewDelegate) {
			this._collectionViewDelegate = collectionViewDelegate;
		}

		private CollectionViewLayout getNibLayout() {
			return this._nibLayout;
		}

		private void setNibLayout(CollectionViewLayout nibLayout) {
			this._nibLayout = nibLayout;
		}

		private HashMap getNibCellsExternalObjects() {
			return this._nibCellsExternalObjects;
		}

		private void setNibCellsExternalObjects(HashMap nibCellsExternalObjects) {
			this._nibCellsExternalObjects = nibCellsExternalObjects;
		}

		private HashMap getSupplementaryViewsExternalObjects() {
			return this._supplementaryViewsExternalObjects;
		}

		private void setSupplementaryViewsExternalObjects(HashMap supplementaryViewsExternalObjects) {
			this._supplementaryViewsExternalObjects = supplementaryViewsExternalObjects;
		}

		private mocha.foundation.IndexPath getTouchingIndexPath() {
			return this._touchingIndexPath;
		}

		private void setTouchingIndexPath(mocha.foundation.IndexPath touchingIndexPath) {
			this._touchingIndexPath = touchingIndexPath;
		}

		private mocha.foundation.IndexPath getCurrentIndexPath() {
			return this._currentIndexPath;
		}

		private void setCurrentIndexPath(mocha.foundation.IndexPath currentIndexPath) {
			this._currentIndexPath = currentIndexPath;
		}

	}

	public interface DataSource extends mocha.foundation.OptionalInterface {

		int collectionViewNumberOfItemsInSection(CollectionView collectionView, int section);

		CollectionViewCell collectionViewCellForItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		int numberOfSectionsInCollectionView(CollectionView collectionView);

		@Optional
		CollectionReusableView collectionViewViewForSupplementaryElementOfKindAtIndexPath(CollectionView collectionView, String kind, mocha.foundation.IndexPath indexPath);

	}

	public interface Delegate extends Listener {

		@Optional
		boolean collectionViewShouldHighlightItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		void collectionViewDidHighlightItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		void collectionViewDidUnhighlightItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		boolean collectionViewShouldSelectItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		boolean collectionViewShouldDeselectItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		void collectionViewDidSelectItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		void collectionViewDidDeselectItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		void collectionViewDidEndDisplayingCellForItemAtIndexPath(CollectionView collectionView, CollectionViewCell cell, mocha.foundation.IndexPath indexPath);

		@Optional
		void collectionViewDidEndDisplayingSupplementaryViewForElementOfKindAtIndexPath(CollectionView collectionView, CollectionReusableView view, String elementKind, mocha.foundation.IndexPath indexPath);

		@Optional
		boolean collectionViewShouldShowMenuForItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath);

		@Optional
		boolean collectionViewCanPerformActionForItemAtIndexPathWithSender(CollectionView collectionView, SEL action, mocha.foundation.IndexPath indexPath, Object sender);

		@Optional
		void collectionViewPerformActionForItemAtIndexPathWithSender(CollectionView collectionView, SEL action, mocha.foundation.IndexPath indexPath, Object sender);

	}

	public enum CollectionElementCategory {
		CELL,
		SUPPLEMENTARY_VIEW,
		DECORATION_VIEW
	}

	public CollectionView(mocha.graphics.Rect frame, CollectionViewLayout layout) {
		super.initWithFrame(frame);

		// Set this as the mocha.ui.ScrollView's delegate
		super.setDelegate(this);
					
		CollectionView._this(this);
		this.setCollectionViewLayout(layout);
		_collectionViewData = new CollectionViewData(this, layout);
	}

	void registerClassForCellWithReuseIdentifier(Class cellClass, String identifier) {
		mocha.foundation.ParameterAssert(cellClass);
		mocha.foundation.ParameterAssert(identifier);
		_cellClassDict.put(identifier, cellClass);
	}

	void registerClassForSupplementaryViewOfKindWithReuseIdentifier(Class viewClass, String elementKind, String identifier) {
		mocha.foundation.ParameterAssert(viewClass);
		mocha.foundation.ParameterAssert(elementKind);
		mocha.foundation.ParameterAssert(identifier);
		String kindAndIdentifier = String.format("%s/%s", elementKind, identifier);
		_supplementaryViewClassDict.put(kindAndIdentifier, viewClass);
	}

	void registerNibForCellWithReuseIdentifier(mocha.ui.Nib nib, String identifier) {
		ArrayList topLevelObjects = nib.instantiateWithOwnerOptions(null, null);
		gma unused(topLevelObjects)
		mocha.foundation.Assert(topLevelObjects.size() == 1 && topLevelObjects.get(0).isKindOfClass(CollectionViewCell.getClass()), "must contain exactly 1 top level object which is a PSTCollectionViewCell");

		_cellNibDict.put(identifier, nib);
	}

	void registerNibForSupplementaryViewOfKindWithReuseIdentifier(mocha.ui.Nib nib, String kind, String identifier) {
		ArrayList topLevelObjects = nib.instantiateWithOwnerOptions(null, null);
		gma unused(topLevelObjects)
		mocha.foundation.Assert(topLevelObjects.size() == 1 && topLevelObjects.get(0).isKindOfClass(CollectionReusableView.getClass()), "must contain exactly 1 top level object which is a PSTCollectionReusableView");

		String kindAndIdentifier = String.format("%s/%s", kind, identifier);
		_supplementaryViewNibDict.put(kindAndIdentifier, nib);
	}

	Object dequeueReusableCellWithReuseIdentifierForIndexPath(String identifier, mocha.foundation.IndexPath indexPath) {
		// de-queue cell (if available)
		ArrayList reusableCells = _cellReuseQueues.get(identifier);
		CollectionViewCell cell = reusableCells.lastObject();
		CollectionViewLayout.Attributes attributes = this.getCollectionViewLayout().layoutAttributesForItemAtIndexPath(indexPath);

		if (cell) {
		    reusableCells.removeObjectAtIndex(reusableCells.size() - 1);
		}else {
		    if (_cellNibDict.get(identifier)) {
		        // Cell was registered via registerNib:forCellWithReuseIdentifier:
		        mocha.ui.Nib cellNib = _cellNibDict.get(identifier);
		        HashMap externalObjects = this.getExtVars().getNibCellsExternalObjects().get(identifier);
		        if (externalObjects) {
		            cell = cellNib.instantiateWithOwnerOptions(this, mocha.foundation.Maps.create(mocha.ui.NibExternalObjects, externalObjects)).get(0);
		        }else {
		            cell = cellNib.instantiateWithOwnerOptions(this, null).get(0);
		        }
		    }else {
		        Class cellClass = _cellClassDict.get(identifier);
		        // compatibility layer
		        Class collectionViewCellClass = mocha.foundation.ClassFromString("UICollectionViewCell");
		        if (collectionViewCellClass && cellClass.isEqual(collectionViewCellClass)) {
		            cellClass = CollectionViewCell.getClass();
		        }
		        if (cellClass == null) {
		            @throw Exception.exceptionWithNameReasonUserInfo(mocha.foundation.InvalidArgumentException, String.format("Class not registered for identifier %s", identifier), null);
		        }
		        if (attributes) {
		            cell = new cellClass(attributes.getFrame());
		        }else {
		            cell = new cellClass();
		        }
		    }
		    cell.setCollectionView(this);
		    cell.setReuseIdentifier(identifier);
		}

		cell.applyLayoutAttributes(attributes);

		return cell;
	}

	Object dequeueReusableSupplementaryViewOfKindWithReuseIdentifierForIndexPath(String elementKind, String identifier, mocha.foundation.IndexPath indexPath) {
		String kindAndIdentifier = String.format("%s/%s", elementKind, identifier);
		ArrayList reusableViews = _supplementaryViewReuseQueues.get(kindAndIdentifier);
		CollectionReusableView view = reusableViews.lastObject();
		if (view) {
		    reusableViews.removeObjectAtIndex(reusableViews.size() - 1);
		}else {
		    if (_supplementaryViewNibDict.get(kindAndIdentifier)) {
		        // supplementary view was registered via registerNib:forCellWithReuseIdentifier:
		        mocha.ui.Nib supplementaryViewNib = _supplementaryViewNibDict.get(kindAndIdentifier);
		        HashMap externalObjects = this.getExtVars().getSupplementaryViewsExternalObjects().get(kindAndIdentifier);
		        if (externalObjects) {
		            view = supplementaryViewNib.instantiateWithOwnerOptions(this, mocha.foundation.Maps.create(mocha.ui.NibExternalObjects, externalObjects)).get(0);
		        }else {
		            view = supplementaryViewNib.instantiateWithOwnerOptions(this, null).get(0);
		        }
		    }else {
		        Class viewClass = _supplementaryViewClassDict.get(kindAndIdentifier);
		        Class reusableViewClass = mocha.foundation.ClassFromString("UICollectionReusableView");
		        if (reusableViewClass && viewClass.isEqual(reusableViewClass)) {
		            viewClass = CollectionReusableView.getClass();
		        }
		        if (viewClass == null) {
		            @throw Exception.exceptionWithNameReasonUserInfo(mocha.foundation.InvalidArgumentException, String.format("Class not registered for kind/identifier %s", kindAndIdentifier), null);
		        }
		        if (this.getCollectionViewLayout()) {
		            CollectionViewLayout.Attributes attributes = this.getCollectionViewLayout().layoutAttributesForSupplementaryViewOfKindAtIndexPath(elementKind, indexPath);
		            if (attributes) {
		                view = new viewClass(attributes.getFrame());
		            }
		        }else {
		            view = new viewClass();
		        }
		    }
		    view.setCollectionView(this);
		    view.setReuseIdentifier(identifier);
		}

		return view;
	}

	ArrayList indexPathsForSelectedItems() {
		return _indexPathsForSelectedItems.allObjects();
	}

	void selectItemAtIndexPathAnimatedScrollPosition(mocha.foundation.IndexPath indexPath, boolean animated, PSTCollectionViewScrollPosition scrollPosition) {
		this.selectItemAtIndexPathAnimatedScrollPositionNotifyDelegate(indexPath, animated, scrollPosition, false);
	}

	void deselectItemAtIndexPathAnimated(mocha.foundation.IndexPath indexPath, boolean animated) {
		this.deselectItemAtIndexPathAnimatedNotifyDelegate(indexPath, animated, false);
	}

	void reloadData() {
		if (_reloadingSuspendedCount != 0) return;
		this.invalidateLayout();
		_allVisibleViewsDict.enumerateKeysAndObjectsUsingBlock(^(id key, id obj, boolean *stop) {
		    if (obj.isKindOfClass(mocha.ui.View.getClass())) {
		        obj.removeFromSuperview();
		    }
		});
		_allVisibleViewsDict.removeAllObjects();

		for (mocha.foundation.IndexPath indexPath in _ : dexPathsForSelectedItems) {
		    CollectionViewCell selectedCell = this.cellForItemAtIndexPath(indexPath);
		    selectedCell.setSelected(false);
		    selectedCell.setHighlighted(false);
		}
		_indexPathsForSelectedItems.removeAllObjects();
		_indexPathsForHighlightedItems.removeAllObjects();

		this.setNeedsLayout();
	}

	void setCollectionViewLayoutAnimated(CollectionViewLayout layout, boolean animated) {
		if (layout == _layout) return;

		// not sure it was it original code, but here this prevents crash
		// in case we switch layout before previous one was initially loaded
		if (mocha.graphics.RectIsEmpty(this.getBounds()) || !_collectionViewFlags.doneFirstLayout) {
		    _layout.setCollectionView(null);
		    _collectionViewData = new CollectionViewData(this, layout);
		    layout.setCollectionView(this);
		    _layout = layout;

		    // originally the use method
		    // _setNeedsVisibleCellsUpdate:withLayoutAttributes:
		    // here with CellsUpdate set to true and LayoutAttributes parameter set to NO
		    // inside this method probably some flags are set and finally
		    // setNeedsDisplay is called

		    _collectionViewFlags.scheduledUpdateVisibleCells = true;
		    _collectionViewFlags.scheduledUpdateVisibleCellLayoutAttributes = false;

		    this.setNeedsDisplay();
		}
		else {
		    layout.setCollectionView(this);
		    
		    _layout.setCollectionView(null);
		    _layout = layout;

		    _collectionViewData = new CollectionViewData(this, layout);
		    _collectionViewData.prepareToLoadData();

		    ArrayList previouslySelectedIndexPaths = this.indexPathsForSelectedItems();
		    HashSet selectedCellKeys = HashSet.setWithCapacity(previouslySelectedIndexPaths.size());

		    for (mocha.foundation.IndexPath indexPath  : previouslySelectedIndexPaths) {
		        selectedCellKeys.addObject(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPath));
		    }

		    ArrayList previouslyVisibleItemsKeys = _allVisibleViewsDict.allKeys();
		    HashSet previouslyVisibleItemsKeysSet = HashSet.setWithArray(previouslyVisibleItemsKeys);
		    HashSet previouslyVisibleItemsKeysSetMutable = HashSet.setWithArray(previouslyVisibleItemsKeys);

		    if (selectedCellKeys.intersectsSet(selectedCellKeys)) {
		        previouslyVisibleItemsKeysSetMutable.intersectSet(previouslyVisibleItemsKeysSetMutable);
		    }

		    this.bringSubviewToFront(_allVisibleViewsDict.get(previouslyVisibleItemsKeysSetMutable.anyObject()));

		    mocha.graphics.Point targetOffset = this.getContentOffset();
		    mocha.graphics.Point centerPoint = new mocha.graphics.Point(this.getBounds().getOrigin().x + this.getBounds().getSize().getWidth() / 2.f,
		            this.getBounds().getOrigin().y + this.getBounds().getSize().getHeight() / 2.f);
		    mocha.foundation.IndexPath centerItemIndexPath = this.indexPathForItemAtPoint(centerPoint);

		    if (!centerItemIndexPath) {
		        ArrayList visibleItems = this.indexPathsForVisibleItems();
		        if (visibleItems.size() > 0) {
		            centerItemIndexPath = visibleItemsvisibleItems.size()./ 2();
		        }
		    }

		    if (centerItemIndexPath) {
		        CollectionViewLayout.Attributes layoutAttributes = layout.layoutAttributesForItemAtIndexPath(centerItemIndexPath);
		        if (layoutAttributes) {
		            CollectionViewScrollPosition scrollPosition = CollectionViewScrollPositionCenteredVertically|CollectionViewScrollPositionCenteredHorizontally;
		            mocha.graphics.Rect targetRect = this.makeRectToScrollPosition(layoutAttributes.getFrame(), scrollPosition);
		            targetOffset = new mocha.graphics.Point(Math.max(0.f, targetRect.origin.x), Math.max(0.f, targetRect.origin.y));
		        }
		    }

		    mocha.graphics.Rect newlyBounds = new mocha.graphics.Rect(targetOffset.x, targetOffset.y, this.getBounds().getSize().getWidth(), this.getBounds().getSize().getHeight());
		    ArrayList newlyVisibleLayoutAttrs = _collectionViewData.layoutAttributesForElementsInRect(newlyBounds);

		    HashMap layoutInterchangeData = HashMap.dictionaryWithCapacity(newlyVisibleLayoutAttrs.size() + previouslyVisibleItemsKeysSet.size());

		    HashSet newlyVisibleItemsKeys = new HashSet();
		    for (CollectionViewLayout.Attributes attr  : newlyVisibleLayoutAttrs) {
		        CollectionViewItemKey newKey = CollectionViewItemKey.collectionItemKeyForLayoutAttributes(attr);
		        newlyVisibleItemsKeys.addObject(newKey);

		        CollectionViewLayout.Attributes prevAttr = null;
		        CollectionViewLayout.Attributes newAttr = null;

		        if (newKey.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            prevAttr = this.getCollectionViewLayout().layoutAttributesForDecorationViewOfKindAtIndexPath(attr.getRepresentedElementKind(), newKey.getIndexPath());
		            newAttr = layout.layoutAttributesForDecorationViewOfKindAtIndexPath(attr.getRepresentedElementKind(), newKey.getIndexPath());
		        }
		        else if (newKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            prevAttr = this.getCollectionViewLayout().layoutAttributesForItemAtIndexPath(newKey.getIndexPath());
		            newAttr = layout.layoutAttributesForItemAtIndexPath(newKey.getIndexPath());
		        }
		        else {
		            prevAttr = this.getCollectionViewLayout().layoutAttributesForSupplementaryViewOfKindAtIndexPath(attr.getRepresentedElementKind(), newKey.getIndexPath());
		            newAttr = layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(attr.getRepresentedElementKind(), newKey.getIndexPath());
		        }

		        if (prevAttr != null && newAttr != null) {
		            layoutInterchangeData.put(newKey, mocha.foundation.Maps.create("previousLayoutInfos", prevAttr, "newLayoutInfos", newAttr));
		        }
		    }

		    for (CollectionViewItemKey key  : previouslyVisibleItemsKeysSet) {
		        CollectionViewLayout.Attributes prevAttr = null;
		        CollectionViewLayout.Attributes newAttr = null;

		        if (key.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            CollectionReusableView decorView = _allVisibleViewsDict.get(key);
		            prevAttr = this.getCollectionViewLayout().layoutAttributesForDecorationViewOfKindAtIndexPath(decorView.getReuseIdentifier(), key.getIndexPath());
		            newAttr = layout.layoutAttributesForDecorationViewOfKindAtIndexPath(decorView.getReuseIdentifier(), key.getIndexPath());
		        }
		        else if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            prevAttr = this.getCollectionViewLayout().layoutAttributesForItemAtIndexPath(key.getIndexPath());
		            newAttr = layout.layoutAttributesForItemAtIndexPath(key.getIndexPath());
		        }
		        else if (key.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		            CollectionReusableView suuplView = _allVisibleViewsDict.get(key);
		            prevAttr = this.getCollectionViewLayout().layoutAttributesForSupplementaryViewOfKindAtIndexPath(suuplView.getLayoutAttributes().getRepresentedElementKind(), key.getIndexPath());
		            newAttr = layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(suuplView.getLayoutAttributes().getRepresentedElementKind(), key.getIndexPath());
		        }

		        HashMap layoutInterchangeDataValue = new HashMap();
		        if (prevAttr) layoutInterchangeDataValue.put("previousLayoutInfos", prevAttr);
		        if (newAttr) layoutInterchangeDataValue.put("newLayoutInfos", newAttr);
		        layoutInterchangeData.put(key, layoutInterchangeDataValue);
		    }

		    for (CollectionViewItemKey key  : layoutInterchangeData.keyEnumerator()) {
		        if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            CollectionViewCell cell = _allVisibleViewsDict.get(key);

		            if (!cell) {
		                cell = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(key.getIndexPath(), layoutInterchangeData.get(key).get("previousLayoutInfos"));
		                _allVisibleViewsDict.put(key, cell);
		                this.addControlledSubview(cell);
		            }
		            else cell.applyLayoutAttributes(layoutInterchangeData.get(key).get("previousLayoutInfos"));
		        }
		        else if (key.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		            CollectionReusableView view = _allVisibleViewsDict.get(key);
		            if (!view) {
		                CollectionViewLayout.Attributes attrs = layoutInterchangeData.get(key).get("previousLayoutInfos");
		                view = this.createPreparedSupplementaryViewForElementOfKindAtIndexPathWithLayoutAttributes(attrs.getRepresentedElementKind(), attrs.getIndexPath(), attrs);
		                _allVisibleViewsDict.put(key, view);
		                this.addControlledSubview(view);
		            }
		        }
		        else if (key.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            CollectionReusableView view = _allVisibleViewsDict.get(key);
		            if (!view) {
		                CollectionViewLayout.Attributes attrs = layoutInterchangeData.get(key).get("previousLayoutInfos");
		                view = this.dequeueReusableOrCreateDecorationViewOfKindForIndexPath(attrs.getRepresentedElementKind(), attrs.getIndexPath());
		                _allVisibleViewsDict.put(key, view);
		                this.addControlledSubview(view);
		            }
		        }
		    };

		    mocha.graphics.Rect contentRect = _collectionViewData.collectionViewContentRect();

		    void (^applyNewLayoutBlock)(void) = new Runnable { public void run() {
		        mocha.foundation.Enumerator keys = layoutInterchangeData.keyEnumerator();
		        for (CollectionViewItemKey key  : keys) {
		            // TODO: This is most likely not 100% the same time as in mocha.ui.CollectionView. Needs to be investigated.
		            CollectionViewCell cell = (CollectionViewCell)_allVisibleViewsDict.get(key);
		            cell.willTransitionFromLayoutToLayout(_layout, layout);
		            cell.applyLayoutAttributes(layoutInterchangeData.get(key).get("newLayoutInfos"));
		            cell.didTransitionFromLayoutToLayout(_layout, layout);
		        }
		    } };

		    void (^freeUnusedViews)(void) = new Runnable { public void run() {
		        HashSet toRemove = new HashSet();
		        for (CollectionViewItemKey key in _allVisibleViewsDict.keyEnumerator()) {
		            if (!newlyVisibleItemsKeys.conta : sObject(key)) {
		                if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		                    this.reuseCell(_allVisibleViewsDict.get(key));
		                    toRemove.addObject(key);
		                }
		                else if (key.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		                    this.reuseSupplementaryView(_allVisibleViewsDict.get(key));
		                    toRemove.addObject(key);
		                }
		                else if (key.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		                    this.reuseDecorationView(_allVisibleViewsDict.get(key));
		                    toRemove.addObject(key);
		                }
		            }
		        }

		        for (id key in toRemove)
		            _allVisibleViewsDict.removeObjectForKey(key);
		    } };

		    if (animated) {
		        mocha.ui.View.animateWithDurationAnimationsCompletion(.3, new Runnable { public void run() {
		            _collectionViewFlags.updatingLayout = true;
		            this.setContentOffset(targetOffset);
		            this.setContentSize(contentRect.size);
		            applyNewLayoutBlock();
		        } }, ^(boolean finished) {
		            freeUnusedViews();
		            _collectionViewFlags.updatingLayout = false;

		            // layout subviews for updating content offset or size while updating layout
		            if (!this.getContentOffset().equals(targetOffset)
		                    || !this.getContentSize().equals(contentRect.size)) {
		                this.layoutSubviews();
		            }
		        });
		    }
		    else {
		        this.setContentOffset(targetOffset);
		        this.setContentSize(contentRect.size);
		        applyNewLayoutBlock();
		        freeUnusedViews();
		    }
		}
	}

	int numberOfSections() {
		return _collectionViewData.numberOfSections();
	}

	int numberOfItemsInSection(int section) {
		return _collectionViewData.numberOfItemsInSection(section);
	}

	CollectionViewLayout.Attributes layoutAttributesForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		return this.collectionViewLayout().layoutAttributesForItemAtIndexPath(indexPath);
	}

	CollectionViewLayout.Attributes layoutAttributesForSupplementaryElementOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath) {
		return this.collectionViewLayout().layoutAttributesForSupplementaryViewOfKindAtIndexPath(kind, indexPath);
	}

	mocha.foundation.IndexPath indexPathForItemAtPoint(mocha.graphics.Point point) {
		CollectionViewLayout.Attributes attributes = this.getCollectionViewLayout().layoutAttributesForElementsInRect(new mocha.graphics.Rect(point.x, point.y, 1, 1)).lastObject();
		return attributes.getIndexPath();
	}

	mocha.foundation.IndexPath indexPathForCell(CollectionViewCell cell) {
		__block mocha.foundation.IndexPath indexPath = null;
		_allVisibleViewsDict.enumerateKeysAndObjectsWithOptionsUsingBlock(kNilOptions, ^(id key, id obj, boolean *stop) {
		    CollectionViewItemKey itemKey = (CollectionViewItemKey)key;
		    if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		        CollectionViewCell currentCell = (CollectionViewCell)obj;
		        if (currentCell == cell) {
		            indexPath = itemKey.getIndexPath();
		            *stop = true;
		        }
		    }
		});
		return indexPath;
	}

	CollectionViewCell cellForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		// int index = _collectionViewData.globalIndexForItemAtIndexPath(indexPath);
		// TODO Apple uses some kind of globalIndex for this.
		__block CollectionViewCell cell = null;
		_allVisibleViewsDict.enumerateKeysAndObjectsWithOptionsUsingBlock(0, ^(id key, id obj, boolean *stop) {
		    CollectionViewItemKey itemKey = (CollectionViewItemKey)key;
		    if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		        if (itemKey.getIndexPath().isEqual(indexPath)) {
		            cell = obj;
		            *stop = true;
		        }
		    }
		});
		return cell;
	}

	ArrayList visibleCells() {
		return _allVisibleViewsDict.allValues().filteredArrayUsingPredicate(Predicate.predicateWithBlock(^boolean(id evaluatedObject, HashMap *bindings) {
		    return evaluatedObject.isKindOfClass(CollectionViewCell.getClass()) && mocha.graphics.RectIntersectsRect(this.getBounds(), evaluatedObject.frame());
		}));
	}

	ArrayList indexPathsForVisibleItems() {
		ArrayList visibleCells = this.getVisibleCells();
		ArrayList indexPaths = ArrayList.arrayWithCapacity(visibleCells.size());

		visibleCells.enumerateObjectsUsingBlock(^(id obj, int idx, boolean *stop) {
		    CollectionViewCell cell = (CollectionViewCell)obj;
		    indexPaths.addObject(cell.getLayoutAttributes().getIndexPath());
		});

		return indexPaths;
	}

	void scrollToItemAtIndexPathAtScrollPositionAnimated(mocha.foundation.IndexPath indexPath, PSTCollectionViewScrollPosition scrollPosition, boolean animated) {
		// Ensure grid is laid out; else we can't scroll.
		this.layoutSubviews();

		CollectionViewLayout.Attributes layoutAttributes = this.getCollectionViewLayout().layoutAttributesForItemAtIndexPath(indexPath);
		if (layoutAttributes) {
		    mocha.graphics.Rect targetRect = this.makeRectToScrollPosition(layoutAttributes.getFrame(), scrollPosition);
		    this.scrollRectToVisibleAnimated(targetRect, animated);
		}
	}

	void insertSections(mocha.foundation.IndexSet sections) {
		this.updateSectionsUpdateAction(sections, CollectionViewUpdateItem.CollectionUpdateAction.INSERT);
	}

	void deleteSections(mocha.foundation.IndexSet sections) {
		// First delete all items
		ArrayList paths = new ArrayList();
		sections.enumerateIndexesUsingBlock(^(int idx, boolean *stop) {
		    for (int i = 0; i < this.numberOfItemsInSection(idx); ++i) {
		        paths.addObject(mocha.foundation.IndexPath.indexPathForItemInSection(i, idx));
		    }
		});
		this.deleteItemsAtIndexPaths(paths);
		// Then delete the section.
		this.updateSectionsUpdateAction(sections, CollectionViewUpdateItem.CollectionUpdateAction.DELETE);
	}

	void reloadSections(mocha.foundation.IndexSet sections) {
		this.updateSectionsUpdateAction(sections, CollectionViewUpdateItem.CollectionUpdateAction.RELOAD);
	}

	void moveSectionToSection(int section, int newSection) {
		ArrayList moveUpdateItems = this.arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction.MOVE);
		moveUpdateItems.addObject(new CollectionViewUpdateItem(mocha.foundation.IndexPath.indexPathForItemInSection(mocha.foundation.NotFound, section), mocha.foundation.IndexPath.indexPathForItemInSection(mocha.foundation.NotFound, newSection), CollectionViewUpdateItem.CollectionUpdateAction.MOVE));
		if (!_collectionViewFlags.updating) {
		    this.setupCellAnimations();
		    this.endItemAnimations();
		}
	}

	void insertItemsAtIndexPaths(ArrayList indexPaths) {
		this.updateRowsAtIndexPathsUpdateAction(indexPaths, CollectionViewUpdateItem.CollectionUpdateAction.INSERT);
	}

	void deleteItemsAtIndexPaths(ArrayList indexPaths) {
		this.updateRowsAtIndexPathsUpdateAction(indexPaths, CollectionViewUpdateItem.CollectionUpdateAction.DELETE);
	}

	void reloadItemsAtIndexPaths(ArrayList indexPaths) {
		this.updateRowsAtIndexPathsUpdateAction(indexPaths, CollectionViewUpdateItem.CollectionUpdateAction.RELOAD);
	}

	void moveItemAtIndexPathToIndexPath(mocha.foundation.IndexPath indexPath, mocha.foundation.IndexPath newIndexPath) {
		ArrayList moveUpdateItems = this.arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction.MOVE);
		moveUpdateItems.addObject(new CollectionViewUpdateItem(indexPath, newIndexPath, CollectionViewUpdateItem.CollectionUpdateAction.MOVE));
		if (!_collectionViewFlags.updating) {
		    this.setupCellAnimations();
		    this.endItemAnimations();
		}
	}

	void performBatchUpdatesCompletion(Runnable updates, FinishedBlock completion) {
		this.setupCellAnimations();

		if (updates) updates();
		if (completion) _updateCompletionHandler = completion;

		this.endItemAnimations();
	}

	Object currentUpdate() {

	}

	HashMap visibleViewsDict() {

	}

	CollectionViewData collectionViewData() {

	}

	mocha.graphics.Rect visibleBoundRects() {
		// in original mocha.ui.CollectionView implementation they
		// check for _visibleBounds and can union this.getBounds()
		// with this value. Don't know the meaning of _visibleBounds however.
		return this.getBounds();
	}

	public CollectionView(mocha.graphics.Rect frame) {
		this(frame, null);
	}

	protected String toStringExtra() {
		return String.format("%s; collection view layout: %s", super.toStringExtra(), this.getCollectionViewLayout());
	}

	public void layoutSubviews() {
		super.layoutSubviews();

		// Adding alpha animation to make the relayouting smooth
		if (_collectionViewFlags.fadeCellsForBoundsChange) {
		    CATransition transition = CATransition.animation();
		    transition.setDuration(0.25f * CollectionView.void());
		    transition.setTimingFunction(CAMediaTimingFunction.functionWithName(kCAMediaTimingFunctionEaseInEaseOut));
		    transition.setType(kCATransitionFade);
		    this.getLayer().addAnimationForKey(transition, "rotationAnimation");
		}

		_collectionViewData.validateLayoutInRect(this.getBounds());

		// update cells
		if (_collectionViewFlags.fadeCellsForBoundsChange) {
		    CATransaction.begin();
		    CATransaction.setDisableActions(true);
		}

		if (!_collectionViewFlags.updatingLayout)
		    this.updateVisibleCellsNow(true);

		if (_collectionViewFlags.fadeCellsForBoundsChange) {
		    CATransaction.commit();
		}

		// do we need to update contentSize?
		mocha.graphics.Size contentSize = _collectionViewData.collectionViewContentRect().getSize();
		if (!this.getContentSize().equals(contentSize)) {
		    this.setContentSize(contentSize);

		    // if contentSize is different, we need to re-evaluate layout, bounds (contentOffset) might changed
		    _collectionViewData.validateLayoutInRect(this.getBounds());
		    this.updateVisibleCellsNow(true);
		}

		if (_backgroundView) {
		    _backgroundView.setFrame(new mocha.graphics.Rect(this.getContentOffset(), this.getBounds().size));
		}

		_collectionViewFlags.fadeCellsForBoundsChange = false;
		_collectionViewFlags.doneFirstLayout = true;
	}

	public void setFrame(mocha.graphics.Rect frame) {
		if (!frame.equals(this.getFrame())) {
		    mocha.graphics.Rect bounds = new mocha.graphics.Rect(this.getContentOffset(), frame.size);
		    boolean shouldInvalidate = this.getCollectionViewLayout().shouldInvalidateLayoutForBoundsChange(bounds);
		    super.setFrame(frame);
		    if (shouldInvalidate) {
		        this.invalidateLayout();
		        _collectionViewFlags.fadeCellsForBoundsChange = true;
		    }
		}
	}

	public void setBounds(mocha.graphics.Rect bounds) {
		if (!bounds.equals(this.getBounds())) {
		    boolean shouldInvalidate = this.getCollectionViewLayout().shouldInvalidateLayoutForBoundsChange(bounds);
		    super.setBounds(bounds);
		    if (shouldInvalidate) {
		        this.invalidateLayout();
		        _collectionViewFlags.fadeCellsForBoundsChange = true;
		    }
		}
	}

	@mocha.foundation.RuntimeMethod
	public void didScroll(ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewDidScroll")) {
		    delegate.scrollViewDidScroll(scrollView);
		}
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewDidZoom(mocha.ui.ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewDidZoom")) {
		    delegate.scrollViewDidZoom(scrollView);
		}
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewWillBeginDragging(mocha.ui.ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewWillBeginDragging")) {
		    delegate.scrollViewWillBeginDragging(scrollView);
		}
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewWillEndDraggingWithVelocityTargetContentOffset(mocha.ui.ScrollView scrollView, mocha.graphics.Point velocity, inout mocha.graphics.Point targetContentOffset) {
		// Let collectionViewLayout decide where to stop.
		*targetContentOffset = this.collectionViewLayout().targetContentOffsetForProposedContentOffsetWithScrollingVelocity(*targetContentOffset, velocity);

		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewWillEndDragging")) {
		    //if collectionViewDelegate implements this method, it may modify targetContentOffset as well
		    delegate.scrollViewWillEndDraggingWithVelocityTargetContentOffset(scrollView, velocity, targetContentOffset);
		}
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewDidEndDraggingWillDecelerate(mocha.ui.ScrollView scrollView, boolean decelerate) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewDidEndDragging")) {
		    delegate.scrollViewDidEndDraggingWillDecelerate(scrollView, decelerate);
		}

		// if we are in the middle of a cell touch event, perform the "touchEnded" simulation
		if (this.getExtVars().getTouchingIndexPath()) {
		    this.cellTouchCancelled();
		}
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewWillBeginDecelerating(mocha.ui.ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewWillBeginDecelerating")) {
		    delegate.scrollViewWillBeginDecelerating(scrollView);
		}
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewDidEndDecelerating(mocha.ui.ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewDidEndDecelerating")) {
		    delegate.scrollViewDidEndDecelerating(scrollView);
		}
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewDidEndScrollingAnimation(mocha.ui.ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewDidEndScrollingAnimation")) {
		    delegate.scrollViewDidEndScrollingAnimation(scrollView);
		}
	}

	@mocha.foundation.RuntimeMethod
	public mocha.ui.View viewForZoomingInScrollView(mocha.ui.ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("viewForZoomingInScrollView")) {
		    return delegate.viewForZoomingInScrollView(scrollView);
		}
		return null;
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewWillBeginZoomingWithView(mocha.ui.ScrollView scrollView, mocha.ui.View view) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewWillBeginZooming")) {
		    delegate.scrollViewWillBeginZoomingWithView(scrollView, view);
		}
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewDidEndZoomingWithViewAtScale(mocha.ui.ScrollView scrollView, mocha.ui.View view, float scale) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewDidEndZooming")) {
		    delegate.scrollViewDidEndZoomingWithViewAtScale(scrollView, view, scale);
		}
	}

	@mocha.foundation.RuntimeMethod
	public boolean scrollViewShouldScrollToTop(mocha.ui.ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewShouldScrollToTop")) {
		    return delegate.scrollViewShouldScrollToTop(scrollView);
		}
		return true;
	}

	@mocha.foundation.RuntimeMethod
	public void scrollViewDidScrollToTop(mocha.ui.ScrollView scrollView) {
		CollectionView.Delegate delegate = this.getExtVars().getCollectionView.Delegate();
		if ((id)delegate != this && delegate.respondsToSelector("scrollViewDidScrollToTop")) {
		    delegate.scrollViewDidScrollToTop(scrollView);
		}
	}

	Object dequeueReusableOrCreateDecorationViewOfKindForIndexPath(String elementKind, mocha.foundation.IndexPath indexPath) {
		ArrayList reusableViews = _decorationViewReuseQueues.get(elementKind);
		CollectionReusableView view = reusableViews.lastObject();
		CollectionViewLayout collectionViewLayout = this.getCollectionViewLayout();
		CollectionViewLayout.Attributes attributes = collectionViewLayout.layoutAttributesForDecorationViewOfKindAtIndexPath(elementKind, indexPath);

		if (view) {
		    reusableViews.removeObjectAtIndex(reusableViews.size() - 1);
		}else {
		    HashMap decorationViewNibDict = collectionViewLayout.getDecorationViewNibDict();
		    HashMap decorationViewExternalObjects = collectionViewLayout.getDecorationViewExternalObjectsTables();
		    if (decorationViewNibDict.get(elementKind)) {
		        // supplementary view was registered via registerNib:forCellWithReuseIdentifier:
		        mocha.ui.Nib supplementaryViewNib = decorationViewNibDict.get(elementKind);
		        HashMap externalObjects = decorationViewExternalObjects.get(elementKind);
		        if (externalObjects) {
		            view = supplementaryViewNib.instantiateWithOwnerOptions(this, mocha.foundation.Maps.create(mocha.ui.NibExternalObjects, externalObjects)).get(0);
		        }else {
		            view = supplementaryViewNib.instantiateWithOwnerOptions(this, null).get(0);
		        }
		    }else {
		        HashMap decorationViewClassDict = collectionViewLayout.getDecorationViewClassDict();
		        Class viewClass = decorationViewClassDict.get(elementKind);
		        Class reusableViewClass = mocha.foundation.ClassFromString("UICollectionReusableView");
		        if (reusableViewClass && viewClass.isEqual(reusableViewClass)) {
		            viewClass = CollectionReusableView.getClass();
		        }
		        if (viewClass == null) {
		            @throw Exception.exceptionWithNameReasonUserInfo(mocha.foundation.InvalidArgumentException, String.format("Class not registered for identifier %s", elementKind), null);
		        }
		        if (attributes) {
		            view = new viewClass(attributes.getFrame());
		        }else {
		            view = new viewClass();
		        }
		    }
		    view.setCollectionView(this);
		    view.setReuseIdentifier(elementKind);
		}

		view.applyLayoutAttributes(attributes);

		return view;
	}

	ArrayList allCells() {
		return _allVisibleViewsDict.allValues().filteredArrayUsingPredicate(Predicate.predicateWithBlock(^boolean(id evaluatedObject, HashMap *bindings) {
		    return evaluatedObject.isKindOfClass(CollectionViewCell.getClass());
		}));
	}

	mocha.graphics.Rect makeRectToScrollPosition(mocha.graphics.Rect targetRect, PSTCollectionViewScrollPosition scrollPosition) {
		// split parameters
		int verticalPosition = scrollPosition&0x07; // 0000 0111
		int horizontalPosition = scrollPosition&0x38; // 0011 1000

		if (verticalPosition != CollectionViewScrollPositionNone
		        && verticalPosition != CollectionViewScrollPositionTop
		        && verticalPosition != CollectionViewScrollPositionCenteredVertically
		        && verticalPosition != CollectionViewScrollPositionBottom) {
		    @throw Exception.exceptionWithNameReasonUserInfo(mocha.foundation.InvalidArgumentException, "PSTCollectionViewScrollPosition: attempt to use a scroll position with multiple vertical positioning styles", null);
		}

		if (horizontalPosition != CollectionViewScrollPositionNone
		        && horizontalPosition != CollectionViewScrollPositionLeft
		        && horizontalPosition != CollectionViewScrollPositionCenteredHorizontally
		        && horizontalPosition != CollectionViewScrollPositionRight) {
		    @throw Exception.exceptionWithNameReasonUserInfo(mocha.foundation.InvalidArgumentException, "PSTCollectionViewScrollPosition: attempt to use a scroll position with multiple horizontal positioning styles", null);
		}

		mocha.graphics.Rect frame = this.getLayer().getBounds();
		float calculateX;
		float calculateY;

		switch (verticalPosition) {
		    case CollectionViewScrollPositionCenteredVertically:
		        calculateY = Math.max(targetRect.origin.y - ((frame.size.height / 2) - (targetRect.size.height / 2)), -this.getContentInset().getTop());
		        targetRect = new mocha.graphics.Rect(targetRect.origin.x, calculateY, targetRect.size.width, frame.size.height);
		        break;
		    case CollectionViewScrollPositionTop:
		        targetRect = new mocha.graphics.Rect(targetRect.origin.x, targetRect.origin.y, targetRect.size.width, frame.size.height);
		        break;

		    case CollectionViewScrollPositionBottom:
		        calculateY = Math.max(targetRect.origin.y - (frame.size.height - targetRect.size.height), -this.getContentInset().getTop());
		        targetRect = new mocha.graphics.Rect(targetRect.origin.x, calculateY, targetRect.size.width, frame.size.height);
		        break;
		}

		switch (horizontalPosition) {
		    case CollectionViewScrollPositionCenteredHorizontally:
		        calculateX = targetRect.origin.x - ((frame.size.width / 2) - (targetRect.size.width / 2));
		        targetRect = new mocha.graphics.Rect(calculateX, targetRect.origin.y, frame.size.width, targetRect.size.height);
		        break;

		    case CollectionViewScrollPositionLeft:
		        targetRect = new mocha.graphics.Rect(targetRect.origin.x, targetRect.origin.y, frame.size.width, targetRect.size.height);
		        break;

		    case CollectionViewScrollPositionRight:
		        calculateX = targetRect.origin.x - (frame.size.width - targetRect.size.width);
		        targetRect = new mocha.graphics.Rect(calculateX, targetRect.origin.y, frame.size.width, targetRect.size.height);
		        break;
		}

		return targetRect;
	}

	void touchesBeganWithEvent(HashSet touches, mocha.ui.Event event) {
		super.touchesBeganWithEvent(touches, event);

		// reset touching state vars
		this.getExtVars().setTouchingIndexPath(null);
		this.getExtVars().setCurrentIndexPath(null);

		mocha.graphics.Point touchPoint = touches.anyObject().locationInView(this);
		mocha.foundation.IndexPath indexPath = this.indexPathForItemAtPoint(touchPoint);
		if (indexPath && this.getAllowsSelection()) {
		    if (!this.highlightItemAtIndexPathAnimatedScrollPositionNotifyDelegate(indexPath, true, CollectionViewScrollPositionNone, true))
		        return;

		    this.getExtVars().setTouchingIndexPath(indexPath);
		    this.getExtVars().setCurrentIndexPath(indexPath);

		    if (!this.getAllowsMultipleSelection()) {
		        // temporally unhighlight background on touchesBegan (keeps selected by _indexPathsForSelectedItems)
		        // single-select only mode only though
		        mocha.foundation.IndexPath tempDeselectIndexPath = _indexPathsForSelectedItems.getAnyObject();
		        if (tempDeselectIndexPath && !tempDeselectIndexPath.isEqual(this.getExtVars().getTouchingIndexPath())) {
		            // iOS6 mocha.ui.CollectionView deselects cell without notification
		            CollectionViewCell selectedCell = this.cellForItemAtIndexPath(tempDeselectIndexPath);
		            selectedCell.setSelected(false);
		        }
		    }
		}
	}

	void touchesMovedWithEvent(HashSet touches, mocha.ui.Event event) {
		super.touchesMovedWithEvent(touches, event);

		// allows moving between highlight and unhighlight state only if setHighlighted is not overwritten
		if (this.getExtVars().getTouchingIndexPath()) {
		    mocha.graphics.Point touchPoint = touches.anyObject().locationInView(this);
		    mocha.foundation.IndexPath indexPath = this.indexPathForItemAtPoint(touchPoint);

		    // moving out of bounds
		    if (this.getExtVars().getCurrentIndexPath().isEqual(this.getExtVars().getTouchingIndexPath()) &&
		            !indexPath.isEqual(this.getExtVars().getTouchingIndexPath()) &&
		            this.unhighlightItemAtIndexPathAnimatedNotifyDelegateShouldCheckHighlight(this.getExtVars().getTouchingIndexPath(), true, true, true)) {
		        this.getExtVars().setCurrentIndexPath(indexPath);
		        // moving back into the original touching cell
		    }else if (!this.getExtVars().getCurrentIndexPath().isEqual(this.getExtVars().getTouchingIndexPath()) &&
		            indexPath.isEqual(this.getExtVars().getTouchingIndexPath())) {
		        this.highlightItemAtIndexPathAnimatedScrollPositionNotifyDelegate(this.getExtVars().getTouchingIndexPath(), true, CollectionViewScrollPositionNone, true);
		        this.getExtVars().setCurrentIndexPath(this.getExtVars().getTouchingIndexPath());
		    }
		}
	}

	void touchesEndedWithEvent(HashSet touches, mocha.ui.Event event) {
		super.touchesEndedWithEvent(touches, event);

		if (this.getExtVars().getTouchingIndexPath()) {
		    // first unhighlight the touch operation
		    this.unhighlightItemAtIndexPathAnimatedNotifyDelegate(this.getExtVars().getTouchingIndexPath(), true, true);

		    mocha.graphics.Point touchPoint = touches.anyObject().locationInView(this);
		    mocha.foundation.IndexPath indexPath = this.indexPathForItemAtPoint(touchPoint);
		    if (indexPath.isEqual(this.getExtVars().getTouchingIndexPath())) {
		        this.userSelectedItemAtIndexPath(indexPath);
		    }
		    else if (!this.getAllowsMultipleSelection()) {
		        mocha.foundation.IndexPath tempDeselectIndexPath = _indexPathsForSelectedItems.getAnyObject();
		        if (tempDeselectIndexPath && !tempDeselectIndexPath.isEqual(this.getExtVars().getTouchingIndexPath())) {
		            this.cellTouchCancelled();
		        }
		    }

		    // for pedantic reasons only - always set to null on touchesBegan
		    this.getExtVars().setTouchingIndexPath(null);
		    this.getExtVars().setCurrentIndexPath(null);
		}
	}

	void touchesCancelledWithEvent(HashSet touches, mocha.ui.Event event) {
		super.touchesCancelledWithEvent(touches, event);

		// do not mark touchingIndexPath as null because whoever cancelled this touch will need to signal a touch up event later
		if (this.getExtVars().getTouchingIndexPath()) {
		    // first unhighlight the touch operation
		    this.unhighlightItemAtIndexPathAnimatedNotifyDelegate(this.getExtVars().getTouchingIndexPath(), true, true);
		}
	}

	void cellTouchCancelled() {
		// turn on ALL the *should be selected* cells (iOS6 mocha.ui.CollectionView does no state keeping or other fancy optimizations)
		// there should be no notifications as this is a silent "turn everything back on"
		for (mocha.foundation.IndexPath tempDeselectedIndexPath in _ : dexPathsForSelectedItems.copy()) {
		    CollectionViewCell selectedCell = this.cellForItemAtIndexPath(tempDeselectedIndexPath);
		    selectedCell.setSelected(true);
		}
	}

	void userSelectedItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		if (this.getAllowsMultipleSelection() && _indexPathsForSelectedItems.containsObject(indexPath)) {
		    this.deselectItemAtIndexPathAnimatedNotifyDelegate(indexPath, true, true);
		}
		else if (this.getAllowsSelection()) {
		    this.selectItemAtIndexPathAnimatedScrollPositionNotifyDelegate(indexPath, true, CollectionViewScrollPositionNone, true);
		}
	}

	void selectItemAtIndexPathAnimatedScrollPositionNotifyDelegate(mocha.foundation.IndexPath indexPath, boolean animated, PSTCollectionViewScrollPosition scrollPosition, boolean notifyDelegate) {
		if (this.getAllowsMultipleSelection() && _indexPathsForSelectedItems.containsObject(indexPath)) {
		    boolean shouldDeselect = true;
		    if (notifyDelegate && _collectionViewFlags.delegateShouldDeselectItemAtIndexPath) {
		        shouldDeselect = this.getDelegate().collectionViewShouldDeselectItemAtIndexPath(this, indexPath);
		    }

		    if (shouldDeselect) {
		        this.deselectItemAtIndexPathAnimatedNotifyDelegate(indexPath, animated, notifyDelegate);
		    }
		}
		else {
		    // either single selection, or wasn't already selected in multiple selection mode
		    boolean shouldSelect = true;
		    if (notifyDelegate && _collectionViewFlags.delegateShouldSelectItemAtIndexPath) {
		        shouldSelect = this.getDelegate().collectionViewShouldSelectItemAtIndexPath(this, indexPath);
		    }

		    if (!this.getAllowsMultipleSelection()) {
		        // now unselect the previously selected cell for single selection
		        mocha.foundation.IndexPath tempDeselectIndexPath = _indexPathsForSelectedItems.getAnyObject();
		        if (tempDeselectIndexPath && !tempDeselectIndexPath.isEqual(indexPath)) {
		            this.deselectItemAtIndexPathAnimatedNotifyDelegate(tempDeselectIndexPath, true, true);
		        }
		    }

		    if (shouldSelect) {
		        CollectionViewCell selectedCell = this.cellForItemAtIndexPath(indexPath);
		        selectedCell.setSelected(true);

		        _indexPathsForSelectedItems.addObject(indexPath);

		        selectedCell.performSelectionSegue();

		        if (scrollPosition != CollectionViewScrollPositionNone) {
		            this.scrollToItemAtIndexPathAtScrollPositionAnimated(indexPath, scrollPosition, animated);
		        }

		        if (notifyDelegate && _collectionViewFlags.delegateDidSelectItemAtIndexPath) {
		            this.getDelegate().collectionViewDidSelectItemAtIndexPath(this, indexPath);
		        }
		    }
		}
	}

	void deselectItemAtIndexPathAnimatedNotifyDelegate(mocha.foundation.IndexPath indexPath, boolean animated, boolean notifyDelegate) {
		boolean shouldDeselect = true;
		// deselect only relevant during multi mode
		if (this.getAllowsMultipleSelection() && notifyDelegate && _collectionViewFlags.delegateShouldDeselectItemAtIndexPath) {
		    shouldDeselect = this.getDelegate().collectionViewShouldDeselectItemAtIndexPath(this, indexPath);
		}

		if (shouldDeselect && _indexPathsForSelectedItems.containsObject(indexPath)) {
		    CollectionViewCell selectedCell = this.cellForItemAtIndexPath(indexPath);
		    if (selectedCell) {
		        if (selectedCell.getSelected()) {
		            selectedCell.setSelected(false);
		        }
		    }
		    _indexPathsForSelectedItems.removeObject(indexPath);

		    if (notifyDelegate && _collectionViewFlags.delegateDidDeselectItemAtIndexPath) {
		        this.getDelegate().collectionViewDidDeselectItemAtIndexPath(this, indexPath);
		    }
		}
	}

	boolean highlightItemAtIndexPathAnimatedScrollPositionNotifyDelegate(mocha.foundation.IndexPath indexPath, boolean animated, PSTCollectionViewScrollPosition scrollPosition, boolean notifyDelegate) {
		boolean shouldHighlight = true;
		if (notifyDelegate && _collectionViewFlags.delegateShouldHighlightItemAtIndexPath) {
		    shouldHighlight = this.getDelegate().collectionViewShouldHighlightItemAtIndexPath(this, indexPath);
		}

		if (shouldHighlight) {
		    CollectionViewCell highlightedCell = this.cellForItemAtIndexPath(indexPath);
		    highlightedCell.setHighlighted(true);
		    _indexPathsForHighlightedItems.addObject(indexPath);

		    if (scrollPosition != CollectionViewScrollPositionNone) {
		        this.scrollToItemAtIndexPathAtScrollPositionAnimated(indexPath, scrollPosition, animated);
		    }

		    if (notifyDelegate && _collectionViewFlags.delegateDidHighlightItemAtIndexPath) {
		        this.getDelegate().collectionViewDidHighlightItemAtIndexPath(this, indexPath);
		    }
		}
		return shouldHighlight;
	}

	boolean unhighlightItemAtIndexPathAnimatedNotifyDelegate(mocha.foundation.IndexPath indexPath, boolean animated, boolean notifyDelegate) {
		return this.unhighlightItemAtIndexPathAnimatedNotifyDelegateShouldCheckHighlight(indexPath, animated, notifyDelegate, false);
	}

	boolean unhighlightItemAtIndexPathAnimatedNotifyDelegateShouldCheckHighlight(mocha.foundation.IndexPath indexPath, boolean animated, boolean notifyDelegate, boolean check) {
		if (_indexPathsForHighlightedItems.containsObject(indexPath)) {
		    CollectionViewCell highlightedCell = this.cellForItemAtIndexPath(indexPath);
		    // iOS6 does not notify any delegate if the cell was never highlighted (setHighlighted overwritten) during touchMoved
		    if (check && !highlightedCell.getHighlighted()) {
		        return false;
		    }

		    // if multiple selection or not unhighlighting a selected item we don't perform any op
		    if (highlightedCell.getHighlighted() && _indexPathsForSelectedItems.containsObject(indexPath)) {
		        highlightedCell.setHighlighted(true);
		    }else {
		        highlightedCell.setHighlighted(false);
		    }

		    _indexPathsForHighlightedItems.removeObject(indexPath);

		    if (notifyDelegate && _collectionViewFlags.delegateDidUnhighlightItemAtIndexPath) {
		        this.getDelegate().collectionViewDidUnhighlightItemAtIndexPath(this, indexPath);
		    }

		    return true;
		}
		return false;
	}

	void setBackgroundView(mocha.ui.View backgroundView) {
		if (backgroundView != _backgroundView) {
		    _backgroundView.removeFromSuperview();
		    _backgroundView = backgroundView;
		    backgroundView.setFrame(new mocha.graphics.Rect(this.getContentOffset(), this.getBounds().size));
		    backgroundView.setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		    this.addSubview(backgroundView);
		    this.sendSubviewToBack(backgroundView);
		}
	}

	void setCollectionViewLayout(CollectionViewLayout layout) {
		this.setCollectionViewLayoutAnimated(layout, false);
	}

	id<PSTCollectionViewDelegate> delegate() {
		return this.getExtVars().getCollectionView.Delegate();
	}

	void setDelegate(id<PSTCollectionViewDelegate> delegate) {
		this.getExtVars().setCollectionView.Delegate(delegate);

		//  Managing the Selected Cells
		_collectionViewFlags.delegateShouldSelectItemAtIndexPath = this.getDelegate().respondsToSelector("collectionView");
		_collectionViewFlags.delegateDidSelectItemAtIndexPath = this.getDelegate().respondsToSelector("collectionView");
		_collectionViewFlags.delegateShouldDeselectItemAtIndexPath = this.getDelegate().respondsToSelector("collectionView");
		_collectionViewFlags.delegateDidDeselectItemAtIndexPath = this.getDelegate().respondsToSelector("collectionView");

		//  Managing Cell Highlighting
		_collectionViewFlags.delegateShouldHighlightItemAtIndexPath = this.getDelegate().respondsToSelector("collectionView");
		_collectionViewFlags.delegateDidHighlightItemAtIndexPath = this.getDelegate().respondsToSelector("collectionView");
		_collectionViewFlags.delegateDidUnhighlightItemAtIndexPath = this.getDelegate().respondsToSelector("collectionView");

		//  Tracking the Removal of Views
		_collectionViewFlags.delegateDidEndDisplayingCell = this.getDelegate().respondsToSelector("collectionView");
		_collectionViewFlags.delegateDidEndDisplayingSupplementaryView = this.getDelegate().respondsToSelector("collectionView");

		//  Managing Actions for Cells
		_collectionViewFlags.delegateSupportsMenus = this.getDelegate().respondsToSelector("collectionView");

		// These aren't present in the flags which is a little strange. Not adding them because that will mess with byte alignment which will affect cross compatibility.
		// The flag names are guesses and are there for documentation purposes.
		// _collectionViewFlags.delegateCanPerformActionForItemAtIndexPath = this.getDelegate().respondsToSelector("collectionView");
		// _collectionViewFlags.delegatePerformActionForItemAtIndexPath    = this.getDelegate().respondsToSelector("collectionView");
	}

	void setDataSource(id<PSTCollectionViewDataSource> dataSource) {
		if (dataSource != _dataSource) {
		    _dataSource = dataSource;

		    //  Getting Item and Section Metrics
		    _collectionViewFlags.dataSourceNumberOfSections = _dataSource.respondsToSelector("numberOfSectionsInCollectionView");

		    //  Getting Views for Items
		    _collectionViewFlags.dataSourceViewForSupplementaryElement = _dataSource.respondsToSelector("collectionView");
		}
	}

	boolean allowsSelection() {
		return _collectionViewFlags.allowsSelection;
	}

	void setAllowsSelection(boolean allowsSelection) {
		_collectionViewFlags.allowsSelection = allowsSelection;
	}

	boolean allowsMultipleSelection() {
		return _collectionViewFlags.allowsMultipleSelection;
	}

	void setAllowsMultipleSelection(boolean allowsMultipleSelection) {
		_collectionViewFlags.allowsMultipleSelection = allowsMultipleSelection;

		// Deselect all objects if allows multiple selection is false
		if (!allowsMultipleSelection && _indexPathsForSelectedItems.size()) {

		    // Note: Apple's implementation leaves a mostly random item selected. Presumably they
		    //       have a good reason for this, but I guess it's just skipping the last or first index.
		    for (mocha.foundation.IndexPath selectedIndexPath in _ : dexPathsForSelectedItems.copy()) {
		        if (_indexPathsForSelectedItems.size() == 1) continue;
		        this.deselectItemAtIndexPathAnimatedNotifyDelegate(selectedIndexPath, true, true);
		    }
		}
	}

	CollectionView.Ext extVars() {
		return objc_getAssociatedObject(this, &CollectionView.P_ST_COLLETION_VIEW_EXT);
	}

	void invalidateLayout() {
		this.getCollectionViewLayout().invalidateLayout();
		this.getCollectionViewData().invalidate(); // invalidate layout cache
	}

	void updateVisibleCellsNow(boolean now) {
		ArrayList layoutAttributesArray = _collectionViewData.layoutAttributesForElementsInRect(this.getBounds());

		if (layoutAttributesArray == null || layoutAttributesArray.size() == 0) {
		    // If our layout source isn't providing any layout information, we should just
		    // stop, otherwise we'll blow away all the currently existing cells.
		    return;
		}

		// create ItemKey/Attributes dictionary
		HashMap itemKeysToAddDict = new HashMap();

		// Add new cells.
		for (CollectionViewLayout.Attributes layoutAttributes  : layoutAttributesArray) {
		    CollectionViewItemKey itemKey = CollectionViewItemKey.collectionItemKeyForLayoutAttributes(layoutAttributes);
		    itemKeysToAddDict.put(itemKey, layoutAttributes);

		    // check if cell is in visible dict; add it if not.
		    CollectionReusableView view = _allVisibleViewsDict.get(itemKey);
		    if (!view) {
		        if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            view = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(itemKey.getIndexPath(), layoutAttributes);

		        }else if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		            view = this.createPreparedSupplementaryViewForElementOfKindAtIndexPathWithLayoutAttributes(layoutAttributes.getRepresentedElementKind(), layoutAttributes.getIndexPath(), layoutAttributes);
		        }else if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            view = this.dequeueReusableOrCreateDecorationViewOfKindForIndexPath(layoutAttributes.getRepresentedElementKind(), layoutAttributes.getIndexPath());
		        }

		        // Supplementary views are optional
		        if (view) {
		            _allVisibleViewsDict.put(itemKey, view);
		            this.addControlledSubview(view);

		            // Always apply attributes. Fixes #203.
		            view.applyLayoutAttributes(layoutAttributes);
		        }
		    }else {
		        // just update cell
		        view.applyLayoutAttributes(layoutAttributes);
		    }
		}

		// Detect what items should be removed and queued back.
		HashSet allVisibleItemKeys = HashSet.setWithArray(_allVisibleViewsDict.allKeys());
		allVisibleItemKeys.minusSet(HashSet.setWithArray(itemKeysToAddDict.allKeys()));

		// Finally remove views that have not been processed and prepare them for re-use.
		for (CollectionViewItemKey itemKey  : allVisibleItemKeys) {
		    CollectionReusableView reusableView = _allVisibleViewsDict.get(itemKey);
		    if (reusableView) {
		        reusableView.removeFromSuperview();
		        _allVisibleViewsDict.removeObjectForKey(itemKey);
		        if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            if (_collectionViewFlags.delegateDidEndDisplayingCell) {
		                this.getDelegate().collectionViewDidEndDisplayingCellForItemAtIndexPath(this, (CollectionViewCell)reusableView, itemKey.getIndexPath());
		            }
		            this.reuseCell((CollectionViewCell)reusableView);
		        }
		        else if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		            if (_collectionViewFlags.delegateDidEndDisplayingSupplementaryView) {
		                this.getDelegate().collectionViewDidEndDisplayingSupplementaryViewForElementOfKindAtIndexPath(this, reusableView, itemKey.getIdentifier(), itemKey.getIndexPath());
		            }
		            this.reuseSupplementaryView(reusableView);
		        }
		        else if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            this.reuseDecorationView(reusableView);
		        }
		    }
		}
	}

	CollectionViewCell createPreparedCellForItemAtIndexPathWithLayoutAttributes(mocha.foundation.IndexPath indexPath, CollectionViewLayout.Attributes layoutAttributes) {
		CollectionViewCell cell = this.getDataSource().collectionViewCellForItemAtIndexPath(this, indexPath);

		// Apply attributes
		cell.applyLayoutAttributes(layoutAttributes);

		// reset selected/highlight state
		cell.setHighlighted(_indexPathsForHighlightedItems.containsObject(indexPath));
		cell.setSelected(_indexPathsForSelectedItems.containsObject(indexPath));

		// voiceover support
		cell.setIsAccessibilityElement(true);

		return cell;
	}

	CollectionReusableView createPreparedSupplementaryViewForElementOfKindAtIndexPathWithLayoutAttributes(String kind, mocha.foundation.IndexPath indexPath, CollectionViewLayout.Attributes layoutAttributes) {
		if (_collectionViewFlags.dataSourceViewForSupplementaryElement) {
		    CollectionReusableView view = this.getDataSource().collectionViewViewForSupplementaryElementOfKindAtIndexPath(this, kind, indexPath);
		    view.applyLayoutAttributes(layoutAttributes);
		    return view;
		}
		return null;
	}

	void queueReusableViewInQueueWithIdentifier(CollectionReusableView reusableView, HashMap queue, String identifier) {
		mocha.foundation.ParameterAssert(identifier.length() > 0);

		reusableView.removeFromSuperview();
		reusableView.prepareForReuse();

		// enqueue cell
		ArrayList reuseableViews = queue.get(identifier);
		if (!reuseableViews) {
		    reuseableViews = new ArrayList();
		    queue.put(identifier, reuseableViews);
		}
		reuseableViews.addObject(reusableView);
	}

	void reuseCell(CollectionViewCell cell) {
		this.queueReusableViewInQueueWithIdentifier(cell, _cellReuseQueues, cell.getReuseIdentifier());
	}

	void reuseSupplementaryView(CollectionReusableView supplementaryView) {
		String kindAndIdentifier = String.format("%s/%s", supplementaryView.getLayoutAttributes().getElementKind(), supplementaryView.getReuseIdentifier());
		this.queueReusableViewInQueueWithIdentifier(supplementaryView, _supplementaryViewReuseQueues, kindAndIdentifier);
	}

	void reuseDecorationView(CollectionReusableView decorationView) {
		this.queueReusableViewInQueueWithIdentifier(decorationView, _decorationViewReuseQueues, decorationView.getReuseIdentifier());
	}

	void addControlledSubview(CollectionReusableView subview) {
		// avoids placing views above the scroll indicator
		// If the collection view is not displaying scrollIndicators then this.getSubviews().size() can be 0.
		// We take the max to ensure we insert at a non negative index because a negative index will silently fail to insert the view
		int insertionIndex = Math.max((int)(this.getSubviews().size() - (this.getDragging() ? 1 : 0)), 0);
		this.insertSubviewAtIndex(subview, insertionIndex);
		mocha.ui.View scrollIndicatorView = null;
		if (this.getDragging()) {
		    scrollIndicatorView = this.getSubviews().lastObject();
		}

		ArrayList floatingViews = new ArrayList();
		for (mocha.ui.View uiView in this.getSubviews()) {
		    if (uiView.isK : dOfClass(CollectionReusableView.getClass()) && (CollectionReusableView)uiView.layoutAttributes().zIndex() > 0) {
		        floatingViews.addObject(uiView);
		    }
		}

		floatingViews.sortUsingComparator(^mocha.foundation.ComparisonResult(CollectionReusableView *obj1, CollectionReusableView *obj2) {
		    float z1 = obj1.layoutAttributes().zIndex();
		    float z2 = obj2.layoutAttributes().zIndex();
		    if (z1 > z2) {
		        return (mocha.foundation.ComparisonResult)mocha.foundation.OrderedDescending;
		    }else if (z1 < z2) {
		        return (mocha.foundation.ComparisonResult)mocha.foundation.OrderedAscending;
		    }else {
		        return (mocha.foundation.ComparisonResult)mocha.foundation.OrderedSame;
		    }
		});

		for (CollectionReusableView uiView in float : gViews) {
		    this.bringSubviewToFront(uiView);
		}

		if (floatingViews.size() && scrollIndicatorView) {
		    this.bringSubviewToFront(scrollIndicatorView);
		}
	}

	void suspendReloads() {
		_reloadingSuspendedCount++;
	}

	void resumeReloads() {
		if (0 < _reloadingSuspendedCount) _reloadingSuspendedCount--;
	}

	ArrayList arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction updateAction) {
		ArrayList updateActions = null;

		switch (updateAction) {
		    case CollectionViewUpdateItem.CollectionUpdateAction.INSERT:
		        if (!_insertItems) _insertItems = new ArrayList();
		        updateActions = _insertItems;
		        break;
		    case CollectionViewUpdateItem.CollectionUpdateAction.DELETE:
		        if (!_deleteItems) _deleteItems = new ArrayList();
		        updateActions = _deleteItems;
		        break;
		    case CollectionViewUpdateItem.CollectionUpdateAction.MOVE:
		        if (!_moveItems) _moveItems = new ArrayList();
		        updateActions = _moveItems;
		        break;
		    case CollectionViewUpdateItem.CollectionUpdateAction.RELOAD:
		        if (!_reloadItems) _reloadItems = new ArrayList();
		        updateActions = _reloadItems;
		        break;
		    default: break;
		}
		return updateActions;
	}

	void prepareLayoutForUpdates() {
		ArrayList array = new ArrayList();
		array.addObjectsFromArray(_originalDeleteItems.sortedArrayUsingSelector("inverseCompareIndexPaths"));
		array.addObjectsFromArray(_originalInsertItems.sortedArrayUsingSelector("compareIndexPaths"));
		array.addObjectsFromArray(_reloadItems.sortedArrayUsingSelector("compareIndexPaths"));
		array.addObjectsFromArray(_moveItems.sortedArrayUsingSelector("compareIndexPaths"));
		_layout.prepareForCollectionViewUpdates(array);
	}

	void updateWithItems(ArrayList items) {
		this.prepareLayoutForUpdates();

		ArrayList animations = new ArrayList();
		HashMap newAllVisibleView = new HashMap();

		HashMap viewsToRemove = HashMap.dictionaryWithObjectsAndKeys(new ArrayList(), CollectionViewLayout.CollectionViewItemType.CELL,
		        new ArrayList(), CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW,
		        new ArrayList(), CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW, null);

		for (CollectionViewUpdateItem updateItem  : items) {
		    if (updateItem.getIsSectionOperation() && updateItem.getUpdateAction() != CollectionViewUpdateItem.CollectionUpdateAction.DELETE) continue;
		    if (updateItem.getIsSectionOperation() && updateItem.getUpdateAction() == CollectionViewUpdateItem.CollectionUpdateAction.DELETE) {
		        int numberOfBeforeSection = _update.get("oldModel").numberOfItemsInSection(updateItem.getIndexPathBeforeUpdate().getSection());
		        for (int i = 0; i < numberOfBeforeSection; i++) {
		            mocha.foundation.IndexPath indexPath = mocha.foundation.IndexPath.indexPathForItemInSection(i, updateItem.getIndexPathBeforeUpdate().getSection());

		            CollectionViewLayout.Attributes finalAttrs = _layout.finalLayoutAttributesForDisappearingItemAtIndexPath(indexPath);
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPath);
		            CollectionReusableView view = _allVisibleViewsDict.get(key);
		            if (view) {
		                CollectionViewLayout.Attributes startAttrs = view.getLayoutAttributes();

		                if (!finalAttrs) {
		                    finalAttrs = startAttrs.copy();
		                    finalAttrs.setAlpha(0);
		                }
		                animations.addObject(mocha.foundation.Maps.create("view", view, "previousLayoutInfos", startAttrs, "newLayoutInfos", finalAttrs));

		                _allVisibleViewsDict.removeObjectForKey(key);

		                (ArrayList)viewsToRemove.get(key.getType()).addObject(view);

		            }
		        }
		        continue;
		    }

		    if (updateItem.getUpdateAction() == CollectionViewUpdateItem.CollectionUpdateAction.DELETE) {
		        mocha.foundation.IndexPath indexPath = updateItem.getIndexPathBeforeUpdate();

		        CollectionViewLayout.Attributes finalAttrs = _layout.finalLayoutAttributesForDisappearingItemAtIndexPath(indexPath);
		        CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPath);
		        CollectionReusableView view = _allVisibleViewsDict.get(key);
		        if (view) {
		            CollectionViewLayout.Attributes startAttrs = view.getLayoutAttributes();

		            if (!finalAttrs) {
		                finalAttrs = startAttrs.copy();
		                finalAttrs.setAlpha(0);
		            }
		            animations.addObject(mocha.foundation.Maps.create("view", view, "previousLayoutInfos", startAttrs, "newLayoutInfos", finalAttrs));

		            _allVisibleViewsDict.removeObjectForKey(key);

		            (ArrayList)viewsToRemove.get(key.getType()).addObject(view);

		        }
		    }
		    else if (updateItem.getUpdateAction() == CollectionViewUpdateItem.CollectionUpdateAction.INSERT) {
		        mocha.foundation.IndexPath indexPath = updateItem.getIndexPathAfterUpdate();
		        CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPath);
		        CollectionViewLayout.Attributes startAttrs = _layout.initialLayoutAttributesForAppearingItemAtIndexPath(indexPath);
		        CollectionViewLayout.Attributes finalAttrs = _layout.layoutAttributesForItemAtIndexPath(indexPath);

		        mocha.graphics.Rect startRect = startAttrs.getFrame();
		        mocha.graphics.Rect finalRect = finalAttrs.getFrame();

		        if (mocha.graphics.RectIntersectsRect(this.getVisibleBoundRects(), startRect) || mocha.graphics.RectIntersectsRect(this.getVisibleBoundRects(), finalRect)) {

		            if (!startAttrs) {
		                startAttrs = finalAttrs.copy();
		                startAttrs.setAlpha(0);
		            }

		            CollectionReusableView view = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(indexPath, startAttrs);
		            this.addControlledSubview(view);

		            newAllVisibleView.put(key, view);
		            animations.addObject(mocha.foundation.Maps.create("view", view, "previousLayoutInfos", startAttrs, "newLayoutInfos", finalAttrs));
		        }
		    }
		    else if (updateItem.getUpdateAction() == CollectionViewUpdateItem.CollectionUpdateAction.MOVE) {
		        mocha.foundation.IndexPath indexPathBefore = updateItem.getIndexPathBeforeUpdate();
		        mocha.foundation.IndexPath indexPathAfter = updateItem.getIndexPathAfterUpdate();

		        CollectionViewItemKey keyBefore = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPathBefore);
		        CollectionViewItemKey keyAfter = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPathAfter);
		        CollectionReusableView view = _allVisibleViewsDict.get(keyBefore);

		        CollectionViewLayout.Attributes startAttrs = null;
		        CollectionViewLayout.Attributes finalAttrs = _layout.layoutAttributesForItemAtIndexPath(indexPathAfter);

		        if (view) {
		            startAttrs = view.getLayoutAttributes();
		            _allVisibleViewsDict.removeObjectForKey(keyBefore);
		            newAllVisibleView.put(keyAfter, view);
		        }
		        else {
		            startAttrs = finalAttrs.copy();
		            startAttrs.setAlpha(0);
		            view = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(indexPathAfter, startAttrs);
		            this.addControlledSubview(view);
		            newAllVisibleView.put(keyAfter, view);
		        }

		        animations.addObject(mocha.foundation.Maps.create("view", view, "previousLayoutInfos", startAttrs, "newLayoutInfos", finalAttrs));
		    }
		}

		for (CollectionViewItemKey key  : _allVisibleViewsDict.keyEnumerator()) {
		    CollectionReusableView view = _allVisibleViewsDict.get(key);

		    if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		        int oldGlobalIndex = _update.get("oldModel").globalIndexForItemAtIndexPath(key.getIndexPath());
		        ArrayList oldToNewIndexMap = _update.get("oldToNewIndexMap");
		        int newGlobalIndex = mocha.foundation.NotFound;
		        if (mocha.foundation.NotFound != oldGlobalIndex && oldGlobalIndex < oldToNewIndexMap.size()) {
		            newGlobalIndex = oldToNewIndexMap.get(oldGlobalIndex).intValue();
		        }
		        mocha.foundation.IndexPath newIndexPath = newGlobalIndex == mocha.foundation.NotFound ? null : _update.get("newModel").indexPathForItemAtGlobalIndex(newGlobalIndex);
		        mocha.foundation.IndexPath oldIndexPath = oldGlobalIndex == mocha.foundation.NotFound ? null : _update.get("oldModel").indexPathForItemAtGlobalIndex(oldGlobalIndex);

		        if (newIndexPath) {
		            CollectionViewLayout.Attributes startAttrs = null;
		            CollectionViewLayout.Attributes finalAttrs = null;

		            startAttrs = _layout.initialLayoutAttributesForAppearingItemAtIndexPath(oldIndexPath);
		            finalAttrs = _layout.layoutAttributesForItemAtIndexPath(newIndexPath);

		            HashMap dic = HashMap.dictionaryWithDictionary(mocha.foundation.Maps.create("view", view));
		            if (startAttrs) dic.put("previousLayoutInfos", startAttrs);
		            if (finalAttrs) dic.put("newLayoutInfos", finalAttrs);

		            animations.addObject(dic);
		            CollectionViewItemKey newKey = key.copy();
		            newKey.setIndexPath(newIndexPath);
		            newAllVisibleView.put(newKey, view);

		        }
		    }else if (key.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		        CollectionViewLayout.Attributes startAttrs = null;
		        CollectionViewLayout.Attributes finalAttrs = null;

		        startAttrs = view.getLayoutAttributes();
		        finalAttrs = _layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(view.getLayoutAttributes().getRepresentedElementKind(), key.getIndexPath());

		        HashMap dic = HashMap.dictionaryWithDictionary(mocha.foundation.Maps.create("view", view));
		        if (startAttrs) dic.put("previousLayoutInfos", startAttrs);
		        if (finalAttrs) dic.put("newLayoutInfos", finalAttrs);

		        animations.addObject(dic);
		        CollectionViewItemKey newKey = key.copy();
		        newAllVisibleView.put(newKey, view);

		    }
		}
		ArrayList allNewlyVisibleItems = _layout.layoutAttributesForElementsInRect(this.getVisibleBoundRects());
		for (CollectionViewLayout.Attributes attrs  : allNewlyVisibleItems) {
		    CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForLayoutAttributes(attrs);

		    if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL && !newAllVisibleView.allKeys().containsObject(key)) {
		        CollectionViewLayout.Attributes startAttrs =
		                _layout.initialLayoutAttributesForAppearingItemAtIndexPath(attrs.getIndexPath());

		        CollectionReusableView view = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(attrs.getIndexPath(), startAttrs);
		        this.addControlledSubview(view);
		        newAllVisibleView.put(key, view);

		        animations.addObject(mocha.foundation.Maps.create("view", view, "previousLayoutInfos", startAttrs ? startAttrs : attrs, "newLayoutInfos", attrs));
		    }
		}

		_allVisibleViewsDict = newAllVisibleView;

		for (HashMap animation  : animations) {
		    CollectionReusableView view = animation.get("view");
		    CollectionViewLayout.Attributes attr = animation.get("previousLayoutInfos");
		    view.applyLayoutAttributes(attr);
		};

		mocha.ui.View.animateWithDurationAnimationsCompletion(.3, new Runnable { public void run() {
		    _collectionViewFlags.updatingLayout = true;

		    CATransaction.begin();
		    CATransaction.setAnimationDuration(.3);

		    // You might wonder why we use CATransaction to handle animation completion
		    // here instead of using the completion: parameter of mocha.ui.View's animateWithDuration:.
		    // The problem is that animateWithDuration: calls this completion block
		    // when other animations are finished. This means that the block is called
		    // after the user releases his finger and the scroll view has finished scrolling.
		    // This can be a large delay, which causes the layout of the cells to be greatly
		    // delayed, and thus, be unrendered. I assume that was done for performance
		    // purposes but it completely breaks our layout logic here.
		    // To get the completion block called immediately after the animation actually
		    // finishes, I switched to use CATransaction.
		    // The only thing I'm not sure about - _completed_ flag. I don't know where to get it
		    // in terms of CATransaction's API, so I use animateWithDuration's completion block
		    // to call _updateCompletionHandler with that flag.
		    // Ideally, _updateCompletionHandler should be called along with the other logic in
		    // CATransaction's completionHandler but I simply don't know where to get that flag.
		    CATransaction.setCompletionBlock(new Runnable { public void run() {
		        // Iterate through all the views that we are going to remove.
		        viewsToRemove.enumerateKeysAndObjectsUsingBlock(^(Number *keyObj, ArrayList *views, boolean *stop) {
		            CollectionViewLayout.CollectionViewItemType type = keyObj.intIntegerValue();
		            for (CollectionReusableView view  : views) {
		                if (type == CollectionViewLayout.CollectionViewItemType.CELL) {
		                    this.reuseCell((CollectionViewCell)view);
		                }else if (type == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		                    this.reuseSupplementaryView(view);
		                }else if (type == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		                    this.reuseDecorationView(view);
		                }
		            }
		        });
		        _collectionViewFlags.updatingLayout = false;
		    } });

		    for (HashMap animation  : animations) {
		        CollectionReusableView view = animation.get("view");
		        CollectionViewLayout.Attributes attrs = animation.get("newLayoutInfos");
		        view.applyLayoutAttributes(attrs);
		    }
		    CATransaction.commit();
		} }, ^(boolean finished) {

		    if (_updateCompletionHandler) {
		        _updateCompletionHandler(finished);
		        _updateCompletionHandler = null;
		    }
		});

		_layout.finalizeCollectionViewUpdates();
	}

	void setupCellAnimations() {
		this.updateVisibleCellsNow(true);
		this.suspendReloads();
		_collectionViewFlags.updating = true;
	}

	void endItemAnimations() {
		_updateCount++;
		CollectionViewData oldCollectionViewData = _collectionViewData;
		_collectionViewData = new CollectionViewData(this, _layout);

		_layout.invalidateLayout();
		_collectionViewData.prepareToLoadData();

		ArrayList someMutableArr1 = new ArrayList();

		ArrayList removeUpdateItems = this.arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction.DELETE).sortedArrayUsingSelector("inverseCompareIndexPaths");

		ArrayList insertUpdateItems = this.arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction.INSERT).sortedArrayUsingSelector("compareIndexPaths");

		ArrayList sortedMutableReloadItems = _reloadItems.sortedArrayUsingSelector("compareIndexPaths").mutableCopy();
		ArrayList sortedMutableMoveItems = _moveItems.sortedArrayUsingSelector("compareIndexPaths").mutableCopy();

		_originalDeleteItems = removeUpdateItems.copy();
		_originalInsertItems = insertUpdateItems.copy();

		ArrayList someMutableArr2 = new ArrayList();
		ArrayList someMutableArr3 = new ArrayList();
		HashMap operations = new HashMap();

		for (CollectionViewUpdateItem updateItem  : sortedMutableReloadItems) {
		    mocha.foundation.Assert(updateItem.getIndexPathBeforeUpdate().getSection() < oldCollectionViewData.numberOfSections(),
		    "attempt to reload item (%s) that doesn't exist (there are only %ld sections before update)",
		    updateItem.getIndexPathBeforeUpdate(), (long)oldCollectionViewData.numberOfSections());

		    if (updateItem.getIndexPathBeforeUpdate().getItem() != mocha.foundation.NotFound) {
		        mocha.foundation.Assert(updateItem.getIndexPathBeforeUpdate().getItem() < oldCollectionViewData.numberOfItemsInSection(updateItem.getIndexPathBeforeUpdate().getSection()),
		        "attempt to reload item (%s) that doesn't exist (there are only %ld items in section %ld before update)",
		        updateItem.getIndexPathBeforeUpdate(),
		        (long)oldCollectionViewData.numberOfItemsInSection(updateItem.getIndexPathBeforeUpdate().getSection()),
		        (long)updateItem.getIndexPathBeforeUpdate().getSection());
		    }

		    someMutableArr2.addObject(new CollectionViewUpdateItem(CollectionViewUpdateItem.CollectionUpdateAction.DELETE, updateItem.getIndexPathBeforeUpdate()));
		    someMutableArr3.addObject(new CollectionViewUpdateItem(CollectionViewUpdateItem.CollectionUpdateAction.INSERT, updateItem.getIndexPathAfterUpdate()));
		}

		ArrayList sortedDeletedMutableItems = _deleteItems.sortedArrayUsingSelector("inverseCompareIndexPaths").mutableCopy();
		ArrayList sortedInsertMutableItems = _insertItems.sortedArrayUsingSelector("compareIndexPaths").mutableCopy();

		for (CollectionViewUpdateItem deleteItem  : sortedDeletedMutableItems) {
		    if (deleteItem.isSectionOperation()) {
		        mocha.foundation.Assert(deleteItem.getIndexPathBeforeUpdate().getSection() < oldCollectionViewData.numberOfSections(),
		        "attempt to delete section (%ld) that doesn't exist (there are only %ld sections before update)",
		        (long)deleteItem.getIndexPathBeforeUpdate().getSection(),
		        (long)oldCollectionViewData.numberOfSections());

		        for (CollectionViewUpdateItem moveItem  : sortedMutableMoveItems) {
		            if (moveItem.getIndexPathBeforeUpdate().getSection() == deleteItem.getIndexPathBeforeUpdate().getSection()) {
		                if (moveItem.getIsSectionOperation())
		                mocha.foundation.Assert(NO, "attempt to delete and move from the same section %ld", (long)deleteItem.getIndexPathBeforeUpdate().getSection());
		                else
		                        mocha.foundation.Assert(NO, "attempt to delete and move from the same section (%s)", moveItem.getIndexPathBeforeUpdate());
		            }
		        }
		    }else {
		        mocha.foundation.Assert(deleteItem.getIndexPathBeforeUpdate().getSection() < oldCollectionViewData.numberOfSections(),
		        "attempt to delete item (%s) that doesn't exist (there are only %ld sections before update)",
		        deleteItem.getIndexPathBeforeUpdate(),
		        (long)oldCollectionViewData.numberOfSections());
		        mocha.foundation.Assert(deleteItem.getIndexPathBeforeUpdate().getItem() < oldCollectionViewData.numberOfItemsInSection(deleteItem.getIndexPathBeforeUpdate().getSection()),
		        "attempt to delete item (%s) that doesn't exist (there are only %ld items in section%ld before update)",
		        deleteItem.getIndexPathBeforeUpdate(),
		        (long)oldCollectionViewData.numberOfItemsInSection(deleteItem.getIndexPathBeforeUpdate().getSection()),
		        (long)deleteItem.getIndexPathBeforeUpdate().getSection());

		        for (CollectionViewUpdateItem moveItem  : sortedMutableMoveItems) {
		            mocha.foundation.Assert(!deleteItem.getIndexPathBeforeUpdate().isEqual(moveItem.getIndexPathBeforeUpdate()),
		            "attempt to delete and move the same item (%s)", deleteItem.getIndexPathBeforeUpdate());
		        }

		        if (!operations.get(deleteItem.getIndexPathBeforeUpdate().getSection()))
		            new operations(deleteItem.getIndexPathBeforeUpdate().getSection(), new HashMap());

		        operations.get(deleteItem.getIndexPathBeforeUpdate().getSection()).put("deleted", (operations.get(deleteItem.getIndexPathBeforeUpdate().getSection()).get("deleted").intValue() + 1));
		    }
		}

		for (int i = 0; i < sortedInsertMutableItems.size(); i++) {
		    CollectionViewUpdateItem insertItem = sortedInsertMutableItems.get(i);
		    mocha.foundation.IndexPath indexPath = insertItem.getIndexPathAfterUpdate();

		    boolean sectionOperation = insertItem.isSectionOperation();
		    if (sectionOperation) {
		        mocha.foundation.Assert(indexPath.section() < _collectionViewData.numberOfSections(),
		        "attempt to insert %ld but there are only %ld sections after update",
		        (long)indexPath.section(), (long)_collectionViewData.numberOfSections());

		        for (CollectionViewUpdateItem moveItem in sortedMutableMoveItems) {
		            if (moveItem.getIndexPathAfterUpdate().isEqual( : dexPath)) {
		                if (moveItem.getIsSectionOperation())
		                mocha.foundation.Assert(NO, "attempt to perform an insert and a move to the same section (%ld)", (long)indexPath.section);
		            }
		        }

		        int j = i + 1;
		        while (j < sortedInsertMutableItems.size()) {
		            CollectionViewUpdateItem nextInsertItem = sortedInsertMutableItems.get(j);

		            if (nextInsertItem.getIndexPathAfterUpdate().getSection() == indexPath.section) {
		                mocha.foundation.Assert(nextInsertItem.getIndexPathAfterUpdate().getItem() < _collectionViewData.numberOfItemsInSection(indexPath.section),
		                "attempt to insert item %ld into section %ld, but there are only %ld items in section %ld after the update",
		                (long)nextInsertItem.getIndexPathAfterUpdate().getItem(),
		                (long)indexPath.section,
		                (long)_collectionViewData.numberOfItemsInSection(indexPath.section),
		                (long)indexPath.section);
		                sortedInsertMutableItems.removeObjectAtIndex(j);
		            }
		            else break;
		        }
		    }else {
		        mocha.foundation.Assert(indexPath.item < _collectionViewData.numberOfItemsInSection(indexPath.section),
		        "attempt to insert item to (%s) but there are only %ld items in section %ld after update",
		        indexPath,
		        (long)_collectionViewData.numberOfItemsInSection(indexPath.section),
		        (long)indexPath.section);

		        if (!operations.get(indexPath.section))
		            new operations(indexPath.section, new HashMap());

		        operations.get(indexPath.section).put("inserted", (operations.get(indexPath.section).get("inserted").intValue() + 1));
		    }
		}

		for (CollectionViewUpdateItem sortedItem  : sortedMutableMoveItems) {
		    if (sortedItem.getIsSectionOperation()) {
		        mocha.foundation.Assert(sortedItem.getIndexPathBeforeUpdate().getSection() < oldCollectionViewData.numberOfSections(),
		        "attempt to move section (%ld) that doesn't exist (%ld sections before update)",
		        (long)sortedItem.getIndexPathBeforeUpdate().getSection(),
		        (long)oldCollectionViewData.numberOfSections());
		        mocha.foundation.Assert(sortedItem.getIndexPathAfterUpdate().getSection() < _collectionViewData.numberOfSections(),
		        "attempt to move section to %ld but there are only %ld sections after update",
		        (long)sortedItem.getIndexPathAfterUpdate().getSection(),
		        (long)_collectionViewData.numberOfSections());
		    }else {
		        mocha.foundation.Assert(sortedItem.getIndexPathBeforeUpdate().getSection() < oldCollectionViewData.numberOfSections(),
		        "attempt to move item (%s) that doesn't exist (%ld sections before update)",
		        sortedItem, (long)oldCollectionViewData.numberOfSections());
		        mocha.foundation.Assert(sortedItem.getIndexPathBeforeUpdate().getItem() < oldCollectionViewData.numberOfItemsInSection(sortedItem.getIndexPathBeforeUpdate().getSection()),
		        "attempt to move item (%s) that doesn't exist (%ld items in section %ld before update)",
		        sortedItem,
		        (long)oldCollectionViewData.numberOfItemsInSection(sortedItem.getIndexPathBeforeUpdate().getSection()),
		        (long)sortedItem.getIndexPathBeforeUpdate().getSection());

		        mocha.foundation.Assert(sortedItem.getIndexPathAfterUpdate().getSection() < _collectionViewData.numberOfSections(),
		        "attempt to move item to (%s) but there are only %ld sections after update",
		        sortedItem.getIndexPathAfterUpdate(),
		        (long)_collectionViewData.numberOfSections());
		        mocha.foundation.Assert(sortedItem.getIndexPathAfterUpdate().getItem() < _collectionViewData.numberOfItemsInSection(sortedItem.getIndexPathAfterUpdate().getSection()),
		        "attempt to move item to (%s) but there are only %ld items in section %ld after update",
		        sortedItem,
		        (long)_collectionViewData.numberOfItemsInSection(sortedItem.getIndexPathAfterUpdate().getSection()),
		        (long)sortedItem.getIndexPathAfterUpdate().getSection());
		    }

		    if (!operations.get(sortedItem.getIndexPathBeforeUpdate().getSection()))
		        new operations(sortedItem.getIndexPathBeforeUpdate().getSection(), new HashMap());
		    if (!operations.get(sortedItem.getIndexPathAfterUpdate().getSection()))
		        new operations(sortedItem.getIndexPathAfterUpdate().getSection(), new HashMap());

		    operations.get(sortedItem.getIndexPathBeforeUpdate().getSection()).put("movedOut", (operations.get(sortedItem.getIndexPathBeforeUpdate().getSection()).get("movedOut").intValue() + 1));

		    operations.get(sortedItem.getIndexPathAfterUpdate().getSection()).put("movedIn", (operations.get(sortedItem.getIndexPathAfterUpdate().getSection()).get("movedIn").intValue() + 1));
		}

		!defined  mocha.foundation._BLOCK_ASSERTIONS
		for (Number sectionKey  : operations.keyEnumerator()) {
		    int section = sectionKey.intValue();

		    int insertedCount = operations.get(sectionKey).get("inserted").intValue();
		    int deletedCount = operations.get(sectionKey).get("deleted").intValue();
		    int movedInCount = operations.get(sectionKey).get("movedIn").intValue();
		    int movedOutCount = operations.get(sectionKey).get("movedOut").intValue();

		    mocha.foundation.Assert(oldCollectionViewData.numberOfItemsInSection(section) + insertedCount - deletedCount + movedInCount - movedOutCount ==
		            _collectionViewData.numberOfItemsInSection(section),
		    "invalid update in section %ld: number of items after update (%ld) should be equal to the number of items before update (%ld) "\
		             "plus count of inserted items (%ld), minus count of deleted items (%ld), plus count of items moved in (%ld), minus count of items moved out (%ld)",
		    (long)section,
		    (long)_collectionViewData.numberOfItemsInSection(section),
		    (long)oldCollectionViewData.numberOfItemsInSection(section),
		    (long)insertedCount, (long)deletedCount, (long)movedInCount, (long)movedOutCount);
		}
		if

		someMutableArr2.addObjectsFromArray(sortedDeletedMutableItems);
		someMutableArr3.addObjectsFromArray(sortedInsertMutableItems);
		someMutableArr1.addObjectsFromArray(someMutableArr2.sortedArrayUsingSelector("inverseCompareIndexPaths"));
		someMutableArr1.addObjectsFromArray(sortedMutableMoveItems);
		someMutableArr1.addObjectsFromArray(someMutableArr3.sortedArrayUsingSelector("compareIndexPaths"));

		ArrayList layoutUpdateItems = new ArrayList();

		layoutUpdateItems.addObjectsFromArray(sortedDeletedMutableItems);
		layoutUpdateItems.addObjectsFromArray(sortedMutableMoveItems);
		layoutUpdateItems.addObjectsFromArray(sortedInsertMutableItems);

		ArrayList newModel = new ArrayList();
		for (int i = 0; i < oldCollectionViewData.numberOfSections(); i++) {
		    ArrayList sectionArr = new ArrayList();
		    for (int j = 0; j < oldCollectionViewData.numberOfItemsInSection(i); j++)
		        sectionArr.addObject((oldCollectionViewData.globalIndexForItemAtIndexPath(mocha.foundation.IndexPath.indexPathForItemInSection(j, i))));
		    newModel.addObject(sectionArr);
		}

		for (CollectionViewUpdateItem updateItem  : layoutUpdateItems) {
		    switch (updateItem.getUpdateAction()) {
		        case CollectionViewUpdateItem.CollectionUpdateAction.DELETE: {
		            if (updateItem.getIsSectionOperation()) {
		                // section updates are ignored anyway in animation code. If not commented, mixing rows and section deletion causes crash in else below
		                // newModel.removeObjectAtIndex(updateItem.getIndexPathBeforeUpdate().getSection());
		            }else {
		                (ArrayList)newModel.get(updateItem.getIndexPathBeforeUpdate().getSection()).removeObjectAtIndex(updateItem.getIndexPathBeforeUpdate().getItem());
		            }
		        }
		            break;
		        case CollectionViewUpdateItem.CollectionUpdateAction.INSERT: {
		            if (updateItem.getIsSectionOperation()) {
		                newModel.insertObjectAtIndex(new ArrayList().getSection());
		            }else {
		                (ArrayList)newModel.get(updateItem.getIndexPathAfterUpdate().getSection()).insertObjectAtIndex(mocha.foundation.NotFound, updateItem.getIndexPathAfterUpdate().getItem());
		            }
		        }
		            break;

		        case CollectionViewUpdateItem.CollectionUpdateAction.MOVE: {
		            if (updateItem.getIsSectionOperation()) {
		                id section = newModel.get(updateItem.getIndexPathBeforeUpdate().getSection());
		                newModel.insertObjectAtIndex(section, updateItem.getIndexPathAfterUpdate().getSection());
		            }
		            else {
		                id object = (oldCollectionViewData.globalIndexForItemAtIndexPath(updateItem.getIndexPathBeforeUpdate()));
		                newModel.get(updateItem.getIndexPathBeforeUpdate().getSection()).removeObject(object);
		                newModel.get(updateItem.getIndexPathAfterUpdate().getSection()).insertObjectAtIndex(object, updateItem.getIndexPathAfterUpdate().getItem());
		            }
		        }
		            break;
		        default: break;
		    }
		}

		ArrayList oldToNewMap = ArrayList.arrayWithCapacity(oldCollectionViewData.numberOfItems());
		ArrayList newToOldMap = ArrayList.arrayWithCapacity(_collectionViewData.numberOfItems());

		for (int i = 0; i < oldCollectionViewData.numberOfItems(); i++)
		    oldToNewMap.addObject(mocha.foundation.NotFound);

		for (int i = 0; i < _collectionViewData.numberOfItems(); i++)
		    newToOldMap.addObject(mocha.foundation.NotFound);

		for (int i = 0; i < newModel.size(); i++) {
		    ArrayList section = newModel.get(i);
		    for (int j = 0; j < section.size(); j++) {
		        int newGlobalIndex = _collectionViewData.globalIndexForItemAtIndexPath(mocha.foundation.IndexPath.indexPathForItemInSection(j, i));
		        if (section.get(j).integerValue() != mocha.foundation.NotFound)
		            oldToNewMap.put(section.get(j).intValue(), newGlobalIndex);
		        if (newGlobalIndex != mocha.foundation.NotFound)
		            new newToOldMap(newGlobalIndex, section.get(j));
		    }
		}

		_update = mocha.foundation.Maps.create("oldModel", oldCollectionViewData, "newModel", _collectionViewData, "oldToNewIndexMap", oldToNewMap, "newToOldIndexMap", newToOldMap);

		this.updateWithItems(someMutableArr1);

		_originalInsertItems = null;
		_originalDeleteItems = null;
		_insertItems = null;
		_deleteItems = null;
		_moveItems = null;
		_reloadItems = null;
		_update = null;
		_updateCount--;
		_collectionViewFlags.updating = false;
		this.resumeReloads();
	}

	void updateRowsAtIndexPathsUpdateAction(ArrayList indexPaths, CollectionViewUpdateItem.CollectionUpdateAction updateAction) {
		boolean updating = _collectionViewFlags.updating;
		if (!updating) this.setupCellAnimations();

		ArrayList array = this.arrayForUpdateAction(updateAction); //returns appropriate empty array if not exists

		for (mocha.foundation.IndexPath indexPath in  : dexPaths) {
		    CollectionViewUpdateItem updateItem = new CollectionViewUpdateItem(updateAction, indexPath);
		    array.addObject(updateItem);
		}

		if (!updating) this.endItemAnimations();
	}

	void updateSectionsUpdateAction(mocha.foundation.IndexSet sections, CollectionViewUpdateItem.CollectionUpdateAction updateAction) {
		boolean updating = _collectionViewFlags.updating;
		if (!updating) this.setupCellAnimations();

		ArrayList updateActions = this.arrayForUpdateAction(updateAction);

		sections.enumerateIndexesUsingBlock(^(int section, boolean *stop) {
		    CollectionViewUpdateItem updateItem = new CollectionViewUpdateItem(updateAction, mocha.foundation.IndexPath.indexPathForItemInSection(mocha.foundation.NotFound, section));
		    updateActions.addObject(updateItem);
		});

		if (!updating) this.endItemAnimations();
	}

	private static float PSTSimulatorAnimationDragCoefficient(void void) {
		static float (*mocha.ui.AnimationDragCoefficient)(void) = null;
		TARGET_IPHONE_SIMULATOR
		ort <dlfcn.h>
		static dispatch_once_t onceToken;
		dispatch_once(&onceToken, new Runnable { public void run() {
		    mocha.ui.AnimationDragCoefficient = (float (*)(void))dlsym(RTLD_DEFAULT, "UIAnimationDragCoefficient");
		} });
		if
		return mocha.ui.AnimationDragCoefficient ? mocha.ui.AnimationDragCoefficient() : 1.f;
	}

	private static void PSTCollectionViewCommonSetup(CollectionView _self) {
		_this.setAllowsSelection(true);
		_this->_indexPathsForSelectedItems = new HashSet();
		_this->_indexPathsForHighlightedItems = new HashSet();
		_this->_cellReuseQueues = new HashMap();
		_this->_supplementaryViewReuseQueues = new HashMap();
		_this->_decorationViewReuseQueues = new HashMap();
		_this->_allVisibleViewsDict = new HashMap();
		_this->_cellClassDict = new HashMap();
		_this->_cellNibDict = new HashMap();
		_this->_supplementaryViewClassDict = new HashMap();
		_this->_supplementaryViewNibDict = new HashMap();

		// add class that saves additional ivars
		    objc_setAssociatedObject(_this, &CollectionView.P_ST_COLLETION_VIEW_EXT, new CollectionView.Ext(), OBJC_ASSOCIATION_RETAIN_NONATOMIC);
	}

	/* Setters & Getters */
	/* ========================================== */

	public CollectionViewLayout getCollectionViewLayout() {
		return this._layout;
	}

	public void setCollectionViewLayout(CollectionViewLayout collectionViewLayout) {
		this._layout = collectionViewLayout;
	}

	public CollectionView.Delegate getDelegate() {
		return this._delegate;
	}

	public void setDelegate(CollectionView.Delegate delegate) {
		this._delegate = delegate;
	}

	public CollectionView.DataSource getDataSource() {
		return this._dataSource;
	}

	public void setDataSource(CollectionView.DataSource dataSource) {
		this._dataSource = dataSource;
	}

	public mocha.ui.View getBackgroundView() {
		return this._backgroundView;
	}

	public void setBackgroundView(mocha.ui.View backgroundView) {
		this._backgroundView = backgroundView;
	}

	public boolean getAllowsSelection() {
		return this._allowsSelection;
	}

	public void setAllowsSelection(boolean allowsSelection) {
		this._allowsSelection = allowsSelection;
	}

	public boolean getAllowsMultipleSelection() {
		return this._allowsMultipleSelection;
	}

	public void setAllowsMultipleSelection(boolean allowsMultipleSelection) {
		this._allowsMultipleSelection = allowsMultipleSelection;
	}

	private CollectionViewData getCollectionViewData() {
		return this._collectionViewData;
	}

	private void setCollectionViewData(CollectionViewData collectionViewData) {
		this._collectionViewData = collectionViewData;
	}

	private CollectionView.Ext getExtVars() {
		return this._extVars;
	}

	private void setExtVars(CollectionView.Ext extVars) {
		this._extVars = extVars;
	}

	private Object getCurrentUpdate() {
		return this._update;
	}

	private void setCurrentUpdate(Object currentUpdate) {
		this._update = currentUpdate;
	}

	private HashMap getVisibleViewsDict() {
		return this._allVisibleViewsDict;
	}

	private void setVisibleViewsDict(HashMap visibleViewsDict) {
		this._allVisibleViewsDict = visibleViewsDict;
	}

	private mocha.graphics.Rect getVisibleBoundRects() {
		if(this._visibleBoundRects != null) {
			return this._visibleBoundRects.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	private void setVisibleBoundRects(mocha.graphics.Rect visibleBoundRects) {
		if(this._visibleBoundRects != null) {
			this._visibleBoundRects = visibleBoundRects.copy();
		} else {
			this._visibleBoundRects = mocha.graphics.Rect.zero();
		}
	}

}

