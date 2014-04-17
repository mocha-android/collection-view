package mocha.ui.collectionview;

import mocha.foundation.IndexSet;
import mocha.foundation.MObject;
import mocha.graphics.AffineTransform;
import mocha.graphics.Rect;
import mocha.graphics.Size;

import java.io.Serializable;
import java.util.*;

abstract public class CollectionViewLayout extends MObject implements Serializable {
	private CollectionView _collectionView;
	private Map<String,Class<? extends CollectionReusableView>> _decorationViewClassDict;
	private Map<CollectionViewItemKey,Attributes> _initialAnimationLayoutAttributesDict;
	private Map<CollectionViewItemKey,Attributes> _finalAnimationLayoutAttributesDict;
	private IndexSet _deletedSectionsSet;
	private IndexSet _insertedSectionsSet;

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
		private CollectionViewLayout.CollectionViewItemType _elementCategory;

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
			    Attributes otherLayoutAttributes = (Attributes)other;
			    if (_elementCategory == otherLayoutAttributes.getElementCategory() && _elementKind.equals(otherLayoutAttributes.getElementKind()) && _indexPath.equals(otherLayoutAttributes.getIndexPath())) {
			        return true;
			    }
			}

			return false;
		}

		protected String toStringExtra() {
			return String.format("frame:%s indexPath:%s elementKind:%s", this.getFrame(), this.getIndexPath(), this.getElementKind());
		}

		public CollectionViewLayout.CollectionViewItemType representedElementCategory() {
			return this._elementCategory;
		}

		public String representedElementKind() {
			return this.getElementKind();
		}

		private void updateFrame() {
			_frame = new mocha.graphics.Rect(_center.x - _size.width / 2, _center.y - _size.height / 2, _size.width, _size.height);
		}

		public void setFrame(mocha.graphics.Rect frame) {
			if(frame != null) {
				this._frame = frame.copy();
			} else {
				this._frame = mocha.graphics.Rect.zero();
			}

			this._size = this._frame.size.copy();
			this._center = new mocha.graphics.Point(this._frame.midX(), this._frame.midY());
		}

		public Attributes copy() {
			Attributes layoutAttributes;

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
			if(center != null) {
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
			if(size != null) {
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
			if(transform3D != null) {
				this._transform3D = transform3D.copy();
			} else {
				this._transform3D = AffineTransform.identity();
			}
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

		private void setElementKind(String elementKind) {
			this._elementKind = elementKind;
		}

		public String getRepresentedElementKind() {
			return this._elementKind;
		}

		private void setRepresentedElementKind(String representedElementKind) {
			this._elementKind = representedElementKind;
		}

		public CollectionViewLayout.CollectionViewItemType getRepresentedElementCategory() {
			return this._elementCategory;
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

	public CollectionViewLayout() {
		_decorationViewClassDict = new HashMap<>();
		_initialAnimationLayoutAttributesDict = new HashMap<>();
		_finalAnimationLayoutAttributesDict = new HashMap<>();
		_insertedSectionsSet = new IndexSet();
		_deletedSectionsSet = new IndexSet();
	}

	public void invalidateLayout() {
		this._collectionView.collectionViewData().invalidate();
		this._collectionView.setNeedsLayout();
	}

	public void registerClassForDecorationViewOfKind(Class<? extends CollectionReusableView> viewClass, String kind) {
		this._decorationViewClassDict.put(kind, viewClass);
	}

	public Class<? extends Attributes> layoutAttributesClass() {
		return Attributes.class;
	}

	public void collectionViewDelegateDidChange() {

	}

	abstract public void prepareLayout();

	abstract public List<Attributes> layoutAttributesForElementsInRect(mocha.graphics.Rect rect);

	abstract public Attributes layoutAttributesForItemAtIndexPath(mocha.foundation.IndexPath indexPath);

	abstract public Attributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath);

	abstract public Attributes layoutAttributesForDecorationViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath);

	public boolean shouldInvalidateLayoutForBoundsChange(mocha.graphics.Rect newBounds) {
		// not sure about his..
		Rect bounds = this.getCollectionView().getBounds();
		return (bounds.size.width != newBounds.size.width) || (bounds.size.height != newBounds.size.height);
	}

	public mocha.graphics.Point targetContentOffsetForProposedContentOffsetWithScrollingVelocity(mocha.graphics.Point proposedContentOffset, mocha.graphics.Point velocity) {
		return proposedContentOffset;
	}

	abstract public mocha.graphics.Size collectionViewContentSize();

	public void prepareForCollectionViewUpdates(List<CollectionViewUpdateItem> updateItems) {
		CollectionView.UpdateTransaction update = _collectionView.currentUpdate();

		for (CollectionReusableView view  : _collectionView.visibleViewsDict().values()) {
		    Attributes attr = view.getLayoutAttributes();

		    if (attr != null) {
				attr = attr.copy();

		        if (attr.isCell()) {
		            int index = update.oldModel.globalIndexForItemAtIndexPath(attr.getIndexPath());

		            if (index != -1) {
		                attr.setIndexPath(attr.getIndexPath());
		            }
		        }

		        this._initialAnimationLayoutAttributesDict.put(CollectionViewItemKey.collectionItemKeyForLayoutAttributes(attr), attr);
		    }
		}

		CollectionViewData collectionViewData = _collectionView.collectionViewData();

		Rect bounds = _collectionView.visibleBoundRects();

		for (Attributes attr  : collectionViewData.layoutAttributesForElementsInRect(bounds)) {
		    if (attr.isCell()) {
		        int index = collectionViewData.globalIndexForItemAtIndexPath(attr.getIndexPath());

		        index = update.newToOldIndexMap.get(index);
		        if (index != -1) {
		            Attributes finalAttrs = attr.copy();
		            finalAttrs.setIndexPath(update.oldModel.indexPathForItemAtGlobalIndex(index));
		            finalAttrs.setAlpha(0);
		            _finalAnimationLayoutAttributesDict.put(CollectionViewItemKey.collectionItemKeyForLayoutAttributes(finalAttrs), finalAttrs);
		        }
		    }
		}

		for (CollectionViewUpdateItem updateItem : updateItems) {
		    CollectionViewUpdateItem.CollectionUpdateAction action = updateItem.updateAction();

		    if (updateItem.isSectionOperation()) {
		        if (action == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD) {
		            _deletedSectionsSet.add(updateItem.indexPathBeforeUpdate().section);
		            _insertedSectionsSet.add(updateItem.indexPathAfterUpdate().section);
		        }
		        else {
		            IndexSet indexSet = action == CollectionViewUpdateItem.CollectionUpdateAction.INSERT ? _insertedSectionsSet : _deletedSectionsSet;
		            indexSet.add(updateItem.indexPath().section);
		        }
		    }
		    else {
		        if (action == CollectionViewUpdateItem.CollectionUpdateAction.DELETE) {
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(updateItem.indexPathBeforeUpdate());

		            Attributes attrs = _finalAnimationLayoutAttributesDict.get(key).copy();

		            if (attrs != null) {
		                attrs.setAlpha(0);
		                _finalAnimationLayoutAttributesDict.put(key, attrs);
		            }
		        }
		        else if (action == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD || action == CollectionViewUpdateItem.CollectionUpdateAction.INSERT) {
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(updateItem.indexPathAfterUpdate());
		            Attributes attrs = _initialAnimationLayoutAttributesDict.get(key).copy();

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

	public Attributes initialLayoutAttributesForAppearingItemAtIndexPath(mocha.foundation.IndexPath itemIndexPath) {
		Attributes attrs = _initialAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(itemIndexPath));

		if (_insertedSectionsSet.contains(itemIndexPath.section)) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}

		return attrs;
	}

	public Attributes finalLayoutAttributesForDisappearingItemAtIndexPath(mocha.foundation.IndexPath itemIndexPath) {
		Attributes attrs = _finalAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(itemIndexPath));

		if (_deletedSectionsSet.contains(itemIndexPath.section)) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}
		return attrs;
	}

	public Attributes initialLayoutAttributesForInsertedSupplementaryElementOfKind(String elementKind, mocha.foundation.IndexPath elementIndexPath) {
		Attributes attrs = _initialAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(elementIndexPath));

		if (_insertedSectionsSet.contains(elementIndexPath.section)) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}

		return attrs;
	}

	Attributes finalLayoutAttributesForDeletedSupplementaryElementOfKindAtIndexPath(String elementKind, mocha.foundation.IndexPath elementIndexPath) {
		return null;
	}

	abstract CollectionReusableView decorationViewForCollectionViewWithReuseIdentifierIndexPath(CollectionView collectionView, String reuseIdentifier, mocha.foundation.IndexPath indexPath);

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

	Map<String, Class<? extends CollectionReusableView>> getDecorationViewClassDict() {
		return this._decorationViewClassDict;
	}

}

