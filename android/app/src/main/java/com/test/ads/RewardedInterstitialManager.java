package com.test.ads;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

public final class RewardedInterstitialManager {

    private static final String TAG = "RewardedInterstitialAd";

    private static RewardedInterstitialAd rewardedInterstitialAd = null;
    private static boolean isLoading = false;

    private RewardedInterstitialManager() {}

    public interface VoidCallback { void call(); }
    public interface ErrorCallback { void call(String message); }
    public interface RewardCallback { void call(RewardItem rewardItem); }

    public static void load(Context context,
                            String adUnitId,
                            @Nullable VoidCallback onLoaded,
                            @Nullable ErrorCallback onFailed) {

        if (isLoading) return;

        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedInterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedInterstitialAd ad) {
                        rewardedInterstitialAd = ad;
                        isLoading = false;
                        if (onLoaded != null) onLoaded.call();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        rewardedInterstitialAd = null;
                        isLoading = false;
                        if (onFailed != null) onFailed.call(adError.getMessage());
                    }
                }
        );
    }

    public static void show(Activity activity,
                            String adId,
                            @Nullable RewardCallback onReward,
                            @Nullable VoidCallback onClosed,
                            @Nullable ErrorCallback onFailed) {

        RewardedInterstitialAd ad = rewardedInterstitialAd;
        if (ad == null) {
            if (onFailed != null) onFailed.call("Ad not ready");
            return;
        }

        ad.setFullScreenContentCallback(new FullScreenContentCallback() {

            @Override
            public void onAdShowedFullScreenContent() {
                rewardedInterstitialAd = null; // prevent reuse
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                if (onClosed != null) onClosed.call();
                // reload after dismissal
                load(activity.getApplicationContext(), adId, null, null);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                rewardedInterstitialAd = null;
                if (onFailed != null) onFailed.call(adError.getMessage());
            }
        });

        ad.show(activity, rewardItem -> {
            if (onReward != null) onReward.call(rewardItem);
        });
    }

    public static boolean isAdReady() {
        return rewardedInterstitialAd != null;
    }

    public static void clear() {
        rewardedInterstitialAd = null;
        isLoading = false;
    }
}