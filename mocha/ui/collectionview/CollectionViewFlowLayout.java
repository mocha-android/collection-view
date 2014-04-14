package mocha.ui.collectionview;

import android.util.SparseArray;
import mocha.foundation.Assert;
import mocha.foundation.IndexPath;
import mocha.foundation.OptionalInterface;
import mocha.foundation.OptionalInterfaceHelper;
import mocha.graphics.Rect;
import mocha.ui.EdgeInsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionViewFlowLayout extends CollectionViewLayout {
	public static final String PSTCollectionElementKindSectionHeader = "UICollectionElementKindSectionHeader";
	public static final String PSTCollectionElementKindSectionFooter = "UICollectionElementKindSectionFooter";

	static final String PSTFlowLayoutCommonRowHorizontalAlignmentKey = "UIFlowLayoutCommonRowHorizontalAlignmentKey";
	static final String PSTFlowLayoutLastRowHorizontalAlignmentKey = "UIFlowLayoutLastRowHorizontalAlignmentKey";
	static final String PSTFlowLayoutRowVerticalAlignmentKey = "UIFlowLayoutRowVerticalAlignmentKey";

	private mocha.graphics.Size _itemSize;
	private CollectionViewFlowLayout.CollectionViewScrollDirection _scrollDirection;
	private mocha.graphics.Size _headerReferenceSize;
	private mocha.graphics.Size _footerReferenceSize;
	private mocha.ui.EdgeInsets _sectionInset;
	private Map<String,FlowLayoutAlignment> _rowAlignmentsOptionsDictionary;
	private float _lineSpacing;
	private float _interitemSpacing;
	private GridLayoutInfo _data;
	private mocha.graphics.Size _currentLayoutSize;
	private Map _insertedItemsAttributesDict;
	private Map _insertedSectionHeadersAttributesDict;
	private Map _insertedSectionFootersAttributesDict;
	private Map _deletedItemsAttributesDict;
	private Map _deletedSectionHeadersAttributesDict;
	private Map _deletedSectionFootersAttributesDict;
	private mocha.graphics.Rect _visibleBounds;
	private GridLayoutFlagsStruct _gridLayoutFlags = new GridLayoutFlagsStruct();
	private SparseArray<List<Rect>> rectCache;

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

	enum FlowLayoutAlignment {
		MIN,
		MID,
		MAX,
		JUSTIFY
	}

	public CollectionViewFlowLayout() {
		this._itemSize = new mocha.graphics.Size(50.f, 50.f);
		this._lineSpacing = 10.f;
		this._interitemSpacing = 10.f;
		this._sectionInset = mocha.ui.EdgeInsets.zero();
		this._scrollDirection = CollectionViewScrollDirection.VERTICAL;
		this._headerReferenceSize = mocha.graphics.Size.zero();
		this._footerReferenceSize = mocha.graphics.Size.zero();

		this.rectCache = new SparseArray<>();
					
		// set default values for row alignment.
		_rowAlignmentsOptionsDictionary = mocha.foundation.Maps.create(
			// TODO, those values are some enum. find out what that is.
			PSTFlowLayoutCommonRowHorizontalAlignmentKey, CollectionViewFlowLayout.FlowLayoutAlignment.JUSTIFY,
			PSTFlowLayoutLastRowHorizontalAlignmentKey, CollectionViewFlowLayout.FlowLayoutAlignment.JUSTIFY,
			PSTFlowLayoutRowVerticalAlignmentKey, CollectionViewFlowLayout.FlowLayoutAlignment.MIN
		);
	}

	public void collectionViewDelegateDidChange() {
		super.collectionViewDelegateDidChange();

		CollectionView.Delegate delegate = this.getCollectionView().getDelegate();
		CollectionViewDelegateFlowLayout flowLayoutDelegate;

		if(delegate instanceof CollectionViewDelegateFlowLayout) {
			flowLayoutDelegate = (CollectionViewDelegateFlowLayout)delegate;
		} else {
			flowLayoutDelegate = null;
		}

		this._gridLayoutFlags.delegateSizeForItem = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, "collectionViewLayoutSizeForItemAtIndexPath", CollectionView.class, CollectionViewLayout.class, IndexPath.class);
		this._gridLayoutFlags.delegateInsetForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, "collectionViewLayoutInsetForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this._gridLayoutFlags.delegateLineSpacingForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, "collectionViewLayoutMinimumLineSpacingForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this._gridLayoutFlags.delegateInteritemSpacingForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, "collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this._gridLayoutFlags.delegateReferenceSizeForHeader = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, "collectionViewLayoutReferenceSizeForHeaderInSection", CollectionView.class, CollectionViewLayout.class, int.class);
		this._gridLayoutFlags.delegateReferenceSizeForFooter = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, "collectionViewLayoutReferenceSizeForFooterInSection", CollectionView.class, CollectionViewLayout.class, int.class);
	}

	public List<Attributes> layoutAttributesForElementsInRect(mocha.graphics.Rect rect) {
		// Apple calls _layoutAttributesForItemsInRect
		if (_data == null) this.prepareLayout();

		List<Attributes> layoutAttributesArray = new ArrayList<>();

		for (GridLayoutSection section  : _data.getSections()) {
		    if (section.getFrame().intersects(rect)) {

		        // if we have fixed size, calculate item frames only once.
		        // this also uses the default PSTFlowLayoutCommonRowHorizontalAlignmentKey alignment
		        // for the last row. (we want this effect!)
		        int sectionIndex = _data.getSections().indexOf(section);

		        mocha.graphics.Rect normalizedHeaderFrame = section.getHeaderFrame();
		        normalizedHeaderFrame.origin.x += section.getFrame().origin.x;
		        normalizedHeaderFrame.origin.y += section.getFrame().origin.y;
		        if (!normalizedHeaderFrame.empty() && normalizedHeaderFrame.intersects(rect)) {
		            CollectionViewLayout.Attributes layoutAttributes = Attributes.layoutAttributesForSupplementaryViewOfKindWithIndexPath(this.layoutAttributesClass(), PSTCollectionElementKindSectionHeader, mocha.foundation.IndexPath.withItemInSection(0, sectionIndex));
		            layoutAttributes.setFrame(normalizedHeaderFrame);
		            layoutAttributesArray.add(layoutAttributes);
		        }

		        List<Rect> itemRects = rectCache.get(sectionIndex);
		        if (itemRects == null && section.getFixedItemSize() && section.getRows().size() > 0) {
		            itemRects = (section.getRows()).get(0).itemRects();
		            if (itemRects != null) {
						rectCache.put(sectionIndex, itemRects);
					}
		        }
		        
		        for (GridLayoutRow row  : section.getRows()) {
		            mocha.graphics.Rect normalizedRowFrame = row.getRowFrame();
		            
		            normalizedRowFrame.origin.x += section.getFrame().origin.x;
		            normalizedRowFrame.origin.y += section.getFrame().origin.y;
		            
		            if (normalizedRowFrame.intersects(rect)) {
		                // TODO be more fine-grained for items

		                for (int itemIndex = 0; itemIndex < row.getItemCount(); itemIndex++) {
		                    CollectionViewLayout.Attributes layoutAttributes;
		                    int sectionItemIndex;
		                    mocha.graphics.Rect itemFrame;

		                    if (row.getFixedItemSize() && itemRects != null) {
		                        itemFrame = itemRects.get(itemIndex);
		                        sectionItemIndex = row.getIndex() * section.getItemsByRowCount() + itemIndex;
		                    } else {
		                        GridLayoutItem item = row.getItems().get(itemIndex);
		                        sectionItemIndex = section.getItems().indexOf(item);
		                        itemFrame = item.getItemFrame();
		                    }

		                    mocha.graphics.Rect normalisedItemFrame = new mocha.graphics.Rect(normalizedRowFrame.origin.x + itemFrame.origin.x, normalizedRowFrame.origin.y + itemFrame.origin.y, itemFrame.size.width, itemFrame.size.height);
		                    
		                    if (normalisedItemFrame.intersects(rect)) {
		                        layoutAttributes = Attributes.layoutAttributesForCellWithIndexPath(this.layoutAttributesClass(), mocha.foundation.IndexPath.withItemInSection(sectionItemIndex, sectionIndex));
		                        layoutAttributes.setFrame(normalisedItemFrame);
		                        layoutAttributesArray.add(layoutAttributes);
		                    }
		                }
		            }
		        }

		        mocha.graphics.Rect normalizedFooterFrame = section.getFooterFrame();
		        normalizedFooterFrame.origin.x += section.getFrame().origin.x;
		        normalizedFooterFrame.origin.y += section.getFrame().origin.y;
		        if (!normalizedFooterFrame.empty() && normalizedFooterFrame.intersects(rect)) {
		            CollectionViewLayout.Attributes layoutAttributes = Attributes.layoutAttributesForSupplementaryViewOfKindWithIndexPath(this.layoutAttributesClass(), PSTCollectionElementKindSectionFooter, mocha.foundation.IndexPath.indexPathForItemInSection(0, sectionIndex));
		            layoutAttributes.setFrame(normalizedFooterFrame);
		            layoutAttributesArray.add(layoutAttributes);
		        }
		    }
		}
		return layoutAttributesArray;
	}

	public CollectionViewLayout.Attributes layoutAttributesForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		if (_data == null) this.prepareLayout();

		GridLayoutSection section = _data.getSections().get(indexPath.section);
		GridLayoutRow row = null;
		mocha.graphics.Rect itemFrame = mocha.graphics.Rect.zero();

		if (section.getFixedItemSize() && section.getItemsByRowCount() > 0 && indexPath.item / section.getItemsByRowCount() < section.getRows().size()) {
		    row = section.getRows().get(indexPath.item / section.getItemsByRowCount());

		    int itemIndex = indexPath.item % section.getItemsByRowCount();
		    List<Rect> itemRects = row.itemRects();
		    itemFrame = itemRects.get(itemIndex);
		}else if (indexPath.item < section.getItems().size()) {
		    GridLayoutItem item = section.getItems().get(indexPath.item);
		    row = item.getRowObject();
		    itemFrame = item.getItemFrame();
		}

		CollectionViewLayout.Attributes layoutAttributes = Attributes.layoutAttributesForCellWithIndexPath(this.layoutAttributesClass(), indexPath);

		if(row != null) {
			// calculate item rect
			mocha.graphics.Rect normalizedRowFrame = row.getRowFrame();
			normalizedRowFrame.origin.x += section.getFrame().origin.x;
			normalizedRowFrame.origin.y += section.getFrame().origin.y;
			layoutAttributes.setFrame(new mocha.graphics.Rect(normalizedRowFrame.origin.x + itemFrame.origin.x, normalizedRowFrame.origin.y + itemFrame.origin.y, itemFrame.size.width, itemFrame.size.height));
		}

		return layoutAttributes;
	}

	public CollectionViewLayout.Attributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, mocha.foundation.IndexPath indexPath) {
		if (_data == null) this.prepareLayout();

		int sectionIndex = indexPath.section;
		CollectionViewLayout.Attributes layoutAttributes = null;

		if (sectionIndex < _data.getSections().size()) {
		    GridLayoutSection section = _data.getSections().get(sectionIndex);

		    mocha.graphics.Rect normalizedFrame = mocha.graphics.Rect.zero();
		    if (kind.equals(PSTCollectionElementKindSectionHeader)) {
		        normalizedFrame = section.getHeaderFrame();
		    }
		    else if (kind.equals(PSTCollectionElementKindSectionFooter)) {
		        normalizedFrame = section.getFooterFrame();
		    }

		    if (!normalizedFrame.empty()) {
		        normalizedFrame.origin.x += section.getFrame().origin.x;
		        normalizedFrame.origin.y += section.getFrame().origin.y;

		        layoutAttributes = Attributes.layoutAttributesForSupplementaryViewOfKindWithIndexPath(this.layoutAttributesClass(), kind, mocha.foundation.IndexPath.withItemInSection(0, sectionIndex));
		        layoutAttributes.setFrame(normalizedFrame);
		    }
		}
		return layoutAttributes;
	}

	public Attributes layoutAttributesForDecorationViewOfKindAtIndexPath(String kind, IndexPath indexPath) {
		return null;
	}

	CollectionViewLayout.Attributes layoutAttributesForDecorationViewWithReuseIdentifierAtIndexPath(String identifier, mocha.foundation.IndexPath indexPath) {
		return null;
	}

	public mocha.graphics.Size collectionViewContentSize() {
		if (_data == null) this.prepareLayout();

		return _data.getContentSize();
	}

	CollectionReusableView decorationViewForCollectionViewWithReuseIdentifierIndexPath(CollectionView collectionView, String reuseIdentifier, IndexPath indexPath) {
		return null;
	}

	public void setSectionInset(mocha.ui.EdgeInsets sectionInset) {
		sectionInset = sectionInset != null ? sectionInset : EdgeInsets.zero();

		if(!sectionInset.equals(this._sectionInset)) {
		    _sectionInset = sectionInset;
		    this.invalidateLayout();
		}
	}

	public void invalidateLayout() {
		super.invalidateLayout();
		this.rectCache.clear();
		_data = null;
	}

	public boolean shouldInvalidateLayoutForBoundsChange(mocha.graphics.Rect newBounds) {
		// we need to recalculate on width changes
		if ((_visibleBounds.size.width != newBounds.size.width && this.getScrollDirection() == CollectionViewFlowLayout.CollectionViewScrollDirection.VERTICAL) || (_visibleBounds.size.height != newBounds.size.height && this.getScrollDirection() == CollectionViewFlowLayout.CollectionViewScrollDirection.HORIZONTAL)) {
		    _visibleBounds = this.getCollectionView().getBounds();
		    return true;
		}
		return false;
	}

	public mocha.graphics.Point targetContentOffsetForProposedContentOffsetWithScrollingVelocity(mocha.graphics.Point proposedContentOffset, mocha.graphics.Point velocity) {
		return proposedContentOffset;
	}

	public void prepareLayout() {
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
		Assert.condition(_data.getSections().size() == 0, "Grid layout is already populated?");


		CollectionViewDelegateFlowLayout flowDataSource = null;

		if(this.getCollectionView().getDelegate() instanceof CollectionViewDelegateFlowLayout) {
			flowDataSource = (CollectionViewDelegateFlowLayout)this.getCollectionView().getDelegate();
		}

		int numberOfSections = this.getCollectionView().numberOfSections();
		for (int section = 0; section < numberOfSections; section++) {
		    GridLayoutSection layoutSection = _data.addSection();
		    layoutSection.setVerticalInterstice(_data.getHorizontal() ? this.getMinimumInteritemSpacing() : this.getMinimumLineSpacing());
		    layoutSection.setHorizontalInterstice(!_data.getHorizontal() ? this.getMinimumInteritemSpacing() : this.getMinimumLineSpacing());

		    if (flowDataSource != null && this._gridLayoutFlags.delegateInsetForSection) {
		        layoutSection.setSectionMargins(flowDataSource.collectionViewLayoutInsetForSectionAtIndex(this.getCollectionView(), this, section));
		    }else {
		        layoutSection.setSectionMargins(this.getSectionInset());
		    }

		    if (flowDataSource != null && this._gridLayoutFlags.delegateLineSpacingForSection) {
		        float minimumLineSpacing = flowDataSource.collectionViewLayoutMinimumLineSpacingForSectionAtIndex(this.getCollectionView(), this, section);
		        if (_data.getHorizontal()) {
		            layoutSection.setHorizontalInterstice(minimumLineSpacing);
		        }else {
		            layoutSection.setVerticalInterstice(minimumLineSpacing);
		        }
		    }

			if (flowDataSource != null && this._gridLayoutFlags.delegateInteritemSpacingForSection) {
		        float minimumInterimSpacing = flowDataSource.collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex(this.getCollectionView(), this, section);
		        if (_data.getHorizontal()) {
		            layoutSection.setVerticalInterstice(minimumInterimSpacing);
		        }else {
		            layoutSection.setHorizontalInterstice(minimumInterimSpacing);
		        }
		    }

		    mocha.graphics.Size headerReferenceSize;
			if (flowDataSource != null && this._gridLayoutFlags.delegateReferenceSizeForHeader) {
		        headerReferenceSize = flowDataSource.collectionViewLayoutReferenceSizeForHeaderInSection(this.getCollectionView(), this, section);
		    } else {
		        headerReferenceSize = this.getHeaderReferenceSize();
		    }
		    layoutSection.setHeaderDimension(_data.getHorizontal() ? headerReferenceSize.width : headerReferenceSize.height);

		    mocha.graphics.Size footerReferenceSize;
			if (flowDataSource != null && this._gridLayoutFlags.delegateReferenceSizeForFooter) {
		        footerReferenceSize = flowDataSource.collectionViewLayoutReferenceSizeForFooterInSection(this.getCollectionView(), this, section);
		    } else {
		        footerReferenceSize = this.getFooterReferenceSize();
		    }
		    layoutSection.setFooterDimension(_data.getHorizontal() ? footerReferenceSize.width : footerReferenceSize.height);

		    int numberOfItems = this.getCollectionView().numberOfItemsInSection(section);

		    // if delegate implements size delegate, query it for all items
			if (flowDataSource != null && this._gridLayoutFlags.delegateSizeForItem) {
		        for (int item = 0; item < numberOfItems; item++) {
		            mocha.foundation.IndexPath indexPath = mocha.foundation.IndexPath.withItemInSection(item, section);
		            mocha.graphics.Size itemSize = flowDataSource.collectionViewLayoutSizeForItemAtIndexPath(this.getCollectionView(), this, indexPath);

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
		        contentSize.width += section.getFrame().size.width + section.getFrame().origin.x;
		        contentSize.height = Math.max(contentSize.height, sectionFrame.size.height + section.getFrame().origin.y + section.getSectionMargins().top + section.getSectionMargins().bottom);
		    }else {
		        sectionFrame.origin.y += contentSize.height;
		        contentSize.height += sectionFrame.size.height + section.getFrame().origin.y;
		        contentSize.width = Math.max(contentSize.width, sectionFrame.size.width + section.getFrame().origin.x + section.getSectionMargins().left + section.getSectionMargins().right);
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

	public Map<String,FlowLayoutAlignment> getRowAlignmentOptions() {
		return this._rowAlignmentsOptionsDictionary;
	}

	public void setRowAlignmentOptions(Map<String,FlowLayoutAlignment> rowAlignmentOptions) {
		this._rowAlignmentsOptionsDictionary.clear();

		if(rowAlignmentOptions != null) {
			this._rowAlignmentsOptionsDictionary.putAll(rowAlignmentOptions);
		}
	}

}

