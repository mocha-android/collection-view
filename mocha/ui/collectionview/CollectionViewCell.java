class CollectionViewCell extends CollectionReusableView {
	private mocha.ui.View _contentView;
	private boolean _selected;
	private boolean _highlighted;
	private mocha.ui.View _backgroundView;
	private mocha.ui.View _selectedBackgroundView;
	private mocha.ui.LongPressGestureRecognizer _menuGesture;
	private Object _selectionSegueTemplate;
	private Object _highlightingSupport;
	private CollectionCellFlagsStruct _collectionCellFlags = new CollectionCellFlagsStruct();

	private class CollectionCellFlagsStruct {
		boolean selected;
		boolean highlighted;
		boolean showingMenu;
		boolean clearSelectionWhenMenuDisappears;
		boolean waitingForSelectionAnimationHalfwayPoint;

	}

	void performSelectionSegue() {
		/*
		    Currently there's no "official" way to trigger a storyboard segue
		    using UIStoryboardSegueTemplate, so we're doing it in a semi-legal way.
		 */
		SEL selector = mocha.foundation.SelectorFromString(String.format("per%s", "form:"));
		if (this->_selectionSegueTemplate.respondsToSelector(selector)) {
		    this->_selectionSegueTemplate.performSelectorWithObject(selector, this);
		}
	}

	public CollectionViewCell(mocha.graphics.Rect frame) {
		super.initWithFrame(frame);

		_backgroundView = new mocha.ui.View(this.getBounds());
		_backgroundView.setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		this.addSubview(_backgroundView);
					
		_contentView = new mocha.ui.View(this.getBounds());
		_contentView.setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		this.addSubview(_contentView);
					
		_menuGesture = new mocha.ui.LongPressGestureRecognizer(this, "menuGesture");
	}

	public CollectionViewCell(mocha.foundation.Coder aDecoder) {
		super.initWithCoder(aDecoder);

		if (this.getSubviews().size() > 0) {
		    _contentView = this.getSubviews().get(0);
		}else {
		    _contentView = new mocha.ui.View(this.getBounds());
		    _contentView.setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		    this.addSubview(_contentView);
		}
					
		_backgroundView = new mocha.ui.View(this.getBounds());
		_backgroundView.setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		this.insertSubviewBelowSubview(_backgroundView, _contentView);
					
		_menuGesture = new mocha.ui.LongPressGestureRecognizer(this, "menuGesture");
	}

	void prepareForReuse() {
		this.setLayoutAttributes(null);
		this.setSelected(false);
		this.setHighlighted(false);
		this.setAccessibilityTraits(mocha.ui.AccessibilityTraitNone);
	}

	void setSelected(boolean selected) {
		_collectionCellFlags.selected = selected;
		this.setAccessibilityTraits(selected ? mocha.ui.AccessibilityTraitSelected : mocha.ui.AccessibilityTraitNone);
		this.updateBackgroundView(selected);
	}

	@mocha.foundation.RuntimeMethod
	public void setHighlighted(boolean highlighted) {
		_collectionCellFlags.highlighted = highlighted;
		this.updateBackgroundView(highlighted);
	}

	void updateBackgroundView(boolean highlight) {
		_selectedBackgroundView.setAlpha(highlight ? 1.0f : 0.0f);
		this.setHighlightedForViews(highlight, this.getContentView().getSubviews());
	}

	void setHighlightedForViews(boolean highlighted, Object subviews) {
		for (id view in subviews) {
		    // Ignore the events if view wants to
		    if (!((mocha.ui.View)view).getIsUserInteractionEnabled() &&
		            view.respondsToSelector("setHighlighted") &&
		            !view.isK : dOfClass(mocha.ui.Control.getClass())) {
		        view.setHighlighted(highlighted);

		        this.setHighlightedForViews(highlighted, view.subviews());
		    }
		}
	}

	@mocha.foundation.RuntimeMethod
	public void menuGesture(mocha.ui.LongPressGestureRecognizer recognizer) {
		MLog("Not yet implemented: %s", StringFromSelector(_cmd));
	}

	void setBackgroundView(mocha.ui.View backgroundView) {
		if (_backgroundView != backgroundView) {
		    _backgroundView.removeFromSuperview();
		    _backgroundView = backgroundView;
		    _backgroundView.setFrame(this.getBounds());
		    _backgroundView.setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		    this.insertSubviewAtIndex(_backgroundView, 0);
		}
	}

	void setSelectedBackgroundView(mocha.ui.View selectedBackgroundView) {
		if (_selectedBackgroundView != selectedBackgroundView) {
		    _selectedBackgroundView.removeFromSuperview();
		    _selectedBackgroundView = selectedBackgroundView;
		    _selectedBackgroundView.setFrame(this.getBounds());
		    _selectedBackgroundView.setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		    _selectedBackgroundView.setAlpha(this.getSelected() ? 1.0f : 0.0f);
		    if (_backgroundView) {
		        this.insertSubviewAboveSubview(_selectedBackgroundView, _backgroundView);
		    }
		    else {
		        this.insertSubviewAtIndex(_selectedBackgroundView, 0);
		    }
		}
	}

	boolean isSelected() {
		return _collectionCellFlags.selected;
	}

	boolean isHighlighted() {
		return _collectionCellFlags.highlighted;
	}

	mocha.foundation.MethodSignature methodSignatureForSelector(SEL selector) {
		mocha.foundation.MethodSignature sig = super.methodSignatureForSelector(selector);
		if(!sig) {
		    String selString = StringFromSelector(selector);
		    if (selString.hasPrefix("_")) {
		        SEL cleanedSelector = mocha.foundation.SelectorFromString(selString.substringFromIndex(1));
		        sig = super.methodSignatureForSelector(cleanedSelector);
		    }
		}
		return sig;
	}

	void forwardInvocation(mocha.foundation.Invocation inv) {
		String selString = StringFromSelector(inv.selector());
		if (selString.hasPrefix("_")) {
		    SEL cleanedSelector = mocha.foundation.SelectorFromString(selString.substringFromIndex(1));
		    if (this.respondsToSelector(cleanedSelector)) {
		        inv.setSelector(cleanedSelector);
		        inv.invokeWithTarget(this);
		    }
		}else {
		    super.forwardInvocation(inv);
		}
	}

	/* Setters & Getters */
	/* ========================================== */

	public mocha.ui.View getContentView() {
		return this._contentView;
	}

	public void setContentView(mocha.ui.View contentView) {
		this._contentView = contentView;
	}

	public boolean getSelected() {
		return this._selected;
	}

	public void setSelected(boolean selected) {
		this._selected = selected;
	}

	public boolean getHighlighted() {
		return this._highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		this._highlighted = highlighted;
	}

	public mocha.ui.View getBackgroundView() {
		return this._backgroundView;
	}

	public void setBackgroundView(mocha.ui.View backgroundView) {
		this._backgroundView = backgroundView;
	}

	public mocha.ui.View getSelectedBackgroundView() {
		return this._selectedBackgroundView;
	}

	public void setSelectedBackgroundView(mocha.ui.View selectedBackgroundView) {
		this._selectedBackgroundView = selectedBackgroundView;
	}

}

