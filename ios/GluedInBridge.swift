//
//  GluedInBridge.swift
//  Demo
//
//  Created by Amit Choudhary on 16/12/24.
//

import Foundation
import React
internal import GluedInSDK
internal import GluedInCoreSDK
internal import GluedInCreatorSDK
internal import GluedInFeedSDK
import UIKit

//@objc(GluedInBridge)
@objc(GluedInBridge) // ✅ Ensure this annotation is present

class GluedInBridge: RCTEventEmitter {
  
  //MARK: - Supported events for callback-
  override func supportedEvents() -> [String]! {
    return ["onSignInClick", "onSignUpClick"]
  }
  
  //MARK: - Initialize SDK on Launch -
  @objc
  func initializeSDKOnLaunch(_ apiKey: String, secretKey: String, callback: @escaping RCTResponseSenderBlock) {
    GluedInCore.shared.initSdk(
      apiKey: apiKey,
      secretKey: secretKey
    ) {
      callback([NSNull(), "Initialize successful"])
    } failure: { error, code in
      callback([error, NSNull()])
    }
  }
  
  //MARK: - Initialize SDK with user info -
  @objc
  func initWithUserInfo(
    _ apiKey: String,
    secretKey: String,
    email:String,
    password: String,
    fullName: String,
    callback: @escaping RCTResponseSenderBlock
  ) {
    GluedIn.shared.initWithUserInfo(
      apiKey: apiKey,
      secretKey: secretKey,
      email: email,
      password: password,
      fullName: fullName,
      personaType: ""
    ) {
      callback([NSNull(), "Initialize successful"])
    } failure: { error, code in
      callback([error, NSNull()])
    }
  }
  
  //widgetDetailWithFeed
  //MARK: - widget Detail With Feed -
  @objc
  func widgetDetailWithFeed(
    _ byAssetId: String,
    callback: @escaping RCTResponseSenderBlock
  ) {
    GluedIn.shared.widgetDetailWithFeed(byAssetId: byAssetId) { WidgetResponse, FeedDataModel in
      let dataToResponse = [
        "WidgetResponse": WidgetResponse?.toJSON(),
        "FeedDataModel": FeedDataModel?.toJSON(),
        "isRewardEnable": GluedInCore.shared.isRewardEnable()
      ]
      callback([NSNull(), dataToResponse])
    } failure: { error, code in
      callback([error ?? "", NSNull()])
    }
  }
  
  //MARK: - widget Detail With Feed -
  @objc
  func getTrandingRailResult(
    _ apiKey: String,
    secretKey: String,
    railId: String,
    callback: @escaping RCTResponseSenderBlock
  ) {
    GluedInCore.shared.initSdk(
      apiKey: apiKey,
      secretKey: secretKey
    ) {
      DiscoverData.sharedInstance.getCuratedRailDetails(railId: railId) { railDataResponse in
        let dataToResponse = ["railResponse": railDataResponse.toJSON()]
        callback([NSNull(), dataToResponse])
      } failure: { error, code in
        print(error)
      }
    } failure: { error, code in
      print(error)
    }
  }
  
  @objc
  func launchSDKFromMicrocommunity(
    _ assetId: String,
    assetName: String,
    discountPrice: Double,
    imageURL: String,
    discountEndDate: String,
    discountStartDate: String,
    callToAction: String,
    mrp: Double,
    shoppableLink: String,
    currencySymbol: String,
    contextId: String,
    callback: @escaping RCTResponseSenderBlock
  ) {
    let item: Asset = Asset(
      id: assetId,
      assetName: assetName,
      discountPrice: discountPrice,
      imageUrl: imageURL,
      discountEndDate: discountEndDate,
      discountStartDate: discountStartDate,
      callToAction: callToAction,
      mrp: mrp,
      shoppableLink: shoppableLink,
      currencySymbol: currencySymbol
    )
    GluedIn.shared.launchSDK(
        typeOfEntry: .subFeed,
        assets: item,
        challenge: nil,
        contextType: .asset,
        contextId: assetId,
        selectedContentId: contextId,
        challengeInfo: nil,
        isRewardCallback: false,
        delegate: self
    ) { [weak self] controller in
      guard let self = self else { return }
      guard let viewController = controller else { return }
      self.present(viewController)
    } failure: { error in
      print(error)
    }
  }

