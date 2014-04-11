class CollectionViewData extends MObject {
	private boolean _layoutIsPrepared;
	private mocha.graphics.Rect _validLayoutRect;
	private int _numItems;
	private int _numSections;
	private int _sectionItemCounts;
	private mocha.graphics.Size _contentSize;
	private CollectionViewDataFlagsStruct _collectionViewDataFlags = new CollectionViewDataFlagsStruct();
	private CollectionView _collectionView;
	private CollectionViewLayout _layout;
	private ArrayList _cachedLayoutAttributes;

	private class CollectionViewDataFlagsStruct {
		boolean contentSizeIsValid;
		boolean itemCountsAreValid;
		boolean layoutIsPrepared;

	}

	public CollectionViewData(CollectionView collectionView, CollectionViewLayout layout) {
		super.init();

		_collectionView = collectionView;
		_layout = layout;
	}

	void validateLayoutInRect(mocha.graphics.Rect rect) {
		this.validateItemCounts();
		this.prepareToLoadData();

		// TODO: check if we need to fetch data from layout
		if (!_validLayoutRect.equals(rect)) {
		    _validLayoutRect = rect;
		    // we only want cell layoutAttributes & supplementaryView layoutAttributes
		    this.getCachedLayoutAttributes() = this.getLayout().layoutAttributesForElementsInRect(rect).filteredArrayUsingPredicate(mocha.foundation.Predicate.predicateWithBlock(^boolean(CollectionViewLayout.Attributes *evaluatedObject, HashMap *bindings) {
		        return (evaluatedObject.isKindOfClass(CollectionViewLayout.Attributes.getClass()) &&
		                (evaluatedObject.isCell() ||
		                        evaluatedObject.isSupplementaryView() ||
		                        evaluatedObject.isDecorationView()));
		    }));
		}
	}

	mocha.graphics.Rect rectForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		return mocha.graphics.Rect.zero();
	}

	int globalIndexForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		int offset = this.numberOfItemsBeforeSection(indexPath.section) + indexPath.item;
		return (int)offset;
	}

	mocha.foundation.IndexPath indexPathForItemAtGlobalIndex(int index) {
		this.validateItemCounts();

		mocha.foundation.Assert(index < _numItems, "request for index path for global index %ld when there are only %ld items in the collection view", (long)index, (long)_numItems);

		int section = 0;
		int countItems = 0;
		for (section = 0; section < _numSections; section++) {
		    int countIncludingThisSection = countItems + _sectionItemCounts.get(section);
		    if (countIncludingThisSection > index) break;
		    countItems = countIncludingThisSection;
		}

		int item = index - countItems;

		return mocha.foundation.IndexPath.indexPathForItemInSection(item, section);
	}

	ArrayList layoutAttributesForElementsInRect(mocha.graphics.Rect rect) {
		this.validateLayoutInRect(rect);
		return this.getCachedLayoutAttributes();
	}

	void invalidate() {
		_collectionViewDataFlags.itemCountsAreValid = false;
		_collectionViewDataFlags.layoutIsPrepared = false;
		_validLayoutRect = mocha.graphics.Rect.nll();  // don't set mocha.graphics.Rect.zero() in case of _contentSize=mocha.graphics.Size.zero()
	}

	int numberOfItemsBeforeSection(int section) {
		this.validateItemCounts();

		mocha.foundation.Assert(section < _numSections, "request for number of items in section %ld when there are only %ld sections in the collection view", (long)section, (long)_numSections);

		int returnCount = 0;
		for (int i = 0; i < section; i++) {
		    returnCount += _sectionItemCounts.get(i);
		}

		return returnCount;
	}

	int numberOfItemsInSection(int section) {
		this.validateItemCounts();
		if (section >= _numSections || section < 0) {
		    // In case of inconsistency returns the 'less harmful' amount of items. Throwing an exception here potentially
		    // causes exceptions when data is consistent. Deleting sections is one of the parts sensitive to this.
		    // All checks via assertions are done on CollectionView animation methods, specially 'endAnimations'.
		    return 0;
		    //@throw Exception.exceptionWithNameReasonUserInfo(mocha.foundation.InvalidArgumentException, String.format("Section %d out of range: 0...%d", section, _numSections), null);
		}

		int numberOfItemsInSection = 0;
		if (_sectionItemCounts) {
		    numberOfItemsInSection = _sectionItemCounts.get(section);
		}
		return numberOfItemsInSection;
	}

	int numberOfItems() {
		this.validateItemCounts();
		return _numItems;
	}

	int numberOfSections() {
		this.validateItemCounts();
		return _numSections;
	}

	mocha.graphics.Rect collectionViewContentRect() {
		return new mocha.graphics.Rect(mocha.graphics.Point.zero(), _contentSize);
	}

	void prepareToLoadData() {
		if (!this.getLayoutIsPrepared()) {
		    this.getLayout().prepareLayout();
		    _contentSize = this.getLayout().getCollectionViewContentSize();
		    this.setLayoutIsPrepared(true);
		}
	}

	protected void finalize() throws java.lang.Throwable {
		free(_sectionItemCounts);
		super.finalize();
	}

	String description() {
		return String.format("<%s: %p numItems:%ld numSections:%ld>", StringFromClass(this.getClass()), this, (long)this.getNumberOfItems(), (long)this.getNumberOfSections());
	}

	boolean layoutIsPrepared() {
		return _collectionViewDataFlags.layoutIsPrepared;
	}

	void setLayoutIsPrepared(boolean layoutIsPrepared) {
		_collectionViewDataFlags.layoutIsPrepared = layoutIsPrepared;
	}

	void validateItemCounts() {
		if (!_collectionViewDataFlags.itemCountsAreValid) {
		    this.updateItemCounts();
		}
	}

	void updateItemCounts() {
		// query how many sections there will be
		_numSections = 1;
		if (this.getCollectionView().getDataSource().respondsToSelector("numberOfSectionsInCollectionView")) {
		    _numSections = this.getCollectionView().getDataSource().numberOfSectionsInCollectionView(this.getCollectionView());
		}
		if (_numSections <= 0) { // early bail-out
		    _numItems = 0;
		    free(_sectionItemCounts);
		    _sectionItemCounts = 0;
		    _collectionViewDataFlags.itemCountsAreValid = true;
		    return;
		}
		// allocate space
		if (!_sectionItemCounts) {
		    _sectionItemCounts = malloc(_numSections * sizeof(int));
		}else {
		    _sectionItemCounts = realloc(_sectionItemCounts, _numSections * sizeof(int));
		}

		// query cells per section
		_numItems = 0;
		for (int i = 0; i < _numSections; i++) {
		    int cellCount = this.getCollectionView().getDataSource().collectionViewNumberOfItemsInSection(this.getCollectionView(), i);
		    new _sectionItemCounts(i, cellCount);
		    _numItems += cellCount;
		}

		_collectionViewDataFlags.itemCountsAreValid = true;
	}

	/* Setters & Getters */
	/* ========================================== */

	private CollectionView getCollectionView() {
		return this.collectionView;
	}

	private void setCollectionView(CollectionView collectionView) {
		this.collectionView = collectionView;
	}

	private CollectionViewLayout getLayout() {
		return this.layout;
	}

	private void setLayout(CollectionViewLayout layout) {
		this.layout = layout;
	}

	private ArrayList getCachedLayoutAttributes() {
		return this.cachedLayoutAttributes;
	}

	private void setCachedLayoutAttributes(ArrayList cachedLayoutAttributes) {
		this.cachedLayoutAttributes = cachedLayoutAttributes;
	}

}

