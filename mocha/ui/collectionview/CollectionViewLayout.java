package mocha.ui.collectionview;

import mocha.foundation.MObject;
import mocha.graphics.AffineTransform;
import mocha.graphics.Rect;
import mocha.graphics.Size;
import mocha.ui.View;

import java.io.Serializable;
import java.util.*;

abstract public class CollectionViewLayout extends MObject implements Serializable {
	private CollectionView _collectionView;
	private Map<String,Class<? extends View>> _decorationViewClassDict;
	private HashMap _decorationViewExternalObjectsTables;
	private mocha.graphics.Size _collectionViewBoundsSize;
	private Map<CollectionViewItemKey,Attributes> _initialAnimationLayoutAttributesDict;
	private Map<CollectionViewItemKey,Attributes> _finalAnimationLayoutAttributesDict;
	private Set<Integer> _deletedSectionsSet;
	private Set<Integer> _insertedSectionsSet;

	public static class Attributes extends MObject implements mocha.foundation.Copying<Attributes> {
		private mocha.graphics.Rect _frame;
		private mocha.graphics.Point _center;
		private mocha.graphics.Size _size;
		private AffineTransform _transform3D;
		private float _alpha;
		private int _zIndex;
		private boolean _hidden;
		private mocha.foundation.IndexPath _indexPath;
		private String _elementKind;
		private String _representedElementKind;
		private CollectionViewLayout.CollectionViewItemType _representedElementCategory;
		private LayoutFlagsStruct _layoutFlags = new LayoutFlagsStruct();
		private CollectionViewLayout.CollectionViewItemType _elementCategory;

		private class LayoutFlagsStruct {
			boolean isCellKind;
			boolean isDecorationView;
			boolean isHidden;
		}

		public static Attributes layoutAttributesForCellWithIndexPath(mocha.foundation.IndexPath indexPath) {
			return layoutAttributesForCellWithIndexPath(Attributes.class, indexPath);
		}

