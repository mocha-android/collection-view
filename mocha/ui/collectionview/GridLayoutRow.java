class GridLayoutRow extends MObject {
	private GridLayoutSection _section;
	private ArrayList _items;
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
		_items.addObject(item);
		item.setRowObject(this);
		this.invalidate();
	}

	void layoutRow() {
		this.layoutRowAndGenerateRectArray(false);
	}

	ArrayList itemRects() {
		return this.layoutRowAndGenerateRectArray(true);
	}

	void invalidate() {
		_isValid = false;
		_rowSize = mocha.graphics.Size.zero();
		_rowFrame = mocha.graphics.Rect.zero();
	}

	GridLayoutRow snapshot() {
		GridLayoutRow snapshotRow = this.getClass().new();
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
		super.init();

		_items = new ArrayList();
	}

	String description() {
		return String.format("<%s: %p frame:%s index:%ld items:%s>", StringFromClass(this.getClass()), this, StringFromCGRect(this.getRowFrame()), (long)this.getIndex(), this.getItems());
	}

	ArrayList layoutRowAndGenerateRectArray(boolean generateRectArray) {
		ArrayList rects = generateRectArray ? new ArrayList() : null;
		if (!_isValid || generateRectArray) {
		    // properties for aligning
		    boolean isHorizontal = this.getSection().getLayoutInfo().getHorizontal();
		    boolean isLastRow = this.getSection().getIndexOfImcompleteRow() == this.getIndex();
		    CollectionViewFlowLayout.FlowLayoutHorizontalAlignment horizontalAlignment = this.getSection().getRowAlignmentOptionsisLastRow().? PSTFlowLayoutLastRowHorizontalAlignmentKey :.PSTFlowLayoutCommonRowHorizontalAlignmentKey() integerValue();

		    // calculate space that's left over if we would align it from left to right.
		    CGFloat leftOverSpace = self.section.layoutInfo.dimension;
		    if (isHorizontal) {
		        leftOverSpace -= self.section.sectionMargins.top + self.section.sectionMargins.bottom;
		    }else {
		        leftOverSpace -= self.section.sectionMargins.left + self.section.sectionMargins.right;
		    }

		    // calculate the space that we have left after counting all items.
		    // UICollectionView is smart and lays out items like they would have been placed on a full row
		    // So we need to calculate the "usedItemCount" with using the last item as a reference size.
		    // This allows us to correctly justify-place the items in the grid.
		    NSUInteger usedItemCount = 0;
		    NSInteger itemIndex = 0;
		    CGFloat spacing = isHorizontal ? self.section.verticalInterstice : self.section.horizontalInterstice;
		    // the last row should justify as if it is filled with more (invisible) items so that the whole
		    // UICollectionView feels more like a grid than a random line of blocks
		    while (itemIndex < self.itemCount || isLastRow) {
		        CGFloat nextItemSize;
		        // first we need to find the size (width/height) of the next item to fit
		        if (!self.fixedItemSize) {
		            PSTGridLayoutItem *item = self.items[MIN(itemIndex, self.itemCount - 1)];
		            nextItemSize = isHorizontal ? item.itemFrame.size.height : item.itemFrame.size.width;
		        }else {
		            nextItemSize = isHorizontal ? self.section.itemSize.height : self.section.itemSize.width;
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
		    CGPoint itemOffset = CGPointZero;
		    if (horizontalAlignment == PSTFlowLayoutHorizontalAlignmentRight) {
		        itemOffset.x += leftOverSpace;
		    }else if (horizontalAlignment == PSTFlowLayoutHorizontalAlignmentCentered ||
		            (horizontalAlignment == PSTFlowLayoutHorizontalAlignmentJustify && usedItemCount == 1)) {
		        // Special case one item row to split leftover space in half
		        itemOffset.x += leftOverSpace / 2;
		    }

		    // calculate the justified spacing among all items in a row if we are using
		    // the default PSTFlowLayoutHorizontalAlignmentJustify layout
		    CGFloat interSpacing = usedItemCount <= 1 ? 0 : leftOverSpace / (CGFloat)(usedItemCount - 1);

		    // calculate row frame as union of all items
		    CGRect frame = CGRectZero;
		    CGRect itemFrame = (CGRect){.size=self.section.itemSize};
		    for (itemIndex = 0; itemIndex < self.itemCount; itemIndex++) {
		        PSTGridLayoutItem *item = nil;
		        if (!self.fixedItemSize) {
		            item = self.items[itemIndex];
		            itemFrame = [item itemFrame];
		        }
		        // depending on horizontal/vertical for an item size (height/width),
		        // we add the minimum separator then an equally distributed spacing
		        // (since our default mode is justify) calculated from the total leftover
		        // space divided by the number of intervals
		        if (isHorizontal) {
		            itemFrame.origin.y = itemOffset.y;
		            itemOffset.y += itemFrame.size.height + self.section.verticalInterstice;
		            if (horizontalAlignment == PSTFlowLayoutHorizontalAlignmentJustify) {
		                itemOffset.y += interSpacing;
		            }
		        }else {
		            itemFrame.origin.x = itemOffset.x;
		            itemOffset.x += itemFrame.size.width + self.section.horizontalInterstice;
		            if (horizontalAlignment == PSTFlowLayoutHorizontalAlignmentJustify) {
		                itemOffset.x += interSpacing;
		            }
		        }
		        item.itemFrame = itemFrame; // might call nil; don't care
		        rects.addObject(mocha.foundation.Value.valueWithCGRect(itemFrame));
		        frame = mocha.graphics.RectUnion(frame, itemFrame);
		    }
		    _rowSize = frame.getSize();
		    //        _rowFrame = frame; // set externally
		    _isValid = true;
		}
		return rects;
	}

	GridLayoutRow copyFromSection(GridLayoutSection section) {
		return null; // ???
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
		return this.section;
	}

	public void setSection(GridLayoutSection section) {
		this.section = section;
	}

	public ArrayList getItems() {
		return this.items;
	}

	public void setItems(ArrayList items) {
		this.items = items;
	}

	public mocha.graphics.Size getRowSize() {
		if(this.rowSize != null) {
			return this.rowSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setRowSize(mocha.graphics.Size rowSize) {
		if(this.rowSize != null) {
			this.rowSize = rowSize.copy();
		} else {
			this.rowSize = mocha.graphics.Size.zero();
		}
	}

	public mocha.graphics.Rect getRowFrame() {
		if(this.rowFrame != null) {
			return this.rowFrame.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	public void setRowFrame(mocha.graphics.Rect rowFrame) {
		if(this.rowFrame != null) {
			this.rowFrame = rowFrame.copy();
		} else {
			this.rowFrame = mocha.graphics.Rect.zero();
		}
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean getComplete() {
		return this.complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public boolean getFixedItemSize() {
		return this.fixedItemSize;
	}

	public void setFixedItemSize(boolean fixedItemSize) {
		this.fixedItemSize = fixedItemSize;
	}

	public int getItemCount() {
		return this.itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

}

