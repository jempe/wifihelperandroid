package com.julianogv.wifihelper.listeners;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by juliano.vieira on 18/03/14.
 */

public class InterstitialAdListener extends AdListener {
    InterstitialAd interstitialAd;

    public InterstitialAdListener(InterstitialAd interstitialAd){
        this.interstitialAd = interstitialAd;
    }
    @Override
    public void onAdLeftApplication() {
        super.onAdLeftApplication();
    }

    @Override
    public void onAdClosed() {
        super.onAdClosed();
    }

    @Override
    public void onAdOpened() {
        super.onAdOpened();
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
        interstitialAd.show();
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        super.onAdFailedToLoad(errorCode);
    }
}
