package com.pixelpalette;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;

public class AndroidLauncher extends AndroidApplication {
	private final PlatformOperations platformOperations = new PlatformOperations() {
		@Override
		public void runScanner(String path) {
			File file = new File(Gdx.files.getExternalStoragePath() + path);
			if(!file.exists()) {
				Gdx.app.log("Bad Location", "(");
			}
			Uri uri = Uri.fromFile(file);
			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intent.setData(uri);
			sendBroadcast(intent);
		}

		@Override
		public void showAd() {
			AndroidLauncher.this.runOnUiThread(new Runnable() {
				@Override public void run() {
					if(interstitialAd.isLoaded())
					{
						interstitialAd.show();
					}
				}
			});
		}

		@Override
		public void askPermissions() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (AndroidLauncher.this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
					AndroidLauncher.this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
				}
				if (AndroidLauncher.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
					AndroidLauncher.this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
				}
			}
		}
	};

	private static final String TAG = "AndroidLauncher";
	//protected AdView adView;
	protected InterstitialAd interstitialAd;
	// ca-app-pub-8883079456205295~9414673447 : App ID
	// ca-app-pub-8883079456205295/3399929432 : Ad ID

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		platformOperations.askPermissions();
		super.onCreate(savedInstanceState);

		RelativeLayout layout = new RelativeLayout(this);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		View gameView = initializeForView(new com.pixelpalette.PixelApp(platformOperations), config);
		layout.addView(gameView);

		interstitialAd = new InterstitialAd(this);
		interstitialAd.setAdListener(new AdListener()
		{
			@Override
			public void onAdClosed() {
				AdRequest.Builder builder = new AdRequest.Builder();
				//builder.addTestDevice("A9DCF2CC33097ACF310E563A44E7801F");
				interstitialAd.loadAd(builder.build());
			}
		});
		interstitialAd.setAdUnitId("ca-app-pub-8883079456205295/3399929432");

		/*adView = new AdView(this);
		adView.setAdListener(new AdListener(){
			@Override
			public void onAdLoaded() {
				Log.i(TAG, "Ad Loaded");
			}
		});
		adView.setAdSize(AdSize.SMART_BANNER);
		adView.setAdUnitId("ca-app-pub-8883079456205295/2816108522");*/
		/*RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
		);*/
		/*layout.addView(adView, adParams);
		adView.loadAd(builder.build());*/

		AdRequest.Builder builder = new AdRequest.Builder();
		//builder.addTestDevice("A9DCF2CC33097ACF310E563A44E7801F");
		interstitialAd.loadAd(builder.build());

		setContentView(layout);
	}
}
