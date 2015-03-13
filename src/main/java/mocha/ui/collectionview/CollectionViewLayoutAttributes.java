/**
 *  @author Shaun Harison
 *  @date 4/29/14
 *  @copyright 2014 Mocha. All rights reserved.
 */
package mocha.ui.collectionview;

import mocha.foundation.IndexPath;
import mocha.foundation.MObject;
import mocha.graphics.AffineTransform;
import mocha.graphics.Point;
import mocha.graphics.Rect;
import mocha.graphics.Size;

public class CollectionViewLayoutAttributes extends MObject implements mocha.foundation.Copying<CollectionViewLayoutAttributes> {
	public static final String REUSE_IDENTIFIER = "mocha.ui.collectionview.CollectionViewLayoutAttributes";

	final Rect frame;
	final Point center;
	final Size size;
	final AffineTransform transform3D;
	float alpha;
	int zIndex;
	boolean hidden;
	IndexPath indexPath;
	String representedElementKind;
	CollectionElementCategory representedElementCategory;

	String reuseIdentifier;

	public CollectionViewLayoutAttributes() {
		this.alpha = 1.0f;
		this.transform3D = AffineTransform.identity();
		this.frame = Rect.zero();
		this.size = Size.zero();
		this.center = Point.zero();
	}

	boolean isDecorationView() {
		return this.representedElementCategory == CollectionElementCategory.DECORATION_VIEW;
	}

	boolean isSupplementaryView() {
		return this.representedElementCategory == CollectionElementCategory.SUPPLEMENTARY_VIEW;
	}

	boolean isCell() {
		return this.representedElementCategory == CollectionElementCategory.CELL;
	}

	public int hashCode() {
		return (this.representedElementKind.hashCode() * 31) + this.indexPath.hashCode();
	}

	public boolean equals(Object other) {
		if(other == this) {
			return true;
		} else if(this.getClass().isAssignableFrom(other.getClass())) {
			CollectionViewLayoutAttributes otherLayoutAttributes = (CollectionViewLayoutAttributes)other;

			if (this.representedElementCategory == otherLayoutAttributes.representedElementCategory && this.representedElementKind.equals(otherLayoutAttributes.representedElementKind) && this.indexPath.equals(otherLayoutAttributes.indexPath)) {
				return true;
			}
		}

		return false;
	}

	public void prepareForReuse() {
		this.alpha = 1.0f;
		this.transform3D.set(null);
		this.frame.set(null);
		this.size.set(null);
		this.center.set(null);
	}

	protected String toStringExtra() {
		return String.format("frame=%s; indexPath=%s; elementKind=%s", this.frame, this.indexPath, this.representedElementKind);
	}

	public CollectionViewLayoutAttributes copy() {
		CollectionViewLayoutAttributes layoutAttributes;

		try {
			layoutAttributes = this.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		layoutAttributes.indexPath = this.indexPath;
		layoutAttributes.representedElementKind = this.representedElementKind;
		layoutAttributes.representedElementCategory = this.representedElementCategory;
		layoutAttributes.center.set(this.center);
		layoutAttributes.size.set(this.size);
		layoutAttributes.frame.set(this.frame);
		layoutAttributes.transform3D.set(this.transform3D);
		layoutAttributes.alpha = this.alpha;
		layoutAttributes.zIndex = this.zIndex;
		layoutAttributes.hidden = this.hidden;

		return layoutAttributes;
	}

	public final Rect getFrame() {
		return this.frame.copy();
	}

	public void setFrame(Rect frame) {
		this.frame.set(frame);
		this.size.set(frame.size);
		this.center.set(this.frame.mid());
	}

	public final Point getCenter() {
		return this.center.copy();
	}

	public void setCenter(Point center) {
		this.center.set(center);
		this.updateFrame();
	}

	private void updateFrame() {
		this.frame.size.set(this.size);
		this.frame.origin.x = this.center.x - (this.size.width / 2.0f);
		this.frame.origin.y = this.center.y - (this.size.height / 2.0f);
	}

	public final Size getSize() {
		return this.size.copy();
	}

	public void setSize(Size size) {
		this.size.set(size);
		this.updateFrame();
	}

	public final AffineTransform getTransform3D() {
		return this.transform3D.copy();
	}

	public void setTransform3D(AffineTransform transform3D) {
		this.transform3D.set(transform3D);
	}

	public final float getAlpha() {
		return this.alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public final int getZIndex() {
		return this.zIndex;
	}

	public void setZIndex(int zIndex) {
		this.zIndex = zIndex;
	}

	public final boolean getHidden() {
		return this.hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public final IndexPath getIndexPath() {
		return this.indexPath;
	}

	public final void setIndexPath(IndexPath indexPath) {
		this.indexPath = indexPath;
	}

	public final String getRepresentedElementKind() {
		return this.representedElementKind;
	}

	final void setRepresentedElementKind(String representedElementKind) {
		this.representedElementKind = representedElementKind;
	}

	public final CollectionElementCategory getRepresentedElementCategory() {
		return this.representedElementCategory;
	}

	final void setRepresentedElementCategory(CollectionElementCategory representedElementCategory) {
		this.representedElementCategory = representedElementCategory;
	}

}
