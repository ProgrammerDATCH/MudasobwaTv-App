package com.mudasobwatv;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class HomeActivity extends AppCompatActivity {
    ImageView imgNtakomisiyo;
    ImageView errorImg;
    Button errorReflesh;
    TextView errorTxt;
    WebView webView;

    SwipeRefreshLayout mySwipeRefreshLayout;
    private RewardedAd rewardedAd;
    private AdView mAdView;
    boolean isAdLoading = false;
    boolean isFirstAdsLoaded = false;


    private static final int MY_REQUEST_CODE = 107;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        loadRewardedAds();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        checkAppUpdate();


        //runADs every Minute
        HandlerThread handlerThread = new HandlerThread("AdsThread");
        handlerThread.start();

        Handler adsHandler = new Handler(handlerThread.getLooper());
        Runnable runningAds = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showRewardedAds();
                    }
                });
                adsHandler.postDelayed(this, 40000);
            }
        };


        // Start the initial execution of the Runnable
        adsHandler.post(runningAds);


        mySwipeRefreshLayout = (SwipeRefreshLayout)this.findViewById(R.id.swipeContainer);
        webView = findViewById(R.id.webView);
        imgNtakomisiyo = findViewById(R.id.imgNtakomisiyo);
        errorImg = findViewById(R.id.errorImg);
        errorReflesh = findViewById(R.id.errorRefleshBtn);
        errorTxt = findViewById(R.id.errorTxt);
        errorReflesh.setVisibility(View.GONE);
        errorTxt.setVisibility(View.GONE);
        errorImg.setVisibility(View.GONE);
        webView.getSettings().setJavaScriptEnabled(true);
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);
        webView.loadUrl("https://mudasobwatv.000webhostapp.com/");

        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
//        webView.setWebViewClient(new WebViewClientDemo());
        //start trying



        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        webView.reload();
                    }
                }
        );
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

