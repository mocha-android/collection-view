class CollectionViewController extends mocha.ui.ViewController implements CollectionView.Delegate, CollectionView.DataSource {
	private CollectionView _collectionView;
	private boolean _clearsSelectionOnViewWillAppear;
	private CollectionViewLayout _layout;
	private Character filler;
	private CollectionViewControllerFlagsStruct _collectionViewControllerFlags = new CollectionViewControllerFlagsStruct();

	private class CollectionViewControllerFlagsStruct {
		boolean clearsSelectionOnViewWillAppear;
		boolean appearsFirstTime;

	}

	public CollectionViewController(CollectionViewLayout layout) {
		super.init();

		this.setLayout(layout);
		this.setClearsSelectionOnViewWillAppear(true);
		_collectionViewControllerFlags.appearsFirstTime = true;
	}

	public CollectionViewController(mocha.foundation.Coder coder) {
		super.initWithCoder(coder);

		this.setLayout(new CollectionViewFlowLayout());
		this.setClearsSelectionOnViewWillAppear(true);
		_collectionViewControllerFlags.appearsFirstTime = true;
	}

	void loadView() {
		super.loadView();

		// if this is restored from IB, we don't have plain main view.
		if (this.getView().isKindOfClass(CollectionView.getClass())) {
		    _collectionView = (CollectionView)this.getView();
		    this.setView(new mocha.ui.View(this.getView().getBounds()));
		    this.getCollectionView().setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		}

		if (_collectionView.getDelegate() == null) _collectionView.setDelegate(this);
		if (_collectionView.getDataSource() == null) _collectionView.setDataSource(this);

		// only create the collection view if it is not already created (by IB)
		if (!_collectionView) {
		    this.setCollectionView(new CollectionView(this.getView().getBounds(), this.getLayout()));
		    this.getCollectionView().setDelegate(this);
		    this.getCollectionView().setDataSource(this);
		}
	}

	void viewDidLoad() {
		super.viewDidLoad();

		// This seems like a hack, but is needed for real compatibility
		// There can be implementations of loadView that don't call super and don't set the view, yet it works in mocha.ui.CollectionViewController.
		if (!this.getIsViewLoaded()) {
		    this.setView(new mocha.ui.View(mocha.graphics.Rect.zero()));
		}

		// Attach the view
		if (this.getView() != this.getCollectionView()) {
		    this.getView().addSubview(this.getCollectionView());
		    this.getCollectionView().setFrame(this.getView().getBounds());
		    this.getCollectionView().setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		}
	}

	void viewWillAppear(boolean animated) {
		super.viewWillAppear(animated);

		if (_collectionViewControllerFlags.appearsFirstTime) {
		    _collectionView.reloadData();
		    _collectionViewControllerFlags.appearsFirstTime = false;
		}

		if (_collectionViewControllerFlags.clearsSelectionOnViewWillAppear) {
		    for (mocha.foundation.IndexPath aIndexPath in _collectionView. : dexPathsForSelectedItems().copy()) {
		        _collectionView.deselectItemAtIndexPathAnimated(aIndexPath, animated);
		    }
		}
	}

	CollectionView collectionView() {
		if (!_collectionView) {
		    _collectionView = new CollectionView(mocha.ui.Screen.getMainScreen().getBounds(), this.getLayout());
		    _collectionView.setDelegate(this);
		    _collectionView.setDataSource(this);

		    // If the collection view isn't the main view, add it.
		    if (this.getIsViewLoaded() && this.getView() != this.getCollectionView()) {
		        this.getView().addSubview(this.getCollectionView());
		        this.getCollectionView().setFrame(this.getView().getBounds());
		        this.getCollectionView().setAutoresizingMask(View.Autoresizing.FLEXIBLE_HEIGHT_MARGIN, View.Autoresizing.FLEXIBLE_WIDTH);
		    }
		}
		return _collectionView;
	}

	void setClearsSelectionOnViewWillAppear(boolean clearsSelectionOnViewWillAppear) {
		_collectionViewControllerFlags.clearsSelectionOnViewWillAppear = clearsSelectionOnViewWillAppear;
	}

	boolean clearsSelectionOnViewWillAppear() {
		return _collectionViewControllerFlags.clearsSelectionOnViewWillAppear;
	}

	int collectionViewNumberOfItemsInSection(CollectionView collectionView, int section) {
		return 0;
	}

	CollectionViewCell collectionViewCellForItemAtIndexPath(CollectionView collectionView, mocha.foundation.IndexPath indexPath) {
		this.doesNotRecognizeSelector(_cmd);
		return null;
	}

	/* Setters & Getters */
	/* ========================================== */

	public CollectionView getCollectionView() {
		return this.collectionView;
	}

	public void setCollectionView(CollectionView collectionView) {
		this.collectionView = collectionView;
	}

	public boolean getClearsSelectionOnViewWillAppear() {
		return this.clearsSelectionOnViewWillAppear;
	}

	public void setClearsSelectionOnViewWillAppear(boolean clearsSelectionOnViewWillAppear) {
		this.clearsSelectionOnViewWillAppear = clearsSelectionOnViewWillAppear;
	}

	private CollectionViewLayout getLayout() {
		return this.layout;
	}

	private void setLayout(CollectionViewLayout layout) {
		this.layout = layout;
	}

}

