package mocha.ui.collectionview;

import mocha.foundation.MObject;

class GridLayoutItem extends MObject {
	private GridLayoutSection section;
	private GridLayoutRow rowObject;
	private mocha.graphics.Rect itemFrame;

	protected String toStringExtra() {
		return String.format("itemFrame:%s", this.getItemFrame());
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
		if(itemFrame != null) {
			this.itemFrame = itemFrame.copy();
		} else {
			this.itemFrame = mocha.graphics.Rect.zero();
		}
	}

}

