/**
 *  @author Shaun
 *  @date 4/17/14
 *  @copyright 2014 Mocha. All rights reserved.
 */
package mocha.ui.collectionview;

import mocha.foundation.IndexPath;
import mocha.foundation.MObject;
import mocha.graphics.Point;
import mocha.graphics.Rect;
import mocha.graphics.Size;
import mocha.ui.EdgeInsets;
import mocha.ui.ScreenMath;

import java.util.Arrays;

class FlowLayoutSection extends MObject {
	public final Rect frame = new Rect();
	public final EdgeInsets edgeInsets = new EdgeInsets();

	public final Rect headerFrame = new Rect();
	public final Rect footerFrame = new Rect();

	public Rect[] itemFrames;
	public IndexPath[] indexPaths;
	public int numberOfItems;
	public float minimumLineSpacing;
	public float minimumInteritemSpacing;
	public float headerSize;
	public float footerSize;
	public CollectionViewItemKey[] itemKeys;
	public int index;

	public void reloadData(FastFlowLayout flowLayout, CollectionView collectionView, Rect bounds, FastFlowLayout.Delegate delegate, Point offset, int section, boolean vertical) {
		this.numberOfItems = collectionView.numberOfItemsInSection(section);
		boolean usesVariableItemSizes = flowLayout.delegateSizeForItem;

		if(this.itemFrames == null) {
			this.itemFrames = new Rect[this.numberOfItems];
		} else if(this.itemFrames.length != this.numberOfItems) {
			this.itemFrames = Arrays.copyOf(this.itemFrames, this.numberOfItems);
		}

		if(this.indexPaths == null) {
			this.indexPaths = new IndexPath[this.numberOfItems];
		} else if(this.indexPaths.length != this.numberOfItems) {
			this.indexPaths = Arrays.copyOf(this.indexPaths, this.numberOfItems);
		}

		if(this.itemKeys == null) {
			this.itemKeys = new CollectionViewItemKey[this.numberOfItems];
		} else if(this.indexPaths.length != this.numberOfItems) {
			this.itemKeys = Arrays.copyOf(this.itemKeys, this.numberOfItems);
		}

		this.frame.origin.x = offset.x;
		this.frame.origin.y = offset.y;

		float maxX = bounds.size.width - this.edgeInsets.right;
		float maxY = bounds.size.width - this.edgeInsets.right;

		Size fixedItemSize = usesVariableItemSizes && delegate != null ? null : flowLayout.itemSize.copy();
		Point itemOffset = offset.copy();

		if(this.headerSize > 0) {
			if(vertical) {
				this.headerFrame.origin.x = this.frame.origin.x;
				this.headerFrame.origin.y = itemOffset.y;
				this.headerFrame.size.width = bounds.size.width;
				this.headerFrame.size.height = this.headerSize;
				itemOffset.y += this.headerSize;
			} else {
				this.headerFrame.origin.x = itemOffset.x;
				this.headerFrame.origin.y = this.frame.origin.y;
				this.headerFrame.size.width = this.headerSize;
				this.headerFrame.size.height = bounds.size.height;
				itemOffset.x += this.headerSize;
			}
		} else {
			this.headerFrame.set(null);
		}

		itemOffset.x += this.edgeInsets.left;
		itemOffset.y += this.edgeInsets.top;

		int rowStartItem = 0;

		for(int item = 0; item < this.numberOfItems; item++) {
			IndexPath indexPath = this.indexPaths[item];

			if(indexPath == null || (indexPath.section != section || indexPath.item != item)) {
				indexPath = IndexPath.withItemInSection(item, section);
				this.indexPaths[item] = indexPath;
			}

			Size itemSize;

			if(usesVariableItemSizes && delegate != null) {
				itemSize = delegate.collectionViewLayoutSizeForItemAtIndexPath(collectionView, flowLayout, indexPath);
			} else {
				itemSize = fixedItemSize;
			}

			if(itemSize == null) {
				itemSize = Size.zero();
			}

			Rect itemFrame = this.itemFrames[item];

			if(itemFrame == null) {
				itemFrame = new Rect();
				this.itemFrames[item] = itemFrame;
			}

			itemFrame.size.width = itemSize.width;
			itemFrame.size.height = itemSize.height;

			// Move to a new line if our size will go beyond the max
			if(vertical && (itemOffset.x + itemFrame.size.width > maxX)) {
				itemOffset.y += this.alignItems(true, bounds, rowStartItem, item - 1);
				itemOffset.x = offset.x + this.edgeInsets.left;
				rowStartItem = item;
			} else if(!vertical && itemOffset.y + itemFrame.size.height > maxY) {
				itemOffset.x += this.alignItems(false, bounds, rowStartItem, item - 1);
				itemOffset.y = offset.y + this.edgeInsets.top;
				rowStartItem = item;
			}

			itemFrame.origin.x = itemOffset.x;
			itemFrame.origin.y = itemOffset.y;

			if(vertical) {
				itemOffset.x += itemFrame.size.width + this.minimumInteritemSpacing;
			} else {
				itemOffset.y += itemFrame.size.height + this.minimumInteritemSpacing;
			}
		}

		// Align the last line
		float lineSize = this.alignItems(vertical, bounds, rowStartItem, this.numberOfItems - 1);

		if(vertical) {
			itemOffset.y += lineSize - this.minimumLineSpacing;
		} else {
			itemOffset.x += lineSize - this.minimumLineSpacing;
		}

		itemOffset.x += this.edgeInsets.right;
		itemOffset.y += this.edgeInsets.bottom;

		// Adjust for footer, if we have one
		if(this.footerSize > 0) {
			if(vertical) {
				this.footerFrame.origin.x = this.frame.origin.x;
				this.footerFrame.origin.y = itemOffset.y;
				this.footerFrame.size.width = bounds.size.width;
				this.footerFrame.size.height = this.footerSize;
				itemOffset.y += this.footerSize;
			} else {
				this.footerFrame.origin.x = itemOffset.x;
				this.footerFrame.origin.y = this.frame.origin.y;
				this.footerFrame.size.width = this.footerSize;
				this.footerFrame.size.height = bounds.size.height;
				itemOffset.x += this.footerSize;
			}
		} else {
			this.footerFrame.set(null);
		}

		if(vertical) {
			this.frame.size.width = bounds.size.width;
			this.frame.size.height = itemOffset.y - this.frame.origin.y;
		} else {
			this.frame.size.width = itemOffset.x - this.frame.origin.x;
			this.frame.size.height = bounds.size.height;
		}
	}

