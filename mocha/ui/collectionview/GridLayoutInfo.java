class GridLayoutInfo extends MObject {
	private ArrayList _sections;
	private HashMap _rowAlignmentOptions;
	private boolean _usesFloatingHeaderFooter;
	private float _dimension;
	private boolean _horizontal;
	private boolean _leftToRight;
	private mocha.graphics.Size _contentSize;
	private mocha.graphics.Rect _visibleBounds;
	private mocha.graphics.Size _layoutSize;
	private boolean _isValid;

	mocha.graphics.Rect frameForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		GridLayoutSection section = this.getSections().get(indexPath.section);
		mocha.graphics.Rect itemFrame;
		if (section.getFixedItemSize()) {
		    itemFrame = new mocha.graphics.Rect(mocha.graphics.Point.zero(), section.itemSize);
		}else {
		    itemFrame = section.getItems().get(indexPath.item).itemFrame();
		}
		return itemFrame.copy();
	}

	Object addSection() {
		GridLayoutSection section = new GridLayoutSection();
		section.setRowAlignmentOptions(this.getRowAlignmentOptions());
		section.setLayoutInfo(this);
		_sections.addObject(section);
		this.invalidate(false);
		return section;
	}

	void invalidate(boolean arg) {
		_isValid = false;
	}

	GridLayoutInfo snapshot() {
		GridLayoutInfo layoutInfo = this.getClass().new();
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
		super.init();

		_sections = new ArrayList();
	}

	String description() {
		return String.format("<%s: %p dimension:%.1f horizontal:%d contentSize:%s sections:%s>", StringFromClass(this.getClass()), this, this.getDimension(), this.getHorizontal(), StringFromCGSize(this.getContentSize()), this.getSections());
	}

	/* Setters & Getters */
	/* ========================================== */

	public ArrayList getSections() {
		return this._sections;
	}

	public void setSections(ArrayList sections) {
		this._sections = sections;
	}

	public HashMap getRowAlignmentOptions() {
		return this._rowAlignmentOptions;
	}

	public void setRowAlignmentOptions(HashMap rowAlignmentOptions) {
		this._rowAlignmentOptions = rowAlignmentOptions;
	}

	public boolean getUsesFloatingHeaderFooter() {
		return this._usesFloatingHeaderFooter;
	}

	public void setUsesFloatingHeaderFooter(boolean usesFloatingHeaderFooter) {
		this._usesFloatingHeaderFooter = usesFloatingHeaderFooter;
	}

	public float getDimension() {
		return this._dimension;
	}

	public void setDimension(float dimension) {
		this._dimension = dimension;
	}

	public boolean getHorizontal() {
		return this._horizontal;
	}

	public void setHorizontal(boolean horizontal) {
		this._horizontal = horizontal;
	}

	public boolean getLeftToRight() {
		return this._leftToRight;
	}

	public void setLeftToRight(boolean leftToRight) {
		this._leftToRight = leftToRight;
	}

	public mocha.graphics.Size getContentSize() {
		if(this._contentSize != null) {
			return this._contentSize.copy();
		} else {
			return mocha.graphics.Size.zero();
		}
	}

	public void setContentSize(mocha.graphics.Size contentSize) {
		if(this._contentSize != null) {
			this._contentSize = contentSize.copy();
		} else {
			this._contentSize = mocha.graphics.Size.zero();
		}
	}

}