  /// Launch SDK from  the leaderboard:
  ///
  @objc
  func launchSDKFromLeaderboard(
    _ assetId: String,
    challangeInfo: [String: Any],
    callback: @escaping RCTResponseSenderBlock
  ) {
    let challangeInforData: HashtagChallengeModel? = HashtagChallengeModel.init(JSON: challangeInfo)
    GluedIn.shared.launchSDK(
      typeOfEntry: .leaderboard,
      challengeInfo: challangeInforData,
      delegate: self
    ) { [weak self] controller in
      guard let self = self else { return }
      guard let viewController = controller else { return }
      self.present(viewController)
    } failure: { error in
      print(error)
    }
  }

  @objc
  func launchSDKFromReward(
    _ assetId: String,
    challangeInfo: [String: Any],
    callback: @escaping RCTResponseSenderBlock
  ) {
    GluedIn.shared.launchSDK(
      typeOfEntry: .reward,
      isRewardCallback: false,
      delegate: self
    ) { [weak self] controller in
      guard let self = self else { return }
      if let viewController = controller {
        self.present(viewController)
      } else {
        callback(["Launch controller is nil", NSNull()])
      }
    } failure: { error in
      print(error)
    }
  }
  
  //MARK: - widget Detail With Feed -
  @objc
  func launchSDKWithCreator(
    _ byAssetId: String,
    assetName: String,
    discountPrice: Double,
    imageURL: String,
    discountEndDate: String,
    discountStartDate: String,
    callToAction: String,
    mrp: Double,
    shoppableLink: String,
    currencySymbol: String,
    challenge: String,
    isRewardCallback:String,
    callback: @escaping RCTResponseSenderBlock
  ) {
    let item = Asset(
      id: byAssetId,
      assetName: assetName,
      discountPrice: discountPrice,
      imageUrl: imageURL,
      discountEndDate: discountEndDate,
      discountStartDate: discountStartDate,
      callToAction: callToAction,
      mrp: mrp,
      shoppableLink: shoppableLink,
      currencySymbol: currencySymbol
    )

    GluedIn.shared.launchSDK(
      typeOfEntry: .creator,
      assets: item,
      challenge: challenge,
      isRewardCallback: false,
      delegate: self
    ) { [weak self] viewController in
      guard let self = self else { return }
      guard let controller = viewController else { return }
      self.present(controller)
    } failure: { error in
      print(error)
    }
  }
  
  //MARK: - Launch SDK as guest -
  @objc
  func launchSDK(
    _ apiKey: String,
    secretKey: String,
    email: String,
    password: String,
    fullName: String,
    persona: String,
    callback: @escaping RCTResponseSenderBlock
  ) {
    GluedIn.shared.initSdk(apiKey: apiKey, secretKey: secretKey) { [weak self] in
      guard let self = self else { return }
      self.launchGluedIn(
        email: email,
        password: password,
        fullName: fullName,
        persona: persona,
        onlyShortsSubFeed: false,
        callback: callback
      )
    } failure: { error, code in
      callback([error, NSNull()])
    }
  }
  
  //MARK: - Perform Login -
  @objc
  func performLogin(_ username: String, password: String, callback: @escaping RCTResponseSenderBlock) {
    launchSDKwithUserName(email: username, password: password) { isSuccess, errorMessage in
      if isSuccess{
        callback([NSNull(), "Login successful"])
      }else{
        callback([errorMessage, NSNull()])
      }
    }
  }
  
  //MARK: - Perform Signup-
  @objc
  func performSignup(_ name: String, email: String, password: String, username: String, callback: @escaping RCTResponseSenderBlock) {
    registerUser(fullName: name, username: username, email: email, password: password) { isSuccess, errorMessage in
      if isSuccess{
        callback([NSNull(), "register success"])
      }else{
        callback([errorMessage, NSNull()])
      }
    }
  }
  
