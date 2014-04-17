package mocha.ui.collectionview;

import android.util.SparseArray;
import mocha.foundation.Assert;
import mocha.foundation.IndexPath;
import mocha.foundation.OptionalInterfaceHelper;
import mocha.graphics.Point;
import mocha.graphics.Rect;
import mocha.graphics.Size;
import mocha.ui.EdgeInsets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CollectionViewFlowLayout extends CollectionViewLayout {
	public static final String PSTCollectionElementKindSectionHeader = "UICollectionElementKindSectionHeader";
	public static final String PSTCollectionElementKindSectionFooter = "UICollectionElementKindSectionFooter";

	static final String PSTFlowLayoutCommonRowHorizontalAlignmentKey = "UIFlowLayoutCommonRowHorizontalAlignmentKey";
	static final String PSTFlowLayoutLastRowHorizontalAlignmentKey = "UIFlowLayoutLastRowHorizontalAlignmentKey";
	static final String PSTFlowLayoutRowVerticalAlignmentKey = "UIFlowLayoutRowVerticalAlignmentKey";

	private Size _itemSize;
	private CollectionViewFlowLayout.CollectionViewScrollDirection _scrollDirection;
	private Size _headerReferenceSize;
	private Size _footerReferenceSize;
	private mocha.ui.EdgeInsets _sectionInset;
	private Map<String, FlowLayoutAlignment> _rowAlignmentsOptionsDictionary;
	private float _lineSpacing;
	private float _interitemSpacing;
	private GridLayoutInfo _data;
	private Size _currentLayoutSize;
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
		Size collectionViewLayoutSizeForItemAtIndexPath(CollectionView collectionView, CollectionViewLayout collectionViewLayout, mocha.foundation.IndexPath indexPath);

		@Optional
		mocha.ui.EdgeInsets collectionViewLayoutInsetForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		float collectionViewLayoutMinimumLineSpacingForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		float collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		Size collectionViewLayoutReferenceSizeForHeaderInSection(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

		@Optional
		Size collectionViewLayoutReferenceSizeForFooterInSection(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section);

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
		this._itemSize = new Size(50.f, 50.f);
		this._lineSpacing = 10.f;
		this._interitemSpacing = 10.f;
		this._sectionInset = mocha.ui.EdgeInsets.zero();
		this._scrollDirection = CollectionViewScrollDirection.VERTICAL;
		this._headerReferenceSize = Size.zero();
		this._footerReferenceSize = Size.zero();

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

		if (delegate instanceof CollectionViewDelegateFlowLayout) {
			flowLayoutDelegate = (CollectionViewDelegateFlowLayout) delegate;
		} else {
			flowLayoutDelegate = null;
		}

		this._gridLayoutFlags.delegateSizeForItem = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, CollectionViewDelegateFlowLayout.class, "collectionViewLayoutSizeForItemAtIndexPath", CollectionView.class, CollectionViewLayout.class, IndexPath.class);
		this._gridLayoutFlags.delegateInsetForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, CollectionViewDelegateFlowLayout.class, "collectionViewLayoutInsetForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this._gridLayoutFlags.delegateLineSpacingForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, CollectionViewDelegateFlowLayout.class, "collectionViewLayoutMinimumLineSpacingForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this._gridLayoutFlags.delegateInteritemSpacingForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, CollectionViewDelegateFlowLayout.class, "collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this._gridLayoutFlags.delegateReferenceSizeForHeader = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, CollectionViewDelegateFlowLayout.class, "collectionViewLayoutReferenceSizeForHeaderInSection", CollectionView.class, CollectionViewLayout.class, int.class);
		this._gridLayoutFlags.delegateReferenceSizeForFooter = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, CollectionViewDelegateFlowLayout.class, "collectionViewLayoutReferenceSizeForFooterInSection", CollectionView.class, CollectionViewLayout.class, int.class);
	}

	public List<Attributes> layoutAttributesForElementsInRect(mocha.graphics.Rect rect) {
		// Apple calls _layoutAttributesForItemsInRect
		if (_data == null) this.prepareLayout();

		List<Attributes> layoutAttributesArray = new ArrayList<>();

		MWarn("CV_TEST layoutAttributesForElementsInRect: " + _data.getSections().size() + ", " + rect);

		List<GridLayoutSection> sections = _data.getSections();
		int sectionsCount = _data.getSections().size();
		for(int sectionIndex = 0; sectionIndex < sectionsCount; sectionIndex++) {
			GridLayoutSection section = sections.get(sectionIndex);

			MLog("CV_TEST layoutAttributesForElementsInRect - section: " + section.getFrame() + ", " + section.getRows());

			if (section.getFrame().intersects(rect)) {
				// if we have fixed size, calculate item frames only once.
				// this also uses the default PSTFlowLayoutCommonRowHorizontalAlignmentKey alignment
				// for the last row. (we want this effect!)

				Rect normalizedHeaderFrame = section.getHeaderFrame();
				normalizedHeaderFrame.origin.x += section.getFrame().origin.x;
				normalizedHeaderFrame.origin.y += section.getFrame().origin.y;
				if (!normalizedHeaderFrame.empty() && normalizedHeaderFrame.intersects(rect)) {
					Attributes layoutAttributes = Attributes.layoutAttributesForSupplementaryViewOfKindWithIndexPath(this.layoutAttributesClass(), PSTCollectionElementKindSectionHeader, IndexPath.withItemInSection(0, sectionIndex));
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

				for (GridLayoutRow row : section.getRows()) {
					Rect normalizedRowFrame = row.getRowFrame();

					normalizedRowFrame.origin.x += section.getFrame().origin.x;
					normalizedRowFrame.origin.y += section.getFrame().origin.y;

					if (normalizedRowFrame.intersects(rect)) {
						// TODO be more fine-grained for items

						int itemCount = row.getItemCount();

						for (int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
							Attributes layoutAttributes;
							int sectionItemIndex;
							Rect itemFrame;

							if (row.getFixedItemSize() && itemRects != null) {
								itemFrame = itemRects.get(itemIndex);
								sectionItemIndex = row.getIndex() * section.getItemsByRowCount() + itemIndex;
							} else {
								GridLayoutItem item = row.getItems().get(itemIndex);
								sectionItemIndex = section.getItems().indexOf(item);
								itemFrame = item.getItemFrame();
							}

							Rect normalizedItemFrame = new Rect(normalizedRowFrame.origin.x + itemFrame.origin.x, normalizedRowFrame.origin.y + itemFrame.origin.y, itemFrame.size.width, itemFrame.size.height);

							if (normalizedItemFrame.intersects(rect)) {
								layoutAttributes = Attributes.layoutAttributesForCellWithIndexPath(this.layoutAttributesClass(), IndexPath.withItemInSection(sectionItemIndex, sectionIndex));
								layoutAttributes.setFrame(normalizedItemFrame);
								layoutAttributesArray.add(layoutAttributes);
								MWarn("CV_TEST ADDING: " + sectionItemIndex + ", " + normalizedItemFrame);
							}
						}
					}
				}

				Rect normalizedFooterFrame = section.getFooterFrame();
				normalizedFooterFrame.origin.x += section.getFrame().origin.x;
				normalizedFooterFrame.origin.y += section.getFrame().origin.y;
				if (!normalizedFooterFrame.empty() && normalizedFooterFrame.intersects(rect)) {
					Attributes layoutAttributes = Attributes.layoutAttributesForSupplementaryViewOfKindWithIndexPath(this.layoutAttributesClass(), PSTCollectionElementKindSectionFooter, IndexPath.withItemInSection(0, sectionIndex));
					layoutAttributes.setFrame(normalizedFooterFrame);
					layoutAttributesArray.add(layoutAttributes);
				}
			}
		}

		MWarn("CV_TEST layoutAttributesArray: " + layoutAttributesArray);

		return layoutAttributesArray;
	}

	public CollectionViewLayout.Attributes layoutAttributesForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		if (_data == null) this.prepareLayout();

		GridLayoutSection section = _data.getSections().get(indexPath.section);
		GridLayoutRow row = null;
		Rect itemFrame = mocha.graphics.Rect.zero();

		if (section.getFixedItemSize() && section.getItemsByRowCount() > 0 && indexPath.item / section.getItemsByRowCount() < section.getRows().size()) {
			row = section.getRows().get(indexPath.item / section.getItemsByRowCount());

			int itemIndex = indexPath.item % section.getItemsByRowCount();
			List<Rect> itemRects = row.itemRects();
			itemFrame = itemRects.get(itemIndex);
		} else if (indexPath.item < section.getItems().size()) {
			GridLayoutItem item = section.getItems().get(indexPath.item);
			row = item.getRowObject();
			itemFrame = item.getItemFrame();
		}

		CollectionViewLayout.Attributes layoutAttributes = Attributes.layoutAttributesForCellWithIndexPath(this.layoutAttributesClass(), indexPath);

		if (row != null) {
			// calculate item rect
			Rect normalizedRowFrame = row.getRowFrame();
			// normalizedRowFrame.offset(section.getFrame().origin);
			normalizedRowFrame.origin.x += section.getFrame().origin.x;
			normalizedRowFrame.origin.y += section.getFrame().origin.y;
			layoutAttributes.setFrame(new Rect(normalizedRowFrame.origin.x + itemFrame.origin.x, normalizedRowFrame.origin.y + itemFrame.origin.y, itemFrame.size.width, itemFrame.size.height));
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
			} else if (kind.equals(PSTCollectionElementKindSectionFooter)) {
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

	public Size collectionViewContentSize() {
		if (_data == null) this.prepareLayout();

		return _data.getContentSize();
	}

	CollectionReusableView decorationViewForCollectionViewWithReuseIdentifierIndexPath(CollectionView collectionView, String reuseIdentifier, IndexPath indexPath) {
		return null;
	}

	public void setSectionInset(mocha.ui.EdgeInsets sectionInset) {
		sectionInset = sectionInset != null ? sectionInset.copy() : EdgeInsets.zero();

		if (!sectionInset.equals(this._sectionInset)) {
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
		if (_visibleBounds == null || (_visibleBounds.size.width != newBounds.size.width && this.getScrollDirection() == CollectionViewFlowLayout.CollectionViewScrollDirection.VERTICAL) || (_visibleBounds.size.height != newBounds.size.height && this.getScrollDirection() == CollectionViewFlowLayout.CollectionViewScrollDirection.HORIZONTAL)) {
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
		Size collectionViewSize = _visibleBounds.size;
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
		MWarn("CV_TEST,  IN getSizingInfos(): " + this._itemSize + ", " + this._data.getSections().size());

		CollectionViewDelegateFlowLayout flowDataSource = null;

		if (this.getCollectionView().getDelegate() instanceof CollectionViewDelegateFlowLayout) {
			flowDataSource = (CollectionViewDelegateFlowLayout) this.getCollectionView().getDelegate();
		}

		int numberOfSections = this.getCollectionView().numberOfSections();
		for (int section = 0; section < numberOfSections; section++) {
			GridLayoutSection layoutSection = _data.addSection();
			layoutSection.setVerticalInterstice(_data.getHorizontal() ? this.getMinimumInteritemSpacing() : this.getMinimumLineSpacing());
			layoutSection.setHorizontalInterstice(!_data.getHorizontal() ? this.getMinimumInteritemSpacing() : this.getMinimumLineSpacing());

			if (flowDataSource != null && this._gridLayoutFlags.delegateInsetForSection) {
				layoutSection.setSectionMargins(flowDataSource.collectionViewLayoutInsetForSectionAtIndex(this.getCollectionView(), this, section));
			} else {
				layoutSection.setSectionMargins(this.getSectionInset());
			}

			if (flowDataSource != null && this._gridLayoutFlags.delegateLineSpacingForSection) {
				float minimumLineSpacing = flowDataSource.collectionViewLayoutMinimumLineSpacingForSectionAtIndex(this.getCollectionView(), this, section);
				if (_data.getHorizontal()) {
					layoutSection.setHorizontalInterstice(minimumLineSpacing);
				} else {
					layoutSection.setVerticalInterstice(minimumLineSpacing);
				}
			}

			if (flowDataSource != null && this._gridLayoutFlags.delegateInteritemSpacingForSection) {
				float minimumInterimSpacing = flowDataSource.collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex(this.getCollectionView(), this, section);
				if (_data.getHorizontal()) {
					layoutSection.setVerticalInterstice(minimumInterimSpacing);
				} else {
					layoutSection.setHorizontalInterstice(minimumInterimSpacing);
				}
			}

			Size headerReferenceSize;
			if (flowDataSource != null && this._gridLayoutFlags.delegateReferenceSizeForHeader) {
				headerReferenceSize = flowDataSource.collectionViewLayoutReferenceSizeForHeaderInSection(this.getCollectionView(), this, section);
			} else {
				headerReferenceSize = this.getHeaderReferenceSize();
			}
			layoutSection.setHeaderDimension(_data.getHorizontal() ? headerReferenceSize.width : headerReferenceSize.height);

			Size footerReferenceSize;
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
					IndexPath indexPath = IndexPath.withItemInSection(item, section);
					Size itemSize = flowDataSource.collectionViewLayoutSizeForItemAtIndexPath(this.getCollectionView(), this, indexPath);

					GridLayoutItem layoutItem = layoutSection.addItem();
					layoutItem.setItemFrame(new mocha.graphics.Rect(Point.zero(), itemSize));
				}
				// if not, go the fast path
			} else {
				layoutSection.setFixedItemSize(true);
				layoutSection.setItemSize(this._itemSize);
				layoutSection.setItemsCount(numberOfItems);
			}
		}

		MWarn("CV_TEST, OUT getSizingInfos(): " + this._itemSize + ", " + this._data.getSections().size());
	}

	void updateItemsLayout() {
		Size contentSize = Size.zero();

		int s = 0;
		MLog("CV_TEST, updateItemsLayout " + _data.getSections().size());
		for (GridLayoutSection section : _data.getSections()) {
			section.computeLayout();

			// update section offset to make frame absolute (section only calculates relative)
			mocha.graphics.Rect sectionFrame = section.getFrame();
			if (_data.getHorizontal()) {
				sectionFrame.origin.x += contentSize.width;
				contentSize.width += section.getFrame().size.width + section.getFrame().origin.x;
				contentSize.height = Math.max(contentSize.height, sectionFrame.size.height + section.getFrame().origin.y + section.getSectionMargins().top + section.getSectionMargins().bottom);
			} else {
				sectionFrame.origin.y += contentSize.height;
				contentSize.height += sectionFrame.size.height + section.getFrame().origin.y;
				contentSize.width = Math.max(contentSize.width, sectionFrame.size.width + section.getFrame().origin.x + section.getSectionMargins().left + section.getSectionMargins().right);
			}
			section.setFrame(sectionFrame);
			MLog("CV_TEST, section: " + (s++) + ", frame: " + sectionFrame);
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

	public Size getItemSize() {
		if (this._itemSize != null) {
			return this._itemSize.copy();
		} else {
			return Size.zero();
		}
	}

	public void setItemSize(Size itemSize) {
		if (this._itemSize != null) {
			this._itemSize = itemSize.copy();
		} else {
			this._itemSize = Size.zero();
		}
	}

	public CollectionViewFlowLayout.CollectionViewScrollDirection getScrollDirection() {
		return this._scrollDirection;
	}

	public void setScrollDirection(CollectionViewFlowLayout.CollectionViewScrollDirection scrollDirection) {
		this._scrollDirection = scrollDirection;
	}

	public Size getHeaderReferenceSize() {
		if (this._headerReferenceSize != null) {
			return this._headerReferenceSize.copy();
		} else {
			return Size.zero();
		}
	}

	public void setHeaderReferenceSize(Size headerReferenceSize) {
		if (headerReferenceSize != null) {
			this._headerReferenceSize = headerReferenceSize.copy();
		} else {
			this._headerReferenceSize = Size.zero();
		}
	}

	public Size getFooterReferenceSize() {
		if (this._footerReferenceSize != null) {
			return this._footerReferenceSize.copy();
		} else {
			return Size.zero();
		}
	}

	public void setFooterReferenceSize(Size footerReferenceSize) {
		if (footerReferenceSize != null) {
			this._footerReferenceSize = footerReferenceSize.copy();
		} else {
			this._footerReferenceSize = Size.zero();
		}
	}

	public mocha.ui.EdgeInsets getSectionInset() {
		if (this._sectionInset != null) {
			return this._sectionInset.copy();
		} else {
			return mocha.ui.EdgeInsets.zero();
		}
	}

	public Map<String, FlowLayoutAlignment> getRowAlignmentOptions() {
		return this._rowAlignmentsOptionsDictionary;
	}

	public void setRowAlignmentOptions(Map<String, FlowLayoutAlignment> rowAlignmentOptions) {
		this._rowAlignmentsOptionsDictionary.clear();

		if (rowAlignmentOptions != null) {
			this._rowAlignmentsOptionsDictionary.putAll(rowAlignmentOptions);
		}
	}

}

