package com.test;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;

import com.gluedin.domain.entities.challengeDetail.widgetConfig.WidgetConfigDetails;
import com.google.gson.Gson;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.app.usecase.config.LaunchConfig;
import com.app.usecase.config.UserInfoAutoSignIn;
import com.app.usecase.constants.GluedInConstants;
import com.app.usecase.discover.DiscoverInteractor;
import com.app.usecase.config.AppConfigInteractor;
import com.app.usecase.challengeDetail.WidgetInteractor;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.app.usecase.config.LaunchConfig;
import com.gluedin.GluedInInitializer;
import com.gluedin.analytics.GluedInAnalyticsCallback;
import com.gluedin.callback.GIInitCallback;
import com.gluedin.callback.GISdkCallback;
import com.gluedin.callback.SDKInitStatus;
import com.gluedin.callback.UserAuthStatus;
import com.gluedin.data.persistence.analytics.AnalyticsEvents;
import com.gluedin.domain.entities.config.ShareData;
import com.gluedin.domain.entities.feed.VideoInfo;
import com.gluedin.domain.entities.feed.ads.AdsRequestParams;
import com.gluedin.domain.entities.feed.ads.AdsType;
import com.gluedin.exception.GluedInSdkException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.gluedin.domain.entities.feed.AssetsInformation;

