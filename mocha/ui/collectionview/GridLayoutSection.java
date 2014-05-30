package mocha.ui.collectionview;

import mocha.foundation.Assert;
import mocha.foundation.Copying;
import mocha.foundation.MObject;
import mocha.graphics.Rect;
import mocha.graphics.Size;
import mocha.ui.EdgeInsets;

import java.util.*;

class GridLayoutSection extends MObject implements Copying<GridLayoutSection> {
	private List<GridLayoutItem> items;
	private List<GridLayoutRow> rows;
	private boolean fixedItemSize;
	private mocha.graphics.Size itemSize;
	private int itemsCount;
	private float verticalInterstice;
	private float horizontalInterstice;
	private EdgeInsets edgeInsets;
	private Rect frame;
	private Rect headerFrame;
	private Rect footerFrame;
	private float headerDimension;
	private float footerDimension;
	private GridLayoutInfo layoutInfo;
	private GridLayoutAlignmentOptions rowAlignmentOptions;
	private float otherMargin;
	private float beginMargin;
	private float endMargin;
	private float actualGap;
	private float lastRowBeginMargin;
	private float lastRowEndMargin;
	private float lastRowActualGap;
	private boolean lastRowIncomplete;
	private int itemsByRowCount;
	private int indexOfImcompleteRow;
	private boolean valid;

	public GridLayoutSection() {
		this.items = new ArrayList<>();
		this.rows = new ArrayList<>();

		/*this.edgeInsets = EdgeInsets.zero();
		this.frame = Rect.zero();
		this.headerFrame = Rect.zero();
		this.footerFrame = Rect.zero();*/
	}

	private void recomputeFromIndex(int index) {
		// TODO: use index.
		this.invalidate();
		this.computeLayout();
	}

	public void invalidate() {
		this.valid = false;
		this.setRows(null);
	}

