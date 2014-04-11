class GridLayoutItem extends MObject {
	private GridLayoutSection _section;
	private GridLayoutRow _rowObject;
	private mocha.graphics.Rect _itemFrame;

	String description() {
		return String.format("<%s: %p itemFrame:%s>", StringFromClass(this.getClass()), this, StringFromCGRect(this.getItemFrame()));
	}

	/* Setters & Getters */
	/* ========================================== */

	public GridLayoutSection getSection() {
		return this.section;
	}

	public void setSection(GridLayoutSection section) {
		this.section = section;
	}

	public GridLayoutRow getRowObject() {
		return this.rowObject;
	}

	public void setRowObject(GridLayoutRow rowObject) {
		this.rowObject = rowObject;
	}

	public mocha.graphics.Rect getItemFrame() {
		if(this.itemFrame != null) {
			return this.itemFrame.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	public void setItemFrame(mocha.graphics.Rect itemFrame) {
		if(this.itemFrame != null) {
			this.itemFrame = itemFrame.copy();
		} else {
			this.itemFrame = mocha.graphics.Rect.zero();
		}
	}

}

