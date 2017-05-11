package com.yingyang.wallx.activities;

import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.yingyang.wallx.Config;
import com.yingyang.wallx.R;
import com.yingyang.wallx.cropper.CropImageView;

import java.io.IOException;

public class ActivitySetAsWallpaper extends AppCompatActivity {

    private CropImageView mCropImageView;
    String[] str_image, str_cat_name;
    int position;
    private InterstitialAd interstitialAd;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_as_wallpaper);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadInterstitialAd();

        Intent i = getIntent();
        str_image = i.getStringArrayExtra("WALLPAPER_IMAGE_URL");
        str_cat_name = i.getStringArrayExtra("WALLPAPER_IMAGE_CATEGORY");
        position = i.getIntExtra("POSITION_ID", 0);
        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);

        fab = (FloatingActionButton) findViewById(R.id.setAsWallpaper);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new SetWallpaperTask(ActivitySetAsWallpaper.this)).execute("");
            }
        });

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        ImageLoader.getInstance().loadImage(Config.SERVER_URL + "/upload/" + str_image[position], new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String arg0, View arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                // TODO Auto-generated method stub
                mCropImageView.setImageBitmap(arg2);
            }

            @Override
            public void onLoadingCancelled(String arg0, View arg1) {
                // TODO Auto-generated method stub

            }
        });

    }

    public class SetWallpaperTask extends AsyncTask<String, String, String> {
        private Context context;
        private ProgressDialog pDialog;
        Bitmap bmImg = null;

        public SetWallpaperTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            pDialog = new ProgressDialog(context);
            pDialog.setMessage(getResources().getString(R.string.please_wait));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            bmImg = mCropImageView.getCroppedImage();
            return null;
        }

        @Override
        protected void onPostExecute(String args) {
            // TODO Auto-generated method stub
            WallpaperManager wpm = WallpaperManager.getInstance(getApplicationContext()); // --The method context() is undefined for the type SetWallpaperTask
            try {
                wpm.setBitmap(bmImg);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            pDialog.dismiss();
            Toast.makeText(ActivitySetAsWallpaper.this, getResources().getString(R.string.wallpaper_set), Toast.LENGTH_SHORT).show();
            showInterstitialAd();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void loadInterstitialAd() {
        Log.d("TAG", "showAd");
        interstitialAd = new InterstitialAd(ActivitySetAsWallpaper.this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void showInterstitialAd() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

}