	private float alignItems(boolean vertical, Rect bounds, int start, int end) {
		if(start == end) {
			return this.itemFrames[start].size.height + this.minimumLineSpacing;
		}

		float max;
		float y = bounds.origin.y + this.edgeInsets.top;
		float x = bounds.origin.x + this.edgeInsets.left;

		if(vertical) {
			max = bounds.size.width - this.edgeInsets.left - this.edgeInsets.right;
		} else {
			max = bounds.size.height - this.edgeInsets.top - this.edgeInsets.bottom;
		}

		float total = 0.0f;
		float otherMax = 0.0f;

		// Loop through to get the total size + the max size of the opposite dimension
		for(int item = start; item <= end; item++) {
			Rect frame = this.itemFrames[item];

			if(vertical) {
				total += frame.size.width;

				if(frame.size.height > otherMax) {
					otherMax = frame.size.height;
				}
			} else {
				total += frame.size.height;

				if(frame.size.width > otherMax) {
					otherMax = frame.size.width;
				}
			}
		}

		float spacing = ScreenMath.floor((max - total) / (float) (end - start));

		// Align items
		for(int item = start; item <= end; item++) {
			Rect frame = this.itemFrames[item];

			if(vertical) {
				if(item == end) {
					frame.origin.x = bounds.origin.x + this.edgeInsets.left + (max - frame.size.width);
				} else {
					frame.origin.x = x;
					x += frame.size.width + spacing;
				}

				frame.origin.y += ScreenMath.floor((otherMax - frame.size.height) / 2.0f);
			} else {
				if(item == end) {
					frame.origin.y = bounds.origin.y + this.edgeInsets.top + (max - frame.size.height);
				} else {
					frame.origin.y = y;
					y += frame.size.height + spacing;
				}

				frame.origin.x += ScreenMath.floor((otherMax - frame.size.width) / 2.0f);
			}
		}

		return otherMax + this.minimumLineSpacing;
	}

	protected String toStringExtra() {
		return String.format(
			"section=%d; header=%s; footer=%s; cells=%s; frame=%s; inset=%s",
			this.index,
			this.headerSize == 0 ? "none" : this.headerFrame,
			this.footerSize == 0 ? "none" : this.footerFrame,
			Arrays.toString(this.itemFrames),
			this.frame,
			this.edgeInsets
		);
	}

}
