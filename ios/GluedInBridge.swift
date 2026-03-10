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
import StoreKit
import UIKit
import GoogleMobileAds

//@objc(GluedInBridge)
@objc(GluedInBridge) // ✅ Ensure this annotation is present

class GluedInBridge: RCTEventEmitter {
  
  var seriesId: String?
  var assetId: String?
  var packageId: String?
  var skuId: String?
  var paymentUrl: String?
  var userId: String?
  var paymentType: PaymentMethod = .inAppPurchase
  
  var gadNativeAd: NativeAd?
  var gadCustomNativeAd: CustomNativeAd?
  
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
      baseURL: "https://apiv2.gluedin.io/",
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
    baseUrl: String,
    email: String,
    password: String,
    fullName: String,
    persona: String,
    callback: @escaping RCTResponseSenderBlock
  ) {
    GluedIn.shared.initSdk(apiKey: apiKey, secretKey: secretKey) { [weak self] in
      guard let self = self else { return }
      self.launchGluedIn(
        api: apiKey,
        secret: secretKey,
        baseUrl: baseUrl,
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
    baseUrl: String,
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
          api: apiKey,
          secret: secretKey,
          baseUrl: baseUrl,
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
          api: apiKey,
          secret: secretKey,
          baseUrl: baseUrl,
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
    api: String,
    secret: String,
    baseUrl: String,
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
    let builder = GluedInLaunchBuilder()
      .setApiAndSecret(api, secret)
      .setUserInfo(
        email: email,
        password: password,
        fullName: fullName.isEmpty ? "" : fullName,
        profilePhoto: "")
      .setDelegate(self)
      .setUserPersona(persona)
      .setAutoCreate(!fullName.isEmpty)
      .setFeedType(.vertical)
      .setDarkTheme(true)
      .setApiServerUrl(baseUrl)
      .setCarouselDetails(selectedRailContentId: selectedRailContentId, railContentIds: railContentIds, onlyShortsSubFeed: onlyShortsSubFeed, entryPoint: .none)
      .setSeriesInfo(seriesId: seriesId, selectedEpisodeNumber: nil, onlyShortsSubFeed: onlyShortsSubFeed)
      .build()
    builder.launch { controller in
      if let vc = controller {
        self.present(vc)
        callback([NSNull(), ["status": "success"]])
      } else {
        callback(["Launch controller is nil", NSNull()])
      }
    } authenticationfailure: { error, code in
      Debug.Log(message: error)
      callback([error, NSNull()])
    } sdkInitializationFailure: { error, code in
      Debug.Log(message: error)
      callback([error, NSNull()])
    }
    
//    GluedIn.shared.quickLaunch(
//      email: email,
//      password: password,
//      firebaseToken: "",
//      fullName: fullName,
//      autoCreate: true,
//      termConditionAccepted: true,
//      userType: "",
//      personaType: persona,
//      adsParameter: nil,
//      selectedRailContentId: selectedRailContentId,
//      railContentIds: railContentIds,
//      onlyShortsSubFeed: onlyShortsSubFeed,
//      delegate: self,
//      seriesId: seriesId,
//      selectedEpisodeNumber: nil
//    ) { [weak self] controller in
//      guard let self = self else { return }
//      if let vc = controller {
//        self.present(vc)
//        callback([NSNull(), ["status": "success"]])
//      } else {
//        callback(["Launch controller is nil", NSNull()])
//      }
//    } failure: { error, code in
//      Debug.Log(message: error)
//      callback([error, NSNull()])
//    }
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
extension GluedInBridge : GluedInDelegate,
                          WebViewControllerDelegate,
                          ProductDetailViewDelegate,
                          AddToCartDelegate {
  
  func showAutoRenewalSubscribtion(viewController: UIViewController?) {
    if #available(iOS 15.0, *) {
      // ✅ Apple native subscription screen
      if let scene = viewController?.view.window?.windowScene {
        Task { @MainActor in
          do {
            try await AppStore.showManageSubscriptions(in: scene)
          } catch {
            // Fallback if Apple API fails
            openSubscriptionsURL()
          }
        }
      } else {
        openSubscriptionsURL()
      }
    } else {
      // ❌ iOS 12–14: URL is the ONLY option
      openSubscriptionsURL()
    }
  }
  
  func openSubscriptionsURL() {
    guard let url = URL(string: "https://apps.apple.com/account/subscriptions") else { return }
    UIApplication.shared.open(url, options: [:], completionHandler: nil)
  }
  
  
  func onInitiateSeriesPurchase(
    paymentType: PaymentMethod,
    inAppSkuId: String?,
    purchaseUrl: String?,
    seriesId: String?,
    packageId: String?,
    episodeNumber: Int?,
    userId: String?,
    controller: UIViewController?
  ) {
    self.userId = userId
    self.seriesId = seriesId
    self.skuId = inAppSkuId
    self.paymentUrl = purchaseUrl
    self.paymentType = paymentType
    self.packageId = packageId
    
    switch paymentType {
      
    case .inAppPurchase, .inAppPurchaseSubscription:
      guard let id = skuId, !id.isEmpty else { return }
      guard SKPaymentQueue.canMakePayments() else {
        GluedIn.shared.notifyPaymentResult(
          status: .paymentFailed,
          transactionId: nil,
          seriesId: seriesId,
          skuId: skuId,
          paymenyUrl: paymentUrl,
          packageId: packageId,
          paymentType: paymentType
        )
        return
      }
      
      if #available(iOS 15.0, *) {
        startObservingStoreKit2IfNeeded()
        purchaseStoreKit2(productId: id, userId: userId)
      } else {
        // ✅ existing SK1 flow
        let set: Set<String> = [id]
        let productsRequest = SKProductsRequest(productIdentifiers: set)
        productsRequest.delegate = self
        productsRequest.start()
      }
      
    case .paymentGateway:
      if let urlString = paymentUrl {
        ToastManager.shared.showToast(text: "onInitiateSeriesPurchase => Link - \(urlString)")
      }
      
    case .subscription:
      ToastManager.shared.showToast(
        text: "\(#function) => seriesId - \(seriesId ?? "no series") Link - \(purchaseUrl ?? "no link")"
      )
    }
  }
  
  func onFetchPrice(productIDs: [String]?, paymentType: GluedInCoreSDK.PaymentMethod, completion: @escaping (Any?) -> Void, onError: @escaping ((any Error)?) -> Void) {
    guard let productIDs = productIDs, !productIDs.isEmpty else {
        completion(nil)
        return
    }
    switch paymentType {
    case .inAppPurchase:
        IAPManager.shared.fetchPrices(for: productIDs) { infos in
            DispatchQueue.main.async { completion(infos) }
        } onError: { err in
            DispatchQueue.main.async { onError(err) }
        }

    case .inAppPurchaseSubscription:
        IAPManager.shared.fetchSubscriptionInfos(for: productIDs) { infos in
            DispatchQueue.main.async { completion(infos) }
        } onError: { err in
            DispatchQueue.main.async { onError(err) }
        }

    case .paymentGateway, .subscription:
        completion(nil)
    }
  }
  
  func onRewardedAdRequested(
    viewController: UIViewController?,
    adsType: GluedInCoreSDK.AdsType,
    adUnitID: String?,
    customParmas: [GluedInCoreSDK.GAMExtraParams]?,
    seriesId: String?
  ) {
    if let adId = adUnitID {
      GluedIn.shared.logAds(type: .adMobRewardedInterstitial, status: .impression, error: nil, seriesId: seriesId, assetId: assetId)
      GADRewardedInterstitialManager.shared.loadRewardedInterstitial(adUnitID: adId) { [weak self] didCompleted in
        guard let weakSelf = self else { return }
        if let controller = viewController {
          weakSelf.seriesId = seriesId
          weakSelf.assetId = adUnitID
          weakSelf.showRewardInterstitialAds(view: controller)
          GluedIn.shared.logAds(type: .adMobRewardedInterstitial, status: .completed, error: nil, seriesId: seriesId, assetId: adUnitID)
        }
      } didCompleteWithError: { didCompleteWithError in
        GluedIn.shared.logAds(type: .adMobRewardedInterstitial, status: .failed, error: "\(didCompleteWithError)", seriesId: seriesId, assetId: "")
      }
    }
  }
  
  func showRewardInterstitialAds(view: UIViewController) {
    GADRewardedInterstitialManager.shared.showRewardedInterstitial(from: view) { [weak self] in
      guard let self = self else { return }
      GluedIn.shared.logAds(type: .adMobRewardedInterstitial, status: .showAds, error: nil, seriesId: seriesId, assetId: assetId)
      
    } didDismiss: { [weak self] in
      guard let self = self else { return }
      GluedIn.shared.logAds(type: .adMobRewardedInterstitial, status: .dismiss, error: nil, seriesId: self.seriesId, assetId: self.assetId)
      
    } didFailToPresent: { [weak self] didFailToPresentWithError in
      guard let self = self else { return }
      GluedIn.shared.logAds(type: .adMobRewardedInterstitial, status: .failed, error: "\(didFailToPresentWithError)", seriesId: seriesId, assetId: assetId)
      
    } earnReward: { [weak self] type, amount in
      guard let self = self else { return }
      GluedIn.shared.logAds(type: .adMobRewardedInterstitial, status: .earned, error: nil, seriesId: seriesId, assetId: assetId)
    }
  }
  
  func appViewClickEvent(device_ID: String, user_email: String, user_name: String, platform_name: String) {
    
  }
  
  func appLaunchEvent(deviceID: String, platformName: String) {
    
  }
  
  func onWatchNowAction(deeplink: String) {
    Alert.showToast(message: "\(deeplink)")
  }
  
  func onUserAction(
    action: UserAction,
    assetId: String?,
    productUrl: String?,
    eventRefId: Int,
    navigationController: UINavigationController
  ) {
    switch action {
    case .addToCart:
      guard let gid = assetId else { return }
      Global.shared.fetchProductDetails(productId: gid) { result in
        DispatchQueue.main.async {
          switch result {
          case .success(let product):
            let bundle = Bundle(for: ProductDetailViewController.self)
            let storyboard = UIStoryboard(
              name: "ProductDetailStoryboard",
              bundle: bundle)
            guard let controller = storyboard.instantiateViewController(
              withIdentifier: ProductDetailViewController.className
            ) as? ProductDetailViewController else {
              return
            }
            controller.product = product
            controller.variants = product.variants
            controller.delegate = self
            controller.modalPresentationStyle = .overCurrentContext
            navigationController.present(controller, animated: true, completion: nil)
            
          case .failure(_):
            Alert.showToast(message: "Failure to fetch the product")
          }
        }
      }
      
    case .openBrowser:
      // Client app can decide to push a web view or handoff to Safari.
      Alert.showToast(message: "\(productUrl ?? "")")
    }
  }
  
  func onTapAddToCart(product: VariantVM?) {
    if let productId = product?.id {
      Global.shared.addVariantToShopifyCart(
        variantId: productId
      ) { [weak self] result in
        guard let weakSelf = self else { return }
        switch result {
        case .success(let info):
          // Store the cart URL (not the checkout URL)
          let cartId = info.checkoutUrl.components(separatedBy: "/cart/").last
          Global.shared.setShopifyCartUrl(shopifyCartUrl: "https://\(ShopifyConfig.shopDomain)/cart/\(cartId ?? "")")
          GluedIn.shared.logShoppingStage(Stage: .addToCart, status: true, error: nil)
          
        case .failure(let error):
          print(error)
          GluedIn.shared.logShoppingStage(Stage: .addToCart, status: false, error: error.localizedDescription)
        }
      }
    }
  }
  
  func navigateToCart(viewController: UIViewController?) {
    let bundle = Bundle(for: AddToCartViewController.self)
    let storyboard = UIStoryboard(
      name: "AddToCartStoryboard",
      bundle: bundle)
    guard let controller = storyboard.instantiateViewController(
      withIdentifier: AddToCartViewController.className
    ) as? AddToCartViewController else {
      return
    }
    controller.shopifyCartId = Global.shared.getShopifyCartId()
    controller.shopifyCartUrl = Global.shared.getShopifyCartUrl()
    controller.parentView = viewController
    controller.delegate = self
    viewController?.navigationController?.pushViewController(controller, animated: false)
  }
  
  /// User chose to proceed to checkout from the cart screen.
  /// Opens the last-known `shopifyCartUrl` in the app web view.
  func onTapCheckout(carts: [CartLineVM]?, view: UIViewController?) {
    if let shopifyCartUrl = Global.shared.getShopifyCartUrl() {
      openWebView(url: shopifyCartUrl, title: "Checkout", viewController: view)
    }
  }
  
  func showOrderHistory(viewController: UIViewController?) {
    let shop = ShopifyConfig.shopDomain
    let base = "https://\(shop)"
    let loginPath = "/account" // storefront login
    let urlString = base + loginPath
    openWebView(url: urlString, title: "My Orders", viewController: viewController)
  }
  
  func openWebView(url: String, title: String? = nil, viewController: UIViewController?) {
    let bundle = Bundle(for: WebViewController.self)
    let storyboard = UIStoryboard(
      name: "WebStoryboard",
      bundle: bundle)
    guard let controller = storyboard.instantiateViewController(
      withIdentifier: WebViewController.className
    ) as? WebViewController else {
      return
    }
    controller.url = url
    controller.navTitle = title
    controller.delegate = self
    viewController?.navigationController?.pushViewController(controller, animated: false)
  }
  
  func didFinish(title: String?) {
    switch title {
    case "Checkout":
      GluedIn.shared.logShoppingStage(Stage: .checkoutExit, status: true, error: nil)
    case "My Orders":
      GluedIn.shared.logShoppingStage(Stage: .myOrderExit, status: true, error: nil)
    default:
      break
    }
  }
  
  func getCartItemCount(completion: @escaping (Result<Int, any Error>) -> Void) {
    Global.shared.fetchCartItemsQuantity(completion: completion)
  }
  
  func onAnalyticsEvent(name: String, properties: [String : Any]) {
    
  }
  
  func requestForBannerAds(viewController: UIViewController?, adsType: GluedInCoreSDK.AdsType, adUnitID: String?, customParmas: [GluedInCoreSDK.GAMExtraParams]?, completion: @escaping (UIView?) -> Void) {
    guard let adUnitID else {
      completion(nil)
      return
    }
    
    GADBannerManager.shared.loadBanner(adUnitID: adUnitID, viewController: viewController) { banner in
      // banner == nil => not loaded / failed => don't show
      completion(banner)
    }
  }
  
  func requestForInterstitialAds(viewController: UIViewController?, adsType: GluedInCoreSDK.AdsType, adUnitID: String?, customParmas: [GluedInCoreSDK.GAMExtraParams]?) {
    if let adId = adUnitID {
      GADInterstitialManager.shared.loadInterstitialAds(adUnitID: adId) { [weak self] didCompleted in
        guard let weakSelf = self else { return }
        if let controller = viewController {
          weakSelf.getNativeAdControllerInter(view: controller)
        }
      } didCompleteWithError: { didCompleteWithError in
        Debug.Log(message: "Error - \(didCompleteWithError)")
      }
    }
  }
  
  func getNativeAdControllerInter(view: UIViewController) {
    GADInterstitialManager.shared.showInterstitialAds(
      view: view,
      didPresent: {
        debugPrint("In Present")
      },
      didDismiss: {
        debugPrint("didDismiss")
      },
      didFailToPresent: { didFailToPresentWithError in
        debugPrint("didFailToPresent")
      })
  }
  
  func requestForAdmobNativeAds(viewController: UIViewController?, adUnitID: String?, adsType: GluedInCoreSDK.AdsType, customParmas: [GluedInCoreSDK.GAMExtraParams]?) {
    if let adUnitID = adUnitID {
      GADNativeManager().fetchAdsNative(adUnitID: adUnitID) { [weak self] nativeAd in
        guard let weakSelf = self else { return }
        weakSelf.gadNativeAd = nativeAd
      } didFailedWithError: { error in
        Debug.Log(message: "Error - \(error)")
      }
    }
  }
  
  func getAdmobNativeAdsController() -> UIViewController? {
    guard let ads = gadNativeAd else { return nil }
    let bundle = Bundle(for: NativeAdVerticalViewController.self)
    let storyboard = UIStoryboard(
      name: "NativeAdVerticalView",
      bundle: bundle)
    guard let controller = storyboard.instantiateViewController(
      withIdentifier: NativeAdVerticalViewController.className
    ) as? NativeAdVerticalViewController else {
      return UIViewController()
    }
    controller.NativeAds = ads
    return controller
  }
  
  func requestForGamNativeAds(adUnitID: String?, adsType: GluedInCoreSDK.AdsType, configParams: [String : String]?, extraParams: [GluedInCoreSDK.GAMExtraParams]?, adsFormatId: [String]?) {
    DispatchQueue.main.async {
      GADNativeManager().loadNativeAds(
        configParams: configParams,
        gamExtraParams: extraParams,
        adUnitID: adUnitID,
        adsFormatId: adsFormatId
      ) { [weak self] customNativeAd in
        guard let weakSelf = self else { return }
        weakSelf.gadCustomNativeAd = customNativeAd
      } didFailedWithError: { error in
        debugPrint("error", error)
      }
    }
  }
  
  func getGamNativeAdsController() -> UIViewController? {
    guard let ads = gadCustomNativeAd else { return nil }
    let bundle = Bundle(for: NativeAdVerticalViewController.self)
    let storyboard = UIStoryboard(
      name: "NativeAdVerticalView",
      bundle: bundle)
    guard let controller = storyboard.instantiateViewController(
      withIdentifier: NativeAdVerticalViewController.className
    ) as? NativeAdVerticalViewController else {
      return UIViewController()
    }
    controller.customNativeAds = ads
    return controller
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
    //    let subDomain = "links.gluedin.io"
    let subDomain = "stag-links.gluedin.io"
    let urlStringWithQueryItems = "https://\(subDomain)/\(shareData.deeplink)"
    guard let urlToShare = URL(string: urlStringWithQueryItems) else {
      print("Invalid URL(s)")
      return
    }
    // Prepare the items to share
    let itemsToShare: [Any] = [urlToShare]
    // No thumbnail, just present the URL
    let activityViewController = UIActivityViewController(activityItems: itemsToShare, applicationActivities: nil)
    viewController?.present(activityViewController, animated: true, completion: nil)
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
  
  func getNativeAdNibName() -> String {
    return "UnifiedNativeAdCell"
  }
  
  func requestNativeAdCell() -> UITableViewCell {
    return UITableViewCell()
  }
}

extension GluedInBridge: SKProductsRequestDelegate, SKPaymentTransactionObserver {
  
  func paymentQueue(_ queue: SKPaymentQueue, updatedTransactions transactions: [SKPaymentTransaction]) {
    for transaction in transactions {
      switch transaction.transactionState {
        
      case .purchasing:
        GluedIn.shared.notifyPaymentResult(
          status: .paymentStarted,
          transactionId: transaction.transactionIdentifier,
          seriesId: seriesId,
          skuId: skuId,
          paymenyUrl: paymentUrl,
          packageId: packageId,
          paymentType: paymentType
        )
        
      case .purchased:
        SKPaymentQueue.default().finishTransaction(transaction)
        GluedIn.shared.notifyPaymentResult(
          status: .paymentSuccess,
          transactionId: transaction.transactionIdentifier,
          seriesId: seriesId,
          skuId: skuId,
          paymenyUrl: paymentUrl,
          packageId: packageId,
          paymentType: paymentType
        )
        
      case .failed:
        SKPaymentQueue.default().finishTransaction(transaction)
        GluedIn.shared.notifyPaymentResult(
          status: .paymentFailed,
          transactionId: transaction.transactionIdentifier,
          seriesId: seriesId,
          skuId: skuId,
          paymenyUrl: paymentUrl,
          packageId: packageId,
          paymentType: paymentType
        )
        
      case .restored:
        SKPaymentQueue.default().finishTransaction(transaction)
        GluedIn.shared.notifyPaymentResult(
          status: .paymentRestored,
          transactionId: transaction.transactionIdentifier,
          seriesId: seriesId,
          skuId: skuId,
          paymenyUrl: paymentUrl,
          packageId: packageId,
          paymentType: paymentType
        )
        
      case .deferred:
        GluedIn.shared.notifyPaymentResult(
          status: .paymentDeferred,
          transactionId: transaction.transactionIdentifier,
          seriesId: seriesId,
          skuId: skuId,
          paymenyUrl: paymentUrl,
          packageId: packageId,
          paymentType: paymentType
        )
        
      @unknown default:
        break
      }
    }
  }
  
  func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
    if let oProduct = response.products.first {
      purchase(aProduct: oProduct)
    } else {
      GluedIn.shared.notifyPaymentResult(
        status: .paymentCancelled,
        transactionId: nil,
        seriesId: seriesId,
        skuId: skuId,
        paymenyUrl: paymentUrl,
        packageId: packageId,
        paymentType: paymentType
      )
    }
  }
  
  func purchase(aProduct: SKProduct) {
    let payment = SKMutablePayment(product: aProduct)
    payment.applicationUsername = userId
    SKPaymentQueue.default().add(self)
    SKPaymentQueue.default().add(payment)
  }
}

extension GluedInBridge {
  
  @available(iOS 15.0, *)
  private static var sk2ObserverStarted = false
  
  @available(iOS 15.0, *)
  private static var sk2UpdatesTask: Task<Void, Never>?
  
  @available(iOS 15.0, *)
  private func startObservingStoreKit2IfNeeded() {
    guard !Self.sk2ObserverStarted else { return }
    Self.sk2ObserverStarted = true
    
    // ✅ Capture snapshot on MainActor (safe for Swift 6)
    let snapshot = self.currentPaymentSnapshot()
    
    Self.sk2UpdatesTask = Task.detached(priority: .background) {
      for await result in Transaction.updates {
        
        let transaction: Transaction
        do {
          transaction = try SK2Verifier.checkVerified(result)
        } catch {
          continue
        }
        
        // Only handle matching product
        if let currentSku = snapshot.skuId, !currentSku.isEmpty, transaction.productID != currentSku {
          await transaction.finish()
          continue
        }
        
        let txId = String(transaction.id)
        
        // ✅ Notify on main thread (UI safe)
        // Don't notify success here (avoid double log).
        // Observer will emit success from Transaction.updates.
        /*
         await MainActor.run {
         GluedIn.shared.notifyPaymentResult(
         status: .paymentSuccess,
         transactionId: txId,
         seriesId: snapshot.seriesId,
         skuId: snapshot.skuId,
         paymenyUrl: snapshot.paymentUrl,
         packageId: snapshot.packageId,
         paymentType: snapshot.paymentType
         )
         }
         */
        await transaction.finish()
      }
    }
  }
  
  @available(iOS 15.0, *)
  private func makeAccountToken(from userId: String) -> UUID? {
    let trimmed = userId.trimmingCharacters(in: .whitespacesAndNewlines)
    return UUID(uuidString: trimmed)
  }
  
  @available(iOS 15.0, *)
  private func purchaseStoreKit2(
    productId: String,
    userId: String?
  ) {
    
    let snapshot = self.currentPaymentSnapshot()
    
    // ✅ Only for subscription we pass appAccountToken
    let appAccountToken: UUID? = {
      guard snapshot.paymentType == .inAppPurchaseSubscription,
            let userId = userId,
            !userId.isEmpty
      else {
        return nil   // ✅ IAP → nil
      }
      return makeAccountToken(from: userId)
    }()
    
    Task.detached(priority: .userInitiated) {
      
      // ---- Payment started
      await MainActor.run {
        GluedIn.shared.notifyPaymentResult(
          status: .paymentStarted,
          transactionId: nil,
          seriesId: snapshot.seriesId,
          skuId: snapshot.skuId,
          paymenyUrl: snapshot.paymentUrl,
          packageId: snapshot.packageId,
          paymentType: snapshot.paymentType
        )
      }
      
      do {
        let products = try await Product.products(for: [productId])
        
        guard let product = products.first else {
          await MainActor.run {
            GluedIn.shared.notifyPaymentResult(
              status: .paymentCancelled,
              transactionId: nil,
              seriesId: snapshot.seriesId,
              skuId: snapshot.skuId,
              paymenyUrl: snapshot.paymentUrl,
              packageId: snapshot.packageId,
              paymentType: snapshot.paymentType
            )
          }
          return
        }
        
        // ✅ Purchase (token only for subscription)
        let result: Product.PurchaseResult
        if let token = appAccountToken {
          result = try await product.purchase(
            options: [.appAccountToken(token)]
          )
        } else {
          result = try await product.purchase()
        }
        
        switch result {
          
        case .success(let verificationResult):
          let transaction = try SK2Verifier.checkVerified(verificationResult)
          let txId = String(transaction.id)
          
          await MainActor.run {
            GluedIn.shared.notifyPaymentResult(
              status: .paymentSuccess,
              transactionId: txId,
              seriesId: snapshot.seriesId,
              skuId: snapshot.skuId,
              paymenyUrl: snapshot.paymentUrl,
              packageId: snapshot.packageId,
              paymentType: snapshot.paymentType
            )
          }
          
          await transaction.finish()
          
        case .userCancelled:
          await MainActor.run {
            GluedIn.shared.notifyPaymentResult(
              status: .paymentCancelled,
              transactionId: nil,
              seriesId: snapshot.seriesId,
              skuId: snapshot.skuId,
              paymenyUrl: snapshot.paymentUrl,
              packageId: snapshot.packageId,
              paymentType: snapshot.paymentType
            )
          }
          
        case .pending:
          await MainActor.run {
            GluedIn.shared.notifyPaymentResult(
              status: .paymentDeferred,
              transactionId: nil,
              seriesId: snapshot.seriesId,
              skuId: snapshot.skuId,
              paymenyUrl: snapshot.paymentUrl,
              packageId: snapshot.packageId,
              paymentType: snapshot.paymentType
            )
          }
          
        @unknown default:
          await MainActor.run {
            GluedIn.shared.notifyPaymentResult(
              status: .paymentFailed,
              transactionId: nil,
              seriesId: snapshot.seriesId,
              skuId: snapshot.skuId,
              paymenyUrl: snapshot.paymentUrl,
              packageId: snapshot.packageId,
              paymentType: snapshot.paymentType
            )
          }
        }
        
      } catch {
        await MainActor.run {
          GluedIn.shared.notifyPaymentResult(
            status: .paymentFailed,
            transactionId: nil,
            seriesId: snapshot.seriesId,
            skuId: snapshot.skuId,
            paymenyUrl: snapshot.paymentUrl,
            packageId: snapshot.packageId,
            paymentType: snapshot.paymentType
          )
        }
      }
    }
  }
  
  // MARK: - Snapshot helper (isolated access in one place)
  
  private struct PaymentSnapshot {
    let seriesId: String?
    let skuId: String?
    let paymentUrl: String?
    let packageId: String?
    let paymentType: PaymentMethod
  }
  
  // ✅ Read actor-isolated properties in one place
  private func currentPaymentSnapshot() -> PaymentSnapshot {
    return PaymentSnapshot(
      seriesId: self.seriesId,
      skuId: self.skuId,
      paymentUrl: self.paymentUrl,
      packageId: self.packageId,
      paymentType: self.paymentType
    )
  }
}