import java.util.ArrayList;

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

        GluedInInitializer.Configurations gluedInConfigurations = new GluedInInitializer.Configurations.Builder().setLogEnabled(true, Log.DEBUG).setApiKey(API_KEY).setSecretKey(SECRET_KEY).setSdkInitCallback(initCallback).setBaseUrl(BASE_URL).setHttpLogEnabled(true, 3).setUserInfoForAutoSignIn(new UserInfoAutoSignIn(email, password, fullName, profilePic, "")).setFeedType(GluedInInitializer.Configurations.FeedType.VERTICAL).create();
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
                public void onPaywallActionClicked(@NonNull String seriesId, int currentEpisode, @NonNull String deeplink) {
                    Toast.makeText(context, "User clicked on Paywall: " + deeplink, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUserAuthStatus(@NonNull UserAuthStatus userAuthStatus, @Nullable VideoInfo videoInfo) {
                }

                @Override
                public void onShareAction(@NonNull ShareData shareData) {
                    Toast.makeText(context, "User clicked on share: " + shareData.getTitle(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProductClick(@NonNull String s, int i) {
                    Toast.makeText(context, "User clicked on Product: " + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUserProfileClick(@NonNull String s) {
                }

                @Nullable
                @Override
                public Fragment onAdsRequest(@NonNull AdsType adsType, @NonNull AdsRequestParams adsRequestParams) {
                    return null;
                }

                @Override
                public void onRewardClick() {
                }

                @Override
                public void onWatchNowAction(String deeplink) {
                }
            };

            GluedInInitializer.Configurations gluedInConfigurations = new GluedInInitializer.Configurations.Builder().setLogEnabled(true, Log.DEBUG).setApiKey(API_KEY).setSecretKey(SECRET_KEY).setSdkInitCallback(initCallback).setSdkCallback(sdkCallback).setBaseUrl(BASE_URL).setHttpLogEnabled(true, 3).setUserInfoForAutoSignIn(new UserInfoAutoSignIn(email, password, fullName, profilePic, "")).setFeedType(GluedInInitializer.Configurations.FeedType.VERTICAL).setSeriesDetails(seriesId, -1).setUserPersona(userPersona).enableBackButton(true).create();
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
                public void onPaywallActionClicked(@NonNull String seriesId, int currentEpisode, @NonNull String deeplink) {
                    Toast.makeText(context, "User clicked on Paywall: " + deeplink, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUserAuthStatus(@NonNull UserAuthStatus userAuthStatus, @Nullable VideoInfo videoInfo) {
                }

                @Override
                public void onShareAction(@NonNull ShareData shareData) {
                    Toast.makeText(context, "User clicked on share: " + shareData.getTitle(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProductClick(@NonNull String s, int i) {
                    Toast.makeText(context, "User clicked on Product: " + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUserProfileClick(@NonNull String s) {
                }

                @Nullable
                @Override
                public Fragment onAdsRequest(@NonNull AdsType adsType, @NonNull AdsRequestParams adsRequestParams) {
                    return null;
                }

                @Override
                public void onRewardClick() {
                }

                @Override
                public void onWatchNowAction(String deeplink) {
                }
            };

            GluedInInitializer.Configurations gluedInConfigurations = new GluedInInitializer.Configurations.Builder().setLogEnabled(true, Log.DEBUG).setApiKey(API_KEY).setSecretKey(SECRET_KEY).setSdkInitCallback(initCallback).setSdkCallback(sdkCallback).setBaseUrl(BASE_URL).setHttpLogEnabled(true, 3).setUserInfoForAutoSignIn(new UserInfoAutoSignIn(email, password, fullName, profilePic, "")).setFeedType(GluedInInitializer.Configurations.FeedType.VERTICAL).setCarouselDetails(GluedInConstants.CarouselType.NONE, selectedContentId, railList, false).enableBackButton(true).create();
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
            UserInfoAutoSignIn userInfo = new UserInfoAutoSignIn(email, password, fullName, profilePic, "");

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
                public void onPaywallActionClicked(@NonNull String s, int i, @NonNull String s1) {
                }

                @Override
                public void onUserAuthStatus(@NonNull UserAuthStatus userAuthStatus, @Nullable VideoInfo videoInfo) {
                }

                @Override
                public void onShareAction(@NonNull ShareData shareData) {
                    Toast.makeText(context, "User clicked on share: " + shareData.getTitle(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProductClick(@NonNull String s, int i) {
                    Toast.makeText(context, "User clicked on Product: " + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUserProfileClick(@NonNull String s) {
                }

                @Nullable
                @Override
                public Fragment onAdsRequest(@NonNull AdsType adsType, @NonNull AdsRequestParams adsRequestParams) {
                    return null;
                }

                @Override
                public void onRewardClick() {
                }

                @Override
                public void onWatchNowAction(String deeplink) {
                }
            };

            gluedInConfigurations = new GluedInInitializer.Configurations.Builder().setLogEnabled(true, Log.DEBUG).setApiKey(API_KEY).setSecretKey(SECRET_KEY).setSdkInitCallback(initCallback).setSdkCallback(sdkCallback).setBaseUrl(BASE_URL).setHttpLogEnabled(true, 3).setUserInfoForAutoSignIn(userInfo).enableBackButton(true).setFeedType(GluedInInitializer.Configurations.FeedType.VERTICAL).create();

            gluedInConfigurations.validateGluedInSDK(context, GluedInConstants.LaunchType.APP);

        });
    }

    @ReactMethod
    public void launchGluedInSDK(String apiKey, String secretKey, String baseUrl, String email, String password, String fullName, String profilePic, Promise promise) {

        ReactApplicationContext context = getReactApplicationContext();
        context.runOnUiQueueThread(() -> {
            UserInfoAutoSignIn userInfo = new UserInfoAutoSignIn(email, password, fullName, profilePic, "");
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
                public void onPaywallActionClicked(@NonNull String s, int i, @NonNull String s1) {
                }

                @Override
                public void onUserAuthStatus(@NonNull UserAuthStatus userAuthStatus, @Nullable VideoInfo videoInfo) {
                }

                @Override
                public void onShareAction(@NonNull ShareData shareData) {
                    Toast.makeText(context, "User clicked on share: " + shareData.getTitle(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProductClick(@NonNull String s, int i) {
                    Toast.makeText(context, "User clicked on Product: " + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUserProfileClick(@NonNull String s) {
                }

                @Nullable
                @Override
                public Fragment onAdsRequest(@NonNull AdsType adsType, @NonNull AdsRequestParams adsRequestParams) {
                    return null;
                }

                @Override
                public void onRewardClick() {
                }

                @Override
                public void onWatchNowAction(String deeplink) {
                }
            };

            GluedInInitializer.Configurations gluedInConfigurations = new GluedInInitializer.Configurations.Builder().setLogEnabled(true, Log.DEBUG).setApiKey(API_KEY).setSecretKey(SECRET_KEY).setSdkInitCallback(initCallback).setSdkCallback(sdkCallback).setBaseUrl(BASE_URL).setHttpLogEnabled(true, 3).setUserInfoForAutoSignIn(userInfo).enableBackButton(true).setFeedType(GluedInInitializer.Configurations.FeedType.VERTICAL).create();
            gluedInConfigurations.validateAndLaunchGluedInSDK(context, GluedInConstants.LaunchType.APP, intent, GluedInConstants.EntryPoint.NONE, null, null, null, null);

        });
    }
}
