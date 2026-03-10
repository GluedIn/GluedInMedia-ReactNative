package com.test.ads;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

/**
 * Interstitial Ad Object Class
 * Using Google AdMob SDK
 */
public class InterstitialAdManager {

    private static InterstitialAdManager instance;
    private InterstitialAd interstitialAd = null;
    private boolean isLoading = false;

    private InterstitialAdManager() {
        // Private constructor for singleton
    }

    public static synchronized InterstitialAdManager getInstance() {
        if (instance == null) {
            instance = new InterstitialAdManager();
        }
        return instance;
    }

    /**
     * Interface to handle the ad closed callback in Java
     */
    public interface OnAdClosedListener {
        void onAdClosed();
    }

    /**
     * Call this ONE function anywhere
     */
    public void loadAndShow(@NonNull Activity activity, @NonNull String adUnitId, @Nullable OnAdClosedListener onAdClosed) {
        // Ad already ready → show immediately
        if (interstitialAd != null) {
            showAd(activity, adUnitId, interstitialAd, onAdClosed);
            return;
        }

        // Ad is loading → skip waiting and continue flow
        if (isLoading) {
            if (onAdClosed != null) {
                onAdClosed.onAdClosed();
            }
            return;
        }

        // Load ad
        isLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(
                activity,
                adUnitId,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        isLoading = false;
                        interstitialAd = ad;
                        showAd(activity, adUnitId, ad, onAdClosed);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        isLoading = false;
                        interstitialAd = null;
                        if (onAdClosed != null) {
                            onAdClosed.onAdClosed();
                        }
                    }
                }
        );
    }

    private void showAd(Activity activity, String adUnitId, InterstitialAd ad, OnAdClosedListener onAdClosed) {
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                interstitialAd = null;
                preload(activity, adUnitId);
                if (onAdClosed != null) {
                    onAdClosed.onAdClosed();
                }
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                interstitialAd = null;
                if (onAdClosed != null) {
                    onAdClosed.onAdClosed();
                }
            }
        });

        ad.show(activity);
    }

    private void preload(Context context, String adUnitId) {
        if (isLoading || interstitialAd != null) return;

        isLoading = true;
        InterstitialAd.load(
                context,
                adUnitId,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                        isLoading = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        isLoading = false;
                    }
                }
        );
    }
}
