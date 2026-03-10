package com.test.ads;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.gluedin.domain.entities.feed.ads.AdsRequestParams;
import com.gluedin.domain.entities.feed.ads.BannerAdsType;
import com.gluedin.view.BannerAdView;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

import java.util.Collections;
import java.util.Map;

public class BannerAdLoader {

    @Nullable private final Context context;
    private final BannerAdView bannerAdView;
    private final BannerAdsType adsType;
    private final AdsRequestParams adsRequestParams;

    public BannerAdLoader(@Nullable Context context,
                          BannerAdView bannerAdView,
                          BannerAdsType adsType,
                          AdsRequestParams adsRequestParams) {
        this.context = context;
        this.bannerAdView = bannerAdView;
        this.adsType = adsType;
        this.adsRequestParams = adsRequestParams;
    }

    public void loadAd() {
        if (adsType == BannerAdsType.AD_MOB_BANNER) {
            loadAdMobAds();
        } else if (adsType == BannerAdsType.GAM_BANNER) {
            loadGamAds();
        }
    }

    private void loadAdMobAds() {
        Bundle extras = new Bundle();
        Map<String, String> params = adsRequestParams.getConfigCustomParams();
        if (params == null) params = Collections.emptyMap();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            extras.putString(entry.getKey(), entry.getValue());
        }

        if (context == null) return;

        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adsRequestParams.getAdsId());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                bannerAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                bannerAdView.setVisibility(View.GONE);
            }
        });

        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();

        bannerAdView.removeAllViews();
        bannerAdView.setAdView(adView);
        adView.loadAd(adRequest);
    }

    private void loadGamAds() {
        if (context == null) return;

        AdManagerAdView adView = new AdManagerAdView(context);
        adView.setAdUnitId(adsRequestParams.getAdsId());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                bannerAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                bannerAdView.setVisibility(View.GONE);
            }
        });

        AdManagerAdRequest.Builder requestBuilder = new AdManagerAdRequest.Builder();

        Map<String, String> params = adsRequestParams.getConfigCustomParams();
        if (params == null) params = Collections.emptyMap();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            requestBuilder.addCustomTargeting(entry.getKey(), entry.getValue());
        }

        bannerAdView.removeAllViews();
        bannerAdView.setAdView(adView);
        adView.loadAd(requestBuilder.build());
    }
}