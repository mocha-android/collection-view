package mocha.ui.collectionview;

import android.util.SparseArray;
import mocha.foundation.*;
import mocha.graphics.Point;
import mocha.graphics.Rect;
import mocha.graphics.Size;
import mocha.ui.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CollectionView extends ScrollView {
	private Delegate _delegate;
	private DataSource _dataSource;
	private View _backgroundView;
	private boolean _allowsSelection;
	private boolean _allowsMultipleSelection;
	private CollectionViewLayout _layout;
	private Set<IndexPath> _indexPathsForSelectedItems;
	private Map<String,List<CollectionViewCell>> _cellReuseQueues;
	private Map<String,List<CollectionReusableView>> _supplementaryViewReuseQueues;
	private Map<String,List<CollectionReusableView>> _decorationViewReuseQueues;
	private Set<IndexPath> _indexPathsForHighlightedItems;
	private int _reloadingSuspendedCount;
	private CollectionReusableView _firstResponderView;
	private mocha.ui.View _newContentView;
	private int _firstResponderViewType;
	private String _firstResponderViewKind;
	private IndexPath _firstResponderIndexPath;
	private Map<CollectionViewItemKey,CollectionReusableView> _allVisibleViewsDict;
	private mocha.foundation.IndexPath _pendingSelectionIndexPath;
	private Set<IndexPath> _pendingDeselectionIndexPaths;
	private CollectionViewData _collectionViewData;
	private UpdateTransaction _update;
	private Rect _visibleBoundRects;
	private Rect _preRotationBounds;
	private Point _rotationBoundsOffset;
	private int _rotationAnimationCount;
	private int _updateCount;
	private List<CollectionViewUpdateItem> _insertItems;
	private List<CollectionViewUpdateItem> _deleteItems;
	private List<CollectionViewUpdateItem> _reloadItems;
	private List<CollectionViewUpdateItem> _moveItems;
	private List<CollectionViewUpdateItem> _originalInsertItems;
	private List<CollectionViewUpdateItem> _originalDeleteItems;
	private mocha.ui.Touch _currentTouch;
	private boolean finished;
	private Map<String,Class<? extends CollectionViewCell>> _cellClassDict;
	private Map<String,Class<? extends CollectionReusableView>> _supplementaryViewClassDict;
	private mocha.graphics.Point _lastLayoutOffset;
	private CollectionViewFlagsStruct _collectionViewFlags = new CollectionViewFlagsStruct();
	private CollectionView.Delegate _collectionViewDelegate;
	private mocha.foundation.IndexPath _touchingIndexPath;
	private mocha.foundation.IndexPath _currentIndexPath;
	private FinishedBlock _updateCompletionHandler;
	private boolean hasLoadedData;

	public interface FinishedBlock {

		void execute(boolean finished);

	}

	private class CollectionViewFlagsStruct {
		boolean delegateShouldHighlightItemAtIndexPath;
		boolean delegateDidHighlightItemAtIndexPath;
		boolean delegateDidUnhighlightItemAtIndexPath;
		boolean delegateShouldSelectItemAtIndexPath;
		boolean delegateShouldDeselectItemAtIndexPath;
		boolean delegateDidSelectItemAtIndexPath;
		boolean delegateDidDeselectItemAtIndexPath;
		boolean delegateSupportsMenus;
		boolean delegateDidEndDisplayingCell;
		boolean delegateDidEndDisplayingSupplementaryView;
		boolean dataSourceNumberOfSections;
		boolean dataSourceViewForSupplementaryElement;
		boolean reloadSkippedDuringSuspension;
		boolean scheduledUpdateVisibleCells;
		boolean scheduledUpdateVisibleCellLayoutAttributes;
		boolean updating;
		boolean fadeCellsForBoundsChange;
		boolean updatingLayout;
		boolean needsReload;
		boolean reloading;
		boolean skipLayoutDuringSnapshotting;
		boolean layoutInvalidatedSinceLastCellUpdate;
		boolean doneFirstLayout;
	}

	class UpdateTransaction extends HashMap {
		CollectionViewData oldModel;
		CollectionViewData newModel;
		List<Integer> oldToNewIndexMap;
		List<Integer> newToOldIndexMap;
	}

	class UpdateAnimation {
		CollectionReusableView view;
		CollectionViewLayout.Attributes previousLayoutInfos;
		CollectionViewLayout.Attributes newLayoutInfos;

		private UpdateAnimation() {
		}

		private UpdateAnimation(CollectionReusableView view, CollectionViewLayout.Attributes previousLayoutInfos, CollectionViewLayout.Attributes newLayoutInfos) {
			this.view = view;
			this.previousLayoutInfos = previousLayoutInfos;
			this.newLayoutInfos = newLayoutInfos;
		}

		private UpdateAnimation(CollectionViewLayout.Attributes previousLayoutInfos, CollectionViewLayout.Attributes newLayoutInfos) {
			this.previousLayoutInfos = previousLayoutInfos;
			this.newLayoutInfos = newLayoutInfos;
		}
	}

	private static final int SCROLL_POSITION_NONE = 0;
	private static final int SCROLL_POSITION_TOP = 1 << 0;
	private static final int SCROLL_POSITION_CENTERED_VERTICALLY = 1 << 1;
	private static final int SCROLL_POSITION_BOTTOM = 1 << 2;
	private static final int SCROLL_POSITION_LEFT = 1 << 3;
	private static final int SCROLL_POSITION_CENTERED_HORIZONTALLY = 1 << 4;
	private static final int SCROLL_POSITION_RIGHT = 1 << 5;

	public enum ScrollPosition {
		NONE(SCROLL_POSITION_NONE),

		// The vertical positions are mutually exclusive to each other, but are bitwise or-able with the horizontal scroll positions.
		// Combining positions from the same grouping (horizontal or vertical) will result in an InvalidArgumentException.
		TOP(SCROLL_POSITION_TOP),
		CENTERED_VERTICALLY(SCROLL_POSITION_CENTERED_VERTICALLY),
		BOTTOM(SCROLL_POSITION_BOTTOM),

		// Likewise, the horizontal positions are mutually exclusive to each other.
		LEFT(SCROLL_POSITION_LEFT),
		CENTERED_HORIZONTALLY(SCROLL_POSITION_CENTERED_HORIZONTALLY),
		RIGHT(SCROLL_POSITION_RIGHT);

		private int value;

		private ScrollPosition(int value) {
			this.value = value;
		}

		static int mask(ScrollPosition... scrollPositions) {
			int mask = 0;

			for(ScrollPosition scrollPosition : scrollPositions) {
				mask &= scrollPosition.value;
			}

			return mask;
		}
	}

	public interface DataSource extends OptionalInterface {

		int collectionViewNumberOfItemsInSection(CollectionView collectionView, int section);

		CollectionViewCell collectionViewCellForItemAtIndexPath(CollectionView collectionView, IndexPath indexPath);

		@Optional
		int numberOfSectionsInCollectionView(CollectionView collectionView);

		@Optional
		CollectionReusableView collectionViewViewForSupplementaryElementOfKindAtIndexPath(CollectionView collectionView, String kind, IndexPath indexPath);

	}

	public interface Delegate extends OptionalInterface {

		@Optional
		boolean collectionViewShouldHighlightItemAtIndexPath(CollectionView collectionView, IndexPath indexPath);

		@Optional
		void collectionViewDidHighlightItemAtIndexPath(CollectionView collectionView, IndexPath indexPath);

		@Optional
		void collectionViewDidUnhighlightItemAtIndexPath(CollectionView collectionView, IndexPath indexPath);

		@Optional
		boolean collectionViewShouldSelectItemAtIndexPath(CollectionView collectionView, IndexPath indexPath);

		@Optional
		boolean collectionViewShouldDeselectItemAtIndexPath(CollectionView collectionView, IndexPath indexPath);

		@Optional
		void collectionViewDidSelectItemAtIndexPath(CollectionView collectionView, IndexPath indexPath);

		@Optional
		void collectionViewDidDeselectItemAtIndexPath(CollectionView collectionView, IndexPath indexPath);

		@Optional
		void collectionViewDidEndDisplayingCellForItemAtIndexPath(CollectionView collectionView, CollectionViewCell cell, IndexPath indexPath);

		@Optional
		void collectionViewDidEndDisplayingSupplementaryViewForElementOfKindAtIndexPath(CollectionView collectionView, CollectionReusableView view, String elementKind, IndexPath indexPath);

	}

	public enum CollectionElementCategory {
		CELL,
		SUPPLEMENTARY_VIEW,
		DECORATION_VIEW
	}

	public CollectionView(mocha.graphics.Rect frame, CollectionViewLayout layout) {
		super(frame);

		this._allowsSelection = true;
		this._indexPathsForSelectedItems = new HashSet<>();
		this._indexPathsForHighlightedItems = new HashSet<>();
		this._cellReuseQueues = new HashMap<>();
		this._supplementaryViewReuseQueues = new HashMap<>();
		this._decorationViewReuseQueues = new HashMap<>();
		this._allVisibleViewsDict = new HashMap<>();
		this._cellClassDict = new HashMap<>();
		this._supplementaryViewClassDict = new HashMap<>();

		this.setCollectionViewLayout(layout);
		this._collectionViewData = new CollectionViewData(this, layout);
	}

	public void registerClassForCellWithReuseIdentifier(Class<? extends CollectionViewCell> cellClass, String identifier) {
		Assert.condition(cellClass != null, "Cell class must not be null");
		Assert.condition(identifier != null, "Identifier must not be null");
		MWarn("CV_TEST Registering %s to %s on %s", identifier, cellClass, this);
		this._cellClassDict.put(identifier, cellClass);
	}

	public void registerClassForSupplementaryViewOfKindWithReuseIdentifier(Class<? extends CollectionReusableView> viewClass, String elementKind, String identifier) {
		Assert.condition(viewClass != null, "View class must not be null");
		Assert.condition(elementKind != null, "Element kind must not be null");
		Assert.condition(identifier != null, "Identifier must not be null");
		String kindAndIdentifier = String.format("%s/%s", elementKind, identifier);
		this._supplementaryViewClassDict.put(kindAndIdentifier, viewClass);
	}

	public CollectionViewCell dequeueReusableCellWithReuseIdentifierForIndexPath(String identifier, IndexPath indexPath) {
		// de-queue cell (if available)
		List<CollectionViewCell> reusableCells = this._cellReuseQueues.get(identifier);
		CollectionViewCell cell = Lists.last(reusableCells);
		CollectionViewLayout.Attributes attributes = this._layout.layoutAttributesForItemAtIndexPath(indexPath);

		if (cell != null) {
		    reusableCells.remove(reusableCells.size() - 1);
		} else {
			Class<? extends CollectionViewCell> cellClass = this._cellClassDict.get(identifier);

			if (cellClass == null) {
				MWarn("CV_TEST Failed to dequeue %s on %s", identifier, this);
				throw new IllegalArgumentException(String.format("Class not registered for identifier %s %s", identifier, this._cellClassDict));
			}

			try {
				if (attributes != null) {
					cell = cellClass.getConstructor(Rect.class).newInstance(attributes.getFrame());
				} else {
					cell = cellClass.newInstance();
				}
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}

			cell.setCollectionView(this);
		    cell.setReuseIdentifier(identifier);
		}

		cell.applyLayoutAttributes(attributes);

		return cell;
	}

	public CollectionReusableView dequeueReusableSupplementaryViewOfKindWithReuseIdentifierForIndexPath(String elementKind, String identifier, IndexPath indexPath) {
		String kindAndIdentifier = String.format("%s/%s", elementKind, identifier);
		List<CollectionReusableView> reusableViews = _supplementaryViewReuseQueues.get(kindAndIdentifier);
		CollectionReusableView view = Lists.last(reusableViews);

		if (view != null) {
		    reusableViews.remove(reusableViews.size() - 1);
		} else {
			Class<? extends CollectionReusableView> viewClass = _supplementaryViewClassDict.get(kindAndIdentifier);

			if (viewClass == null) {
				throw new IllegalArgumentException(String.format("Class not registered for kind/identifier %s", kindAndIdentifier));
			}

			try {
				if (this._layout != null) {
					CollectionViewLayout.Attributes attributes = this._layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(elementKind, indexPath);
					if (attributes != null) {
						view = viewClass.getConstructor(Rect.class).newInstance(attributes.getFrame());
					}
				} else {
					view = viewClass.newInstance();
				}
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}

			if (view != null) {
				view.setCollectionView(this);
				view.setReuseIdentifier(identifier);
			}
		}

		return view;
	}

	public List<IndexPath> indexPathsForSelectedItems() {
		return Lists.copy(this._indexPathsForSelectedItems);
	}

	public void selectItemAtIndexPathAnimatedScrollPosition(IndexPath indexPath, boolean animated, ScrollPosition... scrollPosition) {
		this.selectItemAtIndexPathAnimatedScrollPositionNotifyDelegate(indexPath, animated, ScrollPosition.mask(scrollPosition), false);
	}

	public void deselectItemAtIndexPathAnimated(IndexPath indexPath, boolean animated) {
		this.deselectItemAtIndexPathAnimatedNotifyDelegate(indexPath, animated, false);
	}

	void reloadData() {
		if (_reloadingSuspendedCount != 0) return;
		this.hasLoadedData = true;
		this.invalidateLayout();

		for(CollectionReusableView view : this._allVisibleViewsDict.values()) {
			if(view != null) {
				view.removeFromSuperview();
			}
		}

		this._allVisibleViewsDict.clear();

		for (IndexPath indexPath : this._indexPathsForSelectedItems) {
		    CollectionViewCell selectedCell = this.cellForItemAtIndexPath(indexPath);
		    selectedCell.setSelected(false);
		    selectedCell.setHighlighted(false);
		}

		this._indexPathsForSelectedItems.clear();
		this._indexPathsForHighlightedItems.clear();

		this.setNeedsLayout();
	}

	public void setCollectionViewLayout(CollectionViewLayout collectionViewLayout) {
		this.setCollectionViewLayout(collectionViewLayout, false);
	}

	public void setCollectionViewLayout(final CollectionViewLayout layout, boolean animated) {
		if(layout == null) {
			throw new IllegalArgumentException("CollectionViewLayout must not be null");
		}

		if (layout == this._layout) return;

		// not sure it was it original code, but here this prevents crash
		// in case we switch layout before previous one was initially loaded
		if (this.getBounds().empty() || !this._collectionViewFlags.doneFirstLayout) {
			if(this._layout != null) {
				this._layout.setCollectionView(null);
			}

		    this._collectionViewData = new CollectionViewData(this, layout);
		    layout.setCollectionView(this);
		    this._layout = layout;

		    // originally the use method
		    // _setNeedsVisibleCellsUpdate:withLayoutAttributes:
		    // here with CellsUpdate set to true and LayoutAttributes parameter set to NO
		    // inside this method probably some flags are set and finally
		    // setNeedsDisplay is called

		    this._collectionViewFlags.scheduledUpdateVisibleCells = true;
			this._collectionViewFlags.scheduledUpdateVisibleCellLayoutAttributes = false;

		    this.setNeedsDisplay();
		} else {
		    layout.setCollectionView(this);

			this._layout.setCollectionView(null);
			this._layout = layout;

			this._collectionViewData = new CollectionViewData(this, layout);
			this._collectionViewData.prepareToLoadData();

		    List<IndexPath> previouslySelectedIndexPaths = this.indexPathsForSelectedItems();
		    Set<CollectionViewItemKey> selectedCellKeys = new HashSet<>(previouslySelectedIndexPaths.size());

		    for (IndexPath indexPath : previouslySelectedIndexPaths) {
		        selectedCellKeys.add(CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPath));
		    }

		    Set<CollectionViewItemKey> previouslyVisibleItemsKeys = _allVisibleViewsDict.keySet();
			Set<CollectionViewItemKey> previouslyVisibleItemsKeysSet = new HashSet<>(previouslyVisibleItemsKeys);
			Set<CollectionViewItemKey> previouslyVisibleItemsKeysSetMutable = new HashSet<>(previouslyVisibleItemsKeys);

			// This was ported as is from PSTCollectionView, but it looks like it does absolutely nothing.
			// Need to investigate if this is just a bug in PSTCollectionView and shouldn't exist, or if
			// the wrong arguments are being used.
			if (Sets.intersects(selectedCellKeys, selectedCellKeys)) {
				previouslyVisibleItemsKeysSetMutable = Sets.intersectedSet(previouslyVisibleItemsKeysSetMutable, previouslyVisibleItemsKeysSetMutable);
		    }

		    this.bringSubviewToFront(this._allVisibleViewsDict.get(Sets.any(previouslyVisibleItemsKeysSetMutable)));

			Rect bounds = this.getBounds();
		    final Point targetOffset = this.getContentOffset();
		    Point centerPoint = new Point(bounds.midX(), bounds.midY());
		    IndexPath centerItemIndexPath = this.indexPathForItemAtPoint(centerPoint);

		    if (centerItemIndexPath == null) {
		        List<IndexPath> visibleItems = this.indexPathsForVisibleItems();
		        if (visibleItems.size() > 0) {
		            centerItemIndexPath = visibleItems.get(visibleItems.size() / 2);
		        }
		    }

		    if (centerItemIndexPath != null) {
		        CollectionViewLayout.Attributes layoutAttributes = layout.layoutAttributesForItemAtIndexPath(centerItemIndexPath);

		        if (layoutAttributes != null) {
		            mocha.graphics.Rect targetRect = this.makeRectToScrollPosition(layoutAttributes.getFrame(), SCROLL_POSITION_CENTERED_VERTICALLY | SCROLL_POSITION_CENTERED_HORIZONTALLY);
		            targetOffset.x = Math.max(0.0f, targetRect.origin.x);
					targetOffset.y = Math.max(0.0f, targetRect.origin.y);
		        }
		    }

			Rect oldBounds = this.getBounds();
		    mocha.graphics.Rect newlyBounds = new mocha.graphics.Rect(targetOffset.x, targetOffset.y, oldBounds.size.width, oldBounds.height());
		    List<CollectionViewLayout.Attributes> newlyVisibleLayoutAttrs = _collectionViewData.layoutAttributesForElementsInRect(newlyBounds);

		    final Map<CollectionViewItemKey,UpdateAnimation> layoutInterchangeData = new HashMap<>(newlyVisibleLayoutAttrs.size() + previouslyVisibleItemsKeysSet.size());

		    final Set<CollectionViewItemKey> newlyVisibleItemsKeys = new HashSet<>();

		    for (CollectionViewLayout.Attributes attr : newlyVisibleLayoutAttrs) {
		        CollectionViewItemKey newKey = CollectionViewItemKey.collectionItemKeyForLayoutAttributes(attr);
		        newlyVisibleItemsKeys.add(newKey);

		        CollectionViewLayout.Attributes prevAttr = null;
		        CollectionViewLayout.Attributes newAttr = null;

		        if (newKey.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            prevAttr = this._layout.layoutAttributesForDecorationViewOfKindAtIndexPath(attr.getRepresentedElementKind(), newKey.getIndexPath());
		            newAttr = layout.layoutAttributesForDecorationViewOfKindAtIndexPath(attr.getRepresentedElementKind(), newKey.getIndexPath());
		        } else if (newKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            prevAttr = this._layout.layoutAttributesForItemAtIndexPath(newKey.getIndexPath());
		            newAttr = layout.layoutAttributesForItemAtIndexPath(newKey.getIndexPath());
		        } else {
		            prevAttr = this._layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(attr.getRepresentedElementKind(), newKey.getIndexPath());
		            newAttr = layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(attr.getRepresentedElementKind(), newKey.getIndexPath());
		        }

		        if (prevAttr != null && newAttr != null) {
		            layoutInterchangeData.put(newKey, new UpdateAnimation(prevAttr, newAttr));
		        }
		    }

		    for (CollectionViewItemKey key : previouslyVisibleItemsKeysSet) {
		        CollectionViewLayout.Attributes prevAttr = null;
		        CollectionViewLayout.Attributes newAttr = null;

		        if (key.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            CollectionReusableView decorView = _allVisibleViewsDict.get(key);
		            prevAttr = this._layout.layoutAttributesForDecorationViewOfKindAtIndexPath(decorView.getReuseIdentifier(), key.getIndexPath());
		            newAttr = layout.layoutAttributesForDecorationViewOfKindAtIndexPath(decorView.getReuseIdentifier(), key.getIndexPath());
		        }

		        else if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            prevAttr = this._layout.layoutAttributesForItemAtIndexPath(key.getIndexPath());
		            newAttr = layout.layoutAttributesForItemAtIndexPath(key.getIndexPath());
		        }

		        else if (key.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		            CollectionReusableView suuplView = _allVisibleViewsDict.get(key);
		            prevAttr = this._layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(suuplView.getLayoutAttributes().getRepresentedElementKind(), key.getIndexPath());
		            newAttr = layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(suuplView.getLayoutAttributes().getRepresentedElementKind(), key.getIndexPath());
		        }

		        layoutInterchangeData.put(key, new UpdateAnimation(prevAttr, newAttr));
		    }

		    for (CollectionViewItemKey key  : layoutInterchangeData.keySet()) {
		        if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            CollectionViewCell cell = (CollectionViewCell)_allVisibleViewsDict.get(key);

		            if (cell == null) {
		                cell = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(key.getIndexPath(), layoutInterchangeData.get(key).previousLayoutInfos);
		                _allVisibleViewsDict.put(key, cell);
		                this.addControlledSubview(cell);
		            } else {
						cell.applyLayoutAttributes(layoutInterchangeData.get(key).previousLayoutInfos);
					}
		        }

		        else if (key.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		            CollectionReusableView view = _allVisibleViewsDict.get(key);

		            if (view == null) {
		                CollectionViewLayout.Attributes attrs = layoutInterchangeData.get(key).previousLayoutInfos;
		                view = this.createPreparedSupplementaryViewForElementOfKindAtIndexPathWithLayoutAttributes(attrs.getRepresentedElementKind(), attrs.getIndexPath(), attrs);
		                _allVisibleViewsDict.put(key, view);
		                this.addControlledSubview(view);
		            }
		        }

		        else if (key.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            CollectionReusableView view = this._allVisibleViewsDict.get(key);

		            if (view == null) {
		                CollectionViewLayout.Attributes attrs = layoutInterchangeData.get(key).previousLayoutInfos;
		                view = this.dequeueReusableOrCreateDecorationViewOfKindForIndexPath(attrs.getRepresentedElementKind(), attrs.getIndexPath());
		                this._allVisibleViewsDict.put(key, view);
		                this.addControlledSubview(view);
		            }
		        }
		    }

		    final Rect contentRect = this._collectionViewData.collectionViewContentRect();

			final Runnable applyNewLayoutBlock = new Runnable() {
				public void run() {
					for (CollectionViewItemKey key  : layoutInterchangeData.keySet()) {
						// TODO: This is most likely not 100% the same time as in UICollectionView. Needs to be investigated.
						CollectionViewCell cell = (CollectionViewCell)_allVisibleViewsDict.get(key);
						cell.willTransitionFromLayoutToLayout(_layout, layout);
						cell.applyLayoutAttributes(layoutInterchangeData.get(key).newLayoutInfos);
						cell.didTransitionFromLayoutToLayout(_layout, layout);
					}
				}
			};

			final Runnable freeUnusedViews = new Runnable() {
				public void run() {
					Set<CollectionViewItemKey> toRemove = new HashSet<>();

					for (CollectionViewItemKey key : _allVisibleViewsDict.keySet()) {
						if (!newlyVisibleItemsKeys.contains(key)) {
							if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
								reuseCell((CollectionViewCell)_allVisibleViewsDict.get(key));
								toRemove.add(key);
							} else if (key.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
								reuseSupplementaryView(_allVisibleViewsDict.get(key));
								toRemove.add(key);
							} else if (key.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
								reuseDecorationView(_allVisibleViewsDict.get(key));
								toRemove.add(key);
							}
						}
					}

					for (CollectionViewItemKey key : toRemove) {
						_allVisibleViewsDict.remove(key);
					}
				}
			};

		    if (animated) {
				View.animateWithDuration(300, new Animations() {
					public void performAnimatedChanges() {
						_collectionViewFlags.updatingLayout = true;
						setContentOffset(targetOffset);
						setContentSize(contentRect.size);
						applyNewLayoutBlock.run();
					}
				}, new AnimationCompletion() {
					public void animationCompletion(boolean finished) {
						freeUnusedViews.run();
						_collectionViewFlags.updatingLayout = false;

						// layout subviews for updating content offset or size while updating layout
						if (!getContentOffset().equals(targetOffset) || !getContentSize().equals(contentRect.size)) {
							layoutSubviews();
						}
					}
				});
		    } else {
		        this.setContentOffset(targetOffset);
		        this.setContentSize(contentRect.size);
		        applyNewLayoutBlock.run();
		        freeUnusedViews.run();
		    }
		}
	}

	public int numberOfSections() {
		return this._collectionViewData.numberOfSections();
	}

	public int numberOfItemsInSection(int section) {
		return this._collectionViewData.numberOfItemsInSection(section);
	}

	public CollectionViewLayout.Attributes layoutAttributesForItemAtIndexPath(IndexPath indexPath) {
		return this._layout.layoutAttributesForItemAtIndexPath(indexPath);
	}

	public CollectionViewLayout.Attributes layoutAttributesForSupplementaryElementOfKindAtIndexPath(String kind, IndexPath indexPath) {
		return this._layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(kind, indexPath);
	}

	public IndexPath indexPathForItemAtPoint(mocha.graphics.Point point) {
		CollectionViewLayout.Attributes attributes = Lists.last(this._layout.layoutAttributesForElementsInRect(new mocha.graphics.Rect(point.x, point.y, 1, 1)));

		if(attributes == null) {
			return null;
		} else {
			return attributes.getIndexPath();
		}
	}

	public IndexPath indexPathForCell(CollectionViewCell cell) {
		for(Map.Entry<CollectionViewItemKey,CollectionReusableView> entry : this._allVisibleViewsDict.entrySet()) {
			CollectionViewItemKey itemKey = entry.getKey();

			if(itemKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
				CollectionReusableView reusableView = entry.getValue();

				if(reusableView instanceof CollectionViewCell && reusableView == cell) {
					return itemKey.getIndexPath();
				}
			}
		}

		return null;
	}

	public CollectionViewCell cellForItemAtIndexPath(IndexPath indexPath) {
		// int index = _collectionViewData.globalIndexForItemAtIndexPath(indexPath);
		// TODO Apple uses some kind of globalIndex for this.

		for(Map.Entry<CollectionViewItemKey,CollectionReusableView> entry : this._allVisibleViewsDict.entrySet()) {
			CollectionViewItemKey itemKey = entry.getKey();

			if(itemKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
				if(itemKey.getIndexPath().equals(indexPath)) {
					CollectionReusableView reusableView = entry.getValue();

					if (reusableView instanceof CollectionViewCell) {
						return (CollectionViewCell) reusableView;
					}
				}
			}
		}

		return null;
	}

	public List<CollectionViewCell> visibleCells() {
		List<CollectionViewCell> visibleCells = new ArrayList<>();
		Rect visibleBounds = this.getBounds();

		for(CollectionReusableView reusableView : this._allVisibleViewsDict.values()) {
			if(reusableView instanceof CollectionViewCell) {
				if(visibleBounds.intersects(reusableView.getFrame())) {
					visibleCells.add((CollectionViewCell) reusableView);
				}
			}
		}

		return visibleCells;
	}

	public List<IndexPath> indexPathsForVisibleItems() {
		List<CollectionViewCell> visibleCells = this.visibleCells();
		List<IndexPath> indexPaths = new ArrayList<>(visibleCells.size());

		for(CollectionViewCell cell : visibleCells) {
			indexPaths.add(cell.getLayoutAttributes().getIndexPath());
		}

		return indexPaths;
	}

	public void scrollToItemAtIndexPathAtScrollPosition(IndexPath indexPath, boolean animated, ScrollPosition... scrollPosition) {
		this.scrollToItemAtIndexPathAtScrollPositionAnimated(indexPath, ScrollPosition.mask(scrollPosition), animated);
	}

	private void scrollToItemAtIndexPathAtScrollPositionAnimated(IndexPath indexPath, int scrollPosition, boolean animated) {
		// Ensure grid is laid out; else we can't scroll.
		this.layoutSubviews();

		CollectionViewLayout.Attributes layoutAttributes = this._layout.layoutAttributesForItemAtIndexPath(indexPath);

		if (layoutAttributes != null) {
		    mocha.graphics.Rect targetRect = this.makeRectToScrollPosition(layoutAttributes.getFrame(), scrollPosition);
		    this.scrollRectToVisible(targetRect, animated);
		}
	}

	public void insertSections(IndexSet sections) {
		this.updateSectionsUpdateAction(sections, CollectionViewUpdateItem.CollectionUpdateAction.INSERT);
	}

	public void deleteSections(IndexSet sections) {
		// First delete all items
		List<IndexPath> indexPaths = new ArrayList<>();
		for(Integer section : sections) {
			int numberOfItemsInSection = this.numberOfItemsInSection(section);
			for (int item = 0; item < numberOfItemsInSection; ++item) {
				indexPaths.add(IndexPath.withItemInSection(item, section));
			}
		}

		this.deleteItemsAtIndexPaths(indexPaths);

		// Then delete the section.
		this.updateSectionsUpdateAction(sections, CollectionViewUpdateItem.CollectionUpdateAction.DELETE);
	}

	public void reloadSections(IndexSet sections) {
		this.updateSectionsUpdateAction(sections, CollectionViewUpdateItem.CollectionUpdateAction.RELOAD);
	}

	public void moveSectionToSection(int section, int newSection) {
		List<CollectionViewUpdateItem> moveUpdateItems = this.arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction.MOVE);
		moveUpdateItems.add(new CollectionViewUpdateItem(IndexPath.withItemInSection(-1, section), IndexPath.withItemInSection(-1, newSection), CollectionViewUpdateItem.CollectionUpdateAction.MOVE));
		if (!_collectionViewFlags.updating) {
		    this.setupCellAnimations();
		    this.endItemAnimations();
		}
	}

	public void insertItemsAtIndexPaths(List<IndexPath> indexPaths) {
		this.updateRowsAtIndexPathsUpdateAction(indexPaths, CollectionViewUpdateItem.CollectionUpdateAction.INSERT);
	}

	public void deleteItemsAtIndexPaths(List<IndexPath> indexPaths) {
		this.updateRowsAtIndexPathsUpdateAction(indexPaths, CollectionViewUpdateItem.CollectionUpdateAction.DELETE);
	}

	public void reloadItemsAtIndexPaths(List<IndexPath> indexPaths) {
		this.updateRowsAtIndexPathsUpdateAction(indexPaths, CollectionViewUpdateItem.CollectionUpdateAction.RELOAD);
	}

	public void moveItemAtIndexPathToIndexPath(IndexPath indexPath, IndexPath newIndexPath) {
		List<CollectionViewUpdateItem> moveUpdateItems = this.arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction.MOVE);
		moveUpdateItems.add(new CollectionViewUpdateItem(indexPath, newIndexPath, CollectionViewUpdateItem.CollectionUpdateAction.MOVE));

		if (!this._collectionViewFlags.updating) {
		    this.setupCellAnimations();
		    this.endItemAnimations();
		}
	}

	public void performBatchUpdatesCompletion(Runnable updates, FinishedBlock completion) {
		this.setupCellAnimations();

		if (updates != null) updates.run();
		if (completion != null) this._updateCompletionHandler = completion;

		this.endItemAnimations();
	}

	UpdateTransaction currentUpdate() {
		return _update;
	}

	Map<CollectionViewItemKey, CollectionReusableView> visibleViewsDict() {
		return _allVisibleViewsDict;
	}

	CollectionViewData collectionViewData() {
		return _collectionViewData;
	}

	mocha.graphics.Rect visibleBoundRects() {
		// in original mocha.ui.CollectionView implementation they
		// check for _visibleBounds and can union this.getBounds()
		// with this value. Don't know the meaning of _visibleBounds however.
		return this.getBounds();
	}

	protected String toStringExtra() {
		return String.format("%s; collection view layout: %s", super.toStringExtra(), this._layout);
	}

	public void layoutSubviews() {
		super.layoutSubviews();

		if (!this.hasLoadedData) {
			return;
		}

		// Adding alpha animation to make the relayouting smooth
		if (this._collectionViewFlags.fadeCellsForBoundsChange) {
//		    CATransition transition = CATransition.animation();
//		    transition.setDuration(0.25f * CollectionView.void());
//		    transition.setTimingFunction(CAMediaTimingFunction.functionWithName(kCAMediaTimingFunctionEaseInEaseOut));
//		    transition.setType(kCATransitionFade);
//		    this.getLayer().addAnimationForKey(transition, "rotationAnimation");
		}

		this._collectionViewData.validateLayoutInRect(this.getBounds());

		// update cells
		if (this._collectionViewFlags.fadeCellsForBoundsChange) {
//		    CATransaction.begin();
//		    CATransaction.setDisableActions(true);
		}

		if (!_collectionViewFlags.updatingLayout) {
			this.updateVisibleCellsNow(true);
		}

		if (this._collectionViewFlags.fadeCellsForBoundsChange) {
//		    CATransaction.commit();
		}

		// do we need to update contentSize?
		Size contentSize = _collectionViewData.collectionViewContentRect().size;
		MLog("CV_TEST, CONTENT_SIZE: " + contentSize);
		if (!this.getContentSize().equals(contentSize)) {
		    this.setContentSize(contentSize);

		    // if contentSize is different, we need to re-evaluate layout, bounds (contentOffset) might changed
		    _collectionViewData.validateLayoutInRect(this.getBounds());
		    this.updateVisibleCellsNow(true);
		}

		if (this._backgroundView != null) {
		    this._backgroundView.setFrame(new mocha.graphics.Rect(this.getContentOffset(), this.getBounds().size));
		}

		this._collectionViewFlags.fadeCellsForBoundsChange = false;
		this._collectionViewFlags.doneFirstLayout = true;
	}

	public void setFrame(mocha.graphics.Rect frame) {
		if (!frame.equals(this.getFrame())) {
		    mocha.graphics.Rect bounds = new mocha.graphics.Rect(this.getContentOffset(), frame.size);
		    boolean shouldInvalidate = this._layout.shouldInvalidateLayoutForBoundsChange(bounds);

			super.setFrame(frame);

		    if (shouldInvalidate) {
		        this.invalidateLayout();
		        _collectionViewFlags.fadeCellsForBoundsChange = true;
		    }
		}
	}

	public void setBounds(mocha.graphics.Rect bounds) {
		if (!bounds.equals(this.getBounds())) {
		    boolean shouldInvalidate = this._layout.shouldInvalidateLayoutForBoundsChange(bounds);

			super.setBounds(bounds);

		    if (shouldInvalidate) {
		        this.invalidateLayout();
		        _collectionViewFlags.fadeCellsForBoundsChange = true;
		    }
		}
	}

	protected boolean willEndDraggingWithVelocityAndTargetContentOffset(Point velocity, Point targetContentOffset) {
		// Let collectionViewLayout decide where to stop.
		targetContentOffset.set(this._layout.targetContentOffsetForProposedContentOffsetWithScrollingVelocity(targetContentOffset, velocity));

		// if we are in the middle of a cell touch event, perform the "touchEnded" simulation
		if (this._touchingIndexPath != null) {
			this.cellTouchCancelled();
		}

		return true;
	}

	public CollectionReusableView dequeueReusableOrCreateDecorationViewOfKindForIndexPath(String elementKind, IndexPath indexPath) {
		List<CollectionReusableView> reusableViews = _decorationViewReuseQueues.get(elementKind);
		CollectionReusableView view = Lists.last(reusableViews);
		CollectionViewLayout collectionViewLayout = this._layout;
		CollectionViewLayout.Attributes attributes = collectionViewLayout.layoutAttributesForDecorationViewOfKindAtIndexPath(elementKind, indexPath);

		if (view != null) {
		    reusableViews.remove(reusableViews.size() - 1);
		} else {
			Map<String, Class<? extends CollectionReusableView>> decorationViewClassDict = collectionViewLayout.getDecorationViewClassDict();
			Class<? extends CollectionReusableView> viewClass = decorationViewClassDict.get(elementKind);

			if (viewClass == null) {
			   throw new IllegalArgumentException("Class not registered for identifier " + elementKind);
			}

			try {
				if (attributes != null) {
					view = viewClass.getConstructor(CollectionViewLayout.Attributes.class).newInstance(attributes.getFrame());
				} else{
					view = viewClass.newInstance();
				}
			} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}

			view.setCollectionView(this);
		    view.setReuseIdentifier(elementKind);
		}

		view.applyLayoutAttributes(attributes);

		return view;
	}

	List<CollectionViewCell> allCells() {
		List<CollectionViewCell> visibleCells = new ArrayList<>();

		for(CollectionReusableView reusableView : this._allVisibleViewsDict.values()) {
			if(reusableView instanceof CollectionViewCell) {
				visibleCells.add((CollectionViewCell) reusableView);
			}
		}

		return visibleCells;
	}

	mocha.graphics.Rect makeRectToScrollPosition(mocha.graphics.Rect targetRect, int scrollPosition) {
		// split parameters
		int verticalPosition = scrollPosition & 0x07; // 0000 0111
		int horizontalPosition = scrollPosition & 0x38; // 0011 1000

		if (verticalPosition != SCROLL_POSITION_NONE
		        && verticalPosition != SCROLL_POSITION_TOP
		        && verticalPosition != SCROLL_POSITION_CENTERED_VERTICALLY
		        && verticalPosition != SCROLL_POSITION_BOTTOM) {
			throw new IllegalArgumentException("CollectionView.ScrollPosition: attempt to use a scroll position with multiple vertical positioning styles");
		}

		if (horizontalPosition != SCROLL_POSITION_NONE
		        && horizontalPosition != SCROLL_POSITION_LEFT
		        && horizontalPosition != SCROLL_POSITION_CENTERED_HORIZONTALLY
		        && horizontalPosition != SCROLL_POSITION_RIGHT) {
		    throw new IllegalArgumentException("CollectionView.ScrollPosition: attempt to use a scroll position with multiple horizontal positioning styles");
		}

		mocha.graphics.Rect frame = this.getLayer().getBounds();
		float calculateX;
		float calculateY;

		EdgeInsets contentInset = this.getContentInset();

		switch (verticalPosition) {
		    case SCROLL_POSITION_CENTERED_VERTICALLY:
		        calculateY = Math.max(targetRect.origin.y - ((frame.size.height / 2) - (targetRect.size.height / 2)), -contentInset.top);
		        targetRect = new mocha.graphics.Rect(targetRect.origin.x, calculateY, targetRect.size.width, frame.size.height);
		        break;
		    case SCROLL_POSITION_TOP:
		        targetRect = new mocha.graphics.Rect(targetRect.origin.x, targetRect.origin.y, targetRect.size.width, frame.size.height);
		        break;

		    case SCROLL_POSITION_BOTTOM:
		        calculateY = Math.max(targetRect.origin.y - (frame.size.height - targetRect.size.height), -contentInset.top);
		        targetRect = new mocha.graphics.Rect(targetRect.origin.x, calculateY, targetRect.size.width, frame.size.height);
		        break;
		}

		switch (horizontalPosition) {
		    case SCROLL_POSITION_CENTERED_HORIZONTALLY:
		        calculateX = targetRect.origin.x - ((frame.size.width / 2) - (targetRect.size.width / 2));
		        targetRect = new mocha.graphics.Rect(calculateX, targetRect.origin.y, frame.size.width, targetRect.size.height);
		        break;

		    case SCROLL_POSITION_LEFT:
		        targetRect = new mocha.graphics.Rect(targetRect.origin.x, targetRect.origin.y, frame.size.width, targetRect.size.height);
		        break;

		    case SCROLL_POSITION_RIGHT:
		        calculateX = targetRect.origin.x - (frame.size.width - targetRect.size.width);
		        targetRect = new mocha.graphics.Rect(calculateX, targetRect.origin.y, frame.size.width, targetRect.size.height);
		        break;
		}

		return targetRect;
	}

	public void touchesBegan(List<Touch> touches, Event event) {
		super.touchesBegan(touches, event);

		// reset touching state vars
		this._touchingIndexPath = null;
		this._currentIndexPath = null;

		Point touchPoint = touches.get(0).locationInView(this);
		IndexPath indexPath = this.indexPathForItemAtPoint(touchPoint);
		if (indexPath != null && this.getAllowsSelection()) {
		    if (!this.highlightItemAtIndexPathAnimatedScrollPositionNotifyDelegate(indexPath, true, SCROLL_POSITION_NONE, true)) {
				return;
			}

			this._touchingIndexPath = indexPath;
			this._currentIndexPath = indexPath;

		    if (!this.getAllowsMultipleSelection()) {
		        // temporally unhighlight background on touchesBegan (keeps selected by _indexPathsForSelectedItems)
		        // single-select only mode only though
		        IndexPath tempDeselectIndexPath = Sets.any(_indexPathsForSelectedItems);

		        if (tempDeselectIndexPath != null && !tempDeselectIndexPath.equals(this._touchingIndexPath)) {
		            // iOS6 mocha.ui.CollectionView deselects cell without notification
		            CollectionViewCell selectedCell = this.cellForItemAtIndexPath(tempDeselectIndexPath);
		            selectedCell.setSelected(false);
		        }
		    }
		}
	}

	public void touchesMoved(List<Touch> touches, Event event) {
		super.touchesMoved(touches, event);

		// allows moving between highlight and unhighlight state only if setHighlighted is not overwritten
		if (this._touchingIndexPath != null) {
		    mocha.graphics.Point touchPoint = touches.get(0).locationInView(this);
		    IndexPath indexPath = this.indexPathForItemAtPoint(touchPoint);

		    // moving out of bounds
		    if (
					this._currentIndexPath.equals(this._touchingIndexPath) &&
					!indexPath.equals(this._touchingIndexPath) &&
					this.unhighlightItemAtIndexPathAnimatedNotifyDelegateShouldCheckHighlight(this._touchingIndexPath, true, true, true)
				) {
				this._currentIndexPath = indexPath;
		        // moving back into the original touching cell
		    } else if (!this._currentIndexPath.equals(this._touchingIndexPath) && indexPath.equals(this._touchingIndexPath)) {
		        this.highlightItemAtIndexPathAnimatedScrollPositionNotifyDelegate(this._touchingIndexPath, true, SCROLL_POSITION_NONE, true);
				this._currentIndexPath = this._touchingIndexPath;
		    }
		}
	}

	public void touchesEnded(List<Touch> touches, Event event) {
		super.touchesEnded(touches, event);

		if (this._touchingIndexPath != null) {
		    // first unhighlight the touch operation
		    this.unhighlightItemAtIndexPathAnimatedNotifyDelegate(this._touchingIndexPath, true, true);

		    mocha.graphics.Point touchPoint = touches.get(0).locationInView(this);
		    IndexPath indexPath = this.indexPathForItemAtPoint(touchPoint);
		    if (indexPath.equals(this._touchingIndexPath)) {
		        this.userSelectedItemAtIndexPath(indexPath);
		    }
		    else if (!this.getAllowsMultipleSelection()) {
		        mocha.foundation.IndexPath tempDeselectIndexPath = Sets.any(this._indexPathsForSelectedItems);
		        if (tempDeselectIndexPath != null && !tempDeselectIndexPath.equals(this._touchingIndexPath)) {
		            this.cellTouchCancelled();
		        }
		    }

		    // for pedantic reasons only - always set to null on touchesBegan
			this._touchingIndexPath = null;
			this._currentIndexPath = null;
		}
	}

	public void touchesCancelled(List<Touch> touches, Event event) {
		super.touchesCancelled(touches, event);

		// do not mark touchingIndexPath as null because whoever cancelled this touch will need to signal a touch up event later
		if (this._touchingIndexPath != null) {
		    // first unhighlight the touch operation
		    this.unhighlightItemAtIndexPathAnimatedNotifyDelegate(this._touchingIndexPath, true, true);
		}
	}

	private void cellTouchCancelled() {
		// turn on ALL the *should be selected* cells (iOS6 mocha.ui.CollectionView does no state keeping or other fancy optimizations)
		// there should be no notifications as this is a silent "turn everything back on"
		for (mocha.foundation.IndexPath tempDeselectedIndexPath : _indexPathsForSelectedItems) {
		    CollectionViewCell selectedCell = this.cellForItemAtIndexPath(tempDeselectedIndexPath);
		    selectedCell.setSelected(true);
		}
	}

	private void userSelectedItemAtIndexPath(IndexPath indexPath) {
		if (this.getAllowsMultipleSelection() && _indexPathsForSelectedItems.contains(indexPath)) {
		    this.deselectItemAtIndexPathAnimatedNotifyDelegate(indexPath, true, true);
		} else if (this.getAllowsSelection()) {
		    this.selectItemAtIndexPathAnimatedScrollPositionNotifyDelegate(indexPath, true, SCROLL_POSITION_NONE, true);
		}
	}

	private void selectItemAtIndexPathAnimatedScrollPositionNotifyDelegate(IndexPath indexPath, boolean animated, int scrollPosition, boolean notifyDelegate) {
		if (this.getAllowsMultipleSelection() && _indexPathsForSelectedItems.contains(indexPath)) {
		    boolean shouldDeselect = true;
		    if (notifyDelegate && _collectionViewFlags.delegateShouldDeselectItemAtIndexPath) {
		        shouldDeselect = this.getDelegate().collectionViewShouldDeselectItemAtIndexPath(this, indexPath);
		    }

		    if (shouldDeselect) {
		        this.deselectItemAtIndexPathAnimatedNotifyDelegate(indexPath, animated, notifyDelegate);
		    }
		} else {
		    // either single selection, or wasn't already selected in multiple selection mode
		    boolean shouldSelect = true;
		    if (notifyDelegate && _collectionViewFlags.delegateShouldSelectItemAtIndexPath) {
		        shouldSelect = this.getDelegate().collectionViewShouldSelectItemAtIndexPath(this, indexPath);
		    }

		    if (!this.getAllowsMultipleSelection()) {
		        // now unselect the previously selected cell for single selection
		        mocha.foundation.IndexPath tempDeselectIndexPath = Sets.any(_indexPathsForSelectedItems);
		        if (tempDeselectIndexPath != null && !tempDeselectIndexPath.equals(indexPath)) {
		            this.deselectItemAtIndexPathAnimatedNotifyDelegate(tempDeselectIndexPath, true, true);
		        }
		    }

		    if (shouldSelect) {
		        CollectionViewCell selectedCell = this.cellForItemAtIndexPath(indexPath);
		        selectedCell.setSelected(true);

		        _indexPathsForSelectedItems.add(indexPath);

		        if (scrollPosition != SCROLL_POSITION_NONE) {
		            this.scrollToItemAtIndexPathAtScrollPositionAnimated(indexPath, scrollPosition, animated);
		        }

		        if (notifyDelegate && _collectionViewFlags.delegateDidSelectItemAtIndexPath) {
		            this.getDelegate().collectionViewDidSelectItemAtIndexPath(this, indexPath);
		        }
		    }
		}
	}

	private void deselectItemAtIndexPathAnimatedNotifyDelegate(IndexPath indexPath, boolean animated, boolean notifyDelegate) {
		boolean shouldDeselect = true;
		// deselect only relevant during multi mode
		if (this.getAllowsMultipleSelection() && notifyDelegate && _collectionViewFlags.delegateShouldDeselectItemAtIndexPath) {
		    shouldDeselect = this.getDelegate().collectionViewShouldDeselectItemAtIndexPath(this, indexPath);
		}

		if (shouldDeselect && _indexPathsForSelectedItems.contains(indexPath)) {
		    CollectionViewCell selectedCell = this.cellForItemAtIndexPath(indexPath);
		    if (selectedCell != null) {
		        if (selectedCell.isSelected()) {
		            selectedCell.setSelected(false);
		        }
		    }

		    _indexPathsForSelectedItems.remove(indexPath);

		    if (notifyDelegate && _collectionViewFlags.delegateDidDeselectItemAtIndexPath) {
		        this.getDelegate().collectionViewDidDeselectItemAtIndexPath(this, indexPath);
		    }
		}
	}

	private boolean highlightItemAtIndexPathAnimatedScrollPositionNotifyDelegate(IndexPath indexPath, boolean animated, int scrollPosition, boolean notifyDelegate) {
		boolean shouldHighlight = true;
		notifyDelegate = notifyDelegate && this._delegate != null;

		if (notifyDelegate && _collectionViewFlags.delegateShouldHighlightItemAtIndexPath) {
		    shouldHighlight = this._delegate.collectionViewShouldHighlightItemAtIndexPath(this, indexPath);
		}

		if (shouldHighlight) {
		    CollectionViewCell highlightedCell = this.cellForItemAtIndexPath(indexPath);
		    highlightedCell.setHighlighted(true);
		    _indexPathsForHighlightedItems.add(indexPath);

		    if (scrollPosition != SCROLL_POSITION_NONE) {
		        this.scrollToItemAtIndexPathAtScrollPositionAnimated(indexPath, scrollPosition, animated);
		    }

		    if (notifyDelegate && _collectionViewFlags.delegateDidHighlightItemAtIndexPath) {
				this._delegate.collectionViewDidHighlightItemAtIndexPath(this, indexPath);
		    }
		}

		return shouldHighlight;
	}

	private boolean unhighlightItemAtIndexPathAnimatedNotifyDelegate(IndexPath indexPath, boolean animated, boolean notifyDelegate) {
		return this.unhighlightItemAtIndexPathAnimatedNotifyDelegateShouldCheckHighlight(indexPath, animated, notifyDelegate, false);
	}

	private boolean unhighlightItemAtIndexPathAnimatedNotifyDelegateShouldCheckHighlight(IndexPath indexPath, boolean animated, boolean notifyDelegate, boolean check) {
		if (_indexPathsForHighlightedItems.contains(indexPath)) {
		    CollectionViewCell highlightedCell = this.cellForItemAtIndexPath(indexPath);

		    // iOS6 does not notify any delegate if the cell was never highlighted (setHighlighted overwritten) during touchMoved
		    if (check && !highlightedCell.isHighlighted()) {
		        return false;
		    }

		    // if multiple selection or not unhighlighting a selected item we don't perform any op
		    if (highlightedCell.isHighlighted() && _indexPathsForSelectedItems.contains(indexPath)) {
		        highlightedCell.setHighlighted(true);
		    }else {
		        highlightedCell.setHighlighted(false);
		    }

		    _indexPathsForHighlightedItems.remove(indexPath);

		    if (notifyDelegate && _collectionViewFlags.delegateDidUnhighlightItemAtIndexPath) {
		        this.getDelegate().collectionViewDidUnhighlightItemAtIndexPath(this, indexPath);
		    }

		    return true;
		}

		return false;
	}

	public void setBackgroundView(View backgroundView) {
		if (backgroundView != _backgroundView) {
			if(_backgroundView != null) {
				_backgroundView.removeFromSuperview();
			}

		    _backgroundView = backgroundView;

			if(_backgroundView != null) {
				_backgroundView.setFrame(new mocha.graphics.Rect(this.getContentOffset(), this.getBounds().size));
				_backgroundView.setAutoresizing(View.Autoresizing.FLEXIBLE_HEIGHT, View.Autoresizing.FLEXIBLE_WIDTH);
				this.addSubview(_backgroundView);
				this.sendSubviewToBack(_backgroundView);
			}
		}
	}

	public void setDelegate(Delegate delegate) {
		this._delegate = delegate;

		//  Managing the Selected Cells
		this._collectionViewFlags.delegateShouldSelectItemAtIndexPath = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewShouldSelectItemAtIndexPath", CollectionView.class, IndexPath.class);
		this._collectionViewFlags.delegateShouldDeselectItemAtIndexPath = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewShouldDeselectItemAtIndexPath", CollectionView.class, IndexPath.class);
		this._collectionViewFlags.delegateDidSelectItemAtIndexPath = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewDidSelectItemAtIndexPath", CollectionView.class, IndexPath.class);
		this._collectionViewFlags.delegateDidDeselectItemAtIndexPath = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewDidDeselectItemAtIndexPath", CollectionView.class, IndexPath.class);

		//  Managing Cell Highlighting
		this._collectionViewFlags.delegateShouldHighlightItemAtIndexPath = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewShouldHighlightItemAtIndexPath", CollectionView.class, IndexPath.class);
		this._collectionViewFlags.delegateDidHighlightItemAtIndexPath = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewDidHighlightItemAtIndexPath", CollectionView.class, IndexPath.class);
		this._collectionViewFlags.delegateDidUnhighlightItemAtIndexPath = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewDidUnhighlightItemAtIndexPath", CollectionView.class, IndexPath.class);

		//  Tracking the Removal of Views
		this._collectionViewFlags.delegateDidEndDisplayingCell = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewDidEndDisplayingCellForItemAtIndexPath", CollectionView.class, CollectionViewCell.class, IndexPath.class);
		this._collectionViewFlags.delegateDidEndDisplayingSupplementaryView = OptionalInterfaceHelper.hasImplemented(delegate, Delegate.class, "collectionViewDidEndDisplayingSupplementaryViewForElementOfKindAtIndexPath", CollectionView.class, CollectionReusableView.class, String.class, IndexPath.class);

		// Notify our layout
		if(this._layout != null) {
			this._layout.collectionViewDelegateDidChange();
		}
	}

	public void setDataSource(DataSource dataSource) {
		if (dataSource != _dataSource) {
		    _dataSource = dataSource;

			// Getting Item and Section Metrics
			this._collectionViewFlags.dataSourceNumberOfSections = OptionalInterfaceHelper.hasImplemented(dataSource, DataSource.class, "numberOfSectionsInCollectionView", CollectionView.class);

		    // Getting Views for Items
			this._collectionViewFlags.dataSourceViewForSupplementaryElement = OptionalInterfaceHelper.hasImplemented(dataSource, DataSource.class, "collectionViewViewForSupplementaryElementOfKindAtIndexPath", CollectionView.class, String.class, IndexPath.class);
		}
	}

	public void setAllowsMultipleSelection(boolean allowsMultipleSelection) {
		this._allowsMultipleSelection = allowsMultipleSelection;

		// Deselect all objects if allows multiple selection is false
		if (!allowsMultipleSelection) {
			while(_indexPathsForSelectedItems.size() > 1) {
				// deselect removes from _indexPathsForSelectedItems
		        this.deselectItemAtIndexPathAnimatedNotifyDelegate(Sets.any(_indexPathsForSelectedItems), true, true);
		    }
		}
	}

	public void invalidateLayout() {
		this._layout.invalidateLayout();
		this._collectionViewData.invalidate(); // invalidate layout cache
	}

	private void updateVisibleCellsNow(boolean now) {
		List<CollectionViewLayout.Attributes> layoutAttributesArray = this._collectionViewData.layoutAttributesForElementsInRect(this.getBounds());
		MLog("CV_TEST updateVisibleCells " + layoutAttributesArray + ", " + this.getBounds());

		if (layoutAttributesArray == null || layoutAttributesArray.size() == 0) {
		    // If our layout source isn't providing any layout information, we should just
		    // stop, otherwise we'll blow away all the currently existing cells.
		    return;
		}

		// create ItemKey/Attributes dictionary
		Map<CollectionViewItemKey,CollectionViewLayout.Attributes> itemKeysToAddDict = new HashMap<>();

		// Add new cells.
		for (CollectionViewLayout.Attributes layoutAttributes  : layoutAttributesArray) {
		    CollectionViewItemKey itemKey = CollectionViewItemKey.collectionItemKeyForLayoutAttributes(layoutAttributes);
		    itemKeysToAddDict.put(itemKey, layoutAttributes);

		    // check if cell is in visible dict; add it if not.
		    CollectionReusableView view = _allVisibleViewsDict.get(itemKey);
		    if (view == null) {
		        if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            view = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(itemKey.getIndexPath(), layoutAttributes);

		        }else if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		            view = this.createPreparedSupplementaryViewForElementOfKindAtIndexPathWithLayoutAttributes(layoutAttributes.getRepresentedElementKind(), layoutAttributes.getIndexPath(), layoutAttributes);
		        }else if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            view = this.dequeueReusableOrCreateDecorationViewOfKindForIndexPath(layoutAttributes.getRepresentedElementKind(), layoutAttributes.getIndexPath());
		        }

		        // Supplementary views are optional
		        if (view != null) {
		            this._allVisibleViewsDict.put(itemKey, view);
		            this.addControlledSubview(view);

		            // Always apply attributes. Fixes #203.
		            view.applyLayoutAttributes(layoutAttributes);
		        }
		    } else {
		        // just update cell
		        view.applyLayoutAttributes(layoutAttributes);
		    }
		}

		// Detect what items should be removed and queued back.
		Set<CollectionViewItemKey> allVisibleItemKeys = new HashSet<>(_allVisibleViewsDict.keySet());
		allVisibleItemKeys.removeAll(itemKeysToAddDict.keySet());

		// Finally remove views that have not been processed and prepare them for re-use.
		for (CollectionViewItemKey itemKey  : allVisibleItemKeys) {
		    CollectionReusableView reusableView = _allVisibleViewsDict.get(itemKey);

		    if (reusableView != null) {
		        reusableView.removeFromSuperview();
		        _allVisibleViewsDict.remove(itemKey);

		        if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		            if (this._delegate != null && this._collectionViewFlags.delegateDidEndDisplayingCell) {
		                this._delegate.collectionViewDidEndDisplayingCellForItemAtIndexPath(this, (CollectionViewCell)reusableView, itemKey.getIndexPath());
		            }

		            this.reuseCell((CollectionViewCell)reusableView);
		        }

				else if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		            if (this._delegate != null && this._collectionViewFlags.delegateDidEndDisplayingSupplementaryView) {
						this._delegate.collectionViewDidEndDisplayingSupplementaryViewForElementOfKindAtIndexPath(this, reusableView, itemKey.getIdentifier(), itemKey.getIndexPath());
		            }

		            this.reuseSupplementaryView(reusableView);
		        }

		        else if (itemKey.getType() == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
		            this.reuseDecorationView(reusableView);
		        }
		    }
		}
	}

	private CollectionViewCell createPreparedCellForItemAtIndexPathWithLayoutAttributes(IndexPath indexPath, CollectionViewLayout.Attributes layoutAttributes) {
		CollectionViewCell cell = this._dataSource.collectionViewCellForItemAtIndexPath(this, indexPath);

		// Apply attributes
		cell.applyLayoutAttributes(layoutAttributes);

		// reset selected/highlight state
		cell.setHighlighted(this._indexPathsForHighlightedItems.contains(indexPath));
		cell.setSelected(this._indexPathsForSelectedItems.contains(indexPath));

		// voiceover support
		cell.setIsAccessibilityElement(true);

		return cell;
	}

	private CollectionReusableView createPreparedSupplementaryViewForElementOfKindAtIndexPathWithLayoutAttributes(String kind, IndexPath indexPath, CollectionViewLayout.Attributes layoutAttributes) {
		if (this._dataSource != null && this._collectionViewFlags.dataSourceViewForSupplementaryElement) {
		    CollectionReusableView view = this._dataSource.collectionViewViewForSupplementaryElementOfKindAtIndexPath(this, kind, indexPath);
		    view.applyLayoutAttributes(layoutAttributes);
		    return view;
		}

		return null;
	}

	private <T extends CollectionReusableView> void queueReusableViewInQueueWithIdentifier(T reusableView, Map<String,List<T>> queue, String identifier) {
		Assert.condition(identifier != null && identifier.length() > 0, "Invalid identifier");

		reusableView.removeFromSuperview();
		reusableView.prepareForReuse();

		// enqueue cell
		List<T> reuseableViews = queue.get(identifier);

		if (reuseableViews == null) {
		    reuseableViews = new ArrayList<>();
		    queue.put(identifier, reuseableViews);
		}

		reuseableViews.add(reusableView);
	}

	private void reuseCell(CollectionViewCell cell) {
		this.queueReusableViewInQueueWithIdentifier(cell, _cellReuseQueues, cell.getReuseIdentifier());
	}

	private void reuseSupplementaryView(CollectionReusableView supplementaryView) {
		String kindAndIdentifier = String.format("%s/%s", supplementaryView.getLayoutAttributes().getElementKind(), supplementaryView.getReuseIdentifier());
		this.queueReusableViewInQueueWithIdentifier(supplementaryView, _supplementaryViewReuseQueues, kindAndIdentifier);
	}

	private void reuseDecorationView(CollectionReusableView decorationView) {
		this.queueReusableViewInQueueWithIdentifier(decorationView, _decorationViewReuseQueues, decorationView.getReuseIdentifier());
	}

	private void addControlledSubview(CollectionReusableView subview) {
		// avoids placing views above the scroll indicator
		// If the collection view is not displaying scrollIndicators then this.getSubviews().size() can be 0.
		// We take the max to ensure we insert at a non negative index because a negative index will silently fail to insert the view
		int insertionIndex = Math.max((this.getSubviews().size() - (this.isDragging() ? 1 : 0)), 0);
		this.insertSubview(subview, insertionIndex);
		mocha.ui.View scrollIndicatorView = null;

		if (this.isDragging()) {
		    scrollIndicatorView = Lists.last(this.getSubviews());
		}

		List<CollectionReusableView> floatingViews = new ArrayList<>();

		for (mocha.ui.View uiView : this.getSubviews()) {
		    if (uiView instanceof CollectionReusableView) {
				CollectionReusableView reusableView = (CollectionReusableView)uiView;

				if (reusableView.getLayoutAttributes().getZIndex() > 0) {
					floatingViews.add(reusableView);
				}
			}
		}

		Collections.sort(floatingViews, new Comparator<CollectionReusableView>() {
			public int compare(CollectionReusableView reusableView1, CollectionReusableView reusableView2) {
				float z1 = reusableView1.getLayoutAttributes().getZIndex();
				float z2 = reusableView2.getLayoutAttributes().getZIndex();

				if (z1 > z2) {
					return ComparisonResult.DESCENDING.getValue();
				}else if (z1 < z2) {
					return ComparisonResult.ASCENDING.getValue();
				}

				return ComparisonResult.SAME.getValue();
			}
		});

		if(floatingViews.size() > 0) {
			for (CollectionReusableView reusableView : floatingViews){
				this.bringSubviewToFront(reusableView);
			}

			if(scrollIndicatorView != null) {
				this.bringSubviewToFront(scrollIndicatorView);
			}
		}
	}

	private void suspendReloads() {
		_reloadingSuspendedCount++;
	}

	private void resumeReloads() {
		if (0 < _reloadingSuspendedCount) _reloadingSuspendedCount--;
	}

	List<CollectionViewUpdateItem> arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction updateAction) {
		List<CollectionViewUpdateItem> updateActions = null;

		switch (updateAction) {
		    case INSERT:
		        if (_insertItems == null) _insertItems = new ArrayList<>();
		        updateActions = _insertItems;
		        break;
		    case DELETE:
		        if (_deleteItems == null) _deleteItems = new ArrayList<>();
		        updateActions = _deleteItems;
		        break;
		    case MOVE:
		        if (_moveItems == null) _moveItems = new ArrayList<>();
		        updateActions = _moveItems;
		        break;
		    case RELOAD:
		        if (_reloadItems == null) _reloadItems = new ArrayList<>();
		        updateActions = _reloadItems;
		        break;
		    default: break;
		}

		return updateActions;
	}

	private void prepareLayoutForUpdates() {
		List<CollectionViewUpdateItem> array = new ArrayList<>();
		array.addAll(Lists.sortedList(_originalDeleteItems, false));
		array.addAll(Lists.sortedList(_originalInsertItems));
		array.addAll(Lists.sortedList(_reloadItems));
		array.addAll(Lists.sortedList(_moveItems));
		_layout.prepareForCollectionViewUpdates(array);
	}

	private void updateWithItems(List<CollectionViewUpdateItem> items) {
		this.prepareLayoutForUpdates();

		final Rect visibleBounds = this.getVisibleBoundRects();

		final List<UpdateAnimation> animations = new ArrayList<>();
		Map<CollectionViewItemKey,CollectionReusableView> newAllVisibleView = new HashMap<>();

		final Map<CollectionViewLayout.CollectionViewItemType,List<CollectionReusableView>> viewsToRemove = new HashMap<>();
		viewsToRemove.put(CollectionViewLayout.CollectionViewItemType.CELL, new ArrayList<CollectionReusableView>());
		viewsToRemove.put(CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW, new ArrayList<CollectionReusableView>());
		viewsToRemove.put(CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW, new ArrayList<CollectionReusableView>());

		for (CollectionViewUpdateItem updateItem  : items) {
		    if (updateItem.isSectionOperation() && updateItem.updateAction() != CollectionViewUpdateItem.CollectionUpdateAction.DELETE) continue;
		    if (updateItem.isSectionOperation() && updateItem.updateAction() == CollectionViewUpdateItem.CollectionUpdateAction.DELETE) {
		        int numberOfBeforeSection = _update.oldModel.numberOfItemsInSection(updateItem.indexPathBeforeUpdate().section);
		        for (int i = 0; i < numberOfBeforeSection; i++) {
		            IndexPath indexPath = IndexPath.withItemInSection(i, updateItem.indexPathBeforeUpdate().section);

		            CollectionViewLayout.Attributes finalAttrs = _layout.finalLayoutAttributesForDisappearingItemAtIndexPath(indexPath);
		            CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPath);
		            CollectionReusableView view = _allVisibleViewsDict.get(key);

		            if (view != null) {
		                CollectionViewLayout.Attributes startAttrs = view.getLayoutAttributes();

		                if (finalAttrs == null) {
		                    finalAttrs = startAttrs.copy();
		                    finalAttrs.setAlpha(0);
		                }

		                animations.add(new UpdateAnimation(view, startAttrs, finalAttrs));

		                _allVisibleViewsDict.remove(key);

		                viewsToRemove.get(key.getType()).add(view);
		            }
		        }

		        continue;
		    }

		    if (updateItem.updateAction() == CollectionViewUpdateItem.CollectionUpdateAction.DELETE) {
		        IndexPath indexPath = updateItem.indexPathBeforeUpdate();

		        CollectionViewLayout.Attributes finalAttrs = _layout.finalLayoutAttributesForDisappearingItemAtIndexPath(indexPath);
		        CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPath);
		        CollectionReusableView view = _allVisibleViewsDict.get(key);

		        if (view != null) {
		            CollectionViewLayout.Attributes startAttrs = view.getLayoutAttributes();

		            if (finalAttrs == null) {
		                finalAttrs = startAttrs.copy();
		                finalAttrs.setAlpha(0);
		            }

		            animations.add(new UpdateAnimation( view, startAttrs, finalAttrs));

		            _allVisibleViewsDict.remove(key);

		            viewsToRemove.get(key.getType()).add(view);
		        }
		    }

		    else if (updateItem.updateAction() == CollectionViewUpdateItem.CollectionUpdateAction.INSERT) {
		        IndexPath indexPath = updateItem.indexPathAfterUpdate();
		        CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPath);
		        CollectionViewLayout.Attributes startAttrs = _layout.initialLayoutAttributesForAppearingItemAtIndexPath(indexPath);
		        CollectionViewLayout.Attributes finalAttrs = _layout.layoutAttributesForItemAtIndexPath(indexPath);

		        mocha.graphics.Rect startRect = startAttrs.getFrame();
		        mocha.graphics.Rect finalRect = finalAttrs.getFrame();

		        if (visibleBounds.intersects(startRect) || visibleBounds.intersects(finalRect)) {

		            if (startAttrs == null) {
		                startAttrs = finalAttrs.copy();
		                startAttrs.setAlpha(0);
		            }

		            CollectionReusableView view = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(indexPath, startAttrs);
		            this.addControlledSubview(view);

		            newAllVisibleView.put(key, view);
		            animations.add(new UpdateAnimation(view, startAttrs, finalAttrs));
		        }
		    }

		    else if (updateItem.updateAction() == CollectionViewUpdateItem.CollectionUpdateAction.MOVE) {
		        IndexPath indexPathBefore = updateItem.indexPathBeforeUpdate();
		        IndexPath indexPathAfter = updateItem.indexPathAfterUpdate();

		        CollectionViewItemKey keyBefore = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPathBefore);
		        CollectionViewItemKey keyAfter = CollectionViewItemKey.collectionItemKeyForCellWithIndexPath(indexPathAfter);
		        CollectionReusableView view = _allVisibleViewsDict.get(keyBefore);

		        CollectionViewLayout.Attributes startAttrs = null;
		        CollectionViewLayout.Attributes finalAttrs = _layout.layoutAttributesForItemAtIndexPath(indexPathAfter);

		        if (view != null) {
		            startAttrs = view.getLayoutAttributes();
		            _allVisibleViewsDict.remove(keyBefore);
		            newAllVisibleView.put(keyAfter, view);
		        } else {
		            startAttrs = finalAttrs.copy();
		            startAttrs.setAlpha(0);
		            view = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(indexPathAfter, startAttrs);
		            this.addControlledSubview(view);
		            newAllVisibleView.put(keyAfter, view);
		        }

		        animations.add(new UpdateAnimation(view, startAttrs, finalAttrs));
		    }
		}

		for(Map.Entry<CollectionViewItemKey,CollectionReusableView> entry : this._allVisibleViewsDict.entrySet()) {
			CollectionViewItemKey key = entry.getKey();
			CollectionReusableView view = entry.getValue();

		    if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL) {
		        int oldGlobalIndex = _update.oldModel.globalIndexForItemAtIndexPath(key.getIndexPath());
		        List<Integer> oldToNewIndexMap = _update.oldToNewIndexMap;

				int newGlobalIndex = -1;

				if (-1 != oldGlobalIndex && oldGlobalIndex < oldToNewIndexMap.size()) {
		            newGlobalIndex = oldToNewIndexMap.get(oldGlobalIndex);
		        }

		        mocha.foundation.IndexPath newIndexPath = newGlobalIndex == -1 ? null : _update.newModel.indexPathForItemAtGlobalIndex(newGlobalIndex);
		        mocha.foundation.IndexPath oldIndexPath = oldGlobalIndex == -1 ? null : _update.oldModel.indexPathForItemAtGlobalIndex(oldGlobalIndex);

		        if (newIndexPath != null) {
					CollectionViewLayout.Attributes startAttrs = _layout.initialLayoutAttributesForAppearingItemAtIndexPath(oldIndexPath);
					CollectionViewLayout.Attributes finalAttrs = _layout.layoutAttributesForItemAtIndexPath(newIndexPath);

		            animations.add(new UpdateAnimation(view, startAttrs, finalAttrs));
		            CollectionViewItemKey newKey = key.copy();
		            newKey.setIndexPath(newIndexPath);
		            newAllVisibleView.put(newKey, view);
		        }

		    } else if (key.getType() == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
		        CollectionViewLayout.Attributes startAttrs = null;
		        CollectionViewLayout.Attributes finalAttrs = null;

		        startAttrs = view.getLayoutAttributes();
		        finalAttrs = _layout.layoutAttributesForSupplementaryViewOfKindAtIndexPath(view.getLayoutAttributes().getRepresentedElementKind(), key.getIndexPath());

		        animations.add(new UpdateAnimation(view, startAttrs, finalAttrs));
		        CollectionViewItemKey newKey = key.copy();
		        newAllVisibleView.put(newKey, view);

		    }
		}

		List<CollectionViewLayout.Attributes> allNewlyVisibleItems = _layout.layoutAttributesForElementsInRect(this.getVisibleBoundRects());

		for (CollectionViewLayout.Attributes attrs  : allNewlyVisibleItems) {
		    CollectionViewItemKey key = CollectionViewItemKey.collectionItemKeyForLayoutAttributes(attrs);

		    if (key.getType() == CollectionViewLayout.CollectionViewItemType.CELL && !newAllVisibleView.keySet().contains(key)) {
		        CollectionViewLayout.Attributes startAttrs = _layout.initialLayoutAttributesForAppearingItemAtIndexPath(attrs.getIndexPath());

		        CollectionReusableView view = this.createPreparedCellForItemAtIndexPathWithLayoutAttributes(attrs.getIndexPath(), startAttrs);
		        this.addControlledSubview(view);
		        newAllVisibleView.put(key, view);

				animations.add(new UpdateAnimation(view, startAttrs != null ? startAttrs : attrs, attrs));
		    }
		}

		_allVisibleViewsDict = newAllVisibleView;

		for (UpdateAnimation animation : animations) {
			animation.view.applyLayoutAttributes(animation.previousLayoutInfos);
		}

		View.animateWithDuration(300, new Animations() {
			public void performAnimatedChanges() {
				_collectionViewFlags.updatingLayout = true;

				for (UpdateAnimation animation : animations) {
					animation.view.applyLayoutAttributes(animation.newLayoutInfos);
				}
			}
		}, new AnimationCompletion() {
			public void animationCompletion(boolean finished) {
				// Iterate through all the views that we are going to remove.
				for(Map.Entry<CollectionViewLayout.CollectionViewItemType,List<CollectionReusableView>> entry : viewsToRemove.entrySet()) {
					CollectionViewLayout.CollectionViewItemType type = entry.getKey();

					for (CollectionReusableView view  : entry.getValue()) {
						if (type == CollectionViewLayout.CollectionViewItemType.CELL) {
							reuseCell((CollectionViewCell)view);
						} else if (type == CollectionViewLayout.CollectionViewItemType.SUPPLEMENTARY_VIEW) {
							reuseSupplementaryView(view);
						} else if (type == CollectionViewLayout.CollectionViewItemType.DECORATION_VIEW) {
							reuseDecorationView(view);
						}
					}
				}

				_collectionViewFlags.updatingLayout = false;

				if (_updateCompletionHandler != null) {
					_updateCompletionHandler.execute(finished);
					_updateCompletionHandler = null;
				}

			}
		});

		_layout.finalizeCollectionViewUpdates();
	}

	private void setupCellAnimations() {
		this.updateVisibleCellsNow(true);
		this.suspendReloads();
		_collectionViewFlags.updating = true;
	}

	private void endItemAnimations() {
		_updateCount++;
		CollectionViewData oldCollectionViewData = _collectionViewData;
		_collectionViewData = new CollectionViewData(this, _layout);

		_layout.invalidateLayout();
		_collectionViewData.prepareToLoadData();

		List<CollectionViewUpdateItem> removeUpdateItems = Lists.sortedList(this.arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction.DELETE), false);

		List<CollectionViewUpdateItem> insertUpdateItems = Lists.sortedList(this.arrayForUpdateAction(CollectionViewUpdateItem.CollectionUpdateAction.INSERT));

		List<CollectionViewUpdateItem> sortedMutableReloadItems = Lists.sortedList(_reloadItems);
		List<CollectionViewUpdateItem> sortedMutableMoveItems = Lists.sortedList(_moveItems);

		_originalDeleteItems = Lists.copy(removeUpdateItems);
		_originalInsertItems = Lists.copy(insertUpdateItems);

		List<CollectionViewUpdateItem> someMutableArr1 = new ArrayList<>();
		List<CollectionViewUpdateItem> someMutableArr2 = new ArrayList<>();
		List<CollectionViewUpdateItem> someMutableArr3 = new ArrayList<>();

		class Operation {
			int deleted = 0;
			int inserted = 0;
			int movedOut = 0;
			int movedIn = 0;
		}

		SparseArray<Operation> operations = new SparseArray<>();

		int oldNumberOfSections = oldCollectionViewData.numberOfSections();
		int newNumberOfSections = _collectionViewData.numberOfSections();

		for (CollectionViewUpdateItem updateItem : sortedMutableReloadItems) {
		    Assert.condition(
				updateItem.indexPathBeforeUpdate().section < oldNumberOfSections,
				"attempt to reload item (%s) that doesn't exist (there are only %d sections before update)",
				updateItem.indexPathBeforeUpdate(), oldCollectionViewData.numberOfSections()
			);

		    if (updateItem.indexPathBeforeUpdate().item != -1) {
		        Assert.condition(
					updateItem.indexPathBeforeUpdate().item < oldCollectionViewData.numberOfItemsInSection(updateItem.indexPathBeforeUpdate().section),
					"attempt to reload item (%s) that doesn't exist (there are only %d items in section %d before update)",
					updateItem.indexPathBeforeUpdate(),
					oldCollectionViewData.numberOfItemsInSection(updateItem.indexPathBeforeUpdate().section),
					updateItem.indexPathBeforeUpdate().section
				);
		    }

		    someMutableArr2.add(new CollectionViewUpdateItem(CollectionViewUpdateItem.CollectionUpdateAction.DELETE, updateItem.indexPathBeforeUpdate()));
		    someMutableArr3.add(new CollectionViewUpdateItem(CollectionViewUpdateItem.CollectionUpdateAction.INSERT, updateItem.indexPathAfterUpdate()));
		}

		List<CollectionViewUpdateItem> sortedDeletedMutableItems = Lists.sortedList(_deleteItems, false);
		List<CollectionViewUpdateItem> sortedInsertMutableItems = Lists.sortedList(_insertItems);

		for (CollectionViewUpdateItem deleteItem  : sortedDeletedMutableItems) {
		    if (deleteItem.isSectionOperation()) {
		        Assert.condition(
					deleteItem.indexPathBeforeUpdate().section < oldCollectionViewData.numberOfSections(),
					"attempt to delete section (%d) that doesn't exist (there are only %d sections before update)",
					deleteItem.indexPathBeforeUpdate().section,
					oldCollectionViewData.numberOfSections()
				);

		        for (CollectionViewUpdateItem moveItem  : sortedMutableMoveItems) {
		            if (moveItem.indexPathBeforeUpdate().section == deleteItem.indexPathBeforeUpdate().section) {
		                if (moveItem.isSectionOperation()) {
							Assert.condition(
								false,
								"attempt to delete and move from the same section %d", deleteItem.indexPathBeforeUpdate().section
							);
						} else {
							Assert.condition(
								false,
								"attempt to delete and move from the same section (%s)", moveItem.indexPathBeforeUpdate()
							);
						}
		            }
		        }
		    } else {
		        Assert.condition(
					deleteItem.indexPathBeforeUpdate().section < oldNumberOfSections,
					"attempt to delete item (%s) that doesn't exist (there are only %d sections before update)",
					deleteItem.indexPathBeforeUpdate(),
					oldCollectionViewData.numberOfSections()
				);

		        Assert.condition(
					deleteItem.indexPathBeforeUpdate().item < oldCollectionViewData.numberOfItemsInSection(deleteItem.indexPathBeforeUpdate().section),
					"attempt to delete item (%s) that doesn't exist (there are only %d items in section %d before update)",
					deleteItem.indexPathBeforeUpdate(),
					oldCollectionViewData.numberOfItemsInSection(deleteItem.indexPathBeforeUpdate().section),
					deleteItem.indexPathBeforeUpdate().section
				);

		        for (CollectionViewUpdateItem moveItem : sortedMutableMoveItems) {
		            Assert.condition(
						!deleteItem.indexPathBeforeUpdate().equals(moveItem.indexPathBeforeUpdate()),
		            	"attempt to delete and move the same item (%s)", deleteItem.indexPathBeforeUpdate()
					);
		        }

				if(operations.indexOfKey(deleteItem.indexPathBeforeUpdate().section) < 0) {
					operations.put(deleteItem.indexPathBeforeUpdate().section, new Operation());
				}

				operations.get(deleteItem.indexPathBeforeUpdate().section).deleted += 1;
		    }
		}

		for (int i = 0; i < sortedInsertMutableItems.size(); i++) {
		    CollectionViewUpdateItem insertItem = sortedInsertMutableItems.get(i);
		    IndexPath indexPath = insertItem.indexPathAfterUpdate();

		    boolean sectionOperation = insertItem.isSectionOperation();
		    if (sectionOperation) {
		        Assert.condition(
					indexPath.section < newNumberOfSections,
					"attempt to insert %d but there are only %d sections after update",
					indexPath.section, newNumberOfSections
				);

		        for (CollectionViewUpdateItem moveItem : sortedMutableMoveItems) {
		            if (moveItem.indexPathAfterUpdate().equals(indexPath)) {
		                if (moveItem.isSectionOperation()) {
							Assert.condition(false, "attempt to perform an insert and a move to the same section (%d)", indexPath.section);
						}
		            }
		        }

		        int j = i + 1;
		        while (j < sortedInsertMutableItems.size()) {
		            CollectionViewUpdateItem nextInsertItem = sortedInsertMutableItems.get(j);

		            if (nextInsertItem.indexPathAfterUpdate().section == indexPath.section) {
						Assert.condition(
							nextInsertItem.indexPathAfterUpdate().item < _collectionViewData.numberOfItemsInSection(indexPath.section),
							"attempt to insert item %d into section %d, but there are only %d items in section %d after the update",
							nextInsertItem.indexPathAfterUpdate().item,
							indexPath.section,
							_collectionViewData.numberOfItemsInSection(indexPath.section),
							indexPath.section
						);

						sortedInsertMutableItems.remove(j);
		            }
		            else break;
		        }
		    } else {
		        Assert.condition(
					indexPath.item < _collectionViewData.numberOfItemsInSection(indexPath.section),
					"attempt to insert item to (%s) but there are only %d items in section %d after update",
					indexPath,
					_collectionViewData.numberOfItemsInSection(indexPath.section),
					indexPath.section
				);

				if(operations.indexOfKey(indexPath.section) < 0) {
					operations.put(indexPath.section, new Operation());
				}

				operations.get(indexPath.section).inserted += 1;
		    }
		}

		for (CollectionViewUpdateItem sortedItem  : sortedMutableMoveItems) {
			IndexPath beforeIndexPath = sortedItem.indexPathBeforeUpdate();
			IndexPath afterIndexPath = sortedItem.indexPathAfterUpdate();

		    if (sortedItem.isSectionOperation()) {
		        Assert.condition(
					beforeIndexPath.section < oldCollectionViewData.numberOfSections(),
					"attempt to move section (%d) that doesn't exist (%d sections before update)",
					beforeIndexPath.section,
					oldCollectionViewData.numberOfSections()
				);

		        Assert.condition(
					afterIndexPath.section < newNumberOfSections,
					"attempt to move section to %d but there are only %d sections after update",
					afterIndexPath.section,
					newNumberOfSections
				);
		    } else {
				Assert.condition(
					beforeIndexPath.section < oldCollectionViewData.numberOfSections(),
					"attempt to move item (%s) that doesn't exist (%d sections before update)",
					sortedItem, oldCollectionViewData.numberOfSections()
				);

				Assert.condition(
					beforeIndexPath.item < oldCollectionViewData.numberOfItemsInSection(beforeIndexPath.section),
					"attempt to move item (%s) that doesn't exist (%d items in section %d before update)",
					sortedItem,
					oldCollectionViewData.numberOfItemsInSection(beforeIndexPath.section),
					beforeIndexPath.section
				);

				Assert.condition(
					afterIndexPath.section < newNumberOfSections,
					"attempt to move item to (%s) but there are only %d sections after update",
					afterIndexPath,
					newNumberOfSections
				);

				Assert.condition(
					afterIndexPath.item < _collectionViewData.numberOfItemsInSection(afterIndexPath.section),
					"attempt to move item to (%s) but there are only %d items in section %d after update",
					sortedItem,
					_collectionViewData.numberOfItemsInSection(afterIndexPath.section),
					afterIndexPath.section
				);
		    }

			if(operations.indexOfKey(beforeIndexPath.section) < 0) {
				operations.put(beforeIndexPath.section, new Operation());
			}

			if(operations.indexOfKey(afterIndexPath.section) < 0) {
				operations.put(afterIndexPath.section, new Operation());
			}

			operations.get(beforeIndexPath.section).movedOut += 1;
			operations.get(afterIndexPath.section).movedIn += 1;
		}

		int operationsSize = operations.size();
		for(int i = 0; i < operationsSize; i++) {
			int section = operations.keyAt(i);

		    int insertedCount = operations.get(section).inserted;
		    int deletedCount = operations.get(section).deleted;
		    int movedInCount = operations.get(section).movedIn;
		    int movedOutCount = operations.get(section).movedOut;

		    Assert.condition(
				oldCollectionViewData.numberOfItemsInSection(section) + insertedCount - deletedCount + movedInCount - movedOutCount == _collectionViewData.numberOfItemsInSection(section),
				"invalid update in section %d: number of items after update (%d) should be equal to the number of items before update (%d) plus count of inserted items (%d), minus count of deleted items (%d), plus count of items moved in (%d), minus count of items moved out (%d)",
				section,
				_collectionViewData.numberOfItemsInSection(section),
				oldCollectionViewData.numberOfItemsInSection(section),
				insertedCount, deletedCount, movedInCount, movedOutCount
			);
		}

		someMutableArr2.addAll(sortedDeletedMutableItems);
		someMutableArr3.addAll(sortedInsertMutableItems);
		someMutableArr1.addAll(Lists.sortedList(someMutableArr2, false));
		someMutableArr1.addAll(sortedMutableMoveItems);
		someMutableArr1.addAll(Lists.sortedList(someMutableArr3));

		List<CollectionViewUpdateItem> layoutUpdateItems = new ArrayList<>();
		layoutUpdateItems.addAll(sortedDeletedMutableItems);
		layoutUpdateItems.addAll(sortedMutableMoveItems);
		layoutUpdateItems.addAll(sortedInsertMutableItems);

		List<List<Integer>> newModel = new ArrayList<>();
		for (int i = 0; i < oldCollectionViewData.numberOfSections(); i++) {
			List<Integer> sectionArr = new ArrayList<>();

			int numberOfItems = oldCollectionViewData.numberOfItemsInSection(i);

			for (int j = 0; j < numberOfItems; j++) {
				sectionArr.add(oldCollectionViewData.globalIndexForItemInSection(j, i));
			}

		    newModel.add(sectionArr);
		}

		for (CollectionViewUpdateItem updateItem  : layoutUpdateItems) {
			IndexPath indexPathBefore = updateItem.indexPathBeforeUpdate();
			IndexPath indexPathAfter = updateItem.indexPathAfterUpdate();

		    switch (updateItem.updateAction()) {
		        case DELETE:
		            if (updateItem.isSectionOperation()) {
		                // section updates are ignored anyway in animation code. If not commented, mixing rows and section deletion causes crash in else below
		                // newModel.remove(indexPathBefore.getSection());
		            } else {
		                newModel.get(indexPathBefore.section).remove(indexPathBefore.item);
		            }

		            break;

		        case INSERT:
		            if (updateItem.isSectionOperation()) {
						newModel.add(indexPathAfter.section, new ArrayList<Integer>());
		            } else {
		                newModel.get(indexPathAfter.section).add(-1, indexPathAfter.item);
		            }

		            break;

		        case MOVE:
		            if (updateItem.isSectionOperation()) {
		                List<Integer> section = newModel.get(indexPathBefore.section);
		                newModel.add(indexPathAfter.section, section);
		            } else {
		                int object = oldCollectionViewData.globalIndexForItemAtIndexPath(indexPathBefore);
		                newModel.get(indexPathBefore.section).remove(object);
		                newModel.get(indexPathAfter.section).add(object, indexPathAfter.item);
		            }

		            break;

		        default:
					break;
		    }
		}

		int oldNumberOfItems = oldCollectionViewData.numberOfItems();
		int newNumberOfItems = _collectionViewData.numberOfItems();

		List<Integer> oldToNewMap = new ArrayList<>(oldNumberOfItems);
		List<Integer> newToOldMap = new ArrayList<>(newNumberOfItems);

		for (int i = 0; i < oldNumberOfItems; i++) {
			oldToNewMap.add(-1);
		}

		for (int i = 0; i < newNumberOfItems; i++) {
			newToOldMap.add(-1);
		}

		int newModelSize = newModel.size();
		for (int i = 0; i < newModelSize; i++) {
		    List<Integer> section = newModel.get(i);

		    for (int j = 0; j < section.size(); j++) {
		        int newGlobalIndex = _collectionViewData.globalIndexForItemAtIndexPath(IndexPath.withItemInSection(j, i));

		        if (section.get(j) != -1) {
					oldToNewMap.set(section.get(j), newGlobalIndex);
				}

				if (newGlobalIndex != -1) {
					newToOldMap.set(newGlobalIndex, section.get(j));
				}
		    }
		}

		_update = new UpdateTransaction();
		_update.oldModel = oldCollectionViewData;
		_update.newModel = _collectionViewData;
		_update.oldToNewIndexMap = oldToNewMap;
		_update.newToOldIndexMap = newToOldMap;

		this.updateWithItems(someMutableArr1);

		_originalInsertItems = null;
		_originalDeleteItems = null;
		_insertItems = null;
		_deleteItems = null;
		_moveItems = null;
		_reloadItems = null;
		_update = null;
		_updateCount--;
		_collectionViewFlags.updating = false;
		this.resumeReloads();
	}

	private void updateRowsAtIndexPathsUpdateAction(List<IndexPath> indexPaths, CollectionViewUpdateItem.CollectionUpdateAction updateAction) {
		boolean updating = _collectionViewFlags.updating;
		if (!updating) this.setupCellAnimations();

		List<CollectionViewUpdateItem> array = this.arrayForUpdateAction(updateAction); //returns appropriate empty array if not exists

		for (IndexPath indexPath : indexPaths) {
		    CollectionViewUpdateItem updateItem = new CollectionViewUpdateItem(updateAction, indexPath);
		    array.add(updateItem);
		}

		if (!updating) this.endItemAnimations();
	}

	private void updateSectionsUpdateAction(IndexSet sections, CollectionViewUpdateItem.CollectionUpdateAction updateAction) {
		boolean updating = _collectionViewFlags.updating;
		if (!updating) this.setupCellAnimations();

		List<CollectionViewUpdateItem> updateActions = this.arrayForUpdateAction(updateAction);

		for(Integer section : sections) {
			CollectionViewUpdateItem updateItem = new CollectionViewUpdateItem(updateAction, IndexPath.withItemInSection(-1, section));
			updateActions.add(updateItem);
		}

		if (!updating) this.endItemAnimations();
	}

	/* Setters & Getters */
	/* ========================================== */

	public CollectionViewLayout getCollectionViewLayout() {
		return this._layout;
	}

	public CollectionView.Delegate getDelegate() {
		return this._delegate;
	}

	public CollectionView.DataSource getDataSource() {
		return this._dataSource;
	}

	public mocha.ui.View getBackgroundView() {
		return this._backgroundView;
	}

	public boolean getAllowsSelection() {
		return this._allowsSelection;
	}

	public void setAllowsSelection(boolean allowsSelection) {
		this._allowsSelection = allowsSelection;
	}

	public boolean getAllowsMultipleSelection() {
		return this._allowsMultipleSelection;
	}

	private mocha.graphics.Rect getVisibleBoundRects() {
		if(this._visibleBoundRects != null) {
			return this._visibleBoundRects.copy();
		} else {
			return mocha.graphics.Rect.zero();
		}
	}

	private void setVisibleBoundRects(mocha.graphics.Rect visibleBoundRects) {
		if(this._visibleBoundRects != null) {
			this._visibleBoundRects = visibleBoundRects.copy();
		} else {
			this._visibleBoundRects = mocha.graphics.Rect.zero();
		}
	}

}

