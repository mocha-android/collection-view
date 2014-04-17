package mocha.ui.collectionview;

import mocha.foundation.Copying;
import mocha.foundation.MObject;
import mocha.graphics.Point;
import mocha.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

class GridLayoutRow extends MObject implements Copying<GridLayoutRow> {
	private GridLayoutSection _section;
	private List<GridLayoutItem> _items;
	private mocha.graphics.Size _rowSize;
	private mocha.graphics.Rect _rowFrame;
	private int _index;
	private boolean _complete;
	private boolean _fixedItemSize;
	private int _itemCount;
	private boolean _isValid;
	private int _verticalAlignement;
	private int _horizontalAlignement;

	void addItem(GridLayoutItem item) {
		_items.add(item);
		item.setRowObject(this);
		this.invalidate();
	}

	void layoutRow() {
		this.layoutRowAndGenerateRectArray(false);
	}

	List<Rect> itemRects() {
		return this.layoutRowAndGenerateRectArray(true);
	}

	void invalidate() {
		_isValid = false;
		_rowSize = mocha.graphics.Size.zero();
		_rowFrame = mocha.graphics.Rect.zero();
	}

	public GridLayoutRow copy() {
		GridLayoutRow snapshotRow = new GridLayoutRow();
		snapshotRow.setSection(this.getSection());
		snapshotRow.setItems(this.getItems());
		snapshotRow.setRowSize(this.getRowSize());
		snapshotRow.setRowFrame(this.getRowFrame());
		snapshotRow.setIndex(this.getIndex());
		snapshotRow.setComplete(this.getComplete());
		snapshotRow.setFixedItemSize(this.getFixedItemSize());
		snapshotRow.setItemCount(this.getItemCount());
		return snapshotRow;
	}

	public GridLayoutRow() {
		_items = new ArrayList<>();
	}

	protected String toStringExtra() {
		return String.format("frame:%s index:%d items:%s", this.getRowFrame(), this.getIndex(), this.getItems());
	}