  //MARK: - User clicked on Hashtag, challenges -
  @objc
  func handleClickedEvents(_ event: String, eventID: String, callback: @escaping RCTResponseSenderBlock) {
    
    switch EventTypes(rawValue: event){
    case .hashTagClick:
      print("hashtag click with ID======>",eventID)
    case .challengeClick:
      print("challenge click with ID=====->",eventID)
    case .none:
      print("none")
    }
  }
    
  //MARK: - SubFeed from the Rail list with selected Item -
  @objc func userDidTapOnFeed(
    _ index: Int,
    type: String,
    feed: NSDictionary,
    feedRailData: [NSDictionary],
    apiKey: String,
    secretKey: String,
    email: String,
    password: String,
    fullName: String,
    persona: String,
    callback: @escaping RCTResponseSenderBlock
  ) {
    GluedInCore.shared.initSdk(apiKey: apiKey, secretKey: secretKey) {
      switch type {
      case "series":
        guard let assetId = feed["assetId"] as? String else {
          callback(["Missing assetId in feed", NSNull()])
          return
        }
        self.launchGluedIn(
          email: email,
          password: password,
          fullName: fullName,
          persona: persona,
          selectedRailContentId: nil,
          railContentIds: nil,
          seriesId: assetId,
          onlyShortsSubFeed: true,
          callback: callback
        )
        
      case "videos":
        let itemIds = feedRailData.compactMap { $0["id"] as? String }
        let videoId = index < itemIds.count ? itemIds[index] : nil
        
        self.launchGluedIn(
          email: email,
          password: password,
          fullName: fullName,
          persona: persona,
          selectedRailContentId: videoId,
          railContentIds: itemIds,
          seriesId: nil,
          onlyShortsSubFeed: false,
          callback: callback
        )
        
      default:
        callback(["Unsupported feed type: \(type)", NSNull()])
      }
    } failure: { error, code in
      callback([error, NSNull()])
    }
  }
  
