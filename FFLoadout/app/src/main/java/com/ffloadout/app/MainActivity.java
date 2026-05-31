package com.ffloadout.app;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    // ── Your AdMob IDs ──
    private static final String AD_UNIT_ID = "ca-app-pub-7729356342196661/8406667301";

    private WebView webView;
    private RewardedAd rewardedAd;
    private boolean adIsLoading = false;
    private String pendingAction = "";   // "apply" or "launch"
    private Dialog loadingDialog;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen / immersive
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        // Init AdMob
        MobileAds.initialize(this, initializationStatus -> loadRewardedAd());

        // Setup WebView
        webView = findViewById(R.id.webview);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Add JavaScript bridge so HTML buttons call Java
        webView.addJavascriptInterface(new AdBridge(), "AndroidAds");

        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(Color.parseColor("#0a0c10"));

        // Load HTML from assets
        webView.loadUrl("file:///android_asset/freefire_loadout.html");
    }

    // ── Load rewarded ad ──
    private void loadRewardedAd() {
        if (adIsLoading || rewardedAd != null) return;
        adIsLoading = true;

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, AD_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                adIsLoading = false;
                setupFullScreenCallback();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                rewardedAd = null;
                adIsLoading = false;
                // Retry after 5 seconds
                webView.postDelayed(() -> loadRewardedAd(), 5000);
            }
        });
    }

    // ── Set up what happens after ad ──
    private void setupFullScreenCallback() {
        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // Ad closed WITHOUT earning reward — do nothing
                rewardedAd = null;
                loadRewardedAd(); // preload next ad
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                rewardedAd = null;
                loadRewardedAd();
                // If ad fails to show, still complete the action
                runOnUiThread(() -> completeAction(pendingAction));
            }
        });
    }

    // ── Show the rewarded ad ──
    private void showRewardedAd(String action) {
        pendingAction = action;

        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                // User finished watching → complete the action
                runOnUiThread(() -> completeAction(action));
            });
        } else {
            // Ad not ready yet — show loading dialog and wait
            showWaitingDialog();
            loadRewardedAd();
            // Poll until ready (max 10 seconds)
            checkAdReady(0, action);
        }
    }

    private void checkAdReady(int attempt, String action) {
        if (attempt > 20) {
            dismissWaitingDialog();
            // Ad never loaded — still let user proceed (optional: block instead)
            runOnUiThread(() -> completeAction(action));
            return;
        }
        webView.postDelayed(() -> {
            if (rewardedAd != null) {
                dismissWaitingDialog();
                rewardedAd.show(this, rewardItem -> {
                    runOnUiThread(() -> completeAction(action));
                });
            } else {
                checkAdReady(attempt + 1, action);
            }
        }, 500);
    }

    // ── What happens AFTER the ad is watched ──
    private void completeAction(String action) {
        switch (action) {
            case "apply":
                // Tell the HTML to apply the loadout
                webView.evaluateJavascript("applyLoadout()", null);
                showToast("✅ Loadout Applied!");
                break;

            case "launch":
                // Open Free Fire
                launchFreeFire();
                break;
        }
        // Preload next ad
        loadRewardedAd();
    }

    // ── Launch Free Fire ──
    private void launchFreeFire() {
        try {
            android.content.Intent intent = getPackageManager()
                .getLaunchIntentForPackage("com.dts.freefireth");
            if (intent == null) {
                // Try the MAX version
                intent = getPackageManager()
                    .getLaunchIntentForPackage("com.dts.freefiremax");
            }
            if (intent != null) {
                startActivity(intent);
            } else {
                // Not installed — open Play Store
                startActivity(new android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("market://details?id=com.dts.freefireth")
                ));
            }
        } catch (Exception e) {
            showToast("Free Fire not installed");
        }
    }

    // ── Loading dialog ──
    private void showWaitingDialog() {
        runOnUiThread(() -> {
            loadingDialog = new Dialog(this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.dialog_loading);
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            loadingDialog.setCancelable(false);
            loadingDialog.show();
        });
    }

    private void dismissWaitingDialog() {
        runOnUiThread(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    // ── JavaScript Bridge ──
    public class AdBridge {
        @JavascriptInterface
        public void showAdForApply() {
            runOnUiThread(() -> showRewardedAd("apply"));
        }

        @JavascriptInterface
        public void showAdForLaunch() {
            runOnUiThread(() -> showRewardedAd("launch"));
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        // Reload ad if none available
        if (rewardedAd == null && !adIsLoading) {
            loadRewardedAd();
        }
    }
}
