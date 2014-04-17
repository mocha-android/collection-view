package mocha.ui.collectionview;

import mocha.foundation.*;
import mocha.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

class CollectionViewData extends MObject {
	private mocha.graphics.Rect _validLayoutRect;
	private int _numItems;
	private int _numSections;
	private int[] _sectionItemCounts;
	private mocha.graphics.Size _contentSize;
	private CollectionView _collectionView;
	private CollectionViewLayout _layout;
	private List<CollectionViewLayout.Attributes> _cachedLayoutAttributes;
	private boolean contentSizeIsValid;
	private boolean itemCountsAreValid;
	private boolean layoutIsPrepared;


	public CollectionViewData(CollectionView collectionView, CollectionViewLayout layout) {
		_collectionView = collectionView;
		_layout = layout;
	}

	void validateLayoutInRect(mocha.graphics.Rect rect) {
		this.validateItemCounts();
		this.prepareToLoadData();

		// TODO: check if we need to fetch data from layout
		if (_validLayoutRect == null || !_validLayoutRect.equals(rect)) {
		    _validLayoutRect = rect.copy();
		    // we only want cell layoutAttributes & supplementaryView layoutAttributes
			this._cachedLayoutAttributes = Lists.filteredList(this.getLayout().layoutAttributesForElementsInRect(rect), new Lists.Filter<CollectionViewLayout.Attributes>() {
				public boolean filter(CollectionViewLayout.Attributes item) {
					return item != null && (item.isCell() || item.isSupplementaryView() || item.isDecorationView());
				}
			});
		}
	}

	mocha.graphics.Rect rectForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		return mocha.graphics.Rect.zero();
	}

	int globalIndexForItemAtIndexPath(mocha.foundation.IndexPath indexPath) {
		return this.globalIndexForItemInSection(indexPath.item, indexPath.section);
	}

	int globalIndexForItemInSection(int item, int section) {
		return this.numberOfItemsBeforeSection(section) + item;
	}

	mocha.foundation.IndexPath indexPathForItemAtGlobalIndex(int index) {
		this.validateItemCounts();

		Assert.condition(index < _numItems, String.format("request for index path for global index %d when there are only %d items in the collection view", index, _numItems));

		int section = 0;
		int countItems = 0;
		for (section = 0; section < _numSections; section++) {
		    int countIncludingThisSection = countItems + _sectionItemCounts[section];
		    if (countIncludingThisSection > index) break;
		    countItems = countIncludingThisSection;
		}

		int item = index - countItems;

		return mocha.foundation.IndexPath.withItemInSection(item, section);
	}

	List<CollectionViewLayout.Attributes> layoutAttributesForElementsInRect(Rect rect) {
		this.validateLayoutInRect(rect);
		return this.getCachedLayoutAttributes();
	}

	void invalidate() {
		this.itemCountsAreValid = false;
		this.layoutIsPrepared = false;
		this._validLayoutRect = null;  // don't set mocha.graphics.Rect.zero() in case of _contentSize=mocha.graphics.Size.zero()
	}

	int numberOfItemsBeforeSection(int section) {
		this.validateItemCounts();

		Assert.condition(section < _numSections, String.format("request for number of items in section %d when there are only %d sections in the collection view", section, _numSections));

		int returnCount = 0;
		for (int i = 0; i < section; i++) {
		    returnCount += _sectionItemCounts[i];
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

		if (_sectionItemCounts != null) {
		    numberOfItemsInSection = _sectionItemCounts[section];
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
		MLog("CV_TEST - PREPARE TO LOAD: " + this.layoutIsPrepared);
		if (!this.layoutIsPrepared) {
		    this.getLayout().prepareLayout();
		    _contentSize = this.getLayout().collectionViewContentSize().copy();
			this.layoutIsPrepared = true;
		}
	}

	protected String toStringExtra() {
		return String.format("numItems:%d numSections:%d", this.numberOfItems(), this.numberOfSections());
	}

	void validateItemCounts() {
		if (!this.itemCountsAreValid) {
		    this.updateItemCounts();
		}
	}

	void updateItemCounts() {
		// query how many sections there will be
		_numSections = 1;

		if (OptionalInterfaceHelper.hasImplemented(this.getCollectionView().getDataSource(), CollectionView.DataSource.class, "numberOfSectionsInCollectionView", CollectionView.class)) {
		    _numSections = this.getCollectionView().getDataSource().numberOfSectionsInCollectionView(this.getCollectionView());
		}

		if (_numSections <= 0) { // early bail-out
		    _numItems = 0;
		    _sectionItemCounts = null;
		    this.itemCountsAreValid = true;
		    return;
		}

		_sectionItemCounts = new int[_numSections];

		// query cells per section
		_numItems = 0;
		for (int section = 0; section < _numSections; section++) {
		    int cellCount = this.getCollectionView().getDataSource().collectionViewNumberOfItemsInSection(this.getCollectionView(), section);
		    _sectionItemCounts[section] = cellCount;
		    _numItems += cellCount;
		}

		this.itemCountsAreValid = true;
	}

	/* Setters & Getters */
	/* ========================================== */

	private CollectionView getCollectionView() {
		return this._collectionView;
	}

	private void setCollectionView(CollectionView collectionView) {
		this._collectionView = collectionView;
	}

	private CollectionViewLayout getLayout() {
		return this._layout;
	}

	private void setLayout(CollectionViewLayout layout) {
		this._layout = layout;
	}

	private List<CollectionViewLayout.Attributes> getCachedLayoutAttributes() {
		return this._cachedLayoutAttributes;
	}

	private void setCachedLayoutAttributes(List<CollectionViewLayout.Attributes> cachedLayoutAttributes) {
		if(cachedLayoutAttributes == null) {
			this._cachedLayoutAttributes = new ArrayList<>();
		} else {
			this._cachedLayoutAttributes.clear();
		}

		if(cachedLayoutAttributes != null) {
			this._cachedLayoutAttributes.addAll(cachedLayoutAttributes);
		}
	}

}