  private func present(_ viewController: UIViewController) {
    DispatchQueue.main.async {
      guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
        return
      }
      if let navVC = appDelegate.window.rootViewController as? UINavigationController {
        navVC.pushViewController(viewController, animated: true)
      }
      else if let navVC = appDelegate.window.rootViewController {
        let nvController = UINavigationController(rootViewController:  viewController)
        nvController.modalPresentationStyle = .fullScreen // Ensure full-screen presentation
        navVC.present(nvController, animated: true)
      }
    }
  }
  
  private func launchGluedIn(
    email: String,
    password: String,
    fullName: String,
    persona: String,
    selectedRailContentId: String? = nil,
    railContentIds: [String]? = nil,
    seriesId: String? = nil,
    onlyShortsSubFeed: Bool,
    callback: @escaping RCTResponseSenderBlock
  ) {
    GluedIn.shared.quickLaunch(
      email: email,
      password: password,
      firebaseToken: "",
      fullName: fullName,
      autoCreate: true,
      termConditionAccepted: true,
      userType: "",
      personaType: persona,
      adsParameter: nil,
      selectedRailContentId: selectedRailContentId,
      railContentIds: railContentIds,
      onlyShortsSubFeed: onlyShortsSubFeed,
      delegate: self,
      seriesId: seriesId,
      selectedEpisodeNumber: nil
    ) { [weak self] controller in
      guard let self = self else { return }
      if let vc = controller {
        self.present(vc)
        callback([NSNull(), ["status": "success"]])
      } else {
        callback(["Launch controller is nil", NSNull()])
      }
    } failure: { error, code in
      Debug.Log(message: error)
      callback([error, NSNull()])
    }
  }
  
  //MARK: - Fetch curated rails-
  @objc
  func getDiscoverSearchInAllVideoRails(_ searchText: String, callback: @escaping RCTResponseSenderBlock) {
    DiscoverData.sharedInstance.getCuratedRailList(limit: 50, offset: 1) { curationData in
      print("curationData",curationData.toJSON())
      if curationData.result != nil{
        //        print("get disciver model data",videosModel.result)
        callback([NSNull(), curationData.toJSON()])
      }else{
        callback(["No data found", NSNull()])
      }
    } failure: { errorMessage, code in
      callback([errorMessage, NSNull()])
    }
  }
  
  //MARK: - open react natve controller and pass event-
  func openParentClassWithEvent(event: String, body: Any){
    guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
      return
    }

  }

  // launch with email and username
  func launchSDKwithUserName(
    email: String,
    password: String,
    completion: @escaping((_ isSuccess: Bool,_ errorMessage: String)-> Void)
  ){
    GluedIn.shared.quickLaunch(
      email: email,
      password: password,
      firebaseToken: "",
      fullName: "",
      autoCreate: true,
      termConditionAccepted: true,
      userType: "SVOD",
      personaType: "",
      adsParameter: nil,
      delegate: self
    ) { controller in
      guard let viewController = controller else { return }
      DispatchQueue.main.async {
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
          return
        }
        if let navVC = appDelegate.window.rootViewController as? UINavigationController{
          navVC.pushViewController(viewController, animated: true)
        }
      }
      completion(true,"")
    } failure: { error, code in
      completion(false,error)
    }
  }
  
  //  signup on SDK
  func registerUser(
    fullName: String,
    username: String,
    email: String,
    password: String,
    completion: @escaping((_ isSuccess: Bool,_ errorMessage: String)-> Void)
  ){
    Auth.sharedInstance.registerUser(
      fullName: fullName,
      email: email,
      password: password,
      userName: username,
      termConditionAccepted: true,
      invitationCode: ""
    ) { message in
      completion(true,"")
    } failure: { error, code in
      completion(false,error)
    }
  }
    
  func launchSDKAsGuest(completion: @escaping ((_ isSuccess: Bool, _ errorMessage: String) -> Void)) {
      if let controller = GluedIn.shared.rootControllerWithSignIn(
          userType: "SVOD",
          adsParameter: nil,
          delegate: self) {
          
        DispatchQueue.main.async {
          guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            return
          }
          if let navVC = appDelegate.window.rootViewController as? UINavigationController{
            navVC.pushViewController(controller, animated: true)
          }
          else if let navVC = appDelegate.window.rootViewController{
            let nvController = UINavigationController(rootViewController:  controller)
            //navVC.present(nvController, animated: true)
            nvController.modalPresentationStyle = .fullScreen // Ensure full-screen presentation
            navVC.present(nvController, animated: true)
          }
          completion(true, "")
        }
      } else {
          completion(false, "Failed to get controller from GluedIn SDK")
      }
  }
  
  //MARK: - get top controller -
  func topViewController(base: UIViewController? = UIApplication.shared.windows.first?.rootViewController) -> UIViewController? {
    if let navigationController = base as? UINavigationController {
      return topViewController(base: navigationController.visibleViewController)
    } else if let tabBarController = base as? UITabBarController {
      if let selected = tabBarController.selectedViewController {
        return topViewController(base: selected)
      }
    } else if let presented = base?.presentedViewController {
      return topViewController(base: presented)
    }
    return base
  }
  
}

// For FaceBook Share callbacks methods
extension GluedInBridge: CreatorProtocol {
  func progressPostingVideo(progressValue: Int) {
    
  }
  
  func contentSocialShare(contentURL: String, title: String, description: String, thumbnailImage: UIImage) {
    print(" contentSocialShare Method ")
  }
}

//MARK: - Extension -
extension GluedInBridge : GluedInDelegate{
  func onAnalyticsEvent(name: String, properties: [String : Any]) {
    
  }
  
  func requestForBannerAds(viewController: UIViewController?, adsType: GluedInCoreSDK.AdsType, adUnitID: String?, customParmas: [GluedInCoreSDK.GAMExtraParams]?) -> UIView? {
    return nil
  }
  