	List<Rect> layoutRowAndGenerateRectArray(boolean generateRectArray) {
		List<Rect> rects = generateRectArray ? new ArrayList<Rect>() : null;

		if (!_isValid || generateRectArray) {
		    // properties for aligning
		    boolean isHorizontal = this.getSection().getLayoutInfo().getHorizontal();
		    boolean isLastRow = this.getSection().getIndexOfImcompleteRow() == this.getIndex();

			CollectionViewFlowLayout.FlowLayoutAlignment horizontalAlignment = this.getSection().getRowAlignmentOptions().get(isLastRow ? CollectionViewFlowLayout.PSTFlowLayoutLastRowHorizontalAlignmentKey : CollectionViewFlowLayout.PSTFlowLayoutCommonRowHorizontalAlignmentKey);

		    // calculate space that's left over if we would align it from left to right.
		    float leftOverSpace = this.getSection().getLayoutInfo().getDimension();
		    if (isHorizontal) {
		        leftOverSpace -= this.getSection().getSectionMargins().top + this.getSection().getSectionMargins().bottom;
		    }else {
		        leftOverSpace -= this.getSection().getSectionMargins().left + this.getSection().getSectionMargins().right;
		    }

		    // calculate the space that we have left after counting all items.
		    // UICollectionView is smart and lays out items like they would have been placed on a full row
		    // So we need to calculate the "usedItemCount" with using the last item as a reference size.
		    // This allows us to correctly justify-place the items in the grid.
		    int usedItemCount = 0;
		    int itemIndex = 0;
		    float spacing = isHorizontal ? this.getSection().getVerticalInterstice() : this.getSection().getHorizontalInterstice();
		    // the last row should justify as if it is filled with more (invisible) items so that the whole
		    // UICollectionView feels more like a grid than a random line of blocks
		    while (itemIndex < this.itemCount() || isLastRow) {
		        float nextItemSize;
		        // first we need to find the size (width/height) of the next item to fit
		        if (!this.getFixedItemSize()) {
		            GridLayoutItem item = this._items.get(Math.min(itemIndex, this.itemCount() - 1));
		            nextItemSize = isHorizontal ? item.getItemFrame().size.height : item.getItemFrame().size.width;
		        } else {
		            nextItemSize = isHorizontal ? this.getSection().getItemSize().height : this.getSection().getItemSize().width;
		        }

		        // the first item does not add a separator spacing,
		        // every one afterwards in the same row will need this spacing constant
		        if (itemIndex > 0) {
		            nextItemSize += spacing;
		        }

		        // check to see if we can at least fit an item (+separator if necessary)
		        if (leftOverSpace < nextItemSize) {
		            break;
		        }

		        // we need to maintain the leftover space after the maximum amount of items have
		        // occupied, so we know how to adjust equal spacing among all the items in a row
		        leftOverSpace -= nextItemSize;

		        itemIndex++;
		        usedItemCount = itemIndex;
		    }

		    // push everything to the right if right-aligning and divide in half for centered
		    // currently there is no public API supporting this behavior
		    Point itemOffset = Point.zero();
		    if (horizontalAlignment == CollectionViewFlowLayout.FlowLayoutAlignment.MAX) {
				itemOffset.x += leftOverSpace;
		    } else if (horizontalAlignment == CollectionViewFlowLayout.FlowLayoutAlignment.MID ||
		            (horizontalAlignment == CollectionViewFlowLayout.FlowLayoutAlignment.JUSTIFY && usedItemCount == 1)) {
		        // Special case one item row to split leftover space in half
				itemOffset.x += leftOverSpace / 2;
		    }

		    // calculate the justified spacing among all items in a row if we are using
		    // the default PSTFlowLayoutHorizontalAlignmentJustify layout
		    float interSpacing = usedItemCount <= 1 ? 0 : leftOverSpace / (float)(usedItemCount - 1);

		    // calculate row frame as union of all items
		    Rect frame = Rect.zero();
		    Rect itemFrame = new Rect(Point.zero(), this.getSection().getItemSize());
		    for (itemIndex = 0; itemIndex < this.getItemCount(); itemIndex++) {
		        GridLayoutItem item = null;

		        if (!this.getFixedItemSize()) {
					item = this._items.get(itemIndex);
					itemFrame = item.getItemFrame();
		        }

		        // depending on horizontal/vertical for an item size (height/width),
		        // we add the minimum separator then an equally distributed spacing
		        // (since our default mode is justify) calculated from the total leftover
		        // space divided by the number of intervals
		        if (isHorizontal) {
		            itemFrame.origin.y = itemOffset.y;
					itemOffset.y += itemFrame.size.height + this.getSection().getVerticalInterstice();

		            if (horizontalAlignment == CollectionViewFlowLayout.FlowLayoutAlignment.JUSTIFY) {
						itemOffset.y += interSpacing;
		            }
		        } else {
		            itemFrame.origin.x = itemOffset.x;
					itemOffset.x += itemFrame.size.width + this.getSection().getHorizontalInterstice();

		            if (horizontalAlignment == CollectionViewFlowLayout.FlowLayoutAlignment.JUSTIFY) {
						itemOffset.x += interSpacing;
		            }
		        }

				if(item != null) {
					item.setItemFrame(itemFrame);
				}

				if(rects != null) {
					rects.add(itemFrame.copy());
				}

		        frame = frame.union(itemFrame);
		    }

		    _rowSize = frame.size.copy();
		    // _rowFrame = frame; // set externally
		    _isValid = true;
		}

		return rects;
	}

	int itemCount() {
		if (this.getFixedItemSize()) {
		    return _itemCount;
		}else {
		    return this.getItems().size();
		}
	}

	/* Setters & Getters */
	/* ========================================== */

	public GridLayoutSection getSection() {
		return this._section;
	}

	public void setSection(GridLayoutSection section) {
		this._section = section;
	}

	public List<GridLayoutItem> getItems() {
		return this._items;
	}

	public void setItems(List<GridLayoutItem> items) {
		this._items.clear();

		if(items != null) {
			this._items.addAll(items);
		}
	}

	public mocha.graphics.Size getRowSize() {
		if(this._rowSize != null) {
			return this._rowSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setRowSize(mocha.graphics.Size rowSize) {
		if(rowSize != null) {
			this._rowSize = rowSize.copy();
		} else {
			this._rowSize = mocha.graphics.Size.zero();
		}
	}

	public mocha.graphics.Rect getRowFrame() {
		if(this._rowFrame != null) {
			return this._rowFrame.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	public void setRowFrame(mocha.graphics.Rect rowFrame) {
		if(rowFrame != null) {
			this._rowFrame = rowFrame.copy();
		} else {
			this._rowFrame = mocha.graphics.Rect.zero();
		}
	}

	public int getIndex() {
		return this._index;
	}

	public void setIndex(int index) {
		this._index = index;
	}

	public boolean getComplete() {
		return this._complete;
	}

	public void setComplete(boolean complete) {
		this._complete = complete;
	}

	public boolean getFixedItemSize() {
		return this._fixedItemSize;
	}

	public void setFixedItemSize(boolean fixedItemSize) {
		this._fixedItemSize = fixedItemSize;
	}

	public int getItemCount() {
		return this._itemCount;
	}

	public void setItemCount(int itemCount) {
		this._itemCount = itemCount;
	}

}