		public static Attributes layoutAttributesForCellWithIndexPath(Class<? extends Attributes> attributesClass, mocha.foundation.IndexPath indexPath) {
			Attributes attributes;

			try {
				attributes = attributesClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			attributes.setElementKind(CollectionViewItemKey.ELEMENT_KIND_CELL);
			attributes.setElementCategory(CollectionViewLayout.CollectionViewItemType.CELL);
			attributes.setIndexPath(indexPath);
			return attributes;
		}

		public static Attributes layoutAttributesForSupplementaryViewOfKindWithIndexPath(String elementKind, mocha.foundation.IndexPath indexPath) {
			return layoutAttributesForSupplementaryViewOfKindWithIndexPath(Attributes.class, elementKind, indexPath);
		}

		public static Attributes layoutAttributesForSupplementaryViewOfKindWithIndexPath(Class<? extends Attributes> attributesClass, String elementKind, mocha.foundation.IndexPath indexPath) {
			Attributes attributes;

			try {
				attributes = attributesClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			attributes.setElementCategory(CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW);
			attributes.setElementKind(elementKind);
			attributes.setIndexPath(indexPath);
			return attributes;
		}

		public static Attributes layoutAttributesForDecorationViewOfKindWithIndexPath(String elementKind, mocha.foundation.IndexPath indexPath) {
			return layoutAttributesForDecorationViewOfKindWithIndexPath(Attributes.class, elementKind, indexPath);
		}

		public static Attributes layoutAttributesForDecorationViewOfKindWithIndexPath(Class<? extends Attributes> attributesClass, String elementKind, mocha.foundation.IndexPath indexPath) {
			Attributes attributes;

			try {
				attributes = attributesClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			attributes.setElementCategory(CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW);
			attributes.setElementKind(elementKind);
			attributes.setIndexPath(indexPath);
			return attributes;
		}

		boolean isDecorationView() {
			return this.getRepresentedElementCategory() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW;
		}

		boolean isSupplementaryView() {
			return this.getRepresentedElementCategory() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW;
		}

		boolean isCell() {
			return this.getRepresentedElementCategory() == CollectionViewLayout.CollectionViewItemType.CELL;
		}

		public Attributes() {
			_alpha = 1.0f;
			_transform3D = AffineTransform.identity();
		}

		public int hashCode() {
			return (_elementKind.hashCode() * 31) + _indexPath.hashCode();
		}

		public boolean equals(Object other) {
			if(other == this) {
				return true;
			} else if(this.getClass().isAssignableFrom(other.getClass())) {
			    CollectionViewLayout.Attributes otherLayoutAttributes = (CollectionViewLayout.Attributes)other;
			    if (_elementCategory == otherLayoutAttributes.getElementCategory() && _elementKind.equals(otherLayoutAttributes.getElementKind()) && _indexPath.equals(otherLayoutAttributes.getIndexPath())) {
			        return true;
			    }
			}

			return false;
		}

		protected String toStringExtra() {
			return String.format("frame:%s indexPath:%s elementKind:%s", this.getFrame(), this.getIndexPath(), this.getElementKind());
		}

		CollectionViewLayout.CollectionViewItemType representedElementCategory() {
			return _elementCategory;
		}

		String representedElementKind() {
			return this.getElementKind();
		}

		void updateFrame() {
			_frame = new mocha.graphics.Rect(_center.x - _size.width / 2, _center.y - _size.height / 2, _size.width, _size.height);
		}

		void setFrame(mocha.graphics.Rect frame) {
			if(this._frame != null) {
				this._frame = frame.copy();
			} else {
				this._frame = mocha.graphics.Rect.zero();
			}

			this._size = this._frame.size;
			this._center = new mocha.graphics.Point(this._frame.midX(), this._frame.midY());
		}

		public Attributes copy() {
			Attributes layoutAttributes = null;
			try {
				layoutAttributes = this.getClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			layoutAttributes.setIndexPath(this._indexPath);
			layoutAttributes.setElementKind(this.getElementKind());
			layoutAttributes.setElementCategory(this.getElementCategory());
			layoutAttributes.setFrame(this._frame);
			layoutAttributes.setTransform3D(this.getTransform3D());
			layoutAttributes.setAlpha(this._alpha);
			layoutAttributes.setZIndex(this._zIndex);
			layoutAttributes.setHidden(this._hidden);

			return layoutAttributes;
		}

		/* Setters & Getters */
		/* ========================================== */

		public mocha.graphics.Rect getFrame() {
			if(this._frame != null) {
				return this._frame.copy();
			} else {
				return mocha.graphics.Rect.zero();
			}
		}

		public mocha.graphics.Point getCenter() {
			if(this._center != null) {
				return this._center.copy();
			} else {
				return mocha.graphics.Point.zero();
			}
		}

		public void setCenter(mocha.graphics.Point center) {
			if(this._center != null) {
				this._center = center.copy();
			} else {
				this._center = mocha.graphics.Point.zero();
			}

			this.updateFrame();
		}

		public mocha.graphics.Size getSize() {
			if(this._size != null) {
				return this._size.copy();
			} else {
				return mocha.graphics.Size.zero();
			}
		}

		public void setSize(mocha.graphics.Size size) {
			if(this._size != null) {
				this._size = size.copy();
			} else {
				this._size = mocha.graphics.Size.zero();
			}

			this.updateFrame();
		}

		public AffineTransform getTransform3D() {
			return this._transform3D;
		}

		public void setTransform3D(AffineTransform transform3D) {
			this._transform3D = transform3D;
		}

		public float getAlpha() {
			return this._alpha;
		}

		public void setAlpha(float alpha) {
			this._alpha = alpha;
		}

		public int getZIndex() {
			return this._zIndex;
		}

		public void setZIndex(int zIndex) {
			this._zIndex = zIndex;
		}

		public boolean getHidden() {
			return this._hidden;
		}

		public void setHidden(boolean hidden) {
			this._hidden = hidden;
		}

		public mocha.foundation.IndexPath getIndexPath() {
			return this._indexPath;
		}

		public void setIndexPath(mocha.foundation.IndexPath indexPath) {
			this._indexPath = indexPath;
		}

		String getElementKind() {
			return this._elementKind;
		}

		void setElementKind(String elementKind) {
			this._elementKind = elementKind;
		}

		String getRepresentedElementKind() {
			return this._representedElementKind;
		}

		void setRepresentedElementKind(String representedElementKind) {
			this._representedElementKind = representedElementKind;
		}

		CollectionViewLayout.CollectionViewItemType getRepresentedElementCategory() {
			return this._representedElementCategory;
		}

		void setRepresentedElementCategory(CollectionViewLayout.CollectionViewItemType representedElementCategory) {
			this._representedElementCategory = representedElementCategory;
		}

		private CollectionViewLayout.CollectionViewItemType getElementCategory() {
			return this._elementCategory;
		}

		private void setElementCategory(CollectionViewLayout.CollectionViewItemType elementCategory) {
			this._elementCategory = elementCategory;
		}

	}

	public enum CollectionViewItemType {
		CELL,
		SUPPLEMENTARY_VIEW,
		DECORATION_VIEW
	}

	public void invalidateLayout() {
		_collectionView.collectionViewData().invalidate();
		_collectionView.setNeedsLayout();
	}

	public void registerClassForDecorationViewOfKind(Class<? extends View> viewClass, String kind) {
		_decorationViewClassDict.put(kind, viewClass);
	}

	public Class<? extends CollectionViewLayout.Attributes> layoutAttributesClass() {
		return CollectionViewLayout.Attributes.class;
	}

	public void collectionViewDelegateDidChange() {

	}

	abstract public void prepareLayout();

	abstract public List<Attributes> layoutAttributesForElementsInRect(mocha.graphics.Rect rect);

	abstract public CollectionViewLayout.Attributes layoutAttributesForItemAtIndexPath(mocha.foundation.IndexPath indexPath);

	abstract public CollectionViewLayout.Attributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath);

	abstract public CollectionViewLayout.Attributes layoutAttributesForDecorationViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath);

	public boolean shouldInvalidateLayoutForBoundsChange(mocha.graphics.Rect newBounds) {
		// not sure about his..
		Rect bounds = this.getCollectionView().getBounds();
		return (bounds.size.width != newBounds.size.width) || (bounds.size.height != newBounds.size.height);
	}

	public mocha.graphics.Point targetContentOffsetForProposedContentOffsetWithScrollingVelocity(mocha.graphics.Point proposedContentOffset, mocha.graphics.Point velocity) {
		return proposedContentOffset;
	}

	abstract public mocha.graphics.Size collectionViewContentSize();

	public void prepareForCollectionViewUpdates(ArrayList updateItems) {
		HashMap update = _collectionView.currentUpdate();

		for (CollectionReusableView view  : _collectionView.visibleViewsDict().objectEnumerator()) {
		    CollectionViewLayout.Attributes attr = view.getLayoutAttributes().copy();
		    if (attr) {
		        if (attr.getIsCell()) {
		            int index = update.get("oldModel").globalIndexForItemAtIndexPath(attr.indexPath());
		            if (index != mocha.foundation.NotFound) {
		                attr.setIndexPath(attr.indexPath());
		            }
		        }
		        _initialAnimationLayoutAttributesDict.put(CollectionViewItemKey.collectionItemKeyForLayoutAttributes(attr), attr);
		    }
		}

		CollectionViewData collectionViewData = _collectionView.collectionViewData();

		mocha.graphics.Rect bounds = _collectionView.visibleBoundRects();

		for (CollectionViewLayout.Attributes attr  : collectionViewData.layoutAttributesForElementsInRect(bounds)) {
		    if (attr.isCell()) {
		        int index = collectionViewData.globalIndexForItemAtIndexPath(attr.getIndexPath());

		        index = update.get("newToOldIndexMap").get(index).intValue();
		        if (index != mocha.foundation.NotFound) {
		            CollectionViewLayout.Attributes finalAttrs = attr.copy();
		            finalAttrs.setIndexPath(update.get("oldModel").indexPathForItemAtGlobalIndex(index));
		            finalAttrs.setAlpha(0);
		            _finalAnimationLayoutAttributesDict.put(CollectionViewItemKey.collectionItemKeyForLayoutAttributes(finalAttrs), finalAttrs);
		        }
		    }
		}

		for (CollectionViewUpdateItem updateItem  : updateItems) {
		    CollectionViewUpdateItem.CollectionUpdateAction action = updateItem.getUpdateAction();

		    if (updateItem.isSectionOperation()) {
		        if (action == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD) {
		            _deletedSectionsSet.addIndex(updateItem.indexPathBeforeUpdate().section());
		            _insertedSectionsSet.addIndex(updateItem.indexPathAfterUpdate().getSection());
		        }
		        else {
		            mocha.foundation.MutableIndexSet indexSet = action == CollectionViewUpdateItem.CollectionUpdateAction.INSERT ? _insertedSectionsSet : _deletedSectionsSet;
		            indexSet.addIndex(updateItem.indexPath().getSection());
		        }
		    }
		    else {
		        if (action == CollectionViewUpdateItem.CollectionUpdateAction.DELETE) {
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(updateItem.indexPathBeforeUpdate());

		            CollectionViewLayout.Attributes attrs = _finalAnimationLayoutAttributesDict.get(key).copy();

		            if (attrs != null) {
		                attrs.setAlpha(0);
		                _finalAnimationLayoutAttributesDict.put(key, attrs);
		            }
		        }
		        else if (action == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD || action == CollectionViewUpdateItem.CollectionUpdateAction.INSERT) {
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(updateItem.indexPathAfterUpdate());
		            CollectionViewLayout.Attributes attrs = _initialAnimationLayoutAttributesDict.get(key).copy();

		            if (attrs != null) {
		                attrs.setAlpha(0);
		                _initialAnimationLayoutAttributesDict.put(key, attrs);
		            }
		        }
		    }
		}
	}

	public void finalizeCollectionViewUpdates() {
		_initialAnimationLayoutAttributesDict.clear();
		_finalAnimationLayoutAttributesDict.clear();
		_deletedSectionsSet.clear();
		_insertedSectionsSet.clear();
	}

	public CollectionViewLayout.Attributes initialLayoutAttributesForAppearingItemAtIndexPath(mocha.foundation.IndexPath itemIndexPath) {
		CollectionViewLayout.Attributes attrs = _initialAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(itemIndexPath));

		if (_insertedSectionsSet.contains(itemIndexPath.section)) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}

