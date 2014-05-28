package mocha.ui.collectionview;

import mocha.foundation.*;
import mocha.graphics.Point;
import mocha.graphics.Rect;
import mocha.graphics.Size;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

abstract public class CollectionViewLayout extends MObject implements Serializable {
	private CollectionView collectionView;
	private Map<String,Class<? extends CollectionReusableView>> decorationViewClassMap;
	private Map<CollectionViewItemKey,CollectionViewLayoutAttributes> initialAnimationLayoutAttributesMap;
	private Map<CollectionViewItemKey,CollectionViewLayoutAttributes> finalAnimationLayoutAttributesMap;

	private Class<? extends CollectionViewLayoutAttributes> layoutAttributesClass;
	private Map<String,List<CollectionViewLayoutAttributes>> layoutAttributesReuseQueues;

	private IndexSet deletedSectionsSet;
	private IndexSet insertedSectionsSet;

	public CollectionViewLayout() {
		this.decorationViewClassMap = new HashMap<>();
		this.initialAnimationLayoutAttributesMap = new HashMap<>();
		this.finalAnimationLayoutAttributesMap = new HashMap<>();
		this.insertedSectionsSet = new IndexSet();
		this.deletedSectionsSet = new IndexSet();

		this.layoutAttributesClass = this.layoutAttributesClass();
		this.layoutAttributesReuseQueues = new HashMap<>();

		if(this.layoutAttributesClass == null) {
			this.layoutAttributesClass = CollectionViewLayoutAttributes.class;
		}
	}

	public void invalidateLayout() {
		this.collectionView.collectionViewData().invalidate();
		this.collectionView.setNeedsLayout();
	}

	public CollectionViewLayoutAttributes dequeueLayoutAttributesForCell(IndexPath indexPath) {
		return this.dequeueLayoutAttributes(CollectionElementCategory.CELL, CollectionViewItemKey.ELEMENT_KIND_CELL, indexPath);
	}

	public CollectionViewLayoutAttributes dequeueLayoutAttributesForSupplementaryViewOfKind(String kind, IndexPath indexPath) {
		return this.dequeueLayoutAttributes(CollectionElementCategory.SUPPLEMENTARY_VIEW, kind, indexPath);
	}

	public CollectionViewLayoutAttributes dequeueLayoutAttributesForDecorationViewOfKind(String kind, IndexPath indexPath) {
		return this.dequeueLayoutAttributes(CollectionElementCategory.DECORATION_VIEW, kind, indexPath);
	}

	private CollectionViewLayoutAttributes dequeueLayoutAttributes(CollectionElementCategory elementCategory, String kind, IndexPath indexPath) {
		Assert.condition(elementCategory != null, "elementCategory can not be null");
		Assert.condition(kind != null, "kind can not be null");
		Assert.condition(indexPath != null, "indexPath can not be null");

		String reuseIdentifier = elementCategory + "/" + kind;

		List<CollectionViewLayoutAttributes> reuseableLayoutAttributes = this.layoutAttributesReuseQueues.get(reuseIdentifier);
		CollectionViewLayoutAttributes layoutAttributes = Lists.removeLast(reuseableLayoutAttributes);

		boolean wasReused = layoutAttributes != null;

		if(!wasReused) {
			// MLog("CV_DEBUG - CREATING ATTRIBUTES: " + reuseIdentifier);
			try {
				layoutAttributes = this.layoutAttributesClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			layoutAttributes.reuseIdentifier = reuseIdentifier;
			layoutAttributes.setRepresentedElementKind(kind);
			layoutAttributes.setRepresentedElementCategory(elementCategory);
		}

		layoutAttributes.setIndexPath(indexPath);

		if(wasReused) {
			layoutAttributes.prepareForReuse();
		}

		return layoutAttributes;
	}

	void enqueueLayoutAttributes(CollectionViewLayoutAttributes layoutAttributes) {
		if(layoutAttributes == null || layoutAttributes.reuseIdentifier == null) return;

		List<CollectionViewLayoutAttributes> reuseableLayoutAttributes = this.layoutAttributesReuseQueues.get(layoutAttributes.reuseIdentifier);

		if (reuseableLayoutAttributes == null) {
			reuseableLayoutAttributes = new ArrayList<>();
			this.layoutAttributesReuseQueues.put(layoutAttributes.reuseIdentifier, reuseableLayoutAttributes);
		}

		reuseableLayoutAttributes.add(layoutAttributes);
	}

	public Class<? extends CollectionViewLayoutAttributes> layoutAttributesClass() {
		return CollectionViewLayoutAttributes.class;
	}

	public void collectionViewDelegateDidChange() {

	}

	abstract public void prepareLayout();

	abstract public List<CollectionViewLayoutAttributes> layoutAttributesForElementsInRect(Rect rect);

	abstract public CollectionViewLayoutAttributes layoutAttributesForItemAtIndexPath(IndexPath indexPath);

	abstract public CollectionViewLayoutAttributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, IndexPath indexPath);

	abstract public CollectionViewLayoutAttributes layoutAttributesForDecorationViewOfKindAtIndexPath(String kind, IndexPath indexPath);

	public boolean shouldInvalidateLayoutForBoundsChange(Rect newBounds) {
		// not sure about this..
		Rect bounds = this.collectionView.getBounds();
		return (bounds.size.width != newBounds.size.width) || (bounds.size.height != newBounds.size.height);
	}

	public Point targetContentOffsetForProposedContentOffsetWithScrollingVelocity(Point proposedContentOffset, Point velocity) {
		return proposedContentOffset;
	}

	abstract public Size collectionViewContentSize();

