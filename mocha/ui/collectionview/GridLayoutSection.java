class GridLayoutSection extends MObject {
	private ArrayList _items;
	private ArrayList _rows;
	private boolean _fixedItemSize;
	private mocha.graphics.Size _itemSize;
	private int _itemsCount;
	private float _verticalInterstice;
	private float _horizontalInterstice;
	private mocha.ui.EdgeInsets _sectionMargins;
	private mocha.graphics.Rect _frame;
	private mocha.graphics.Rect _headerFrame;
	private mocha.graphics.Rect _footerFrame;
	private float _headerDimension;
	private float _footerDimension;
	private GridLayoutInfo _layoutInfo;
	private HashMap _rowAlignmentOptions;
	private float _otherMargin;
	private float _beginMargin;
	private float _endMargin;
	private float _actualGap;
	private float _lastRowBeginMargin;
	private float _lastRowEndMargin;
	private float _lastRowActualGap;
	private boolean _lastRowIncomplete;
	private int _itemsByRowCount;
	private int _indexOfImcompleteRow;
	private boolean _isValid;

	void recomputeFromIndex(int index) {
		// TODO: use index.
		this.invalidate();
		this.computeLayout();
	}

	void invalidate() {
		_isValid = false;
		this.setRows(new ArrayList());
	}

	void computeLayout() {
		if (!_isValid) {
		    mocha.foundation.Assert(this.getRows().size() == 0, "No rows shall be at this point.");

		    // iterate over all items, turning them into rows.
		    mocha.graphics.Size sectionSize = mocha.graphics.Size.zero();
		    int rowIndex = 0;
		    int itemIndex = 0;
		    int itemsByRowCount = 0;
		    float dimensionLeft = 0;
		    GridLayoutRow row = null;
		    // get dimension and compensate for section margin
		    float headerFooterDimension = this.getLayoutInfo().getDimension();
		    float dimension = headerFooterDimension;

		    if (this.getLayoutInfo().getHorizontal()) {
		        dimension -= this.getSectionMargins().top + this.getSectionMargins().bottom;
		        this.setHeaderFrame(new mocha.graphics.Rect(sectionSize.width, 0, this.getHeaderDimension(), headerFooterDimension));
		        sectionSize.width += this.getHeaderDimension() + this.getSectionMargins().left;
		    }else {
		        dimension -= this.getSectionMargins().left + this.getSectionMargins().right;
		        this.setHeaderFrame(new mocha.graphics.Rect(0, sectionSize.height, headerFooterDimension, this.getHeaderDimension()));
		        sectionSize.height += this.getHeaderDimension() + this.getSectionMargins().top;
		    }

		    float spacing = this.getLayoutInfo().getHorizontal() ? this.getVerticalInterstice() : this.getHorizontalInterstice();

		    do {
		        boolean finishCycle = itemIndex >= this.getItemsCount();
		        // TODO: fast path could even remove row creation and just calculate on the fly
		        GridLayoutItem item = null;
		        if (!finishCycle) item = this.getFixedItemSize() ? null : this.getItems().get(itemIndex);

		        mocha.graphics.Size itemSize = this.getFixedItemSize() ? this.getItemSize() : item.getItemFrame().getSize();
		        float itemDimension = this.getLayoutInfo().getHorizontal() ? itemSize.height : itemSize.width;
		        // first item of each row does not add spacing
		        if (itemsByRowCount > 0) itemDimension += spacing;
		        if (dimensionLeft < itemDimension || finishCycle) {
		            // finish current row
		            if (row) {
		                // compensate last row
		                this.setItemsByRowCount(Math.max(itemsByRowCount, this.getItemsByRowCount()));
		                row.setItemCount(itemsByRowCount);

		                // if current row is done but there are still items left, increase the incomplete row counter
		                if (!finishCycle) this.setIndexOfImcompleteRow(rowIndex);

		                row.layoutRow();

		                if (this.getLayoutInfo().getHorizontal()) {
		                    row.setRowFrame(new mocha.graphics.Rect(sectionSize.width, this.getSectionMargins().top, row.getRowSize().getWidth(), row.getRowSize().getHeight()));
		                    sectionSize.height = Math.max(row.getRowSize().getHeight(), sectionSize.height);
		                    sectionSize.width += row.getRowSize().getWidth() + (finishCycle ? 0 : this.getHorizontalInterstice());
		                }else {
		                    row.setRowFrame(new mocha.graphics.Rect(this.getSectionMargins().left, sectionSize.height, row.getRowSize().getWidth(), row.getRowSize().getHeight()));
		                    sectionSize.height += row.getRowSize().getHeight() + (finishCycle ? 0 : this.getVerticalInterstice());
		                    sectionSize.width = Math.max(row.getRowSize().getWidth(), sectionSize.width);
		                }
		            }
		            // add new rows until the section is fully laid out
		            if (!finishCycle) {
		                // create new row
		                row.setComplete(true); // finish up current row
		                row = this.addRow();
		                row.setFixedItemSize(this.getFixedItemSize());
		                row.setIndex(rowIndex);
		                this.setIndexOfImcompleteRow(rowIndex);
		                rowIndex++;
		                // convert an item from previous row to current, remove spacing for first item
		                if (itemsByRowCount > 0) itemDimension -= spacing;
		                dimensionLeft = dimension - itemDimension;
		                itemsByRowCount = 0;
		            }
		        }else {
		            dimensionLeft -= itemDimension;
		        }

		        // add item on slow path
		        if (item) row.addItem(item);

		        itemIndex++;
		        itemsByRowCount++;
		    } while (itemIndex <= this.getItemsCount()); // cycle once more to finish last row

		    if (this.getLayoutInfo().getHorizontal()) {
		        sectionSize.width += this.getSectionMargins().right;
		        this.setFooterFrame(new mocha.graphics.Rect(sectionSize.width, 0, this.getFooterDimension(), headerFooterDimension));
		        sectionSize.width += this.getFooterDimension();
		    }else {
		        sectionSize.height += this.getSectionMargins().bottom;
		        this.setFooterFrame(new mocha.graphics.Rect(0, sectionSize.height, headerFooterDimension, this.getFooterDimension()));
		        sectionSize.height += this.getFooterDimension();
		    }

		    _frame = new mocha.graphics.Rect(0, 0, sectionSize.width, sectionSize.height);
		    _isValid = true;
		}
	}

	GridLayoutItem addItem() {
		GridLayoutItem item = new GridLayoutItem();
		item.setSection(this);
		_items.addObject(item);
		return item;
	}

	GridLayoutRow addRow() {
		GridLayoutRow row = new GridLayoutRow();
		row.setSection(this);
		_rows.addObject(row);
		return row;
	}

	GridLayoutSection snapshot() {
		GridLayoutSection snapshotSection = new GridLayoutSection();
		snapshotSection.setItems(this.getItems().copy());
		snapshotSection.setRows(this.getItems().copy());
		snapshotSection.setVerticalInterstice(this.getVerticalInterstice());
		snapshotSection.setHorizontalInterstice(this.getHorizontalInterstice());
		snapshotSection.setSectionMargins(this.getSectionMargins());
		snapshotSection.setFrame(this.getFrame());
		snapshotSection.setHeaderFrame(this.getHeaderFrame());
		snapshotSection.setFooterFrame(this.getFooterFrame());
		snapshotSection.setHeaderDimension(this.getHeaderDimension());
		snapshotSection.setFooterDimension(this.getFooterDimension());
		snapshotSection.setLayoutInfo(this.getLayoutInfo());
		snapshotSection.setRowAlignmentOptions(this.getRowAlignmentOptions());
		snapshotSection.setFixedItemSize(this.getFixedItemSize());
		snapshotSection.setItemSize(this.getItemSize());
		snapshotSection.setItemsCount(this.getItemsCount());
		snapshotSection.setOtherMargin(this.getOtherMargin());
		snapshotSection.setBeginMargin(this.getBeginMargin());
		snapshotSection.setEndMargin(this.getEndMargin());
		snapshotSection.setActualGap(this.getActualGap());
		snapshotSection.setLastRowBeginMargin(this.getLastRowBeginMargin());
		snapshotSection.setLastRowEndMargin(this.getLastRowEndMargin());
		snapshotSection.setLastRowActualGap(this.getLastRowActualGap());
		snapshotSection.setLastRowIncomplete(this.getLastRowIncomplete());
		snapshotSection.setItemsByRowCount(this.getItemsByRowCount());
		snapshotSection.setIndexOfImcompleteRow(this.getIndexOfImcompleteRow());
		return snapshotSection;
	}

	public GridLayoutSection() {
		super.init();

		_items = new ArrayList();
		_rows = new ArrayList();
	}

	String description() {
		return String.format("<%s: %p itemCount:%ld frame:%s rows:%s>", StringFromClass(this.getClass()), this, (long)this.getItemsCount(), StringFromCGRect(this.getFrame()), this.getRows());
	}

	int itemsCount() {
		return this.getFixedItemSize() ? _itemsCount : this.getItems().size();
	}

	/* Setters & Getters */
	/* ========================================== */

	public ArrayList getItems() {
		return this.items;
	}

	public void setItems(ArrayList items) {
		this.items = items;
	}

	public ArrayList getRows() {
		return this.rows;
	}

	public void setRows(ArrayList rows) {
		this.rows = rows;
	}

	public boolean getFixedItemSize() {
		return this.fixedItemSize;
	}

	public void setFixedItemSize(boolean fixedItemSize) {
		this.fixedItemSize = fixedItemSize;
	}

	public mocha.graphics.Size getItemSize() {
		if(this.itemSize != null) {
			return this.itemSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setItemSize(mocha.graphics.Size itemSize) {
		if(this.itemSize != null) {
			this.itemSize = itemSize.copy();
		} else {
			this.itemSize = mocha.graphics.Size.zero();
		}
	}

	public int getItemsCount() {
		return this.itemsCount;
	}

	public void setItemsCount(int itemsCount) {
		this.itemsCount = itemsCount;
	}

	public float getVerticalInterstice() {
		return this.verticalInterstice;
	}

	public void setVerticalInterstice(float verticalInterstice) {
		this.verticalInterstice = verticalInterstice;
	}

	public float getHorizontalInterstice() {
		return this.horizontalInterstice;
	}

	public void setHorizontalInterstice(float horizontalInterstice) {
		this.horizontalInterstice = horizontalInterstice;
	}

	public mocha.ui.EdgeInsets getSectionMargins() {
		if(this.sectionMargins != null) {
			return this.sectionMargins.copy();
		} else {
			return mocha.ui.EdgeInsets.zero();
		}
	}

	public void setSectionMargins(mocha.ui.EdgeInsets sectionMargins) {
		if(this.sectionMargins != null) {
			this.sectionMargins = sectionMargins.copy();
		} else {
			this.sectionMargins = mocha.ui.EdgeInsets.zero();
		}
	}

	public mocha.graphics.Rect getFrame() {
		if(this.frame != null) {
			return this.frame.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	public void setFrame(mocha.graphics.Rect frame) {
		if(this.frame != null) {
			this.frame = frame.copy();
		} else {
			this.frame = mocha.graphics.Rect.zero();
		}
	}

	public mocha.graphics.Rect getHeaderFrame() {
		if(this.headerFrame != null) {
			return this.headerFrame.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	public void setHeaderFrame(mocha.graphics.Rect headerFrame) {
		if(this.headerFrame != null) {
			this.headerFrame = headerFrame.copy();
		} else {
			this.headerFrame = mocha.graphics.Rect.zero();
		}
	}

	public mocha.graphics.Rect getFooterFrame() {
		if(this.footerFrame != null) {
			return this.footerFrame.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	public void setFooterFrame(mocha.graphics.Rect footerFrame) {
		if(this.footerFrame != null) {
			this.footerFrame = footerFrame.copy();
		} else {
			this.footerFrame = mocha.graphics.Rect.zero();
		}
	}

	public float getHeaderDimension() {
		return this.headerDimension;
	}

	public void setHeaderDimension(float headerDimension) {
		this.headerDimension = headerDimension;
	}

	public float getFooterDimension() {
		return this.footerDimension;
	}

	public void setFooterDimension(float footerDimension) {
		this.footerDimension = footerDimension;
	}

	public GridLayoutInfo getLayoutInfo() {
		return this.layoutInfo;
	}

	public void setLayoutInfo(GridLayoutInfo layoutInfo) {
		this.layoutInfo = layoutInfo;
	}

	public HashMap getRowAlignmentOptions() {
		return this.rowAlignmentOptions;
	}

	public void setRowAlignmentOptions(HashMap rowAlignmentOptions) {
		this.rowAlignmentOptions = rowAlignmentOptions;
	}

	public float getOtherMargin() {
		return this.otherMargin;
	}

	public void setOtherMargin(float otherMargin) {
		this.otherMargin = otherMargin;
	}

	public float getBeginMargin() {
		return this.beginMargin;
	}

	public void setBeginMargin(float beginMargin) {
		this.beginMargin = beginMargin;
	}

	public float getEndMargin() {
		return this.endMargin;
	}

	public void setEndMargin(float endMargin) {
		this.endMargin = endMargin;
	}

	public float getActualGap() {
		return this.actualGap;
	}

	public void setActualGap(float actualGap) {
		this.actualGap = actualGap;
	}

	public float getLastRowBeginMargin() {
		return this.lastRowBeginMargin;
	}

	public void setLastRowBeginMargin(float lastRowBeginMargin) {
		this.lastRowBeginMargin = lastRowBeginMargin;
	}

	public float getLastRowEndMargin() {
		return this.lastRowEndMargin;
	}

	public void setLastRowEndMargin(float lastRowEndMargin) {
		this.lastRowEndMargin = lastRowEndMargin;
	}

	public float getLastRowActualGap() {
		return this.lastRowActualGap;
	}

	public void setLastRowActualGap(float lastRowActualGap) {
		this.lastRowActualGap = lastRowActualGap;
	}

	public boolean getLastRowIncomplete() {
		return this.lastRowIncomplete;
	}

	public void setLastRowIncomplete(boolean lastRowIncomplete) {
		this.lastRowIncomplete = lastRowIncomplete;
	}

	public int getItemsByRowCount() {
		return this.itemsByRowCount;
	}

	public void setItemsByRowCount(int itemsByRowCount) {
		this.itemsByRowCount = itemsByRowCount;
	}

	public int getIndexOfImcompleteRow() {
		return this.indexOfImcompleteRow;
	}

	public void setIndexOfImcompleteRow(int indexOfImcompleteRow) {
		this.indexOfImcompleteRow = indexOfImcompleteRow;
	}

}

