class CollectionViewFlowLayout extends CollectionViewLayout {
	private mocha.graphics.Size _itemSize;
	private CollectionViewFlowLayout.CollectionViewScrollDirection _scrollDirection;
	private mocha.graphics.Size _headerReferenceSize;
	private mocha.graphics.Size _footerReferenceSize;
	private mocha.ui.EdgeInsets _sectionInset;
	private HashMap _rowAlignmentsOptionsDictionary;
	private float _lineSpacing;
	private float _interitemSpacing;
	private GridLayoutInfo _data;
	private mocha.graphics.Size _currentLayoutSize;
	private HashMap _insertedItemsAttributesDict;
	private HashMap _insertedSectionHeadersAttributesDict;
	private HashMap _insertedSectionFootersAttributesDict;
	private HashMap _deletedItemsAttributesDict;
	private HashMap _deletedSectionHeadersAttributesDict;
	private HashMap _deletedSectionFootersAttributesDict;
	private mocha.graphics.Rect _visibleBounds;
	private Character filler;
	private GridLayoutFlagsStruct _gridLayoutFlags = new GridLayoutFlagsStruct();
	private static Character PSTCachedItemRectsKey;

	public interface CollectionViewDelegateFlowLayout extends CollectionView.Delegate {

		@Optional
		mocha.graphics.Size collectionViewLayoutSizeForItemAtIndexPath(CollectionView collectionView, CollectionViewLayout collectionViewLayout, mocha.foundation.IndexPath indexPath);

		@Optional
		mocha.ui.EdgeInsets collectionViewLayoutInsetForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		float collectionViewLayoutMinimumLineSpacingForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		float collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		mocha.graphics.Size collectionViewLayoutReferenceSizeForHeaderInSection(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		mocha.graphics.Size collectionViewLayoutReferenceSizeForFooterInSection(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		CollectionViewLayout.Attributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath);

	}

	private class GridLayoutFlagsStruct {
		boolean delegateSizeForItem;
		boolean delegateReferenceSizeForHeader;
		boolean delegateReferenceSizeForFooter;
		boolean delegateInsetForSection;
		boolean delegateInteritemSpacingForSection;
		boolean delegateLineSpacingForSection;
		boolean delegateAlignmentOptions;
		boolean keepDelegateInfoWhileInvalidating;
		boolean keepAllDataWhileInvalidating;
		boolean layoutDataIsValid;
		boolean delegateInfoIsValid;

	}

	public enum CollectionViewScrollDirection {
		VERTICAL,
		HORIZONTAL
	}

	public enum FlowLayoutHorizontalAlignment {
		LEFT,
		CENTERED,
		RIGHT,
		JUSTIFY
	}

	void commonInit() {
		_itemSize = new mocha.graphics.Size(50.f, 50.f);
		_lineSpacing = 10.f;
		_interitemSpacing = 10.f;
		_sectionInset = mocha.ui.EdgeInsetsZero;
		_scrollDirection = CollectionViewFlowLayout.CollectionViewScrollDirection.VERTICAL;
		_headerReferenceSize = mocha.graphics.Size.zero();
		_footerReferenceSize = mocha.graphics.Size.zero();
	}

	public CollectionViewFlowLayout() {
		super.init();

		this.commonInit();
					
		// set default values for row alignment.
		_rowAlignmentsOptionsDictionary = mocha.foundation.Maps.create(PSTFlowLayoutCommonRowHorizontalAlignmentKey, CollectionViewFlowLayout.FlowLayoutHorizontalAlignment.JUSTIFY, PSTFlowLayoutLastRowHorizontalAlignmentKey, CollectionViewFlowLayout.FlowLayoutHorizontalAlignment.JUSTIFY, // TODO, those values are some enum. find out what that is.
		        PSTFlowLayoutRowVerticalAlignmentKey : 1);
	}

