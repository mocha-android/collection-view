package mocha.ui.collectionview;

import mocha.graphics.Rect;
import mocha.ui.Highlightable;
import mocha.ui.LongPressGestureRecognizer;
import mocha.ui.View;

import java.util.List;

class CollectionViewCell extends CollectionReusableView implements Highlightable {
	private View contentView;
	private View backgroundView;
	private View selectedBackgroundView;
	private LongPressGestureRecognizer menuGesture;
	private CollectionCellFlagsStruct collectionCellFlags = new CollectionCellFlagsStruct();

	private class CollectionCellFlagsStruct {
		boolean selected;
		boolean highlighted;
		boolean showingMenu;
		boolean clearSelectionWhenMenuDisappears;
		boolean waitingForSelectionAnimationHalfwayPoint;
	}

	public CollectionViewCell(mocha.graphics.Rect frame) {
		super(frame);

		Rect bounds = this.getBounds();

		this.backgroundView = new View(bounds);
		this.backgroundView.setAutoresizing(Autoresizing.FLEXIBLE_WIDTH, Autoresizing.FLEXIBLE_HEIGHT);
		this.addSubview(this.backgroundView);

		this.contentView = new View(bounds);
		this.contentView.setAutoresizing(Autoresizing.FLEXIBLE_WIDTH, Autoresizing.FLEXIBLE_HEIGHT);
		this.addSubview(this.contentView);

//		this.menuGesture = new mocha.ui.LongPressGestureRecognizer(new GestureRecognizer.GestureHandler() {
//			public void handleGesture(GestureRecognizer gestureRecognizer) {
//				menuGesture((LongPressGestureRecognizer)gestureRecognizer);
//			}
//		});
	}

	public void prepareForReuse() {
		this.setLayoutAttributes(null);
		this.setSelected(false);
		this.setHighlighted(false);
		this.setAccessibilityTraits(Trait.NONE);
	}

	public void setHighlighted(boolean highlighted) {
		this.collectionCellFlags.highlighted = highlighted;
		this.updateBackgroundView(highlighted);
	}

	void updateBackgroundView(boolean highlight) {
		this.selectedBackgroundView.setAlpha(highlight ? 1.0f : 0.0f);
		this.setHighlightedForViews(highlight, this.getContentView().getSubviews());
	}

	void setHighlightedForViews(boolean highlighted, List<View> subviews) {
		for (View view : subviews) {
			// Ignore the events if view wants to
			if (!view.isUserInteractionEnabled() && view instanceof Highlightable && !(view instanceof mocha.ui.Control)) {
				((Highlightable) view).setHighlighted(highlighted);
				this.setHighlightedForViews(highlighted, view.getSubviews());
			}
		}
	}

	private void menuGesture(LongPressGestureRecognizer recognizer) {

	}

	public void setBackgroundView(View backgroundView) {
		if (this.backgroundView != backgroundView) {
			if (this.backgroundView != null) {
				this.backgroundView.removeFromSuperview();
			}

			this.backgroundView = backgroundView;

			if (this.backgroundView != null) {
				this.backgroundView.setFrame(this.getBounds());
				this.backgroundView.setAutoresizing(Autoresizing.FLEXIBLE_WIDTH, Autoresizing.FLEXIBLE_HEIGHT);
				this.insertSubview(this.backgroundView, 0);
			}
		}
	}

	public void setSelectedBackgroundView(View selectedBackgroundView) {
		if (this.selectedBackgroundView != selectedBackgroundView) {
			if (this.selectedBackgroundView != null) {
				this.selectedBackgroundView.removeFromSuperview();
			}

			this.selectedBackgroundView = selectedBackgroundView;

			if (this.selectedBackgroundView != null) {
				this.selectedBackgroundView.setFrame(this.getBounds());
				this.selectedBackgroundView.setAutoresizing(Autoresizing.FLEXIBLE_WIDTH, Autoresizing.FLEXIBLE_HEIGHT);
				this.selectedBackgroundView.setAlpha(this.isSelected() ? 1.0f : 0.0f);

				if (this.backgroundView != null) {
					this.insertSubviewAboveSubview(this.selectedBackgroundView, this.backgroundView);
				} else {
					this.insertSubview(this.selectedBackgroundView, 0);
				}
			}
		}
	}

	/* Setters & Getters */
	/* ========================================== */

	public View getContentView() {
		return this.contentView;
	}

	public void setContentView(View contentView) {
		this.contentView = contentView;
	}

	public boolean isSelected() {
		return this.collectionCellFlags.selected;
	}

	public void setSelected(boolean selected) {
		this.collectionCellFlags.selected = selected;
		this.setAccessibilityTraits(selected ? Trait.SELECTED : Trait.NONE);
		this.updateBackgroundView(selected);
	}

	public boolean isHighlighted() {
		return collectionCellFlags.highlighted;
	}

	public View getBackgroundView() {
		return this.backgroundView;
	}

	public View getSelectedBackgroundView() {
		return this.selectedBackgroundView;
	}

}