	public void computeLayout() {
		if (this.valid) return;

		Assert.condition(this.rows.size() == 0, "No rows shall be at this point.");
		// MLog("CV_TEST computeLayout: " + this.itemsCount + ", " + this.itemSize);

		// iterate over all items, turning them into rows.
		mocha.graphics.Size sectionSize = mocha.graphics.Size.zero();

		int itemsByRowCount = 0;
		float dimensionLeft = 0;

		// get dimension and compensate for section margin
		float headerFooterDimension = this.layoutInfo.dimension;
		float dimension = headerFooterDimension;

		if (this.layoutInfo.horizontal) {
			dimension -= this.edgeInsets.top + this.edgeInsets.bottom;
			this.setHeaderFrame(new Rect(sectionSize.width, 0, this.headerDimension, headerFooterDimension));
			sectionSize.width += this.headerDimension + this.edgeInsets.left;
		} else {
			dimension -= this.edgeInsets.left + this.edgeInsets.right;
			this.setHeaderFrame(new Rect(0, sectionSize.height, headerFooterDimension, this.headerDimension));
			sectionSize.height += this.headerDimension + this.edgeInsets.top;
		}

		float spacing = this.layoutInfo.horizontal ? this.verticalInterstice : this.horizontalInterstice;

		int rowIndex = 0;
		GridLayoutRow row = null;

		int itemsCount = this.getItemsCount();
		for(int itemIndex = 0; itemIndex <= itemsCount; itemIndex++) {
			boolean finishCycle = itemIndex >= itemsCount;

			// TODO: fast path could even remove row creation and just calculate on the fly
			GridLayoutItem item = null;
			Size itemSize;

			if (finishCycle) {
				itemSize = Size.zero();
			} else {
				item = this.fixedItemSize ? null : this.items.get(itemIndex);
				itemSize = item == null ? this.itemSize : item.getItemFrame().size;
			}

			// MWarn("CV_TEST COMPUTE_LAYOUT %s %s %s %s %s %s %s", this.fixedItemSize, this.itemSize, item, finishCycle, itemIndex, itemsCount, this.items.size());
			float itemDimension = this.layoutInfo.horizontal ? itemSize.height : itemSize.width;

			// first item of each row does not add spacing
			if (itemsByRowCount > 0) {
				itemDimension += spacing;
			}

			if (dimensionLeft < itemDimension || finishCycle) {
				// finish current row
				if (row != null) {
					// compensate last row
					this.itemsByRowCount = Math.max(itemsByRowCount, this.itemsByRowCount);
					row.setItemCount(itemsByRowCount);

					// if current row is done but there are still items left, increase the incomplete row counter
					if (!finishCycle) {
						this.indexOfImcompleteRow = rowIndex;
					}

					row.layoutRow();

					Size rowSize = row.getRowSize();

					if (this.layoutInfo.horizontal) {
						row.setRowFrame(new Rect(sectionSize.width, this.edgeInsets.top, rowSize.width, rowSize.height));
						sectionSize.height = Math.max(rowSize.height, sectionSize.height);
						sectionSize.width += rowSize.width + (finishCycle ? 0 : this.horizontalInterstice);
					} else {
						row.setRowFrame(new Rect(this.edgeInsets.left, sectionSize.height, rowSize.width, rowSize.height));
						sectionSize.height += rowSize.height + (finishCycle ? 0 : this.verticalInterstice);
						sectionSize.width = Math.max(rowSize.width, sectionSize.width);
					}
				}

				// add new rows until the section is fully laid out
				if (!finishCycle) {
					if(row != null) {
						row.setComplete(true); // finish up current row
					}

					// create new row
					row = this.addRow();
					row.setFixedItemSize(this.fixedItemSize);
					row.setIndex(rowIndex);
					this.indexOfImcompleteRow = rowIndex;
					rowIndex++;

					// convert an item from previous row to current, remove spacing for first item
					if (itemsByRowCount > 0) itemDimension -= spacing;
					dimensionLeft = dimension - itemDimension;
					itemsByRowCount = 0;
				}
			} else {
				dimensionLeft -= itemDimension;
			}

			// add item on slow path
			if (item != null && row != null) {
				row.addItem(item);
			}

			// MLog("CV_TEST, computeLayout, adding " + item + " to " + row);

			itemsByRowCount++;
		}

		if (this.layoutInfo.horizontal) {
			sectionSize.width += this.edgeInsets.right;
			this.setFooterFrame(new Rect(sectionSize.width, 0, this.footerDimension, headerFooterDimension));
			sectionSize.width += this.footerDimension;
		} else {
			sectionSize.height += this.edgeInsets.bottom;
			this.setFooterFrame(new Rect(0, sectionSize.height, headerFooterDimension, this.footerDimension));
			sectionSize.height += this.footerDimension;
		}

		this.frame = new Rect(0, 0, sectionSize.width, sectionSize.height);
		this.valid = true;
	}

	public GridLayoutItem addItem() {
		GridLayoutItem item = new GridLayoutItem();
		item.setSection(this);
		this.items.add(item);
		return item;
	}

	public GridLayoutRow addRow() {
		GridLayoutRow row = new GridLayoutRow();
		row.setSection(this);
		this.rows.add(row);
		return row;
	}

	public GridLayoutSection copy() {
		GridLayoutSection copy = new GridLayoutSection();
		copy.rowAlignmentOptions = this.rowAlignmentOptions == null ? null : this.rowAlignmentOptions.copy();
		copy.items.addAll(this.items);
		copy.rows.addAll(this.rows);

		copy.edgeInsets = this.edgeInsets == null ? null : this.edgeInsets.copy();
		copy.itemSize = this.itemSize == null ? null : this.itemSize.copy();

		copy.frame = this.frame == null ? null : this.frame.copy();
		copy.headerFrame = this.headerFrame == null ? null : this.headerFrame.copy();
		copy.footerFrame = this.footerFrame == null ? null : this.footerFrame.copy();

		copy.verticalInterstice = this.verticalInterstice;
		copy.horizontalInterstice = this.horizontalInterstice;
		copy.headerDimension = this.headerDimension;
		copy.footerDimension = this.footerDimension;
		copy.layoutInfo = this.layoutInfo;
		copy.fixedItemSize = this.fixedItemSize;
		copy.itemsCount = this.itemsCount;
		copy.otherMargin = this.otherMargin;
		copy.beginMargin = this.beginMargin;
		copy.endMargin = this.endMargin;
		copy.actualGap = this.actualGap;
		copy.lastRowBeginMargin = this.lastRowBeginMargin;
		copy.lastRowEndMargin = this.lastRowEndMargin;
		copy.lastRowActualGap = this.lastRowActualGap;
		copy.lastRowIncomplete = this.lastRowIncomplete;
		copy.itemsByRowCount = this.itemsByRowCount;
		copy.indexOfImcompleteRow = this.indexOfImcompleteRow;

		return copy;
	}