  func requestForInterstitialAds(viewController: UIViewController?, adsType: GluedInCoreSDK.AdsType, adUnitID: String?, customParmas: [GluedInCoreSDK.GAMExtraParams]?) {
    
  }
  
  func requestForAdmobNativeAds(viewController: UIViewController?, adUnitID: String?, adsType: GluedInCoreSDK.AdsType, customParmas: [GluedInCoreSDK.GAMExtraParams]?) {
    
  }
  
  func getAdmobNativeAdsController() -> UIViewController? {
    return nil
  }
  
  func requestForGamNativeAds(adUnitID: String?, adsType: GluedInCoreSDK.AdsType, configParams: [String : String]?, extraParams: [GluedInCoreSDK.GAMExtraParams]?, adsFormatId: [String]?) {
    
  }
  
  func getGamNativeAdsController() -> UIViewController? {
    return nil
  }
  
  func appContentSwipeEvent(eventName: String?, params: [String : Any]?) {
    
  }
  
  func didClickProductAction(feed: GluedInCoreSDK.FeedModel?, product: GluedInCoreSDK.ShoppableProduct?, eventRefId: Int, navigationController: UINavigationController) {
    
  }
  
  func onPaywallActionClicked(seriesId: String?, currentEpisode: Int?, deeplink: String?, navigationController: UINavigationController?) {
    
  }
  
  func getNativeAdController() -> UIViewController? {
    return UIViewController()
  }
  
  
  func onGluedInShareAction(shareData: GluedInCoreSDK.ShareData, viewController: UIViewController?) {
    print("onGluedInShareAction")
  }
  
  func onPostKeepShoppingClick(navigationController: UINavigationController?) {
    print("GluedInDelegate onPostKeepShoppingClick Method ")
  }
    
  func firebaseAnalyticsEvent(name: String, properties: [String : Any]) {
    // Write code for Firebase analytics
  }
  
  func appScreenViewEvent(
    journeyEntryPoint: String,
    deviceID: String, userEmail: String,
    userName: String, userId: String,
    version: String,
    platformName: String,
    pageName: String
  ) {
  }
  
  func appViewMoreEvent(Journey_entry_point: String, device_ID: String, user_email: String, user_name: String, platform_name: String, page_name: String, tab_name: String, element: String, button_type: String) {
    //
  }
  
  func appContentUnLikeEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appContentLikeEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appVideoReplayEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appSessionEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appVideoPlayEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appVideoPauseEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appVideoResumeEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appCommentsViewedEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appCommentedEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appContentNextEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appContentPreviousEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appVideoStopEvent(device_ID: String, content_id: String, user_email: String, user_name: String, user_id: String, platform_name: String, page_name: String, tab_name: String, creator_userid: String, creator_username: String, hashtag: String, content_type: String, gluedIn_version: String, played_duration: String, content_creator_id: String, dialect_id: String, dialect_language: String, genre: String, genre_id: String, shortvideo_labels: String, video_duration: String, feed: GluedInCoreSDK.FeedModel?) {
    //
  }
  
  func appViewClickEvent(device_ID: String, user_email: String, user_name: String, user_id: String, platform_name: String, page_name: String, tab_name: String, content_type: String, button_type: String, cta_name: String, gluedIn_version: String, feed: GluedInCoreSDK.FeedModel?) {
    //
  }
  
  func appUserFollowEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appUserUnFollowEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appCTAClickedEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appProfileEditEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appExitEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appClickHashtagEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appClickSoundTrackEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appContentMuteEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appContentUnmuteEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func didSelectBack() {
    //
  }
  
  func onGluedInShareAction(shareData: GluedInCoreSDK.ShareData) {
    //
  }
  
  func appSkipLoginEvent(device_ID: String, platform_name: String, page_name: String) {
    //
  }
  
  func didSelectParentApp() {
    //
  }
  
