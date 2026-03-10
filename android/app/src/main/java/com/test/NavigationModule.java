package com.test;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import com.gluedin.domain.entities.challengeDetail.widgetConfig.WidgetConfigDetails;
import com.google.gson.Gson;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.gluedin.usecase.config.LaunchConfig;
import com.gluedin.usecase.config.UserInfoAutoSignIn;
import com.gluedin.usecase.constants.GluedInConstants;
import com.gluedin.usecase.discover.DiscoverInteractor;
import com.gluedin.usecase.config.AppConfigInteractor;
import com.gluedin.usecase.challengeDetail.WidgetInteractor;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.gluedin.GluedInInitializer;
import com.gluedin.analytics.GluedInAnalyticsCallback;
import com.gluedin.callback.GIInitCallback;
import com.gluedin.callback.GISdkCallback;
import com.gluedin.callback.SDKInitStatus;
import com.gluedin.callback.UserAuthStatus;
import com.gluedin.data.persistence.analytics.AnalyticsEvents;
import com.gluedin.domain.entities.config.ShareData;
import com.gluedin.domain.entities.feed.VideoInfo;
import com.gluedin.exception.GluedInSdkException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.gluedin.domain.entities.feed.AssetsInformation;
import org.jetbrains.annotations.NotNull;
import com.gluedin.callback.GIAssetCallback;
import android.content.Context;
import com.gluedin.callback.UserAction;

import com.gluedin.callback.GIAdsCallback;
import com.gluedin.domain.entities.feed.ads.NativeAdsType;
import com.gluedin.domain.entities.feed.ads.AdsRequestParams;
import com.gluedin.domain.entities.feed.ads.BannerAdsType;
import com.gluedin.domain.entities.feed.ads.InterstitialAdsType;
import com.gluedin.view.BannerAdView;
import com.gluedin.callback.GIPaymentCallback;
import com.gluedin.callback.PaymentMethod;
import com.test.ads.NativeAdJavaFragment;
import com.test.ads.BannerAdLoader;
import com.test.ads.InterstitialAdManager;
import com.gluedin.callback.PaymentMethod;
import com.gluedin.callback.PaymentStatus;
import com.gluedin.callback.AdsStatus;
import com.gluedin.callback.SubscriptionDetails;
import com.test.payment.BillingManager;
import com.test.ads.RewardedInterstitialManager;
import com.test.shopify.ShopifyCartManager;
import com.test.shopify.ViewCartActivity;
import com.test.shopify.WebViewActivity;
import androidx.appcompat.app.AppCompatActivity;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function4;
import org.jetbrains.annotations.Nullable;
import com.android.billingclient.api.BillingClient;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

import kotlin.Unit;

public class NavigationModule extends ReactContextBaseJavaModule implements GluedInAnalyticsCallback {
    private static ReactApplicationContext reactContext;
    private Intent intent;
    private String API_KEY = null;
    private String SECRET_KEY = null;
    private String BASE_URL = null;
//    private GluedInInitializer.Configurations gluedInConfigurations = null;
    private GluedInInitializer.Configurations gluedInConfigurations = null;
    private GluedInConstants.EntryPoint sdkEntryPoint = GluedInConstants.EntryPoint.SUB_FEED;

    NavigationModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "NavigationModule";
    } //The name of the component when it is called in the RN code

    @ReactMethod
    public void fetchRailDataFromAndroidSDK(String railId, Promise promise) {
        new Thread(() -> {
            DiscoverInteractor discoverInteractor = new DiscoverInteractor();
            discoverInteractor.getCuratedRailDetails(railId, true, // isJsonResponse
                    (throwable, allVideoInfo, json) -> {
                        Log.d("NavigationModule", "Rail Data Response: " + json);
                        if (json != null) {
                            promise.resolve(json.toString());
                        } else if (throwable != null) {
                            promise.reject("Error", throwable);
                        }
                        return Unit.INSTANCE; // Required as Kotlin uses Unit instead of void
                    });
        }).start();
    }

    @ReactMethod
    public void fetchMicroCommunityDataFromAndroidSDK(String assetId, Promise promise) {
        Log.d("NavigationModule", "fetchMicroCommunityDataFromAndroidSDK called");
        new Thread(() -> {
            WidgetInteractor widgetInteractor = new WidgetInteractor();
            widgetInteractor.getWidgetDataAsJson(assetId, (successWidgetConfig, successHomeFeed) -> {
                Log.d("NavigationModule", "getWidgetDataAsJson: " + successWidgetConfig.toString());
                WritableMap resultMap = Arguments.createMap();
                resultMap.putString("successWidgetConfig", successWidgetConfig);
                resultMap.putString("successHomeFeed", successHomeFeed);
                promise.resolve(resultMap);
                return Unit.INSTANCE; // Required as Kotlin uses Unit instead of void
            }, (failedMessage, failureCode) -> {
                Log.d("NavigationModule", "getWidgetDataAsJson failedMessage: " + failureCode);
                // Handle the widget details if needed
                promise.reject("Error", failureCode.toString());
                return Unit.INSTANCE; // Required as Kotlin uses Unit instead of void
            });
        }).start();
    }

    @ReactMethod
    public void validateGluedInSDKSilently(String apiKey, String secretKey, String baseUrl, String email, String password, String fullName, String profilePic, Promise promise) {
        ReactApplicationContext context = getReactApplicationContext();
        API_KEY = apiKey;
        SECRET_KEY = secretKey;
        BASE_URL = baseUrl;
        Log.d("NavigationModule", "GluedIn validateGluedInSDKSilently else");
        GIInitCallback initCallback = new GIInitCallback() {
            @Override
            public void onSDKLifecycle(@NonNull SDKInitStatus sdkInitStatus, @Nullable GluedInSdkException exception) {
                Log.d("NavigationModule", "SDK Init Status: " + sdkInitStatus);
                switch (sdkInitStatus) {
                    case SDK_INIT:
                        if (sdkInitStatus.getValue()) {
                            Log.d("NavigationModule", "SDK Init Status true: ");
                            promise.resolve("success");
                        } else {
                            Log.d("NavigationModule", "SDK Init Status false: ");
                            promise.resolve("fail");
                        }
                        break;
                }
            }
        };

        GluedInInitializer.Configurations gluedInConfigurations = new GluedInInitializer.Configurations.Builder()
        .setLogEnabled(true, Log.DEBUG)
        .setApiAndSecret(API_KEY, SECRET_KEY)
        .setSdkInitCallback(initCallback)
        .setBaseUrl(BASE_URL)
        .setHttpLogEnabled(true, 3)
        .setUserInfo(email, password, fullName, profilePic, "")
        .create();
        gluedInConfigurations.validateGluedInSDK(context, GluedInConstants.LaunchType.APP);
    }

    @ReactMethod
    public void fetchRewardStatus(Promise promise) {
        new Thread(() -> {
            AppConfigInteractor appConfigInteractor = new AppConfigInteractor();
            promise.resolve(appConfigInteractor.isRewardEnable());
        }).start();
    }

    @Override
    public void onAnalyticsEvent(@NonNull AnalyticsEvents analyticsEvents) {

    }

    @ReactMethod
    public void launchSeriesFeed(String apiKey, String secretKey, String baseUrl, String email, String password, String fullName, String profilePic, String userPersona, String seriesId, Promise promise) {
        ReactApplicationContext context = getReactApplicationContext();
        context.runOnUiQueueThread(() -> {
            API_KEY = apiKey;
            SECRET_KEY = secretKey;
            BASE_URL = baseUrl;
            GIInitCallback initCallback = new GIInitCallback() {
                @Override
                public void onSDKLifecycle(@NonNull SDKInitStatus sdkInitStatus, @Nullable GluedInSdkException exception) {
                    Log.d("NavigationModule", "GluedIn sdkInitStatus: " + sdkInitStatus);

                    if (exception != null) {
                        Log.e("NavigationModule", "GluedIn sdkInitStatus exception: " + exception.getErrorMessage());
                    }
                }
            };

            GISdkCallback sdkCallback = new GISdkCallback() {

                @Override
                public void onUserAuthStatus(@NotNull UserAuthStatus userAuthStatus,
                                            @Nullable VideoInfo currentVideo) {

                    if (currentVideo != null) {
                        // handle video
                    }
                }

                @Override
                public void onShareAction(@NotNull ShareData shareData) {
                    // handle share
                }

                @Override
                public void onUserProfileClick(@NotNull String userId) {
                    // open profile
                }

                @Override
                public void onRewardClick() {
                    // reward logic
                }

                @Override
                public void onWatchNowAction(@NotNull String deeplink) {
                    // handle deeplink
                }
            };

            GluedInInitializer.Configurations gluedInConfigurations = new GluedInInitializer.Configurations.Builder()
            .setLogEnabled(true, Log.DEBUG)
            .setApiAndSecret(API_KEY, SECRET_KEY)
            .setSdkInitCallback(initCallback)
            .setSdkCallback(sdkCallback)
            .setBaseUrl(BASE_URL)
            .setHttpLogEnabled(true, 3)
            .setUserInfo(email, password, fullName, profilePic, "")
            .setSeriesInfo(seriesId, -1)
            .setUserPersona(userPersona)
            .enableBackButton(true)
            .create();
            gluedInConfigurations.validateAndLaunchGluedInSDK(context, GluedInConstants.LaunchType.APP, null, GluedInConstants.EntryPoint.NONE, null, null, null, null);
        });

    }

    @ReactMethod
    public void launchCarouselFeed(String apiKey, String secretKey, String baseUrl, String email, String password, String fullName, String profilePic, int selectedIndex, ReadableArray feedRailData, Promise promise) {
        ReactApplicationContext context = getReactApplicationContext();
        context.runOnUiQueueThread(() -> {
            API_KEY = apiKey;
            SECRET_KEY = secretKey;
            BASE_URL = baseUrl;
            ArrayList<String> railList = new ArrayList<>();
            for (int i = 0; i < feedRailData.size(); i++) {
                ReadableMap item = feedRailData.getMap(i);
                ReadableMapKeySetIterator iterator = item.keySetIterator();
                while (iterator.hasNextKey()) {
                    String key = iterator.nextKey();
                    if (key.equals("video")) {
                        String converted = readableMapToString(item);
                        railList.add(converted);
                    }
                }
            }

            String selectedContentId = null;
            if (railList != null) {
                selectedContentId = railList.get(selectedIndex);
            }

            GIInitCallback initCallback = new GIInitCallback() {
                @Override
                public void onSDKLifecycle(@NonNull SDKInitStatus sdkInitStatus, @Nullable GluedInSdkException exception) {
                    Log.d("NavigationModule", "GluedIn sdkInitStatus: " + sdkInitStatus);

                    if (exception != null) {
                        Log.e("NavigationModule", "GluedIn sdkInitStatus exception: " + exception.getErrorMessage());
                    }
                }
            };

            GISdkCallback sdkCallback = new GISdkCallback() {

                @Override
                public void onUserAuthStatus(@NotNull UserAuthStatus userAuthStatus,
                                            @Nullable VideoInfo currentVideo) {

                    if (currentVideo != null) {
                        // handle video
                    }
                }

                @Override
                public void onShareAction(@NotNull ShareData shareData) {
                    // handle share
                }

                @Override
                public void onUserProfileClick(@NotNull String userId) {
                    // open profile
                }

                @Override
                public void onRewardClick() {
                    // reward logic
                }

                @Override
                public void onWatchNowAction(@NotNull String deeplink) {
                    // handle deeplink
                }
            };

            GluedInInitializer.Configurations gluedInConfigurations = new GluedInInitializer.Configurations.Builder()
            .setLogEnabled(true, Log.DEBUG)
            .setApiAndSecret(API_KEY, SECRET_KEY)
            .setSdkInitCallback(initCallback)
            .setSdkCallback(sdkCallback)
            .setBaseUrl(BASE_URL).setHttpLogEnabled(true, 3)
            .setUserInfo(email, password, fullName, profilePic, "")
            .setCarouselDetails(GluedInConstants.CarouselType.NONE, selectedContentId, railList, false)
            .enableBackButton(true).create();
            gluedInConfigurations.validateAndLaunchGluedInSDK(context, GluedInConstants.LaunchType.APP, null, GluedInConstants.EntryPoint.NONE, null, null, null, null);
        });

    }

    private String readableMapToString(ReadableMap readableMap) {
        String contentId = "";
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            if ("id".equals(key)) {
                String value = readableMap.getString(key);
                contentId = value;
            }
        }
        return contentId;
    }

    @ReactMethod
    public void launchGluedInSDKForMicroCommunity(String apiKey, String secretKey, String baseUrl, String email, String password, String fullName, String profilePic, String assetId, String assetName, String discountPrice, String imageUrl, String discountEndDate, String discountStartDate, String mrp, String shoppableLink, String currencySymbol, String selectedVideoId, String feedRailData, int entryPoint, Promise promise) {

        ReactApplicationContext context = getReactApplicationContext();
        AssetsInformation assetsInformation = new AssetsInformation(assetId, assetName, Double.parseDouble(discountPrice), imageUrl, discountEndDate, discountStartDate, "Buy Now", Double.parseDouble(mrp), shoppableLink, currencySymbol);
        context.runOnUiQueueThread(() -> {
            // UserInfoAutoSignIn userInfo = new UserInfoAutoSignIn(email, password, fullName, profilePic, "");

            switch (entryPoint) {
                case 1:
                    sdkEntryPoint = GluedInConstants.EntryPoint.LEADERBOARD;
                    break;
                case 2:
                    sdkEntryPoint = GluedInConstants.EntryPoint.CREATOR;
                    break;
                case 3:
                    sdkEntryPoint = GluedInConstants.EntryPoint.REWARD;
                    break;
                default:
                    sdkEntryPoint = GluedInConstants.EntryPoint.SUB_FEED;
            }

            GIInitCallback initCallback = new GIInitCallback() {
                @Override
                public void onSDKLifecycle(@NonNull SDKInitStatus sdkInitStatus, @Nullable GluedInSdkException exception) {
                    Log.d("NavigationModule", "sdkInitStatus: " + feedRailData);
                    switch (sdkInitStatus) {
                        case SDK_AUTH:
                            break;
                        case SDK_EXIT:
                            break;
                        case SDK_INIT:
                            if (sdkInitStatus.getValue()) {
                                Gson gson = new Gson();
                                WidgetConfigDetails response = gson.fromJson(feedRailData, WidgetConfigDetails.class);
                                Toast.makeText(context, "GluedInSDK Launched Successfully: ", Toast.LENGTH_SHORT).show();
                                Log.d("NavigationModule", "Response: " + response.toString());
                                gluedInConfigurations.launchSDK(getCurrentActivity(), new LaunchConfig(sdkEntryPoint, assetsInformation, response, selectedVideoId, false, null, null));
                            } else {
                                Toast.makeText(context, "GluedInSDK Launched failed" + exception.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
            };

            GISdkCallback sdkCallback = new GISdkCallback() {

                @Override
                public void onUserAuthStatus(@NotNull UserAuthStatus userAuthStatus,
                                            @Nullable VideoInfo currentVideo) {

                    if (currentVideo != null) {
                        // handle video
                    }
                }

                @Override
                public void onShareAction(@NotNull ShareData shareData) {
                    // handle share
                }

                @Override
                public void onUserProfileClick(@NotNull String userId) {
                    // open profile
                }

                @Override
                public void onRewardClick() {
                    // reward logic
                }

                @Override
                public void onWatchNowAction(@NotNull String deeplink) {
                    // handle deeplink
                }
            };

            gluedInConfigurations = new GluedInInitializer.Configurations.Builder()
            .setLogEnabled(true, Log.DEBUG)
            .setApiAndSecret(API_KEY, SECRET_KEY)
            .setSdkInitCallback(initCallback)
            .setSdkCallback(sdkCallback).setBaseUrl(BASE_URL)
            .setHttpLogEnabled(true, 3)
            .setUserInfo(email, password, fullName, profilePic, "")
            .enableBackButton(true)
            .create();

            gluedInConfigurations.validateGluedInSDK(context, GluedInConstants.LaunchType.APP);

        });
    }

    @ReactMethod
    public void launchGluedInSDK(String apiKey, String secretKey, String baseUrl, String email, String password, String fullName, String profilePic, Promise promise) {

        ReactApplicationContext context = getReactApplicationContext();
        context.runOnUiQueueThread(() -> {
            // UserInfoAutoSignIn userInfo = new UserInfoAutoSignIn(email, password, fullName, profilePic, "");
            GIInitCallback initCallback = new GIInitCallback() {
                @Override
                public void onSDKLifecycle(@NonNull SDKInitStatus sdkInitStatus, @Nullable GluedInSdkException exception) {

                    switch (sdkInitStatus) {
                        case SDK_AUTH:
                            break;
                        case SDK_EXIT:
                            break;
                        case SDK_INIT:
                            if (sdkInitStatus.getValue()) {
                                Toast.makeText(context, "GluedInSDK Launched Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "GluedInSDK Launched failed" + exception.getErrorMessage(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }
            };

            GISdkCallback sdkCallback = new GISdkCallback() {

                @Override
                public void onUserAuthStatus(@NotNull UserAuthStatus userAuthStatus,
                                            @Nullable VideoInfo currentVideo) {

                    switch (userAuthStatus) {

                        case USER_LOGIN_REQUIRED:
                            //GluedInInitializer.closeSDK();
                            // Redirect to your login screen if needed
                            break;
                
                        case USER_LOGOUT:
                            Toast.makeText(getCurrentActivity(),
                                    "user logout",
                                    Toast.LENGTH_SHORT).show();
                            break;
                
                        default:
                            break;
                    }
                }

                @Override
                public void onShareAction(@NotNull ShareData shareData) {
                    String message = "https://gluedin.page.link/data?" + shareData.getDeeplink();

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain"); // Correct way to set MIME type
                    intent.putExtra(Intent.EXTRA_TEXT, message);

                    Intent chooser = Intent.createChooser(intent, "Share via");
                    reactContext.startActivity(chooser);
                }

                @Override
                public void onUserProfileClick(@NotNull String userId) {
                    // open profile
                }

                @Override
                public void onRewardClick() {
                    // reward logic
                }

                @Override
                public void onWatchNowAction(@NotNull String deeplink) {
                    // handle deeplink
                }
            };

            GIAssetCallback giECommerceCallback = new GIAssetCallback() {

                @Override
                public void onUserAction(@NotNull Context context,
                                         @NotNull UserAction action,
                                         @Nullable String assetId,
                                         @Nullable Integer eventRefId,
                                         kotlin.jvm.functions.Function1<? super Integer, kotlin.Unit> callback) {
            
                    if (UserAction.ADD_TO_CART == action) {
                        AppCompatActivity act = (AppCompatActivity) getCurrentActivity();
                        ShopifyCartManager.getInstance().init(act);
                        if (callback != null) {
                            ShopifyCartManager.getInstance().showProductDetails(
                                    context, assetId.toString(),
                                    callback
                            );
                        }
                    }
                }
            
                @Override
                public void navigateToCart() {
                    // Intent intent = new Intent(getCurrentActivity(), ViewCartActivity.class);
                    // startActivity(intent);
                }
            
                @Override
                public void getCartItemCount(@NotNull final kotlin.jvm.functions.Function1<? super Integer, Unit> callback) {
                    // ShopifyCartManager.getInstance().init(getCurrentActivity());
                    // ShopifyCartManager.getInstance().getTotalCartItems(cartCount ->
                    //      callback.invoke(cartCount)
                    // );
                }
            
                @Override
                public void showOrderHistory() {
                    // ShopifyCartManager.getInstance().init(getCurrentActivity());

                    // String orderHistory = ShopifyCartManager.getInstance().getOderHistoryUrl();

                    // Intent intent = new Intent(getCurrentActivity(), WebViewActivity.class);
                    // intent.putExtra("url", orderHistory);
                    // intent.putExtra("title", "My Orders");

                    // startActivity(intent);
                }
            };

            GIAdsCallback giAdsCallBack = new GIAdsCallback() {

                @Override
                @Nullable
                public Fragment onNativeRequest(@NotNull NativeAdsType adsType,
                                                 @NotNull AdsRequestParams adsRequestParams) {
            
                    switch (adsType) {
            
                        case AD_MOB_NATIVE:
                        case GAM_NATIVE:
                             NativeAdJavaFragment fragment = new NativeAdJavaFragment(
                                    adsType,
                                    adsRequestParams,
                                    context
                            );
                            return fragment;
            
                        default:
                            return null;
                    }
                }
            
                @Override
                public void onBannerAdsRequest(@NotNull BannerAdsType adsType,
                                               @NotNull AdsRequestParams adsRequestParams,
                                               @Nullable BannerAdView view) {
            
                    if (view != null) {
                        new BannerAdLoader(
                            view.getContext(),
                            view,
                            adsType,
                            adsRequestParams
                        ).loadAd();
                    }
                }
            
                @Override
                public void onInterstitialAdsRequest(@NotNull InterstitialAdsType adsType,
                                                     @NotNull AdsRequestParams adsRequestParams) {
                    InterstitialAdManager.getInstance().loadAndShow(getCurrentActivity(),adsRequestParams.getAdsId(), ()->{}); 
                }
            };

            GIPaymentCallback giPaymentCallBack = new GIPaymentCallback() {

                @Override
                public void onInitiateSeriesPurchase(@NotNull PaymentMethod paymentMethod, 
                                                     @Nullable String inAppSkuId,
                                                     @Nullable String basePlanId, 
                                                     @Nullable String offerId,
                                                     @Nullable String purchaseUrl, 
                                                     @Nullable String seriesId,
                                                     int episodeNumber, 
                                                     @NotNull String packageId,
                                                     @NotNull String userId, 
                                                     @NotNull Function4<? super PaymentStatus, ? super String, ? super String, ? super PaymentMethod, Unit> onNotifyPaymentResult) {
            
                    // Handle payments (in-app or subscription)
                    if (PaymentMethod.IN_APP_PURCHASE == paymentMethod) {
                        /*
                          //TODO: Enable this method for the actual use case.
                          callBillingManager(
                                  inAppSkuId,
                                  basePlanId,
                                  purchaseUrl,
                                  seriesId,
                                  packageId,
                                  userId,
                                  paymentMethod,
                                  onNotifyPaymentResult
                          );
                        */
            
                        Toast.makeText(
                                getCurrentActivity(),
                                "In-app purchases haven’t been configured yet.",
                                Toast.LENGTH_SHORT
                        ).show();
            
                    } else if (PaymentMethod.SUBSCRIPTION_PLAN == paymentMethod) {
                        /*
                          //TODO: Enable this method for the actual use case.
                          callBillingManagerForSubscription(
                                  inAppSkuId,
                                  basePlanId,
                                  purchaseUrl,
                                  seriesId,
                                  packageId,
                                  userId,
                                  paymentMethod,
                                  onNotifyPaymentResult
                          );
                        */
            
                        Toast.makeText(
                                getCurrentActivity(),
                                "Subscription hasn’t been configured yet.",
                                Toast.LENGTH_SHORT
                        ).show();
            
                    } else if (PaymentMethod.PAYMENT_GATEWAY == paymentMethod) {
            
                        Toast.makeText(
                            context,
                            "onSelectPaymentMethod seriesId :" + seriesId
                                    + " \n paymentUrl : " + purchaseUrl
                                    + " \n deeplink : " + purchaseUrl,
                            Toast.LENGTH_SHORT
                        ).show();

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(purchaseUrl != null ? purchaseUrl : ""));
                        try {
                            reactContext.startActivity(browserIntent);
                        } catch (Exception e) {
                            Toast.makeText(
                                    context,
                                    "Invalid shoppable url",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
            
                    } else {
                        Toast.makeText(context, "Other Payment Method", Toast.LENGTH_SHORT).show();
                    }
                }
            
                @Override
                public void onRewardedAdRequested(@NotNull String adUnitID,
                        @NotNull String adsType,
                        @Nullable String seriesId,
                        @NotNull Function1<? super AdsStatus, Unit> onNotifyAdsResult) {
                        rewardedInterstitialAd(adUnitID, adsType, seriesId, onNotifyAdsResult);
                }
            
                @Override
                public void onProductDetailsFetched(@Nullable List<String> inAppSkuId,
                        @NotNull PaymentMethod paymentMethod,
                        @NotNull Function1<? super Map<String, ?>, Unit> onNotifyPriceResult)  {
            
                    if (PaymentMethod.IN_APP_PURCHASE == paymentMethod) {
                        // Enable this method to initiate Purchase from Google Play Store. 
            
                        // if (inAppSkuId != null) {
                        //     fetchPricePartsForSkus(
                        //             inAppSkuId,
                        //             BillingClient.ProductType.INAPP,
                        //             onNotifyPriceResult
                        //     );
                        // }
            
                    } else if (PaymentMethod.SUBSCRIPTION_PLAN == paymentMethod) {
                        // Enable this method to initiate Purchase from Google Play Store.
            
                        // if (inAppSkuId != null) {
                        //     fetchPricePartsForSkus(
                        //             inAppSkuId,
                        //             BillingClient.ProductType.SUBS,
                        //             onNotifyPriceResult
                        //     );
                        // }
                    }
                }
            
                @Override
                public void onManageSubscription(@NotNull PaymentMethod paymentMethod,
                    @NotNull String inAppSkuId,
                    @NotNull String userId,
                    @NotNull Function1<? super Boolean, Unit> onNotifyResult) {
            
                    BillingManager.getInstance().openPlayStoreSubscription(getCurrentActivity(), inAppSkuId, context.getPackageName());
                }
            
                @Override
                public void onUpgradeSubscriptionList(@NotNull String inAppSkuId,
                    @NotNull PaymentMethod paymentMethod,
                    @NotNull Function2<? super Map<String, SubscriptionDetails>, ? super String, Unit> onNotifyPriceResult) {
                }
            };


            GluedInInitializer.Configurations gluedInConfigurations = new GluedInInitializer.Configurations.Builder()
            .setLogEnabled(true, Log.DEBUG)
            .setApiAndSecret(API_KEY, SECRET_KEY)
            .setSdkInitCallback(initCallback).setSdkCallback(sdkCallback)
            .setGIAssetCallback(giECommerceCallback)
            .setGIAdsCallback(giAdsCallBack)
            .setGIPaymentCallback(giPaymentCallBack)
            .setBaseUrl(BASE_URL).setHttpLogEnabled(true, 3)
            .setUserInfo(email, password, fullName, profilePic, "")
            .enableBackButton(true)
            .create();
            gluedInConfigurations.validateAndLaunchGluedInSDK(context, GluedInConstants.LaunchType.APP, intent, GluedInConstants.EntryPoint.NONE, null, null, null, null);

        });
    }

    private void callBillingManager(
        String skuId,
        String basePlanId,
        String paymentUrl,
        String seriesId,
        String packageId,
        String userId,
        PaymentMethod paymentMethod,
        Function4<? super PaymentStatus, ? super String, ? super String, ? super PaymentMethod, Unit> onNotifyPaymentResult
    ) {
        BillingManager.getInstance().init(
                getCurrentActivity(),                                     // activity
                skuId != null ? skuId : "",               // skuId (orEmpty)
                basePlanId != null ? basePlanId : "",     // basePlanId (orEmpty)
                seriesId,                                 // seriesId
                paymentUrl,                               // paymentUrl
                packageId,                                // packageId
                BillingClient.ProductType.INAPP,          // productType
                userId != null ? userId : "null",         // userId.toString()
                paymentMethod,                            // paymentMethod
                (BillingManager.PaymentCallback) onNotifyPaymentResult                     // callback
        );
    }

    private void callBillingManagerForSubscription(
            String skuId,
            String basePlanId,
            String paymentUrl,
            String seriesId,
            String packageId,
            String userId,
            PaymentMethod paymentMethod,
            Function4<? super PaymentStatus, ? super String, ? super String, ? super PaymentMethod, Unit> onNotifyPaymentResult
    ) {
        BillingManager.getInstance().init(
                getCurrentActivity(),                                     // activity
                skuId != null ? skuId : "",               // skuId.toString() logic
                basePlanId != null ? basePlanId : "",     // basePlanId.orEmpty()
                seriesId,                                 // seriesId
                paymentUrl,                               // paymentUrl
                packageId,                                // packageId
                BillingClient.ProductType.SUBS,           // productType set to SUBS
                userId != null ? userId : "null",         // userId.toString()
                paymentMethod,                            // paymentMethod
                (BillingManager.PaymentCallback) onNotifyPaymentResult                     // callback
        );
    }

    private void rewardedInterstitialAd(
            @NotNull String adId,
            @NotNull String platformName,
            @Nullable String seriesId,
            @NotNull Function1<? super AdsStatus, Unit> onNotifyAdsResult
    ) {
        RewardedInterstitialManager.load(
                reactContext,      // context
                adId,      // adUnitId
                () -> {    // onLoaded callback
                    Log.d("AdDemo", "Ad is loaded and ready!");

                    RewardedInterstitialManager.show(
                            getCurrentActivity(),  // activity
                            adId,  // adId
                            reward -> { // onReward callback
                                Log.d("AdDemo", "Reward earned: " + reward.getAmount() + " " + reward.getType());
                            },
                            () -> { // onClosed callback
                                Log.d("AdDemo", "Ad closed by user");
                                onNotifyAdsResult.invoke(AdsStatus.AdsSuccess);
                            },
                            error -> { // onFailed callback (during show)
                                Log.e("AdDemo", "Failed to show ad: " + error);
                                onNotifyAdsResult.invoke(AdsStatus.AdsFailed);
                            }
                    );
                },
                error -> { // onFailed callback (during load)
                    Toast.makeText(reactContext, "Failed to show ad: " + error, Toast.LENGTH_SHORT).show();
                    Log.e("AdDemo", "Ad failed to load: " + error);
                    onNotifyAdsResult.invoke(AdsStatus.AdsFailed);
                }
        );
    }

    private void fetchPricePartsForSkus(
            List<String> inAppSkuId,
            String productType,
            Function1<? super Map<String, ?>, Unit> onNotifyPriceResult
    ) {
        BillingManager.getInstance().fetchPricePartsForSkus(
                getCurrentActivity(),         // activity
                inAppSkuId,   // skuIds
                productType,  // productType
                result -> reactContext.runOnUiQueueThread(() -> {
                    // Invoke the callback passed into the function
                    onNotifyPriceResult.invoke(result);
                })
        );
    }
}
