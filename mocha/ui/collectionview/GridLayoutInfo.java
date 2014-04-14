package mocha.ui.collectionview;

import mocha.foundation.MObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GridLayoutInfo extends MObject {
	private List<GridLayoutSection> sections;
	private Map<String,CollectionViewFlowLayout.FlowLayoutAlignment> rowAlignmentOptions;
	private boolean usesFloatingHeaderFooter;
	private float dimension;
	private boolean horizontal;
	private boolean leftToRight;
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
		layoutInfo.setDimension(this.getDimension());
		layoutInfo.setHorizontal(this.getHorizontal());
		layoutInfo.setLeftToRight(this.getLeftToRight());
		layoutInfo.setContentSize(this.getContentSize());
		return layoutInfo;
	}

	public GridLayoutInfo() {
		this.sections = new ArrayList<>();
		this.rowAlignmentOptions = new HashMap<>();
	}


	protected String toStringExtra() {
		return String.format("dimension:%.01f horizontal:%d contentSize:%s sections:%s", this.getDimension(), this.getHorizontal() ? 1 : 0, this.getContentSize(), this.getSections());
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

	public Map<String,CollectionViewFlowLayout.FlowLayoutAlignment> getRowAlignmentOptions() {
		return this.rowAlignmentOptions;
	}

	public void setRowAlignmentOptions(Map<String,CollectionViewFlowLayout.FlowLayoutAlignment> rowAlignmentOptions) {
		this.rowAlignmentOptions.clear();

		if(rowAlignmentOptions != null) {
			this.rowAlignmentOptions.putAll(rowAlignmentOptions);
		}
	}

	public boolean getUsesFloatingHeaderFooter() {
		return this.usesFloatingHeaderFooter;
	}

	public void setUsesFloatingHeaderFooter(boolean usesFloatingHeaderFooter) {
		this.usesFloatingHeaderFooter = usesFloatingHeaderFooter;
	}

	public float getDimension() {
		return this.dimension;
	}

	public void setDimension(float dimension) {
		this.dimension = dimension;
	}

	public boolean getHorizontal() {
		return this.horizontal;
	}

	public void setHorizontal(boolean horizontal) {
		this.horizontal = horizontal;
	}

	public boolean getLeftToRight() {
		return this.leftToRight;
	}

	public void setLeftToRight(boolean leftToRight) {
		this.leftToRight = leftToRight;
	}

	public mocha.graphics.Size getContentSize() {
		if(this.contentSize != null) {
			return this.contentSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setContentSize(mocha.graphics.Size contentSize) {
		if(this.contentSize != null) {
			this.contentSize = contentSize.copy();
		} else {
			this.contentSize = mocha.graphics.Size.zero();
		}
	}

}