	protected String toStringExtra() {
		return String.format("itemCount:%d frame:%s rows:%s", this.getItemsCount(), this.getFrame(), this.getRows());
	}

	/* Setters & Getters */
	/* ========================================== */

	public List<GridLayoutItem> getItems() {
		return this.items;
	}

	public void setItems(List<GridLayoutItem> items) {
		this.rows.clear();

		if(items != null) {
			this.items.addAll(items);
		}
	}

	public List<GridLayoutRow> getRows() {
		return this.rows;
	}

	public void setRows(List<GridLayoutRow> rows) {
		this.rows.clear();

		if(rows != null) {
			this.rows.addAll(rows);
		}
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
		if(itemSize != null) {
			this.itemSize = itemSize.copy();
			// MWarn("CV_TEST, SET ITEM SIZE TO: " + itemSize);
		} else {
			this.itemSize = mocha.graphics.Size.zero();
			// MWarn("CV_TEST, SET ITEM SIZE TO: ZERO");
		}
	}

	public int getItemsCount() {
		return this.fixedItemSize ? this.itemsCount : this.items.size();
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

	public EdgeInsets getSectionMargins() {
		if(this.edgeInsets != null) {
			return this.edgeInsets.copy();
		} else {
			return EdgeInsets.zero();
		}
	}

	public void setSectionMargins(EdgeInsets sectionMargins) {
		if(edgeInsets != null) {
			this.edgeInsets = sectionMargins.copy();
		} else {
			this.edgeInsets = EdgeInsets.zero();
		}
	}

	public Rect getFrame() {
		if(this.frame != null) {
			return this.frame.copy();
		} else {
			return Rect.zero();
		}
	}

	public void setFrame(Rect frame) {
		if(frame != null) {
			this.frame = frame.copy();
		} else {
			this.frame = Rect.zero();
		}
	}

	public Rect getHeaderFrame() {
		if(this.headerFrame != null) {
			return this.headerFrame.copy();
		} else {
			return Rect.zero();
		}
	}

	public void setHeaderFrame(Rect headerFrame) {
		if(headerFrame != null) {
			this.headerFrame = headerFrame.copy();
		} else {
			this.headerFrame = Rect.zero();
		}
	}

	public Rect getFooterFrame() {
		if(this.footerFrame != null) {
			return this.footerFrame.copy();
		} else {
			return Rect.zero();
		}
	}

	public void setFooterFrame(Rect footerFrame) {
		if(footerFrame != null) {
			this.footerFrame = footerFrame.copy();
		} else {
			this.footerFrame = Rect.zero();
		}
	}

	public void setHeaderDimension(float headerDimension) {
		this.headerDimension = headerDimension;
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


	public GridLayoutAlignmentOptions getRowAlignmentOptions() {
		return this.rowAlignmentOptions;
	}

	public void setRowAlignmentOptions(GridLayoutAlignmentOptions rowAlignmentOptions) {
		if(rowAlignmentOptions != null) {
			this.rowAlignmentOptions = rowAlignmentOptions.copy();
		} else {
			this.rowAlignmentOptions = null;
		}
	}

	public int getItemsByRowCount() {
		return this.itemsByRowCount;
	}

	public int getIndexOfImcompleteRow() {
		return this.indexOfImcompleteRow;
	}

}