	public CollectionViewFlowLayout(mocha.foundation.Coder decoder) {
		super.initWithCoder(decoder);

		this.commonInit();
					
		// Some properties are not set if they're default (like minimumInteritemSpacing == 10)
		if (decoder.containsValueForKey("UIItemSize"))
		    this.setItemSize(decoder.decodeCGSizeForKey("UIItemSize"));
		if (decoder.containsValueForKey("UIInteritemSpacing"))
		    this.setMinimumInteritemSpacing(decoder.decodeFloatForKey("UIInteritemSpacing"));
		if (decoder.containsValueForKey("UILineSpacing"))
		    this.setMinimumLineSpacing(decoder.decodeFloatForKey("UILineSpacing"));
		if (decoder.containsValueForKey("UIFooterReferenceSize"))
		    this.setFooterReferenceSize(decoder.decodeCGSizeForKey("UIFooterReferenceSize"));
		if (decoder.containsValueForKey("UIHeaderReferenceSize"))
		    this.setHeaderReferenceSize(decoder.decodeCGSizeForKey("UIHeaderReferenceSize"));
		if (decoder.containsValueForKey("UISectionInset"))
		    this.setSectionInset(decoder.decodeUIEdgeInsetsForKey("UISectionInset"));
		if (decoder.containsValueForKey("UIScrollDirection"))
		    this.setScrollDirection(decoder.decodeIntegerForKey("UIScrollDirection"));
	}

	void encodeWithCoder(mocha.foundation.Coder coder) {
		super.encodeWithCoder(coder);
		coder.encodeCGSizeForKey(this.getItemSize(), "UIItemSize");
		coder.encodeFloatForKey((float)this.getMinimumInteritemSpacing(), "UIInteritemSpacing");
		coder.encodeFloatForKey((float)this.getMinimumLineSpacing(), "UILineSpacing");
		coder.encodeCGSizeForKey(this.getFooterReferenceSize(), "UIFooterReferenceSize");
		coder.encodeCGSizeForKey(this.getHeaderReferenceSize(), "UIHeaderReferenceSize");
		coder.encodeUIEdgeInsetsForKey(this.getSectionInset(), "UISectionInset");
		coder.encodeIntegerForKey(this.getScrollDirection(), "UIScrollDirection");
	}

