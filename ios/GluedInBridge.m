//
//  GluedInBridge.m
//  test
//
//  Created by Amit Choudhary on 08/03/25.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(GluedInBridge, NSObject)

RCT_EXTERN_METHOD(launchSDK:(NSString *)apiKey
                  secretKey:(NSString *)secretKey
                  email:(NSString *)email
                  password:(NSString *)password
                  fullName:(NSString *)fullName
                  persona:(NSString *)persona
                  callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(initializeSDKOnLaunch:(NSString *)apiKey secretKey:(NSString *)secretKey callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(performLogin:(NSString *)username password:(NSString *)password callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(performSignup:(NSString *)name email:(NSString *)email password:(NSString *)password username:(NSString *)username callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(getDiscoverSearchInAllVideoRails:(NSString *)searchText callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(handleClickedEvents:(NSString *)event eventID:(NSString *)eventID callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(
                  userDidTapOnFeed:(NSInteger)index
                  type:(NSString *)type
                  feed:(NSDictionary *)feed
                  feedRailData:(NSArray *)feedRailData
                  apiKey:(NSString *)apiKey
                  secretKey:(NSString *)secretKey
                  email:(NSString *)email
                  password:(NSString *)password
                  fullName:(NSString *)fullName
                  persona:(NSString *)persona
                  callback:(RCTResponseSenderBlock)callback
                  )
// Method For Microcommunity
RCT_EXTERN_METHOD(initWithUserInfo:(NSString *)apiKey secretKey:(NSString *)secretKey email:(NSString *)email password:(NSString *)password fullName:(NSString *)fullName callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(widgetDetailWithFeed:(NSString *)byAssetId callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(launchSDKFromMicrocommunity:(NSString *)assetId assetName: (NSString *)assetName discountPrice: (double *)discountPrice  imageURL: (NSString *)imageURL discountEndDate: (NSString *)discountEndDate discountStartDate: (NSString *)discountStartDate callToAction: (NSString *)callToAction mrp: (double *)mrp shoppableLink: (NSString *)shoppableLink currencySymbol: (NSString *)currencySymbol contextId: (NSString *)contextId callback:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(launchSDKWithCreator:(NSString *)byAssetId assetName: (NSString *)assetName discountPrice: (double *)discountPrice imageURL: (NSString *)imageURL discountEndDate: (NSString *)discountEndDate discountStartDate: (NSString *)discountStartDate callToAction: (NSString *)callToAction mrp: (NSString *)mrp shoppableLink: (NSString *)shoppableLink currencySymbol: (NSString *)currencySymbol challenge: (NSString *)challenge isRewardCallback:(NSString *)isRewardCallback callback: (RCTResponseSenderBlock) callback)
RCT_EXTERN_METHOD(
                  launchSDKFromLeaderboard:(NSString *)assetId
                  challangeInfo:(NSDictionary *)challangeInfo
                  callback:(RCTResponseSenderBlock)callback
                  )

RCT_EXTERN_METHOD(
                  launchSDKFromReward:(NSString *)assetId
                  challangeInfo:(NSDictionary *)challangeInfo
                  callback:(RCTResponseSenderBlock)callback
                  )

RCT_EXTERN_METHOD(
                  getTrandingRailResult:(NSString *)apiKey
                  secretKey:(NSString *)secretKey
                  railId:(NSString *)railId
                  callback:(RCTResponseSenderBlock)callback
                  )
@end
