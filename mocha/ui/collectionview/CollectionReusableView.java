class CollectionReusableView extends mocha.ui.View {
	private String _reuseIdentifier;
	private CollectionView _collectionView;
	private CollectionViewLayout.Attributes _layoutAttributes;
	private Character filler;
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

		    this.setBounds(new mocha.graphics.Rect(this.getBounds().getOrigin(), layoutAttributes.size));
		    this.setCenter(layoutAttributes.getCenter());
		    this.setHidden(layoutAttributes.getHidden());
		    this.getLayer().setTransform(layoutAttributes.getTransform3D());
		    this.getLayer().setZPosition(layoutAttributes.getZIndex());
		    this.getLayer().setOpacity(layoutAttributes.getAlpha());
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
		super.initWithFrame(frame);
	}

	public CollectionReusableView(mocha.foundation.Coder aDecoder) {
		super.initWithCoder(aDecoder);

		this.setReuseIdentifier(aDecoder.decodeObjectForKey("UIReuseIdentifier"));
	}

	void awakeFromNib() {
		this.setReuseIdentifier(this.valueForKeyPath("reuseIdentifier"));
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
		return this.reuseIdentifier;
	}

	void setReuseIdentifier(String reuseIdentifier) {
		this.reuseIdentifier = reuseIdentifier;
	}

	CollectionView getCollectionView() {
		return this.collectionView;
	}

	void setCollectionView(CollectionView collectionView) {
		this.collectionView = collectionView;
	}

	CollectionViewLayout.Attributes getLayoutAttributes() {
		return this.layoutAttributes;
	}

	void setLayoutAttributes(CollectionViewLayout.Attributes layoutAttributes) {
		this.layoutAttributes = layoutAttributes;
	}

}

