/**
 *  @author Shaun
 *  @date 4/17/14
 *  @copyright 2014 Mocha. All rights reserved.
 */
package mocha.ui.collectionview;

import mocha.foundation.IndexPath;
import mocha.foundation.OptionalInterfaceHelper;
import mocha.graphics.Rect;
import mocha.graphics.Size;
import mocha.ui.EdgeInsets;

import java.util.List;

public class CollectionViewFlowLayout extends CollectionViewLayout {
	public static final String ELEMENT_KIND_SECTION_HEADER = "ELEMENT_KIND_SECTION_HEADER";
	public static final String ELEMENT_KIND_SECTION_FOOTER = "ELEMENT_KIND_SECTION_FOOTER";

	private CollectionViewFlowLayoutData data;
	private boolean prepared;
	private Size lastBoundsSize;

	float minimumLineSpacing;
	float minimumInteritemSpacing;
	Size itemSize;
	ScrollDirection scrollDirection;
	Size headerReferenceSize;
	Size footerReferenceSize;
	EdgeInsets sectionInset;

	boolean delegateSizeForItem;
	boolean delegateReferenceSizeForHeader;
	boolean delegateReferenceSizeForFooter;
	boolean delegateInsetForSection;
	boolean delegateInteritemSpacingForSection;
	boolean delegateLineSpacingForSection;

	public enum ScrollDirection {
		VERTICAL,
		HORIZONTAL
	}

	public interface Delegate extends CollectionView.Delegate {

		@Optional
		Size collectionViewLayoutSizeForItemAtIndexPath(CollectionView collectionView, CollectionViewLayout collectionViewLayout, IndexPath indexPath);

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

	public CollectionViewFlowLayout() {
		this.minimumInteritemSpacing = 10.0f;
		this.minimumLineSpacing = 10.0f;
		this.itemSize = new Size(50.0f, 50.0f);
		this.scrollDirection = ScrollDirection.VERTICAL;
		this.headerReferenceSize = Size.zero();
		this.footerReferenceSize = Size.zero();
		this.sectionInset = EdgeInsets.zero();
		this.data = new CollectionViewFlowLayoutData(this);
	}

	public void collectionViewDelegateDidChange() {
		super.collectionViewDelegateDidChange();

		CollectionView.Delegate delegate = this.getCollectionView().getDelegate();
		Delegate flowLayoutDelegate;

		if (delegate instanceof Delegate) {
			flowLayoutDelegate = (Delegate)delegate;
		} else {
			flowLayoutDelegate = null;
		}

		this.delegateSizeForItem = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, Delegate.class, "collectionViewLayoutSizeForItemAtIndexPath", CollectionView.class, CollectionViewLayout.class, IndexPath.class);
		this.delegateInsetForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, Delegate.class, "collectionViewLayoutInsetForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this.delegateLineSpacingForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, Delegate.class, "collectionViewLayoutMinimumLineSpacingForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this.delegateInteritemSpacingForSection = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, Delegate.class, "collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex", CollectionView.class, CollectionViewLayout.class, int.class);
		this.delegateReferenceSizeForHeader = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, Delegate.class, "collectionViewLayoutReferenceSizeForHeaderInSection", CollectionView.class, CollectionViewLayout.class, int.class);
		this.delegateReferenceSizeForFooter = OptionalInterfaceHelper.hasImplemented(flowLayoutDelegate, Delegate.class, "collectionViewLayoutReferenceSizeForFooterInSection", CollectionView.class, CollectionViewLayout.class, int.class);
	}

	public void prepareLayout() {
		if(!this.prepared) {
			this.data.reloadData();
			this.prepared = true;
		}
	}

	public void invalidateLayout() {
		super.invalidateLayout();

		this.prepared = false;
	}

	public List<CollectionViewLayoutAttributes> layoutAttributesForElementsInRect(Rect rect) {
		if(!this.prepared) {
			this.prepareLayout();
		}

		return this.data.layoutAttributesForElementsInRect(rect);
	}

	public CollectionViewLayoutAttributes layoutAttributesForItemAtIndexPath(IndexPath indexPath) {
		if(!this.prepared) {
			this.prepareLayout();
		}

		return this.data.layoutAttributesForItemAtIndexPath(indexPath);
	}

	public CollectionViewLayoutAttributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, IndexPath indexPath) {
		if(!this.prepared) {
			this.prepareLayout();
		}

		return this.data.layoutAttributesForSupplementaryViewOfKindAtIndexPath(kind, indexPath);
	}

	public CollectionViewLayoutAttributes layoutAttributesForDecorationViewOfKindAtIndexPath(String kind, IndexPath indexPath) {
		return null;
	}

	public Size collectionViewContentSize() {
		return this.data.contentSize.copy();
	}

	public CollectionReusableView decorationViewForCollectionViewWithReuseIdentifierIndexPath(CollectionView collectionView, String reuseIdentifier, IndexPath indexPath) {
		return null;
	}

	public boolean shouldInvalidateLayoutForBoundsChange(Rect newBounds) {
		boolean invalidate = false;

		if(this.lastBoundsSize == null) {
			invalidate = true;
			this.lastBoundsSize = new Size();
		} else if(this.scrollDirection == ScrollDirection.VERTICAL) {
			invalidate = newBounds.size.width != this.lastBoundsSize.width;
		} else if(this.scrollDirection == ScrollDirection.HORIZONTAL) {
			invalidate = newBounds.size.height != this.lastBoundsSize.height;
		}

		if(invalidate) {
			this.lastBoundsSize.width = newBounds.size.width;
			this.lastBoundsSize.height = newBounds.size.height;
			return true;
		} else {
			return false;
		}
	}

	public float getMinimumLineSpacing() {
		return minimumLineSpacing;
	}

	public void setMinimumLineSpacing(float minimumLineSpacing) {
		this.minimumLineSpacing = minimumLineSpacing;
	}

	public float getMinimumInteritemSpacing() {
		return minimumInteritemSpacing;
	}

	public void setMinimumInteritemSpacing(float minimumInteritemSpacing) {
		this.minimumInteritemSpacing = minimumInteritemSpacing;
	}

	public Size getItemSize() {
		return this.itemSize.copy();
	}

	public void setItemSize(Size itemSize) {
		this.itemSize.set(itemSize);
	}

	public ScrollDirection getScrollDirection() {
		return this.scrollDirection;
	}

	public void setScrollDirection(ScrollDirection scrollDirection) {
		this.scrollDirection = scrollDirection;
	}

	public Size getHeaderReferenceSize() {
		return this.headerReferenceSize.copy();
	}

	public void setHeaderReferenceSize(Size headerReferenceSize) {
		this.headerReferenceSize.set(headerReferenceSize);
	}

	public Size getFooterReferenceSize() {
		return this.footerReferenceSize.copy();
	}

	public void setFooterReferenceSize(Size footerReferenceSize) {
		this.footerReferenceSize.set(footerReferenceSize);
	}

	public EdgeInsets getSectionInset() {
		return this.sectionInset.copy();
	}

	public void setSectionInset(EdgeInsets sectionInset) {
		this.sectionInset.set(sectionInset);
	}
}
