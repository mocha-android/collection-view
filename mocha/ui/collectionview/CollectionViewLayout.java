class CollectionViewLayout extends MObject implements Serializable {
	private CollectionView _collectionView;
	private HashMap _decorationViewClassDict;
	private HashMap _decorationViewNibDict;
	private HashMap _decorationViewExternalObjectsTables;
	private mocha.graphics.Size _collectionViewBoundsSize;
	private HashMap _initialAnimationLayoutAttributesDict;
	private HashMap _finalAnimationLayoutAttributesDict;
	private mocha.foundation.MutableIndexSet _deletedSectionsSet;
	private mocha.foundation.MutableIndexSet _insertedSectionsSet;
	private Character filler;

	public static class Attributes extends MObject implements mocha.foundation.Copying<Attributes> {
		private mocha.graphics.Rect _frame;
		private mocha.graphics.Point _center;
		private mocha.graphics.Size _size;
		private CATransform3D _transform3D;
		private float _alpha;
		private int _zIndex;
		private boolean _hidden;
		private mocha.foundation.IndexPath _indexPath;
		private String _elementKind;
		private String _representedElementKind;
		private CollectionViewLayout.CollectionViewItemType _representedElementCategory;
		private Character filler;
		private LayoutFlagsStruct _layoutFlags = new LayoutFlagsStruct();
		private CollectionViewLayout.CollectionViewItemType _elementCategory;

		private class LayoutFlagsStruct {
			boolean isCellKind;
			boolean isDecorationView;
			boolean isHidden;

		}

		static Attributes layoutAttributesForCellWithIndexPath(mocha.foundation.IndexPath indexPath) {
			CollectionViewLayout.Attributes attributes = new this();
			attributes.setElementKind(PSTCollectionElementKindCell);
			attributes.setElementCategory(CollectionViewLayout.CollectionViewItemType.CELL);
			attributes.setIndexPath(indexPath);
			return attributes;
		}

		static Attributes layoutAttributesForSupplementaryViewOfKindWithIndexPath(String elementKind, mocha.foundation.IndexPath indexPath) {
			CollectionViewLayout.Attributes attributes = new this();
			attributes.setElementCategory(CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW);
			attributes.setElementKind(elementKind);
			attributes.setIndexPath(indexPath);
			return attributes;
		}

		static Attributes layoutAttributesForDecorationViewOfKindWithIndexPath(String elementKind, mocha.foundation.IndexPath indexPath) {
			CollectionViewLayout.Attributes attributes = new this();
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
			super.init();

			_alpha = 1.f;
			_transform3D = CATransform3DIdentity;
		}

		int hash() {
			return (_elementKind.hash() * 31) + _indexPath.hash();
		}

		boolean isEqual(Object other) {
			if (other.isKindOfClass(this.getClass())) {
			    CollectionViewLayout.Attributes otherLayoutAttributes = (CollectionViewLayout.Attributes)other;
			    if (_elementCategory == otherLayoutAttributes.getElementCategory() && _elementKind.isEqual(otherLayoutAttributes.getElementKind()) && _indexPath.isEqual(otherLayoutAttributes.getIndexPath())) {
			        return true;
			    }
			}
			return false;
		}

		String description() {
			return String.format("<%s: %p frame:%s indexPath:%s elementKind:%s>", StringFromClass(this.getClass()), this, StringFromCGRect(this.getFrame()), this.getIndexPath(), this.getElementKind());
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

		void setSize(mocha.graphics.Size size) {
			_size = size;
			this.updateFrame();
		}

		void setCenter(mocha.graphics.Point center) {
			_center = center;
			this.updateFrame();
		}

		void setFrame(mocha.graphics.Rect frame) {
			_frame = frame;
			_size = _frame.size;
			_center = new mocha.graphics.Point(mocha.graphics.RectGetMidX(_frame), mocha.graphics.RectGetMidY(_frame));
		}

		Object copyWithZone(mocha.foundation.Zone zone) {
			CollectionViewLayout.Attributes layoutAttributes = this.getClass().new();
			layoutAttributes.setIndexPath(this.getIndexPath());
			layoutAttributes.setElementKind(this.getElementKind());
			layoutAttributes.setElementCategory(this.getElementCategory());
			layoutAttributes.setFrame(this.getFrame());
			layoutAttributes.setCenter(this.getCenter());
			layoutAttributes.setSize(this.getSize());
			layoutAttributes.setTransform3D(this.getTransform3D());
			layoutAttributes.setAlpha(this.getAlpha());
			layoutAttributes.setZIndex(this.getZIndex());
			layoutAttributes.setHidden(this.getIsHidden());
			return layoutAttributes;
		}

		mocha.foundation.MethodSignature methodSignatureForSelector(SEL selector) {
			mocha.foundation.MethodSignature signature = super.methodSignatureForSelector(selector);
			if (!signature) {
			    String selString = StringFromSelector(selector);
			    if (selString.hasPrefix("_")) {
			        SEL cleanedSelector = mocha.foundation.SelectorFromString(selString.substringFromIndex(1));
			        signature = super.methodSignatureForSelector(cleanedSelector);
			    }
			}
			return signature;
		}

		void forwardInvocation(mocha.foundation.Invocation invocation) {
			String selString = StringFromSelector(invocation.selector());
			if (selString.hasPrefix("_")) {
			    SEL cleanedSelector = mocha.foundation.SelectorFromString(selString.substringFromIndex(1));
			    if (this.respondsToSelector(cleanedSelector)) {
			        invocation.setSelector(cleanedSelector);
			        invocation.invokeWithTarget(this);
			    }
			}else {
			    super.forwardInvocation(invocation);
			}
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

		public void setFrame(mocha.graphics.Rect frame) {
			if(this._frame != null) {
				this._frame = frame.copy();
			} else {
				this._frame = mocha.graphics.Rect.zero();
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
		}

		public CATransform3D getTransform3D() {
			return this._transform3D;
		}

		public void setTransform3D(CATransform3D transform3D) {
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

	void invalidateLayout() {
		_collectionView.collectionViewData().invalidate();
		_collectionView.setNeedsLayout();
	}

	void registerClassForDecorationViewOfKind(Class viewClass, String kind) {
		_decorationViewClassDict.put(kind, viewClass);
	}

	void registerNibForDecorationViewOfKind(mocha.ui.Nib nib, String kind) {
		_decorationViewNibDict.put(kind, nib);
	}

	static Class layoutAttributesClass() {
		return CollectionViewLayout.Attributes.getClass();
	}

	void prepareLayout() {

	}

	ArrayList layoutAttributesForElementsInRect(mocha.graphics.Rect rect) {
		return null;
	}

	CollectionViewLayout.Attributes layoutAttributesForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		return null;
	}

	CollectionViewLayout.Attributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath) {
		return null;
	}

	CollectionViewLayout.Attributes layoutAttributesForDecorationViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath) {
		return null;
	}

	boolean shouldInvalidateLayoutForBoundsChange(mocha.graphics.Rect newBounds) {
		// not sure about his..
		if ((this.getCollectionView().getBounds().getSize().getWidth() != newBounds.size.width) || (this.getCollectionView().getBounds().getSize().getHeight() != newBounds.size.height)) {
		    return true;
		}
		return false;
	}

	mocha.graphics.Point targetContentOffsetForProposedContentOffsetWithScrollingVelocity(mocha.graphics.Point proposedContentOffset, mocha.graphics.Point velocity) {
		return proposedContentOffset;
	}

	mocha.graphics.Size collectionViewContentSize() {
		return mocha.graphics.Size.zero();
	}

	void prepareForCollectionViewUpdates(ArrayList updateItems) {
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
		    if (attr.getIsCell()) {
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

		            if (attrs) {
		                attrs.setAlpha(0);
		                _finalAnimationLayoutAttributesDict.put(key, attrs);
		            }
		        }
		        else if (action == CollectionViewUpdateItem.CollectionUpdateAction.RELOAD || action == CollectionViewUpdateItem.CollectionUpdateAction.INSERT) {
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(updateItem.indexPathAfterUpdate());
		            CollectionViewLayout.Attributes attrs = _initialAnimationLayoutAttributesDict.get(key).copy();

		            if (attrs) {
		                attrs.setAlpha(0);
		                _initialAnimationLayoutAttributesDict.put(key, attrs);
		            }
		        }
		    }
		}
	}

	void finalizeCollectionViewUpdates() {
		_initialAnimationLayoutAttributesDict.removeAllObjects();
		_finalAnimationLayoutAttributesDict.removeAllObjects();
		_deletedSectionsSet.removeAllIndexes();
		_insertedSectionsSet.removeAllIndexes();
	}

	public CollectionViewLayout(mocha.foundation.IndexPath itemIndexPath) {
		CollectionViewLayout.Attributes attrs = _initialAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(itemIndexPath));

		if (_insertedSectionsSet.containsIndex(itemIndexPath.section())) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}
		return attrs;
	}

	CollectionViewLayout.Attributes finalLayoutAttributesForDisappearingItemAtIndexPath(mocha.foundation.IndexPath itemIndexPath) {
		CollectionViewLayout.Attributes attrs = _finalAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(itemIndexPath));

		if (_deletedSectionsSet.containsIndex(itemIndexPath.section())) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}
		return attrs;
	}

	public CollectionViewLayout(String elementKind, mocha.foundation.IndexPath elementIndexPath) {
		CollectionViewLayout.Attributes attrs = _initialAnimationLayoutAttributesDict.get(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(elementIndexPath));

		if (_insertedSectionsSet.containsIndex(elementIndexPath.section())) {
		    attrs = attrs.copy();
		    attrs.setAlpha(0);
		}
		return attrs;
	}

	CollectionViewLayout.Attributes finalLayoutAttributesForDeletedSupplementaryElementOfKindAtIndexPath(String elementKind, mocha.foundation.IndexPath elementIndexPath) {
		return null;
	}

	void setCollectionViewBoundsSize(mocha.graphics.Size size) {
		_collectionViewBoundsSize = size;
	}

	CollectionReusableView decorationViewForCollectionViewWithReuseIdentifierIndexPath(CollectionView collectionView, String reuseIdentifier, mocha.foundation.IndexPath indexPath) {

	}

	public CollectionViewLayout() {
		super.init();

		_decorationViewClassDict = new HashMap();
		_decorationViewNibDict = new HashMap();
		_decorationViewExternalObjectsTables = new HashMap();
		_initialAnimationLayoutAttributesDict = new HashMap();
		_finalAnimationLayoutAttributesDict = new HashMap();
		_insertedSectionsSet = new mocha.foundation.MutableIndexSet();
		_deletedSectionsSet = new mocha.foundation.MutableIndexSet();
	}

	void awakeFromNib() {
		super.awakeFromNib();
	}

	void setCollectionView(CollectionView collectionView) {
		if (collectionView != _collectionView) {
		    _collectionView = collectionView;
		}
	}

	public CollectionViewLayout(mocha.foundation.Coder coder) {
		this.init();
	}

	void encodeWithCoder(mocha.foundation.Coder coder) {

	}

	mocha.foundation.MethodSignature methodSignatureForSelector(SEL selector) {
		mocha.foundation.MethodSignature sig = super.methodSignatureForSelector(selector);
		if(!sig) {
		    String selString = StringFromSelector(selector);
		    if (selString.hasPrefix("_")) {
		        SEL cleanedSelector = mocha.foundation.SelectorFromString(selString.substringFromIndex(1));
		        sig = super.methodSignatureForSelector(cleanedSelector);
		    }
		}
		return sig;
	}

	void forwardInvocation(mocha.foundation.Invocation inv) {
		String selString = StringFromSelector(inv.selector());
		if (selString.hasPrefix("_")) {
		    SEL cleanedSelector = mocha.foundation.SelectorFromString(selString.substringFromIndex(1));
		    if (this.respondsToSelector(cleanedSelector)) {
		        // dynamically add method for faster resolving
		        Method newMethod = class_getInstanceMethod(this.getClass(), inv.selector());
		        IMP underscoreIMP = imp_implementationWithBlock(^(id _this) {
		            return objc_msgSend(_this, cleanedSelector);
		        });
		        class_addMethod(this.getClass(), inv.selector(), underscoreIMP, method_getTypeEncoding(newMethod));
		        // invoke now
		        inv.setSelector(cleanedSelector);
		        inv.invokeWithTarget(this);
		    }
		}else {
		    super.forwardInvocation(inv);
		}
	}

	/* Setters & Getters */
	/* ========================================== */

	public CollectionView getCollectionView() {
		return this._collectionView;
	}

	public void setCollectionView(CollectionView collectionView) {
		this._collectionView = collectionView;
	}

	HashMap getDecorationViewClassDict() {
		return this._decorationViewClassDict;
	}

	void setDecorationViewClassDict(HashMap decorationViewClassDict) {
		this._decorationViewClassDict = decorationViewClassDict;
	}

	HashMap getDecorationViewNibDict() {
		return this._decorationViewNibDict;
	}

	void setDecorationViewNibDict(HashMap decorationViewNibDict) {
		this._decorationViewNibDict = decorationViewNibDict;
	}

	HashMap getDecorationViewExternalObjectsTables() {
		return this._decorationViewExternalObjectsTables;
	}

	void setDecorationViewExternalObjectsTables(HashMap decorationViewExternalObjectsTables) {
		this._decorationViewExternalObjectsTables = decorationViewExternalObjectsTables;
	}

}