  func appTabClickEvent(Journey_entry_point: String, device_ID: String, user_email: String, user_name: String, user_id: String, platform_name: String, page_name: String, tab_name: String, button_type: String, cta_name: String, gluedIn_version: String, played_duration: String, content_creator_id: String, video_duration: String) {
    //
  }
  
  func appRegisterCTAClickEvent(device_ID: String, user_email: String, user_name: String, user_isFollow: String, user_following_count: String, user_follower_count: String, platform_name: String, page_name: String) {
    //
  }
  
  func appLoginCTAClickEvent(device_ID: String, user_email: String, user_name: String, user_id: String, user_isFollow: String, user_following_count: String, user_follower_count: String, platform_name: String, page_name: String, tab_name: String, button_type: String, cta_name: String, gluedIn_version: String, content_creator_id: String, video_duration: String) {
    //
  }
  
  func callClientSignInView() {
    self.openParentClassWithEvent(event: "onSignInClick", body: ["message":"now open sigin view controller"])
  }
  
  func callClientSignUpView() {
    self.openParentClassWithEvent(event: "onSignUpClick", body: ["message":"now open signup view controller"])
  }
  
  func appThumbnailClickEvent(device_ID: String, user_email: String, user_name: String, user_id: String, platform_name: String, page_name: String, tab_name: String, vertical_index: String, horizontal_index: String, element: String, content_type: String, content_genre: String, button_type: String, cta_name: String, gluedIn_version: String, content_creator_id: String, shortvideo_labels: String) {
    //
  }
  
  func appLaunchEvent(email: String, username: String, userId: String, version: String, deviceID: String, platformName: String) {
    //
  }
  
  func appChallengeJoinEvent(page_name: String, tab_name: String, element: String, button_type: String) {
    //
  }
  
  func appSearchButtonClickEvent(eventName: String?, params: [String : Any]?) {
    //
  }
  
  func appChallengeShareClickEvent(device_ID: String, user_email: String, user_name: String, platform_name: String, page_name: String, tab_name: String, element: String, button_type: String, success: String, failure_reason: String, creator_userid: String, creator_username: String) {
    //
  }
  
  func appCreatorRecordingDoneEvent() {
    //
  }
  
  func appCameraOpenEvent() {
    //
  }
  
  func appCreatorFilterAddedEvent() {
    //
  }
  
  func appCreatorMusicAddedEvent() {
    ///
  }
  
  func appCTAsClickEvent(device_ID: String, user_email: String, user_name: String, user_id: String, platform_name: String, page_name: String, tab_name: String, element: String, button_type: String, success: String, failure_reason: String, cta_name: String, gluedIn_version: String, played_duration: String, content_creator_id: String, video_duration: String) {
    //
  }
  
  func appPopupLaunchEvent(device_ID: String, user_email: String, user_name: String, platform_name: String, page_name: String, tab_name: String, popup_name: String, cta_name: String, user_id: String, gluedIn_version: String, content_creator_id: String, video_duration: String) {
    //
  }
  
  func appPopupCTAsEvent(device_ID: String, user_email: String, user_name: String, user_id: String, platform_name: String, page_name: String, tab_name: String, element: String, button_type: String, popup_name: String, cta_name: String, gluedIn_version: String, played_duration: String, content_creator_id: String, video_duration: String) {
    //
  }
  
  func appCreatePostEvent(device_ID: String, user_email: String, user_name: String, platform_name: String, page_name: String, tab_name: String, element: String, button_type: String, success: String, failure_reason: String, creator_userid: String, creator_username: String, hashtag: String, content_type: String, content_genre: String) {
    //
  }
  
  func appViewLeaderboardEvent(Journey_entry_point: String, device_ID: String, user_email: String, user_name: String, platform_name: String, page_name: String, tab_name: String, element: String, button_type: String) {
    //
  }
  
  func appUseThisHashtagEvent(device_ID: String, user_email: String, user_name: String, platform_name: String, page_name: String, tab_name: String, element: String, button_type: String, hashtag: String, content_type: String, content_genre: String) {
    //
  }
  