	public void prepareForCollectionViewUpdates(List<CollectionViewUpdateItem> updateItems) {
		CollectionView.UpdateTransaction update = this.collectionView.currentUpdate();

		for (CollectionReusableView view  : this.collectionView.visibleViewsDict().values()) {
		    CollectionViewLayoutAttributes attr = view.getLayoutAttributes();

		    if (attr != null) {
				attr = attr.copy();

		        if (attr.isCell()) {
		            int index = update.oldModel.globalIndexForItemAtIndexPath(attr.getIndexPath());

		            if (index != -1) {
		                attr.setIndexPath(attr.getIndexPath());
		            }
		        }

		        this.initialAnimationLayoutAttributesMap.put(CollectionViewItemKey.collectionItemKeyForLayoutAttributes(attr), attr);
		    }
		}

		CollectionViewData collectionViewData = this.collectionView.collectionViewData();

		Rect bounds = this.collectionView.visibleBoundRects();

		for (CollectionViewLayoutAttributes attr  : collectionViewData.layoutAttributesForElementsInRect(bounds)) {
		    if (attr.isCell()) {
		        int index = collectionViewData.globalIndexForItemAtIndexPath(attr.getIndexPath());

		        index = update.newToOldIndexMap.get(index);

		        if (index != -1) {
		            CollectionViewLayoutAttributes finalAttrs = attr.copy();
		            finalAttrs.setIndexPath(update.oldModel.indexPathForItemAtGlobalIndex(index));
		            finalAttrs.setAlpha(0);
					this.finalAnimationLayoutAttributesMap.put(CollectionViewItemKey.collectionItemKeyForLayoutAttributes(finalAttrs), finalAttrs);
		        }
		    }
		}

		for (CollectionViewUpdateItem updateItem : updateItems) {
		    CollectionViewUpdateItem.CollectionUpdateAction action = updateItem.updateAction();

		    if (updateItem.isSectionOperation()) {
		        if (action == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD) {
					this.deletedSectionsSet.add(updateItem.indexPathBeforeUpdate().section);
					this.insertedSectionsSet.add(updateItem.indexPathAfterUpdate().section);
		        } else {
		            IndexSet indexSet = action == CollectionViewUpdateItem.CollectionUpdateAction.INSERT ? this.insertedSectionsSet : this.deletedSectionsSet;
		            indexSet.add(updateItem.indexPath().section);
		        }
		    } else {
		        if (action == CollectionViewUpdateItem.CollectionUpdateAction.DELETE) {
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(updateItem.indexPathBeforeUpdate());

		            CollectionViewLayoutAttributes attrs = this.finalAnimationLayoutAttributesMap.get(key);

		            if (attrs != null) {
						attrs = attrs.copy();
		                attrs.setAlpha(0);
						this.finalAnimationLayoutAttributesMap.put(key, attrs);
		            }
		        } else if (action == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD || action == CollectionViewUpdateItem.CollectionUpdateAction.INSERT) {
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(updateItem.indexPathAfterUpdate());
		            CollectionViewLayoutAttributes attrs = this.initialAnimationLayoutAttributesMap.get(key);

		            if (attrs != null) {
						attrs = attrs.copy();
		                attrs.setAlpha(0);
						this.initialAnimationLayoutAttributesMap.put(key, attrs);
		            }
		        }
		    }
		}
	}

	public void finalizeCollectionViewUpdates() {
		this.initialAnimationLayoutAttributesMap.clear();
		this.finalAnimationLayoutAttributesMap.clear();
		this.deletedSectionsSet.clear();
		this.insertedSectionsSet.clear();
	}

	public CollectionViewLayoutAttributes initialLayoutAttributesForAppearingItemAtIndexPath(mocha.foundation.IndexPath itemIndexPath) {
		CollectionViewLayoutAttributes attrs = this.initialAnimationLayoutAttributesMap.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(itemIndexPath));

		if (attrs != null && this.insertedSectionsSet.contains(itemIndexPath.section)) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}

		return attrs;
	}

	public CollectionViewLayoutAttributes finalLayoutAttributesForDisappearingItemAtIndexPath(mocha.foundation.IndexPath itemIndexPath) {
		CollectionViewLayoutAttributes attrs = this.finalAnimationLayoutAttributesMap.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(itemIndexPath));

		if (attrs != null && this.deletedSectionsSet.contains(itemIndexPath.section)) {
			attrs = attrs.copy();
			attrs.setAlpha(0);
		}

		return attrs;
	}

	public CollectionViewLayoutAttributes initialLayoutAttributesForInsertedSupplementaryElementOfKind(String elementKind, mocha.foundation.IndexPath elementIndexPath) {
		CollectionViewLayoutAttributes attrs = this.initialAnimationLayoutAttributesMap.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(elementIndexPath));

		if (attrs != null && this.insertedSectionsSet.contains(elementIndexPath.section)) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}

		return attrs;
	}

	public CollectionViewLayoutAttributes finalLayoutAttributesForDeletedSupplementaryElementOfKindAtIndexPath(String elementKind, mocha.foundation.IndexPath elementIndexPath) {
		return null;
	}

	abstract public CollectionReusableView decorationViewForCollectionViewWithReuseIdentifierIndexPath(CollectionView collectionView, String reuseIdentifier, mocha.foundation.IndexPath indexPath);

	public CollectionView getCollectionView() {
		return this.collectionView;
	}

	public void setCollectionView(CollectionView collectionView) {
		this.collectionView = collectionView;
	}

	Map<String, Class<? extends CollectionReusableView>> getDecorationViewClassDict() {
		return this.decorationViewClassMap;
	}

}

