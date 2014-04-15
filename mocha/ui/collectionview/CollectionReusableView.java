package mocha.ui.collectionview;

public class CollectionReusableView extends mocha.ui.View {
	private String _reuseIdentifier;
	private CollectionView _collectionView;
	private CollectionViewLayout.Attributes _layoutAttributes;
	private ReusableViewFlagsStruct _reusableViewFlags = new ReusableViewFlagsStruct();

	private class ReusableViewFlagsStruct {
		boolean inUpdateAnimation;

	}

	void prepareForReuse() {
		this.setLayoutAttributes(null);
	}

	void applyLayoutAttributes(CollectionViewLayout.Attributes layoutAttributes) {
		if (layoutAttributes != _layoutAttributes) {
		    _layoutAttributes = layoutAttributes;

		    this.setBounds(new mocha.graphics.Rect(this.getBounds().origin, layoutAttributes.getSize()));
		    this.setCenter(layoutAttributes.getCenter());
		    this.setHidden(layoutAttributes.getHidden());
			this.setTransform(layoutAttributes.getTransform3D());
		    this.getLayer().setZPosition(layoutAttributes.getZIndex());
			this.setAlpha(layoutAttributes.getAlpha());
		}
	}

	void willTransitionFromLayoutToLayout(CollectionViewLayout oldLayout, CollectionViewLayout newLayout) {
		_reusableViewFlags.inUpdateAnimation = true;
	}

	void didTransitionFromLayoutToLayout(CollectionViewLayout oldLayout, CollectionViewLayout newLayout) {
		_reusableViewFlags.inUpdateAnimation = false;
	}

	void setIndexPath(mocha.foundation.IndexPath indexPath) {

	}

	public CollectionReusableView(mocha.graphics.Rect frame) {
		super(frame);
	}

	boolean isInUpdateAnimation() {
		return _reusableViewFlags.inUpdateAnimation;
	}

	void setInUpdateAnimation(boolean inUpdateAnimation) {
		_reusableViewFlags.inUpdateAnimation = inUpdateAnimation;
	}

	/* Setters & Getters */
	/* ========================================== */

	String getReuseIdentifier() {
		return this._reuseIdentifier;
	}

	void setReuseIdentifier(String reuseIdentifier) {
		this._reuseIdentifier = reuseIdentifier;
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