	ArrayList layoutAttributesForElementsInRect(mocha.graphics.Rect rect) {
		// Apple calls _layoutAttributesForItemsInRect
		if (!_data) this.prepareLayout();

		ArrayList layoutAttributesArray = new ArrayList();
		for (GridLayoutSection section  : _data.getSections()) {
		    if (mocha.graphics.RectIntersectsRect(section.getFrame(), rect)) {

		        // if we have fixed size, calculate item frames only once.
		        // this also uses the default PSTFlowLayoutCommonRowHorizontalAlignmentKey alignment
		        // for the last row. (we want this effect!)
		        HashMap rectCache = objc_getAssociatedObject(this, &CollectionViewFlowLayout.PSTCachedItemRectsKey);
		        int sectionIndex = _data.getSections().indexOfObjectIdenticalTo(section);

		        mocha.graphics.Rect normalizedHeaderFrame = section.getHeaderFrame();
		        normalizedHeaderFrame.origin.x += section.getFrame().getOrigin().x;
		        normalizedHeaderFrame.origin.y += section.getFrame().getOrigin().y;
		        if (!mocha.graphics.RectIsEmpty(normalizedHeaderFrame) && mocha.graphics.RectIntersectsRect(normalizedHeaderFrame, rect)) {
		            CollectionViewLayout.Attributes layoutAttributes = this.getClass().layoutAttributesClass().layoutAttributesForSupplementaryViewOfKindWithIndexPath(PSTCollectionElementKindSectionHeader, mocha.foundation.IndexPath.indexPathForItemInSection(0, sectionIndex));
		            layoutAttributes.setFrame(normalizedHeaderFrame);
		            layoutAttributesArray.addObject(layoutAttributes);
		        }

		        ArrayList itemRects = rectCache.get(sectionIndex);
		        if (!itemRects && section.getFixedItemSize() && section.getRows().size()) {
		            itemRects = (section.getRows()).get(0).itemRects();
		            if (itemRects) new rectCache(sectionIndex, itemRects);
		        }
		        
		        for (GridLayoutRow row  : section.getRows()) {
		            mocha.graphics.Rect normalizedRowFrame = row.getRowFrame();
		            
		            normalizedRowFrame.origin.x += section.getFrame().getOrigin().x;
		            normalizedRowFrame.origin.y += section.getFrame().getOrigin().y;
		            
		            if (mocha.graphics.RectIntersectsRect(normalizedRowFrame, rect)) {
		                // TODO be more fine-grained for items

		                for (int itemIndex = 0; itemIndex < row.getItemCount(); itemIndex++) {
		                    CollectionViewLayout.Attributes *layoutAttributes;
		                    int sectionItemIndex;
		                    mocha.graphics.Rect itemFrame;
		                    if (row.getFixedItemSize()) {
		                        itemFrame = itemRects.get(itemIndex).mocha.graphics.RectValue();
		                        sectionItemIndex = row.getIndex() * section.getItemsByRowCount() + itemIndex;
		                    }else {
		                        GridLayoutItem item = row.getItems().get(itemIndex);
		                        sectionItemIndex = section.getItems().indexOfObjectIdenticalTo(item);
		                        itemFrame = item.getItemFrame();
		                    }

		                    mocha.graphics.Rect normalisedItemFrame = new mocha.graphics.Rect(normalizedRowFrame.origin.x + itemFrame.origin.x, normalizedRowFrame.origin.y + itemFrame.origin.y, itemFrame.size.width, itemFrame.size.height);
		                    
		                    if (mocha.graphics.RectIntersectsRect(normalisedItemFrame, rect)) {
		                        layoutAttributes = this.getClass().layoutAttributesClass().layoutAttributesForCellWithIndexPath(mocha.foundation.IndexPath.indexPathForItemInSection(sectionItemIndex, sectionIndex));
		                        layoutAttributes.setFrame(normalisedItemFrame);
		                        layoutAttributesArray.addObject(layoutAttributes);
		                    }
		                }
		            }
		        }

		        mocha.graphics.Rect normalizedFooterFrame = section.getFooterFrame();
		        normalizedFooterFrame.origin.x += section.getFrame().getOrigin().x;
		        normalizedFooterFrame.origin.y += section.getFrame().getOrigin().y;
		        if (!mocha.graphics.RectIsEmpty(normalizedFooterFrame) && mocha.graphics.RectIntersectsRect(normalizedFooterFrame, rect)) {
		            CollectionViewLayout.Attributes layoutAttributes = this.getClass().layoutAttributesClass().layoutAttributesForSupplementaryViewOfKindWithIndexPath(PSTCollectionElementKindSectionFooter, mocha.foundation.IndexPath.indexPathForItemInSection(0, sectionIndex));
		            layoutAttributes.setFrame(normalizedFooterFrame);
		            layoutAttributesArray.addObject(layoutAttributes);
		        }
		    }
		}
		return layoutAttributesArray;
	}

	CollectionViewLayout.Attributes layoutAttributesForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		if (!_data) this.prepareLayout();

		GridLayoutSection section = _data.getSections().get(indexPath.section);
		GridLayoutRow row = null;
		mocha.graphics.Rect itemFrame = mocha.graphics.Rect.zero();

		if (section.getFixedItemSize() && section.getItemsByRowCount() > 0 && indexPath.item / section.getItemsByRowCount() < (int)section.getRows().size()) {
		    row = section.getRowsindexPath().getItem()./ section.itemsByRowCount();
		    int itemIndex = indexPath.item % section.getItemsByRowCount();
		    ArrayList itemRects = row.itemRects();
		    itemFrame = itemRects.get(itemIndex).mocha.graphics.RectValue();
		}else if (indexPath.item < (int)section.getItems().size()) {
		    GridLayoutItem item = section.getItems().get(indexPath.item);
		    row = item.getRowObject();
		    itemFrame = item.getItemFrame();
		}

		CollectionViewLayout.Attributes layoutAttributes = this.getClass().layoutAttributesClass().layoutAttributesForCellWithIndexPath(indexPath);

