package mocha.ui.collectionview;

import mocha.graphics.Rect;
import mocha.ui.View;

public class CollectionReusableView extends View {
	String reuseIdentifier;

	private CollectionView collectionView;
	private CollectionViewLayoutAttributes layoutAttributes;
	private boolean inUpdateAnimation;

	public CollectionReusableView(Rect frame) {
		super(frame);
	}

	public void prepareForReuse() {
		this.setLayoutAttributes(null);
	}

	public void applyLayoutAttributes(CollectionViewLayoutAttributes layoutAttributes) {
		if (layoutAttributes != this.layoutAttributes) {
			this.setLayoutAttributes(layoutAttributes);

			this.setFrame(layoutAttributes.frame);
//		    this.setBounds(new Rect(this.getBounds().origin, layoutAttributes.getSize()));
//		    this.setCenter(layoutAttributes.getCenter());
			this.setHidden(layoutAttributes.hidden);
			this.setTransform(layoutAttributes.transform3D);
		    // this.getLayer().setZPosition(layoutAttributes.getZIndex());
			this.setAlpha(layoutAttributes.alpha);
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

	CollectionViewLayoutAttributes getLayoutAttributes() {
		return this.layoutAttributes;
	}

	void setLayoutAttributes(CollectionViewLayoutAttributes layoutAttributes) {
		if(this.layoutAttributes != layoutAttributes) {
			if(this.layoutAttributes != null && this.collectionView != null) {
				// this.collectionView.getCollectionViewLayout().enqueueLayoutAttributes(this.layoutAttributes);
			}

			this.layoutAttributes = layoutAttributes;
		}
	}

}

