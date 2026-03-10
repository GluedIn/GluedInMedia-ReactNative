package com.test.ads;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.test.R;
import com.gluedin.base.presentation.customView.GIAdsFragment;
import com.gluedin.domain.entities.feed.ads.AdsRequestParams;
import com.gluedin.domain.entities.feed.ads.NativeAdsType;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;

import java.util.Collections;
import java.util.Map;


public class NativeAdJavaFragment extends GIAdsFragment {
    private View rootView;
    private NativeAd nativeAd = null;
    private NativeAdsType adsType = null;
    private AdsRequestParams adsRequestParams = null;
    private VideoController videoController = null;
    public Boolean isAdsLoad = false;
    private Context context;
    private NativeCustomFormatAd nativeCustomFormatAd;

    // View references for manual binding
    private View adMobContainer;
    private View adGamContainer;
    private View feedShimmer;
    private ImageView plusSawFeedPlay;
    private FrameLayout gamAdFrame;

    public NativeAdJavaFragment(NativeAdsType adsType, AdsRequestParams adsRequestParams, Context context) {
        this.adsType = adsType;
        this.adsRequestParams = adsRequestParams;
        this.context = context;

        RequestConfiguration configuration = new RequestConfiguration.Builder()
                .setTestDeviceIds(Collections.singletonList("B976D1E523402F533A0D69646F8B39FF"))
                .build();
        MobileAds.setRequestConfiguration(configuration);

        if (adsType != null && NativeAdsType.AD_MOB_NATIVE.name().equals(adsType.name())) {
            loadAds();
        } else {
            loadGamAds(false);
        }
    }

    public NativeAdJavaFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.native_ads_vertical, container, false);

        // Initialize View references
        // Note: Replace these IDs with the actual IDs in your native_ads_vertical.xml and sub-layouts
        adMobContainer = rootView.findViewById(R.id.ad_mob);
        adGamContainer = rootView.findViewById(R.id.ad_gam);
        feedShimmer = rootView.findViewById(R.id.feedShimmer);
        plusSawFeedPlay = rootView.findViewById(R.id.plus_saw_feed_play);
        gamAdFrame = rootView.findViewById(R.id.ad_frame);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adsType != null && NativeAdsType.AD_MOB_NATIVE.name().equals(adsType.name())) {
            if (adMobContainer != null) adMobContainer.setVisibility(View.VISIBLE);
            if (adGamContainer != null) adGamContainer.setVisibility(View.GONE);
            if (nativeAd != null) {
                populateNativeAdsView(nativeAd);
            }
            if (feedShimmer != null) feedShimmer.setVisibility(View.GONE);
        } else {
            if (adMobContainer != null) adMobContainer.setVisibility(View.GONE);
            if (adGamContainer != null) adGamContainer.setVisibility(View.VISIBLE);
            if (nativeCustomFormatAd != null) {
                if (videoController != null) {
                    videoController.play();
                    if (plusSawFeedPlay != null) plusSawFeedPlay.setVisibility(View.GONE);
                } else {
                    displayCustomFormatAd(nativeCustomFormatAd);
                }
            } else {
                loadGamAds(true);
            }
        }
    }

    private void loadAds() {
        if (context == null) return;

        String adMobUnitId = adsRequestParams != null ? adsRequestParams.getAdsId() : "";
        Bundle extras = new Bundle();
        if (adsRequestParams != null && adsRequestParams.getConfigCustomParams() != null) {
            for (Map.Entry<String, String> entry : adsRequestParams.getConfigCustomParams().entrySet()) {
                extras.putString(entry.getKey(), entry.getValue());
            }
        }

        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();

        AdLoader.Builder builder = new AdLoader.Builder(context, adMobUnitId);
        builder.forNativeAd(nativeAds -> {
            if (nativeAds == null) return;
            setAdLoadStatus(true);
            nativeAd = nativeAds;
        });

        VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                setAdLoadStatus(false);
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to load: " + loadAdError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                setAdLoadStatus(true);
            }
        }).build();

        adLoader.loadAd(adRequest);
    }

    private void populateNativeAdsView(NativeAd nativeAd) {
        // Find the NativeAdView from the root or container
        NativeAdView adView = rootView.findViewById(R.id.adView);
        if (adView == null) return;

        adView.setMediaView(adView.findViewById(R.id.ad_media));
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        if (adView.getMediaView() != null) {
            adView.getMediaView().setMediaContent(nativeAd.getMediaContent());
        }

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.GONE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.GONE);
        } else {
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            adView.getCallToActionView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.GONE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.GONE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoController != null) {
            videoController.pause();
            if (plusSawFeedPlay != null) plusSawFeedPlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        if (nativeAd != null) nativeAd.destroy();
        if (nativeCustomFormatAd != null) nativeCustomFormatAd.destroy();
        if (videoController != null) videoController.stop();

        if (gamAdFrame != null) {
            gamAdFrame.removeAllViews();
        }
        rootView = null;
        super.onDestroyView();
    }

    // Stub for displayCustomFormatAd and loadGamAds as they were cut off in original
    private void displayCustomFormatAd(NativeCustomFormatAd ad) { }
    private void loadGamAds(Boolean isReloaded) { }
}