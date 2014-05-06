package mocha.ui.collectionview;

import mocha.foundation.Assert;
import mocha.foundation.IndexPath;
import mocha.graphics.Size;
import mocha.ui.EdgeInsets;
import mocha.ui.View;

import java.util.List;

abstract public class CollectionViewController extends mocha.ui.ViewController implements CollectionView.Delegate, CollectionView.DataSource, CollectionViewFlowLayout.CollectionViewDelegateFlowLayout, FastFlowLayout.Delegate {
	private CollectionView collectionView;
	private boolean clearsSelectionOnViewWillAppear;
	private boolean appearsFirstTime;
	private CollectionViewLayout collectionViewLayout;

	public CollectionViewController(CollectionViewLayout layout) {
		Assert.condition(layout != null, "CollectionViewController must be created with a valid CollectionViewLayout.");

		this.collectionViewLayout = layout;
		this.clearsSelectionOnViewWillAppear = true;
		this.appearsFirstTime = true;
	}

	protected void loadView() {
		super.loadView();

		this.collectionView = new CollectionView(this.getView().getBounds(), this.collectionViewLayout);
		this.collectionView.setDelegate(this);
		this.collectionView.setDataSource(this);
	}

	protected void viewDidLoad() {
		super.viewDidLoad();

		View view = this.getView();

		view.addSubview(this.collectionView);
		this.collectionView.setFrame(view.getBounds());
		this.collectionView.setAutoresizing(View.Autoresizing.FLEXIBLE_SIZE);
	}

	public void viewWillAppear(boolean animated) {
		super.viewWillAppear(animated);

		if (this.appearsFirstTime) {
		    this.collectionView.reloadData();
		    this.appearsFirstTime = false;
		}

		if (this.clearsSelectionOnViewWillAppear) {
			List<IndexPath> indexPaths = this.collectionView.indexPathsForSelectedItems();

			if(indexPaths.size() > 0) {
				for(IndexPath indexPath : indexPaths) {
					this.collectionView.deselectItemAtIndexPathAnimated(indexPath, animated);
				}
			}
		}
	}

	public CollectionView getCollectionView() {
		return this.collectionView;
	}

	public void setClearsSelectionOnViewWillAppear(boolean clearsSelectionOnViewWillAppear) {
		this.clearsSelectionOnViewWillAppear = clearsSelectionOnViewWillAppear;
	}

	public boolean getClearsSelectionOnViewWillAppear() {
		return this.clearsSelectionOnViewWillAppear;
	}

	public CollectionViewLayout getCollectionViewLayout() {
		if(this.collectionView != null) {
			return this.collectionView.getCollectionViewLayout();
		} else {
			return this.collectionViewLayout;
		}
	}

	public int numberOfSectionsInCollectionView(CollectionView collectionView) {
		return 1;
	}

	@NotImplemented
	public CollectionReusableView collectionViewViewForSupplementaryElementOfKindAtIndexPath(CollectionView collectionView, String kind, IndexPath indexPath) {
		return null;
	}

	@NotImplemented
	public boolean collectionViewShouldHighlightItemAtIndexPath(CollectionView collectionView, IndexPath indexPath) {
		return false;
	}

	@NotImplemented
	public void collectionViewDidHighlightItemAtIndexPath(CollectionView collectionView, IndexPath indexPath) {

	}

	@NotImplemented
	public void collectionViewDidUnhighlightItemAtIndexPath(CollectionView collectionView, IndexPath indexPath) {

	}

	@NotImplemented
	public boolean collectionViewShouldSelectItemAtIndexPath(CollectionView collectionView, IndexPath indexPath) {
		return false;
	}

	@NotImplemented
	public boolean collectionViewShouldDeselectItemAtIndexPath(CollectionView collectionView, IndexPath indexPath) {
		return false;
	}

	@NotImplemented
	public void collectionViewDidSelectItemAtIndexPath(CollectionView collectionView, IndexPath indexPath) {

	}

	@NotImplemented
	public void collectionViewDidDeselectItemAtIndexPath(CollectionView collectionView, IndexPath indexPath) {

	}

	@NotImplemented
	public void collectionViewDidEndDisplayingCellForItemAtIndexPath(CollectionView collectionView, CollectionViewCell cell, IndexPath indexPath) {

	}

	@NotImplemented
	public void collectionViewDidEndDisplayingSupplementaryViewForElementOfKindAtIndexPath(CollectionView collectionView, CollectionReusableView view, String elementKind, IndexPath indexPath) {

	}

	@NotImplemented
	public Size collectionViewLayoutReferenceSizeForFooterInSection(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section) {
		return null;
	}

	@NotImplemented
	public Size collectionViewLayoutSizeForItemAtIndexPath(CollectionView collectionView, CollectionViewLayout collectionViewLayout, IndexPath indexPath) {
		return null;
	}

	@NotImplemented
	public EdgeInsets collectionViewLayoutInsetForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section) {
		return null;
	}

	@NotImplemented
	public float collectionViewLayoutMinimumLineSpacingForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section) {
		return 0;
	}

	@NotImplemented
	public float collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section) {
		return 0;
	}

	@NotImplemented
	public Size collectionViewLayoutReferenceSizeForHeaderInSection(CollectionView collectionView, CollectionViewLayout collectionViewLayout, int section) {
		return null;
	}

}

