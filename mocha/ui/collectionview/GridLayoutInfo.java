package mocha.ui.collectionview;

import mocha.foundation.MObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GridLayoutInfo extends MObject {
	private List<GridLayoutSection> sections;
	private GridLayoutAlignmentOptions rowAlignmentOptions;
	public boolean usesFloatingHeaderFooter;
	public float dimension;
	public boolean horizontal;
	public boolean leftToRight;
	private mocha.graphics.Size contentSize;
	private mocha.graphics.Rect visibleBounds;
	private mocha.graphics.Size layoutSize;
	private boolean isValid;

	mocha.graphics.Rect frameForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		GridLayoutSection section = this.sections.get(indexPath.section);

		mocha.graphics.Rect itemFrame;

		if (section.getFixedItemSize()) {
		    itemFrame = new mocha.graphics.Rect(mocha.graphics.Point.zero(), section.getItemSize());
		} else {
		    itemFrame = section.getItems().get(indexPath.item).getItemFrame();
		}

		return itemFrame.copy();
	}

	GridLayoutSection addSection() {
		GridLayoutSection section = new GridLayoutSection();
		section.setRowAlignmentOptions(this.getRowAlignmentOptions());
		section.setLayoutInfo(this);
		sections.add(section);
		this.invalidate(false);
		return section;
	}

	void invalidate(boolean arg) {
		isValid = false;
	}

	GridLayoutInfo snapshot() throws IllegalAccessException, InstantiationException {
		GridLayoutInfo layoutInfo = this.getClass().newInstance();
		layoutInfo.setSections(this.getSections());
		layoutInfo.setRowAlignmentOptions(this.getRowAlignmentOptions());
		layoutInfo.setUsesFloatingHeaderFooter(this.getUsesFloatingHeaderFooter());
		layoutInfo.dimension = this.dimension;
		layoutInfo.horizontal = this.horizontal;
		layoutInfo.leftToRight = this.leftToRight;
		layoutInfo.setContentSize(this.getContentSize());
		return layoutInfo;
	}

	public GridLayoutInfo() {
		this.sections = new ArrayList<>();
	}


	protected String toStringExtra() {
		return String.format("dimension:%.01f horizontal:%d contentSize:%s sections:%s", this.dimension, this.horizontal ? 1 : 0, this.getContentSize(), this.getSections());
	}

	/* Setters & Getters */
	/* ========================================== */

	public List<GridLayoutSection> getSections() {
		return this.sections;
	}

	public void setSections(List<GridLayoutSection> sections) {
		this.sections.clear();

		if(sections != null) {
			this.sections.addAll(sections);
		}
	}

	public GridLayoutAlignmentOptions getRowAlignmentOptions() {
		return rowAlignmentOptions;
	}

	public void setRowAlignmentOptions(GridLayoutAlignmentOptions rowAlignmentOptions) {
		if(rowAlignmentOptions == null) {
			this.rowAlignmentOptions = null;
		} else {
			this.rowAlignmentOptions = rowAlignmentOptions.copy();
		}
	}

	public boolean getUsesFloatingHeaderFooter() {
		return this.usesFloatingHeaderFooter;
	}

	public void setUsesFloatingHeaderFooter(boolean usesFloatingHeaderFooter) {
		this.usesFloatingHeaderFooter = usesFloatingHeaderFooter;
	}

	public mocha.graphics.Size getContentSize() {
		if(this.contentSize != null) {
			return this.contentSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setContentSize(mocha.graphics.Size contentSize) {
		if(contentSize != null) {
			this.contentSize = contentSize.copy();
		} else {
			this.contentSize = mocha.graphics.Size.zero();
		}
	}

}