  func onUserProfileClick(userId: String) {
    //
  }
  
  // MARK: ads call backs and Integration methods:
  /// Native Ads
  func requestForAds(
          feed: FeedModel?,
          genre: [String]?,
          dialect: [String]?,
          originalLanguage: [String]?,
          extraParams: [GAMExtraParams]?,
          adsId: String?,
          adsFormatId: [String]?
      ) {

      }
      
  // MARK: Banner Ads
func requestForBannerView(viewController: UIViewController?) -> UIView {
  
  let vwCell = UIView()
  return vwCell
}

    // MARK: Intertital Ads integation
func requestForAdsInter(view: UIViewController) {

}

func getNativeAdControllerInter(view: UIViewController) {

}

func getNativeAdNibName() -> String {
  
  return "UnifiedNativeAdCell"
}

  // MARK: Card based native Ads
  
//  func requestNativeAdCell() -> UITableViewCell {
//    let nativeAdCell = UITableViewCell()  //UnifiedNativeAdCell(style: .default, reuseIdentifier: "UnifiedNativeAdCellIdentifier")
//    return nativeAdCell
//  }

  func requestNativeAdCell() -> UITableViewCell {
    // Write code for Native Ad on card based feed cell
    // It will return the table view cell which GlueDIn use with in the cell
    let nativeAdCell = UITableViewCell()  //UnifiedNativeAdCell(style: .default, reuseIdentifier: "UnifiedNativeAdCellIdentifier")

//      let nativeAdCell = UnifiedNativeAdCell(style: .default, reuseIdentifier: "UnifiedNativeAdCellIdentifier")
//      // Access the adView directly since it is now initialized programmatically in the UnifiedNativeAdCell class
//      let adView = nativeAdCell.adView
//      // Configure the ad cell with a GADNativeAd
//      nativeAdCell.nativeAdsLoader(requiredNumberOfAds: 1) { nativeAd in
//          //nativeAd.rootViewController = self
//          adView?.nativeAd = nativeAd
//          adView?.mediaView?.mediaContent = nativeAd?.mediaContent
//          if let mediaView = adView?.mediaView,
//             nativeAd?.mediaContent.aspectRatio ?? 0 > 0 {
//              let heightConstraint = NSLayoutConstraint(
//                  item: mediaView,
//                  attribute: .height,
//                  relatedBy: .equal,
//                  toItem: mediaView,
//                  attribute: .width,
//                  multiplier: CGFloat(1 / (nativeAd?.mediaContent.aspectRatio ?? 1)),
//                  constant: 0)
//              heightConstraint.isActive = true
//          }
//          (adView?.headlineView as? UILabel)?.text = nativeAd?.headline
//          adView?.headlineView?.isHidden = nativeAd?.headline == nil
//
//          (adView?.bodyView as? UILabel)?.text = nativeAd?.body
//          adView?.bodyView?.isHidden = nativeAd?.body == nil
//
//          adView?.callToActionView?.layer.cornerRadius = (adView?.callToActionView?.frame.height ?? 0) / 2
//          (adView?.callToActionView as? UIButton)?.setTitleColor(.white, for: .normal)
//          adView?.callToActionView?.backgroundColor = .blue
//          (adView?.callToActionView as? UIButton)?.setTitle(nativeAd?.callToAction, for: .normal)
//          adView?.callToActionView?.isHidden = nativeAd?.callToAction == nil
//          adView?.callToActionView?.isUserInteractionEnabled = false
//          adView?.iconView?.layer.cornerRadius = (adView?.iconView?.frame.height ?? 0) / 2
//          adView?.iconView?.layer.borderColor = UIColor.white.cgColor
//          adView?.iconView?.layer.borderWidth = 1
//          (adView?.iconView as? UIImageView)?.image = nativeAd?.icon?.image
//          adView?.iconView?.isHidden = nativeAd?.icon == nil
//      }

    //nativeAdCell.updateUI()
    return nativeAdCell
  }
}