//                Toast.makeText(HomeActivity.this, "Permission denied to WRITE_EXTERNAL_STORAGE - requesting it", Toast.LENGTH_SHORT).show();
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,1);
            }


        }

        //handle downloading

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie",cookies);
                request.addRequestHeader("User-Agent",userAgent);
                request.setDescription("Downloading file....");
                request.setTitle(URLUtil.guessFileName(url,contentDisposition,mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                showRewardedAds();
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(),"Downloading File from Mudasobwa TV App",Toast.LENGTH_SHORT).show();

            }
        });






        //end trying
        Animation zoomInOut = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        zoomInOut.setDuration(1000);
        zoomInOut.setRepeatCount(Animation.INFINITE);
        zoomInOut.setRepeatMode(Animation.REVERSE);
        imgNtakomisiyo.startAnimation(zoomInOut);

        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
                imgNtakomisiyo.startAnimation(zoomInOut);
                imgNtakomisiyo.setVisibility(View.VISIBLE);

                errorImg.setVisibility(View.GONE);
                errorReflesh.setVisibility(View.GONE);
                errorTxt.setVisibility(View.GONE);
            }




            @Override
            public boolean  shouldOverrideUrlLoading(WebView view, String url) {

                //testing this..
//                view.loadUrl(url);
                if(url != null && (url.contains("shorten.world") || url.contains("ln.run")))
                {
                    //showRewardedAds();
                    showRewardedAds();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                    return true;
                    //then prohibit the user from continue to be in the App but show Ads on resume.
                }

                if (url != null && url.startsWith("whatsapp://")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    Toast.makeText(HomeActivity.this, "NtaKomisiyo on WhatsApp...", Toast.LENGTH_SHORT).show();
                    startActivity(browserIntent);
                    return true;

                }
                else if (url != null && url.startsWith("https://www.youtube.com")) {
                    Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    Toast.makeText(HomeActivity.this, "Programmer DATCH on YouTube", Toast.LENGTH_SHORT).show();
                    startActivity(ytIntent);
                    return true;

                }
                else if (url != null && url.startsWith("https://play.google.com/")) {
                    Intent psIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    Toast.makeText(HomeActivity.this, "NtaKomisiyo App on PlayStore", Toast.LENGTH_SHORT).show();
                    startActivity(psIntent);
                    return true;

                }
                else if (url != null && url.startsWith("https://m.facebook.com")) {
                    Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    Toast.makeText(HomeActivity.this, "Programmer DATCH on Facebook", Toast.LENGTH_SHORT).show();
                    startActivity(fbIntent);
                    return true;

                }
                else if (url != null && url.startsWith("https://twitter")) {
                    Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    Toast.makeText(HomeActivity.this, "Programmer DATCH on Twitter", Toast.LENGTH_SHORT).show();
                    startActivity(twitterIntent);
                    return true;

                }
                else if (url != null && url.startsWith("https://www.instagram.com")) {
                    Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    Toast.makeText(HomeActivity.this, "Programmer DATCH on Instagram", Toast.LENGTH_SHORT).show();
                    startActivity(twitterIntent);
                    return true;

                }
                else {
                    return false;
                }
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                imgNtakomisiyo.clearAnimation();
                imgNtakomisiyo.setVisibility(View.GONE);
                mySwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo == null || !networkInfo.isConnected())
                {
                    Toast.makeText(HomeActivity.this, "Network Disconnected!", Toast.LENGTH_LONG).show();
                    errorTxt.setText("Network Disconnected!, Connect again & Reflesh");
                }
                else
                {
                    errorTxt.setText("Page Loading Failed!, Check Network & Reflesh");
                }

                errorImg.setVisibility(View.VISIBLE);
                errorReflesh.setVisibility(View.VISIBLE);
                errorTxt.setVisibility(View.VISIBLE);
                errorReflesh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String currentLink = view.getUrl();
                        webView.loadUrl(currentLink);
                    }
                });
            }

        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent)
            {
                if (keyEvent.getAction() == keyEvent.ACTION_DOWN) {
                    if(i == KeyEvent.KEYCODE_BACK)
                    {
                        if (webView.canGoBack()) {
                            webView.goBack();
                        }
                        else
                        {
                            webView.loadUrl("https://mudasobwatv.000webhostapp.com/");
                        }
                    }
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                HomeActivity.this.filePathCallback = filePathCallback;
                showFileChooser();
                return true;
            }
        });


    }

    private void showRewardedAds() {
        if (rewardedAd != null) {
            Activity activityContext = HomeActivity.this;
            rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    Toast.makeText(activityContext, "Thank you for Supporting us!", Toast.LENGTH_SHORT).show();
                }
            });
            loadRewardedAds();
        }
        else {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo == null || !networkInfo.isConnected())
            {
                Toast.makeText(HomeActivity.this, "Turn on DATA Connection or WiFi please!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //Toast.makeText(HomeActivity.this, "You may have poor connection!", Toast.LENGTH_SHORT).show();
                if(!isAdLoading)
                {
                    //Toast.makeText(HomeActivity.this, "Never loaded", Toast.LENGTH_SHORT).show();
                    loadRewardedAds();
                }
            }
        }
    }

    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private ValueCallback<Uri[]> filePathCallback;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        Intent chooser = Intent.createChooser(intent, "Choose Image");
        startActivityForResult(chooser, FILE_CHOOSER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MY_REQUEST_CODE)
        {
            if(resultCode != RESULT_OK)
            {
                Toast.makeText(this, "Update Failed. Error: " + resultCode, Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(this) //alert the person knowing they are about to close
                        .setTitle("UPDATE Required")
                        .setMessage("You must Update App to continue!")
                        .setCancelable(false)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkAppUpdate();
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //exit
                                finish();
                            }
                        })
                        .show();
            }
            else
            {
                Toast.makeText(this, "App Updated Successfully!", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == FILE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
        } else {
            filePathCallback.onReceiveValue(null);
        }

        filePathCallback = null;
        super.onActivityResult(requestCode, resultCode, data);
    }



//    private class WebViewClientDemo extends WebViewClient {
//        @Override
//        //Keep webview in app when clicking links
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.loadUrl(url);
//            return true;
//        }
//
//    }

    //set back button functionality
    @Override
    public void onBackPressed() { //if user presses the back button do this
        if (webView.isFocused() && webView.canGoBack()) { //check if in webview and the user can go back
            webView.goBack(); //go back in webview
        } else { //do this if the webview cannot go back any further


            //Show new Ads
            showRewardedAds();

            new AlertDialog.Builder(this) //alert the person knowing they are about to close
                    .setTitle("EXIT")
                    .setMessage("Are you sure. You want to close this app?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            showRewardedAds();
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }



    private void loadRewardedAds() {
        if(rewardedAd == null)
        {
            isAdLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(this, "ca-app-pub-4379611405694352/2689869524",
                    adRequest, new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            rewardedAd = null;
                            isAdLoading = false;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd ad) {
                            rewardedAd = ad;
                            if(!isFirstAdsLoaded)
                            {
                                showRewardedAds();
                                isFirstAdsLoaded = true;
                            }
//                            if(isOnResume)
//                            {
//                                showRewardedAds();
//                                isOnResume = false;
//                            }
                            isAdLoading = false;

                            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdClicked() {
                                    // Called when a click is recorded for an ad.
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    rewardedAd = null;
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
                                    // Called when ad fails to show.
                                    rewardedAd = null;
                                }

                                @Override
                                public void onAdImpression() {
                                    // Called when an impression is recorded for an ad.
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Called when ad is shown.
                                }
                            });
                        }
                    });
        }
    }

    private void checkAppUpdate()
    {
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            MY_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        //show new Ads
//        isOnResume = true;
//        loadRewardedAds();
//    }
//
}