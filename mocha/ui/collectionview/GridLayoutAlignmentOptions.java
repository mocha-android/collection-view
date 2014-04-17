/**
 *  @author Shaun
 *  @date 4/17/14
 *  @copyright 2014 Mocha. All rights reserved.
 */
package mocha.ui.collectionview;

import mocha.foundation.Copying;

class GridLayoutAlignmentOptions implements Copying<GridLayoutAlignmentOptions> {
	public enum Alignment {
		MIN,
		MID,
		MAX,
		JUSTIFY
	}

	public Alignment horizontal;
	public Alignment horizontalLastRow;
	public Alignment vertical;

	GridLayoutAlignmentOptions() {
		this.horizontal = Alignment.JUSTIFY;
		this.horizontalLastRow = Alignment.JUSTIFY;
		this.vertical = Alignment.MIN;
	}

	GridLayoutAlignmentOptions(Alignment horizontal, Alignment horizontalLastRow, Alignment vertical) {
		this.horizontal = horizontal;
		this.horizontalLastRow = horizontalLastRow;
		this.vertical = vertical;
	}

	public GridLayoutAlignmentOptions copy() {
		return new GridLayoutAlignmentOptions(this.horizontal, this.horizontalLastRow, this.vertical);
	}
}
