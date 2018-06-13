package com.pixelapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.pixelapp.PixelApp;

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
			/*File file = new File(path);
			MediaScannerConnection.scanFile(
					getApplicationContext(),
					new String[]{file.getAbsolutePath()},
					null,
					new MediaScannerConnection.OnScanCompletedListener() {
						@Override
						public void onScanCompleted(String path, Uri uri) {
							Gdx.app.log("grokkingandroid",
									"file " + path + " was scanned seccessfully: " + uri);
						}
					});*/
		}
	};

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
			}
			if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
			}
		}

		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new PixelApp(platformOperations), config);
	}
}
