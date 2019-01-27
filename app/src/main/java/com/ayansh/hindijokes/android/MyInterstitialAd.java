package com.ayansh.hindijokes.android;

import android.content.Context;
import android.os.Bundle;

import com.ayansh.hanudroid.Application;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by Varun Verma on 24 Nov 2016.
 */

public class MyInterstitialAd {

    private static InterstitialAd mInterstitialAd;
    private static String pub_id = "ca-app-pub-4571712644338430/7242130706";

    public static InterstitialAd getInterstitialAd(Context context){

        if(mInterstitialAd == null){

            MobileAds.initialize(context, "ca-app-pub-4571712644338430~6266849909");

            mInterstitialAd = new InterstitialAd(context);
            mInterstitialAd.setAdUnitId(pub_id);

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    requestNewInterstitial();
                }
            });

        }

        return mInterstitialAd;
    }

    public static void requestNewInterstitial(){

        if(mInterstitialAd == null){
            return;
        }

        Bundle extras = new Bundle();
        extras.putString("max_ad_content_rating", "G");

        AdRequest adRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        mInterstitialAd.loadAd(adRequest);

    }
}
