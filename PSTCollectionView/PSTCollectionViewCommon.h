//
//  PSTCollectionViewCommon.h
//  PSPDFKit
//
//  Copyright (c) 2012 Peter Steinberger. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@class PSTCollectionView, PSTCollectionViewCell, PSTCollectionReusableView;

@protocol PSTCollectionViewDataSource <NSObject>
@required

- (NSInteger)collectionView:(PSTCollectionView *)collectionView numberOfItemsInSection:(NSInteger)section;

// The cell that is returned must be retrieved from a call to -dequeueReusableCellWithReuseIdentifier:forIndexPath:
- (PSTCollectionViewCell *)collectionView:(PSTCollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath;

@optional

- (NSInteger)numberOfSectionsInCollectionView:(PSTCollectionView *)collectionView;

// The view that is returned must be retrieved from a call to -dequeueReusableSupplementaryViewOfKind:withReuseIdentifier:forIndexPath:
- (PSTCollectionReusableView *)collectionView:(PSTCollectionView *)collectionView viewForSupplementaryElementOfKind:(NSString *)kind atIndexPath:(NSIndexPath *)indexPath;

@end

@protocol PSTCollectionViewDelegate <UIScrollViewDelegate>
@optional

// Methods for notification of selection/deselection and highlight/unhighlight events.
// The sequence of calls leading to selection from a user touch is:
//
// (when the touch begins)
// 1. -collectionView:shouldHighlightItemAtIndexPath:
// 2. -collectionView:didHighlightItemAtIndexPath:
//
// (when the touch lifts)
// 3. -collectionView:shouldSelectItemAtIndexPath: or -collectionView:shouldDeselectItemAtIndexPath:
// 4. -collectionView:didSelectItemAtIndexPath: or -collectionView:didDeselectItemAtIndexPath:
// 5. -collectionView:didUnhighlightItemAtIndexPath:
- (BOOL)collectionView:(PSTCollectionView *)collectionView shouldHighlightItemAtIndexPath:(NSIndexPath *)indexPath;
- (void)collectionView:(PSTCollectionView *)collectionView didHighlightItemAtIndexPath:(NSIndexPath *)indexPath;
- (void)collectionView:(PSTCollectionView *)collectionView didUnhighlightItemAtIndexPath:(NSIndexPath *)indexPath;
- (BOOL)collectionView:(PSTCollectionView *)collectionView shouldSelectItemAtIndexPath:(NSIndexPath *)indexPath;
- (BOOL)collectionView:(PSTCollectionView *)collectionView shouldDeselectItemAtIndexPath:(NSIndexPath *)indexPath; // called when the user taps on an already-selected item in multi-select mode
- (void)collectionView:(PSTCollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath;
- (void)collectionView:(PSTCollectionView *)collectionView didDeselectItemAtIndexPath:(NSIndexPath *)indexPath;

- (void)collectionView:(PSTCollectionView *)collectionView didEndDisplayingCell:(PSTCollectionViewCell *)cell forItemAtIndexPath:(NSIndexPath *)indexPath;
- (void)collectionView:(PSTCollectionView *)collectionView didEndDisplayingSupplementaryView:(PSTCollectionReusableView *)view forElementOfKind:(NSString *)elementKind atIndexPath:(NSIndexPath *)indexPath;

// These methods provide support for copy/paste actions on cells.
// All three should be implemented if any are.
- (BOOL)collectionView:(PSTCollectionView *)collectionView shouldShowMenuForItemAtIndexPath:(NSIndexPath *)indexPath;
- (BOOL)collectionView:(PSTCollectionView *)collectionView canPerformAction:(SEL)action forItemAtIndexPath:(NSIndexPath *)indexPath withSender:(id)sender;
- (void)collectionView:(PSTCollectionView *)collectionView performAction:(SEL)action forItemAtIndexPath:(NSIndexPath *)indexPath withSender:(id)sender;

@end

// Newer runtimes defines this, here's a fallback for the iOS5 SDK.
#ifndef NS_ENUM
#define NS_ENUM(_type, _name) _type _name; enum
#define NS_OPTIONS(_type, _name) _type _name; enum
#endif

// Category exists in iOS6.
#if __IPHONE_OS_VERSION_MIN_REQUIRED < 60000
@interface NSIndexPath (PSTCollectionViewAdditions)
+ (NSIndexPath *)indexPathForItem:(NSInteger)item inSection:(NSInteger)section;
@property (nonatomic, readonly) NSInteger item;
@end
#endif

// compatibility
#ifndef kCFCoreFoundationVersionNumber_iOS_6_0
#define kCFCoreFoundationVersionNumber_iOS_6_0 788.0
#endif

// imp_implementationWithBlock changed it's type in iOS6.
#if __IPHONE_OS_VERSION_MAX_ALLOWED < 60000
#define PSBlockImplCast (__bridge void *)
@interface NSDictionary(PSSubscriptingSupport)
- (id)objectForKeyedSubscript:(id)key;
@end
@interface NSMutableDictionary(PSSubscriptingSupport)
- (void)setObject:(id)obj forKeyedSubscript:(id <NSCopying>)key;
@end
@interface NSArray(PSSubscriptingSupport)
- (id)objectAtIndexedSubscript:(NSUInteger)idx;
@end
@interface NSMutableArray(PSSubscriptingSupport)
- (void)setObject:(id)obj atIndexedSubscript:(NSUInteger)idx;
@end
#else
#define PSBlockImplCast
#endif
