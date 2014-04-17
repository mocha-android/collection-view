package mocha.ui.collectionview;

import mocha.graphics.Rect;
import mocha.ui.View;

public class CollectionReusableView extends View {
	private String reuseIdentifier;
	private CollectionView _collectionView;
	private CollectionViewLayout.Attributes _layoutAttributes;
	private boolean inUpdateAnimation;

	public CollectionReusableView(Rect frame) {
		super(frame);
	}

	public void prepareForReuse() {
		this.setLayoutAttributes(null);
	}

	public void applyLayoutAttributes(CollectionViewLayout.Attributes layoutAttributes) {
		if (layoutAttributes != _layoutAttributes) {
		    _layoutAttributes = layoutAttributes;

		    this.setBounds(new Rect(this.getBounds().origin, layoutAttributes.getSize()));
		    this.setCenter(layoutAttributes.getCenter());
		    this.setHidden(layoutAttributes.getHidden());
			this.setTransform(layoutAttributes.getTransform3D());
		    this.getLayer().setZPosition(layoutAttributes.getZIndex());
			this.setAlpha(layoutAttributes.getAlpha());
		}
	}

	public void willTransitionFromLayoutToLayout(CollectionViewLayout oldLayout, CollectionViewLayout newLayout) {
		this.inUpdateAnimation = true;
	}

	public void didTransitionFromLayoutToLayout(CollectionViewLayout oldLayout, CollectionViewLayout newLayout) {
		this.inUpdateAnimation = false;
	}

	void setIndexPath(mocha.foundation.IndexPath indexPath) {

	}

	boolean isInUpdateAnimation() {
		return this.inUpdateAnimation;
	}

	void setInUpdateAnimation(boolean inUpdateAnimation) {
		this.inUpdateAnimation = inUpdateAnimation;
	}

	/* Setters & Getters */
	/* ========================================== */

	String getReuseIdentifier() {
		return this.reuseIdentifier;
	}

	void setReuseIdentifier(String reuseIdentifier) {
		this.reuseIdentifier = reuseIdentifier;
	}

	CollectionView getCollectionView() {
		return this._collectionView;
	}

	void setCollectionView(CollectionView collectionView) {
		this._collectionView = collectionView;
	}

	CollectionViewLayout.Attributes getLayoutAttributes() {
		return this._layoutAttributes;
	}

	void setLayoutAttributes(CollectionViewLayout.Attributes layoutAttributes) {
		this._layoutAttributes = layoutAttributes;
	}

}

