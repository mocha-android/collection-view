/**
 *  @author Shaun
 *  @date 4/17/14
 *  @copyright 2014 Mocha. All rights reserved.
 */
package mocha.ui.collectionview;

import mocha.foundation.IndexPath;
import mocha.foundation.MObject;
import mocha.foundation.Range;
import mocha.graphics.Point;
import mocha.graphics.Rect;
import mocha.graphics.Size;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CollectionViewFlowLayoutData extends MObject {
	private CollectionViewFlowLayout flowLayout;
	private CollectionViewFlowLayoutSection[] sections;
	private int numberOfItems;
	private boolean vertical;
	private int lastGetSectionsMiddleSection;

	public Size contentSize = new Size();

	public CollectionViewFlowLayoutData(CollectionViewFlowLayout flowLayout) {
		this.flowLayout = flowLayout;
	}

	public void reloadData() {
		CollectionView collectionView = this.flowLayout.getCollectionView();
		int numberOfSections = collectionView.numberOfSections();

		if(numberOfSections == 0) {
			this.sections = new CollectionViewFlowLayoutSection[0];
			return;
		}

		if(this.sections == null) {
			this.sections = new CollectionViewFlowLayoutSection[numberOfSections];
		} else if(this.sections.length != numberOfSections) {
			this.sections = Arrays.copyOf(this.sections, numberOfSections);
		}

		CollectionView.Delegate delegate = collectionView.getDelegate();
		CollectionViewFlowLayout.Delegate flowLayoutDelegate;

		if (delegate instanceof CollectionViewFlowLayout.Delegate) {
			flowLayoutDelegate = (CollectionViewFlowLayout.Delegate)delegate;
		} else {
			flowLayoutDelegate = null;
		}

		Rect bounds = collectionView.getBounds();
		bounds.origin.x = 0.0f;
		bounds.origin.y = 0.0f;

		this.vertical = this.flowLayout.scrollDirection == CollectionViewFlowLayout.ScrollDirection.VERTICAL;
		this.numberOfItems = 0;

		Point offset = Point.zero();

		for(int i = 0; i < numberOfSections; i++) {
			CollectionViewFlowLayoutSection section = this.sections[i];

			if(section == null) {
				section = new CollectionViewFlowLayoutSection();
				this.sections[i] = section;
			}

			if(flowLayoutDelegate != null && this.flowLayout.delegateInsetForSection) {
				section.edgeInsets.set(flowLayoutDelegate.collectionViewLayoutInsetForSectionAtIndex(collectionView, this.flowLayout, i));
			} else {
				section.edgeInsets.set(this.flowLayout.sectionInset);
			}

			if(flowLayoutDelegate != null && this.flowLayout.delegateInteritemSpacingForSection) {
				section.minimumInteritemSpacing = flowLayoutDelegate.collectionViewLayoutMinimumInteritemSpacingForSectionAtIndex(collectionView, this.flowLayout, i);
			} else {
				section.minimumInteritemSpacing = this.flowLayout.minimumInteritemSpacing;
			}

			if(flowLayoutDelegate != null && this.flowLayout.delegateLineSpacingForSection) {
				section.minimumLineSpacing = flowLayoutDelegate.collectionViewLayoutMinimumLineSpacingForSectionAtIndex(collectionView, this.flowLayout, i);
			} else {
				section.minimumLineSpacing = this.flowLayout.minimumLineSpacing;
			}

			if(flowLayoutDelegate != null && this.flowLayout.delegateReferenceSizeForHeader) {
				Size headerSize = flowLayoutDelegate.collectionViewLayoutReferenceSizeForHeaderInSection(collectionView, this.flowLayout, i);

				if(headerSize == null) {
					section.headerSize = 0.0f;
				} else {
					section.headerSize = this.vertical ? headerSize.height : headerSize.width;
				}
			} else {
				section.headerSize = this.vertical ? this.flowLayout.headerReferenceSize.height : this.flowLayout.headerReferenceSize.width;
			}

			if(flowLayoutDelegate != null && this.flowLayout.delegateReferenceSizeForFooter) {
				Size footerSize = flowLayoutDelegate.collectionViewLayoutReferenceSizeForFooterInSection(collectionView, this.flowLayout, i);

				if(footerSize == null) {
					section.footerSize = 0.0f;
				} else {
					section.footerSize = this.vertical ? footerSize.height : footerSize.width;
				}
			} else {
				section.footerSize = this.vertical ? this.flowLayout.footerReferenceSize.height : this.flowLayout.footerReferenceSize.width;
			}

			section.reloadData(this.flowLayout, collectionView, bounds, flowLayoutDelegate, offset, i, this.vertical);
			section.index = i;

			if(this.vertical) {
				offset.y = section.frame.maxY();
			} else {
				offset.x += section.frame.origin.x + section.frame.size.width - offset.x;
			}

			this.numberOfItems += section.numberOfItems;
		}

		this.contentSize.width = offset.x;
		this.contentSize.height = offset.y;
	}

	public List<CollectionViewLayoutAttributes> layoutAttributesForElementsInRect(Rect rect) {
		// TODO: Speed up with binary search

		List<CollectionViewLayoutAttributes> attributeses = new ArrayList<>();

		for(CollectionViewFlowLayoutSection section : this.sections) {
			if(!section.frame.intersects(rect)) continue;

			if(section.headerSize > 0 && section.headerFrame.intersects(rect)) {
				CollectionViewLayoutAttributes attributes = this.flowLayout.dequeueLayoutAttributesForSupplementaryViewOfKind(CollectionViewFlowLayout.ELEMENT_KIND_SECTION_HEADER, IndexPath.withItemInSection(0, section.index));
				attributes.setFrame(section.headerFrame);
				attributeses.add(attributes);
			}

			if(section.numberOfItems > 0) {
				for(int item = 0; item < section.numberOfItems; item++) {
					Rect frame = section.itemFrames[item];

					if(frame.intersects(rect)) {
						CollectionViewLayoutAttributes attributes = this.flowLayout.dequeueLayoutAttributesForCell(section.indexPaths[item]);
						attributes.setFrame(frame);
						attributeses.add(attributes);
					}
				}
			}

			if(section.footerSize > 0 && section.footerFrame.intersects(rect)) {
				CollectionViewLayoutAttributes attributes = this.flowLayout.dequeueLayoutAttributesForSupplementaryViewOfKind(CollectionViewFlowLayout.ELEMENT_KIND_SECTION_FOOTER, IndexPath.withItemInSection(0, section.index));
				attributes.setFrame(section.footerFrame);
				attributeses.add(attributes);
			}
		}

		return attributeses;
	}

	public CollectionViewLayoutAttributes layoutAttributesForItemAtIndexPath(IndexPath indexPath) {
		CollectionViewLayoutAttributes attributes = this.flowLayout.dequeueLayoutAttributesForCell(indexPath);
		attributes.setFrame(this.sections[indexPath.section].itemFrames[indexPath.row]);
		return attributes;
	}

	public CollectionViewLayoutAttributes layoutAttributesForSupplementaryViewOfKindAtIndexPath(String kind, IndexPath indexPath) {
		if(kind != null && kind.equals(CollectionViewFlowLayout.ELEMENT_KIND_SECTION_HEADER)) {
			CollectionViewFlowLayoutSection section = this.sections[indexPath.section];

			if(section.headerSize > 0) {
				CollectionViewLayoutAttributes attributes = this.flowLayout.dequeueLayoutAttributesForSupplementaryViewOfKind(kind, indexPath);
				attributes.setFrame(section.headerFrame);
				return attributes;
			}
		} else if(kind != null && kind.equals(CollectionViewFlowLayout.ELEMENT_KIND_SECTION_FOOTER)) {
			CollectionViewFlowLayoutSection section = this.sections[indexPath.section];

			if(section.footerSize > 0) {
				CollectionViewLayoutAttributes attributes = this.flowLayout.dequeueLayoutAttributesForSupplementaryViewOfKind(kind, indexPath);
				attributes.setFrame(section.footerFrame);
				return attributes;
			}
		}

		return null;
	}

	public Range getSectionsInRect(Rect visibleRect) {
		int numberOfSections = this.sections.length;

		if(numberOfSections < 2) {
			return new Range(0, numberOfSections);
		}

		int minSection = 0;
		int maxSection = numberOfSections - 1;

		int startAt = 0;

		if((this.vertical && visibleRect.origin.y > 0.0f) || (!this.vertical && visibleRect.origin.x > 0.0f)) {
			if(this.lastGetSectionsMiddleSection != -1 && visibleRect.intersects(this.sections[this.lastGetSectionsMiddleSection].frame)) {
				startAt = this.lastGetSectionsMiddleSection;
			} else {
				int mid = numberOfSections >>> 1;

				while(mid > 0 && mid < numberOfSections) {
					Rect rect = this.sections[mid].frame;

					if(rect.intersects(visibleRect)) {
						startAt = mid;
						break;
					} else {
						int mid1;

						if(this.vertical && (rect.origin.y + rect.size.height < visibleRect.origin.y)) {
							minSection = mid;
							mid1 = (mid + maxSection) >>> 1;
						} else if(!this.vertical && (rect.origin.x + rect.size.width < visibleRect.origin.x)) {
							minSection = mid;
							mid1 = (mid + maxSection) >>> 1;
						} else {
							maxSection = mid;
							mid1 = (minSection + mid) >>> 1;
						}

						if(mid1 == mid) {
							startAt = mid;
							break;
						} else {
							mid = mid1;
						}
					}
				}
			}
		}

		minSection = 0;

		for(int section = startAt; section > 0; section--) {
			Rect rect = this.sections[section].frame;

			if(this.vertical) {
				if (rect.origin.y < visibleRect.origin.y) {
					minSection = section;
					break;
				}
			} else {
				if (rect.origin.x < visibleRect.origin.x) {
					minSection = section;
					break;
				}
			}
		}

		maxSection = numberOfSections - 1;

		Point max = visibleRect.max();
		for(int section = startAt; section < numberOfSections; section++) {
			Rect rect = this.sections[section].frame;

			if(this.vertical) {
				if (rect.origin.y + rect.size.height > max.y) {
					maxSection = section;
					break;
				}
			} else {
				if (rect.origin.x + rect.size.width > max.x) {
					maxSection = section;
					break;
				}
			}
		}

		this.lastGetSectionsMiddleSection = (minSection + maxSection) >>> 1;

		return new Range(minSection, (maxSection - minSection) + 1);
	}

	public int getSectionAtPoint(Point point) {
		int numberOfSections = this.sections.length;

		// TODO: Speed up with binary search

		for(int i = 0; i < numberOfSections; i++) {
			CollectionViewFlowLayoutSection section = this.sections[i];

			if(this.vertical) {
				if (point.y <= section.frame.origin.y) {
					return i - 1;
				}
			} else {
				if (point.x <= section.frame.origin.x) {
					return i - 1;
				}
			}
		}

		if(numberOfSections > 0) {
			CollectionViewFlowLayoutSection section = this.sections[numberOfSections - 1];

			if(this.vertical) {
				if (point.y < section.frame.origin.y + section.frame.size.height) {
					return numberOfSections - 1;
				}
			} else {
				if (point.x < section.frame.origin.x + section.frame.size.width) {
					return numberOfSections - 1;
				}
			}
		}

		return -1;
	}
}