		// calculate item rect
		mocha.graphics.Rect normalizedRowFrame = row.getRowFrame();
		normalizedRowFrame.origin.x += section.getFrame().getOrigin().x;
		normalizedRowFrame.origin.y += section.getFrame().getOrigin().y;
		layoutAttributes.setFrame(new mocha.graphics.Rect(normalizedRowFrame.origin.x + itemFrame.origin.x, normalizedRowFrame.origin.y + itemFrame.origin.y, itemFrame.size.width, itemFrame.size.height));

		return layoutAttributes;
	}

	CollectionViewLayout.Attributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath) {
		if (!_data) this.prepareLayout();

		int sectionIndex = indexPath.section;
		CollectionViewLayout.Attributes layoutAttributes = null;

		if (sectionIndex < _data.getSections().size()) {
		    GridLayoutSection section = _data.getSections().get(sectionIndex);

		    mocha.graphics.Rect normalizedFrame = mocha.graphics.Rect.zero();
		    if (kind.isEqualToString(PSTCollectionElementKindSectionHeader)) {
		        normalizedFrame = section.getHeaderFrame();
		    }
		    else if (kind.isEqualToString(PSTCollectionElementKindSectionFooter)) {
		        normalizedFrame = section.getFooterFrame();
		    }

		    if (!mocha.graphics.RectIsEmpty(normalizedFrame)) {
		        normalizedFrame.origin.x += section.getFrame().getOrigin().x;
		        normalizedFrame.origin.y += section.getFrame().getOrigin().y;

		        layoutAttributes = this.getClass().layoutAttributesClass().layoutAttributesForSupplementaryViewOfKindWithIndexPath(kind, mocha.foundation.IndexPath.indexPathForItemInSection(0, sectionIndex));
		        layoutAttributes.setFrame(normalizedFrame);
		    }
		}
		return layoutAttributes;
	}

	CollectionViewLayout.Attributes layoutAttributesForDecorationViewWithReuseIdentifierAtIndexPath(String identifier, mocha.foundation.IndexPath indexPath) {
		return null;
	}

	mocha.graphics.Size collectionViewContentSize() {
		if (!_data) this.prepareLayout();

		return _data.getContentSize();
	}

	void setSectionInset(mocha.ui.EdgeInsets sectionInset) {
		if (!mocha.ui.EdgeInsetsEqualToEdgeInsets(sectionInset, _sectionInset)) {
		    _sectionInset = sectionInset;
		    this.invalidateLayout();
		}
	}

	void invalidateLayout() {
		super.invalidateLayout();
		objc_setAssociatedObject(this, &CollectionViewFlowLayout.PSTCachedItemRectsKey, null, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
		_data = null;
	}

	boolean shouldInvalidateLayoutForBoundsChange(mocha.graphics.Rect newBounds) {
		// we need to recalculate on width changes
		if ((_visibleBounds.size.width != newBounds.size.width && this.getScrollDirection() == CollectionViewFlowLayout.CollectionViewScrollDirection.VERTICAL) || (_visibleBounds.size.height != newBounds.size.height && this.getScrollDirection() == CollectionViewFlowLayout.CollectionViewScrollDirection.HORIZONTAL)) {
		    _visibleBounds = this.getCollectionView().getBounds();
		    return true;
		}
		return false;
	}

	mocha.graphics.Point targetContentOffsetForProposedContentOffsetWithScrollingVelocity(mocha.graphics.Point proposedContentOffset, mocha.graphics.Point velocity) {
		return proposedContentOffset;
	}

	void prepareLayout() {
		// custom ivars
		objc_setAssociatedObject(this, &CollectionViewFlowLayout.PSTCachedItemRectsKey, new HashMap(), OBJC_ASSOCIATION_RETAIN_NONATOMIC);

		_data = new GridLayoutInfo(); // clear old layout data
		_data.setHorizontal(this.getScrollDirection() == CollectionViewFlowLayout.CollectionViewScrollDirection.HORIZONTAL);
		_visibleBounds = this.getCollectionView().getBounds();
		mocha.graphics.Size collectionViewSize = _visibleBounds.size;
		_data.setDimension(_data.getHorizontal() ? collectionViewSize.height : collectionViewSize.width);
		_data.setRowAlignmentOptions(_rowAlignmentsOptionsDictionary);
		this.fetchItemsInfo();
	}

	void fetchItemsInfo() {
		this.getSizingInfos();
		this.updateItemsLayout();
	}

	void getSizingInfos() {
		mocha.foundation.Assert(_data.getSections().size() == 0, "Grid layout is already populated?");

		CollectionView.DelegateFlowLayout flowDataSource = (CollectionView.DelegateFlowLayout)this.getCollectionView().getDelegate();

		boolean implementsSizeDelegate = flowDataSource.respondsToSelector("collectionView");
		boolean implementsHeaderReferenceDelegate = flowDataSource.respondsToSelector("collectionView");
		boolean implementsFooterReferenceDelegate = flowDataSource.respondsToSelector("collectionView");

		int numberOfSections = this.getCollectionView().numberOfSections();
		for (int section = 0; section < numberOfSections; section++) {
		    GridLayoutSection layoutSection = _data.addSection();
		    layoutSection.setVerticalInterstice(_data.getHorizontal() ? this.getMinimumInteritemSpacing() : this.getMinimumLineSpacing());
		    layoutSection.setHorizontalInterstice(!_data.getHorizontal() ? this.getMinimumInteritemSpacing() : this.getMinimumLineSpacing());

		    if (flowDataSource.respondsToSelector("collectionView")) {
		        layoutSection.setSectionMargins(flowDataSource.collectionViewLayoutInsetForSectionAtIndex(this.getCollectionView(), this, section));
		    }else {
		        layoutSection.setSectionMargins(this.getSectionInset());
		    }

		    if (flowDataSource.respondsToSelector("collectionView")) {
		        float minimumLineSpacing = flowDataSource.collectionViewLayoutMinimumLineSpacingForSectionAtIndex(this.getCollectionView(), this, section);
		        if (_data.getHorizontal()) {
		            layoutSection.setHorizontalInterstice(minimumLineSpacing);
		        }else {
		            layoutSection.setVerticalInterstice(minimumLineSpacing);
		        }
		    }

		    if (flowDataSource.respondsToSelector("collectionView")) {
		        float minimumInterimSpacing = flowDataSource.collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex(this.getCollectionView(), this, section);
		        if (_data.getHorizontal()) {
		            layoutSection.setVerticalInterstice(minimumInterimSpacing);
		        }else {
		            layoutSection.setHorizontalInterstice(minimumInterimSpacing);
		        }
		    }

		    mocha.graphics.Size headerReferenceSize;
		    if (implementsHeaderReferenceDelegate) {
		        headerReferenceSize = flowDataSource.collectionViewLayoutReferenceSizeForHeaderInSection(this.getCollectionView(), this, section);
		    }else {
		        headerReferenceSize = this.getHeaderReferenceSize();
		    }
		    layoutSection.setHeaderDimension(_data.getHorizontal() ? headerReferenceSize.width : headerReferenceSize.height);

		    mocha.graphics.Size footerReferenceSize;
		    if (implementsFooterReferenceDelegate) {
		        footerReferenceSize = flowDataSource.collectionViewLayoutReferenceSizeForFooterInSection(this.getCollectionView(), this, section);
		    }else {
		        footerReferenceSize = this.getFooterReferenceSize();
		    }
		    layoutSection.setFooterDimension(_data.getHorizontal() ? footerReferenceSize.width : footerReferenceSize.height);

		    int numberOfItems = this.getCollectionView().numberOfItemsInSection(section);

		    // if delegate implements size delegate, query it for all items
		    if (implementsSizeDelegate) {
		        for (int item = 0; item < numberOfItems; item++) {
		            mocha.foundation.IndexPath indexPath = mocha.foundation.IndexPath.indexPathForItemInSection(item, section);
		            mocha.graphics.Size itemSize = implementsSizeDelegate ? flowDataSource.collectionViewLayoutSizeForItemAtIndexPath(this.getCollectionView(), this, indexPath) : this.getItemSize();

		            GridLayoutItem layoutItem = layoutSection.addItem();
		            layoutItem.setItemFrame(new mocha.graphics.Rect(mocha.graphics.Point.zero(), itemSize));
		        }
		        // if not, go the fast path
		    }else {
		        layoutSection.setFixedItemSize(true);
		        layoutSection.setItemSize(this.getItemSize());
		        layoutSection.setItemsCount(numberOfItems);
		    }
		}
	}

	void updateItemsLayout() {
		mocha.graphics.Size contentSize = mocha.graphics.Size.zero();
		for (GridLayoutSection section  : _data.getSections()) {
		    section.computeLayout();

		    // update section offset to make frame absolute (section only calculates relative)
		    mocha.graphics.Rect sectionFrame = section.getFrame();
		    if (_data.getHorizontal()) {
		        sectionFrame.origin.x += contentSize.width;
		        contentSize.width += section.getFrame().getSize().getWidth() + section.getFrame().getOrigin().x;
		        contentSize.height = Math.max(contentSize.height, sectionFrame.size.height + section.getFrame().getOrigin().y + section.getSectionMargins().getTop() + section.getSectionMargins().getBottom());
		    }else {
		        sectionFrame.origin.y += contentSize.height;
		        contentSize.height += sectionFrame.size.height + section.getFrame().getOrigin().y;
		        contentSize.width = Math.max(contentSize.width, sectionFrame.size.width + section.getFrame().getOrigin().x + section.getSectionMargins().getLeft() + section.getSectionMargins().getRight());
		    }
		    section.setFrame(sectionFrame);
		}
		_data.setContentSize(contentSize);
	}

	/* Setters & Getters */
	/* ========================================== */

	public float getMinimumLineSpacing() {
		return this._lineSpacing;
	}

	public void setMinimumLineSpacing(float minimumLineSpacing) {
		this._lineSpacing = minimumLineSpacing;
	}

	public float getMinimumInteritemSpacing() {
		return this._interitemSpacing;
	}

	public void setMinimumInteritemSpacing(float minimumInteritemSpacing) {
		this._interitemSpacing = minimumInteritemSpacing;
	}

	public mocha.graphics.Size getItemSize() {
		if(this._itemSize != null) {
			return this._itemSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setItemSize(mocha.graphics.Size itemSize) {
		if(this._itemSize != null) {
			this._itemSize = itemSize.copy();
		} else {
			this._itemSize = mocha.graphics.Size.zero();
		}
	}

	public CollectionViewFlowLayout.CollectionViewScrollDirection getScrollDirection() {
		return this._scrollDirection;
	}

	public void setScrollDirection(CollectionViewFlowLayout.CollectionViewScrollDirection scrollDirection) {
		this._scrollDirection = scrollDirection;
	}

	public mocha.graphics.Size getHeaderReferenceSize() {
		if(this._headerReferenceSize != null) {
			return this._headerReferenceSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setHeaderReferenceSize(mocha.graphics.Size headerReferenceSize) {
		if(this._headerReferenceSize != null) {
			this._headerReferenceSize = headerReferenceSize.copy();
		} else {
			this._headerReferenceSize = mocha.graphics.Size.zero();
		}
	}

	public mocha.graphics.Size getFooterReferenceSize() {
		if(this._footerReferenceSize != null) {
			return this._footerReferenceSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setFooterReferenceSize(mocha.graphics.Size footerReferenceSize) {
		if(this._footerReferenceSize != null) {
			this._footerReferenceSize = footerReferenceSize.copy();
		} else {
			this._footerReferenceSize = mocha.graphics.Size.zero();
		}
	}

	public mocha.ui.EdgeInsets getSectionInset() {
		if(this._sectionInset != null) {
			return this._sectionInset.copy();
		} else {
			return mocha.ui.EdgeInsets.zero();
		}
	}

	public void setSectionInset(mocha.ui.EdgeInsets sectionInset) {
		if(this._sectionInset != null) {
			this._sectionInset = sectionInset.copy();
		} else {
			this._sectionInset = mocha.ui.EdgeInsets.zero();
		}
	}

	public HashMap getRowAlignmentOptions() {
		return this._rowAlignmentsOptionsDictionary;
	}

	public void setRowAlignmentOptions(HashMap rowAlignmentOptions) {
		this._rowAlignmentsOptionsDictionary = rowAlignmentOptions;
	}

}