		return attrs;
	}

	public CollectionViewLayout.Attributes finalLayoutAttributesForDisappearingItemAtIndexPath(mocha.foundation.IndexPath itemIndexPath) {
		CollectionViewLayout.Attributes attrs = _finalAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(itemIndexPath));

		if (_deletedSectionsSet.contains(itemIndexPath.section)) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}
		return attrs;
	}

	public CollectionViewLayout.Attributes initialLayoutAttributesForInsertedSupplementaryElementOfKind(String elementKind, mocha.foundation.IndexPath elementIndexPath) {
		CollectionViewLayout.Attributes attrs = _initialAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(elementIndexPath));

		if (_insertedSectionsSet.contains(elementIndexPath.section)) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}

		return attrs;
	}

	CollectionViewLayout.Attributes finalLayoutAttributesForDeletedSupplementaryElementOfKindAtIndexPath(String elementKind, mocha.foundation.IndexPath elementIndexPath) {
		return null;
	}

	void setCollectionViewBoundsSize(mocha.graphics.Size size) {
		if(size != null) {
			_collectionViewBoundsSize = size.copy();
		} else {
			_collectionViewBoundsSize = Size.zero();
		}
	}

	abstract CollectionReusableView decorationViewForCollectionViewWithReuseIdentifierIndexPath(CollectionView collectionView, String reuseIdentifier, mocha.foundation.IndexPath indexPath);

	public CollectionViewLayout(Rect frame) {
		super(frame);

		_decorationViewClassDict = new HashMap<>();
		_decorationViewExternalObjectsTables = new HashMap();
		_initialAnimationLayoutAttributesDict = new HashMap<>();
		_finalAnimationLayoutAttributesDict = new HashMap<>();
		_insertedSectionsSet = new HashSet<>();
		_deletedSectionsSet = new HashSet<>();
	}


	public CollectionViewLayout() {
		this(Rect.zero());
	}

	/* Setters & Getters */
	/* ========================================== */

	public CollectionView getCollectionView() {
		return this._collectionView;
	}

	public void setCollectionView(CollectionView collectionView) {
		if (collectionView != _collectionView) {
			_collectionView = collectionView;
		}
	}

	Map<String, Class<? extends View>> getDecorationViewClassDict() {
		return this._decorationViewClassDict;
	}

	void setDecorationViewClassDict(HashMap decorationViewClassDict) {
		this._decorationViewClassDict = decorationViewClassDict;
	}

	HashMap getDecorationViewExternalObjectsTables() {
		return this._decorationViewExternalObjectsTables;
	}

	void setDecorationViewExternalObjectsTables(HashMap decorationViewExternalObjectsTables) {
		this._decorationViewExternalObjectsTables = decorationViewExternalObjectsTables;
	}

}

