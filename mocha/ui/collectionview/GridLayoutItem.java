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
		return this._section;
	}

	public void setSection(GridLayoutSection section) {
		this._section = section;
	}

	public GridLayoutRow getRowObject() {
		return this._rowObject;
	}

	public void setRowObject(GridLayoutRow rowObject) {
		this._rowObject = rowObject;
	}

	public mocha.graphics.Rect getItemFrame() {
		if(this._itemFrame != null) {
			return this._itemFrame.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	public void setItemFrame(mocha.graphics.Rect itemFrame) {
		if(this._itemFrame != null) {
			this._itemFrame = itemFrame.copy();
		} else {
			this._itemFrame = mocha.graphics.Rect.zero();
		}
	}

}

