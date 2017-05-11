package com.yingyang.wallx.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yingyang.wallx.Config;
import com.yingyang.wallx.R;
import com.yingyang.wallx.firebase.Analytics;
import com.yingyang.wallx.utilities.DatabaseHandler;
import com.yingyang.wallx.utilities.DatabaseHandler.DatabaseManager;
import com.yingyang.wallx.utilities.Pojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ActivitySlideImage extends AppCompatActivity implements SensorEventListener {

    int position;
    String[] str_image, str_image_cat_name, str_image_id;
    public DatabaseHandler databaseHandler;
    ViewPager viewPager;
    int total_images;
    private SensorManager sensorManager;
    private boolean checkImage = false;
    private long lastUpdate;
    Handler handler;
    Runnable runnable;
    boolean Play_Flag = false;
    private Menu menu;
    private DatabaseManager databaseManager;
    String image_cat_name, image_url;
    DisplayImageOptions options;
    private AdView adView;
    private InterstitialAd interstitialAd;
    FloatingActionButton set_as_wallpaper, slideshow, share, save, zoom;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider_image);

        loadInterstitialAd();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHandler = new DatabaseHandler(this);
        databaseManager = DatabaseManager.INSTANCE;
        databaseManager.init(getApplicationContext());

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_thumbnail)
                .showImageOnFail(R.drawable.ic_thumbnail)
                .resetViewBeforeLoading(true)
                .cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        //Firebase LogEvent
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.analytics_item_id_2));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, getResources().getString(R.string.analytics_item_name_2));
        //bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Activity");

        //Logs an app event.
        Analytics.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        //Sets whether analytics collection is enabled for this app on this device.
        Analytics.getFirebaseAnalytics().setAnalyticsCollectionEnabled(true);

        //Sets the minimum engagement time required before starting a session. The default value is 10000 (10 seconds). Let's make it 5 seconds
        Analytics.getFirebaseAnalytics().setMinimumSessionDuration(5000);

        //Sets the duration of inactivity that terminates the current session. The default value is 1800000 (30 minutes). Let’s make it 10.
        Analytics.getFirebaseAnalytics().setSessionTimeoutDuration(1000000);

        //setTitle(JsonConfig.CATEGORY_TITLE);
        adView = (AdView) findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
            }

            @Override
            public void onAdFailedToLoad(int error) {
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }
        });

        set_as_wallpaper = (FloatingActionButton) findViewById(R.id.fab_set_as_wallpaper);
        slideshow = (FloatingActionButton) findViewById(R.id.fab_slideshow);
        share = (FloatingActionButton) findViewById(R.id.fab_share);
        save = (FloatingActionButton) findViewById(R.id.fab_save);
        zoom = (FloatingActionButton) findViewById(R.id.fab_zoom);


        set_as_wallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = viewPager.getCurrentItem();
                Intent intent = new Intent(getApplicationContext(), ActivitySetAsWallpaper.class);
                intent.putExtra("WALLPAPER_IMAGE_URL", str_image);
                intent.putExtra("WALLPAPER_IMAGE_CATEGORY", str_image_cat_name);
                intent.putExtra("POSITION_ID", position);
                startActivity(intent);
            }
        });

        slideshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Play_Flag) {
                    handler.removeCallbacks(runnable);
                    menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play));
                    Play_Flag = false;
                    ShowMenu();
                } else {
                    if (viewPager.getCurrentItem() == total_images) {
                        Toast.makeText(getApplicationContext(), "Currently Last Image!! Not Start Auto Play", Toast.LENGTH_SHORT).show();
                    } else {
                        AutoPlay();
                        menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_stop));
                        Play_Flag = true;
                        HideMenu();
                    }

                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = viewPager.getCurrentItem();
                (new ShareTask(ActivitySlideImage.this)).execute(Config.SERVER_URL + "/upload/" + str_image[position]);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = viewPager.getCurrentItem();
                (new SaveTask(ActivitySlideImage.this)).execute(Config.SERVER_URL + "/upload/" + str_image[position]);
                showInterstitialAd();
            }
        });

        zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                position = viewPager.getCurrentItem();
                Intent intent = new Intent(getApplicationContext(), ActivityPinchZoom.class);
                intent.putExtra("ZOOM_IMAGE_URL", str_image);
                intent.putExtra("ZOOM_IMAGE_CATEGORY", str_image_cat_name);
                intent.putExtra("POSITION_ID", position);
                startActivity(intent);
            }
        });


        Intent i = getIntent();
        position = i.getIntExtra("POSITION_ID", 0);
        str_image = i.getStringArrayExtra("IMAGE_ARRAY");
        str_image_cat_name = i.getStringArrayExtra("IMAGE_CATNAME");
        str_image_id = i.getStringArrayExtra("ITEMID");

        total_images = str_image.length - 1;
        viewPager = (ViewPager) findViewById(R.id.image_slider);
        handler = new Handler();

        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // TODO Auto-generated method stub

                position = viewPager.getCurrentItem();
                image_url = str_image[position];

                List<Pojo> list = databaseHandler.getFavRow(image_url);
                if (list.size() == 0) {
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_outline));
                } else {
                    if (list.get(0).getImageurl().equals(image_url)) {
                        menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int position) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int position) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_images, menu);
        this.menu = menu;
        //for when 1st item of view pager is favorite mode
        FirstFav();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.menu_back:

                position = viewPager.getCurrentItem();
                position--;
                if (position < 0) {
                    position = 0;
                }
                viewPager.setCurrentItem(position);

                return true;

            case R.id.menu_next:

                position = viewPager.getCurrentItem();
                position++;
                if (position == total_images) {
                    position = total_images;
                }
                viewPager.setCurrentItem(position);

                return true;

            case R.id.menu_play:

                if (Play_Flag) {
                    handler.removeCallbacks(runnable);
                    menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play));
                    Play_Flag = false;
                    ShowMenu();
                } else {
                    if (viewPager.getCurrentItem() == total_images) {
                        Toast.makeText(getApplicationContext(), "Currently Last Image!! Not Start Auto Play", Toast.LENGTH_SHORT).show();
                    } else {
                        AutoPlay();
                        menu.getItem(2).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_stop));
                        Play_Flag = true;
                        HideMenu();
                    }

                }
                return true;

            case R.id.menu_fav:

                position = viewPager.getCurrentItem();

                image_url = str_image[position];

                List<Pojo> list = databaseHandler.getFavRow(image_url);
                if (list.size() == 0) {
                    addtoFav(position);
                } else {
                    if (list.get(0).getImageurl().equals(image_url)) {
                        RemoveFav(position);
                    }

                }

                return true;

            case R.id.menu_share:

                position = viewPager.getCurrentItem();
                (new ShareTask(ActivitySlideImage.this)).execute(Config.SERVER_URL + "/upload/" + str_image[position]);

                return true;

            case R.id.menu_save:

                position = viewPager.getCurrentItem();
                (new SaveTask(ActivitySlideImage.this)).execute(Config.SERVER_URL + "/upload/" + str_image[position]);

                return true;

            case R.id.menu_setaswallaper:

                position = viewPager.getCurrentItem();
                Intent intent = new Intent(getApplicationContext(), ActivitySetAsWallpaper.class);
                intent.putExtra("WALLPAPER_IMAGE_URL", str_image);
                intent.putExtra("WALLPAPER_IMAGE_CATEGORY", str_image_cat_name);
                intent.putExtra("POSITION_ID", position);
                startActivity(intent);

                return true;

            case R.id.menu_zoom:
                position = viewPager.getCurrentItem();
                Intent int_zoom = new Intent(getApplicationContext(), ActivityPinchZoom.class);
                int_zoom.putExtra("ZOOM_IMAGE_URL", str_image);
                int_zoom.putExtra("ZOOM_IMAGE_CATEGORY", str_image_cat_name);
                int_zoom.putExtra("POSITION_ID", position);
                startActivity(int_zoom);

                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    //add to favorite
    public void addtoFav(int position) {

        image_cat_name = str_image_cat_name[position];
        image_url = str_image[position];

        databaseHandler.AddtoFavorite(new Pojo(image_cat_name, image_url));
        Toast.makeText(getApplicationContext(), "Added to Favorite", Toast.LENGTH_SHORT).show();
        menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));

    }

    //remove from favorite
    public void RemoveFav(int position) {
        image_url = str_image[position];
        databaseHandler.RemoveFav(new Pojo(image_url));
        Toast.makeText(getApplicationContext(), "Removed from Favorite", Toast.LENGTH_SHORT).show();
        menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_outline));

    }


    //auto play slide show

    public void AutoPlay() {
        runnable = new Runnable() {

            @Override
            public void run() {
                AutoPlay();
                // TODO Auto-generated method stub
                position = viewPager.getCurrentItem();
                position++;
                if (position == total_images) {
                    position = total_images;
                    handler.removeCallbacks(runnable);//when last image play mode goes to Stop
                    Toast.makeText(getApplicationContext(), "Last Image Auto Play Stoped", Toast.LENGTH_SHORT).show();
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play));
                    Play_Flag = false;
                    //Show All Menu when Auto Play Stop
                    ShowMenu();
                }
                viewPager.setCurrentItem(position);

            }
        };

        handler.postDelayed(runnable, 1500);
    }

    public void ShowMenu() {
        menu.getItem(0).setVisible(true);
        menu.getItem(2).setVisible(true);
        menu.getItem(0).setVisible(true);
        menu.getItem(4).setVisible(true);
    }

    public void HideMenu() {
        menu.getItem(0).setVisible(false);
        menu.getItem(2).setVisible(false);
        menu.getItem(0).setVisible(false);
        menu.getItem(4).setVisible(false);
    }

    public void FirstFav() {
        int first = viewPager.getCurrentItem();
        String Image_id = str_image[first];

        List<Pojo> pojolist = databaseHandler.getFavRow(Image_id);
        if (pojolist.size() == 0) {
            menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_outline));

        } else {
            if (pojolist.get(0).getImageurl().equals(Image_id)) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_star_white));

            }

        }
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImagePagerAdapter() {

            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return str_image.length;

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View imageLayout = inflater.inflate(R.layout.view_pager_item, container, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

            ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
            ImageLoader.getInstance().displayImage(Config.SERVER_URL + "/upload/" + str_image[position], imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    spinner.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message = null;
                    switch (failReason.getType()) {
                        case IO_ERROR:
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:
                            message = "Unknown error";
                            break;
                    }
                    Toast.makeText(ActivitySlideImage.this, message, Toast.LENGTH_SHORT).show();

                    spinner.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    spinner.setVisibility(View.GONE);
                }
            });

            container.addView(imageLayout, 0);
            return imageLayout;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = System.currentTimeMillis();
        if (accelationSquareRoot >= 2) //
        {
            if (actualTime - lastUpdate < 200) {
                return;
            }
            lastUpdate = actualTime;

            if (checkImage) {


                position = viewPager.getCurrentItem();
                viewPager.setCurrentItem(position);


            } else {

                position = viewPager.getCurrentItem();
                position++;
                if (position == total_images) {
                    position = total_images;
                }
                viewPager.setCurrentItem(position);
            }
            checkImage = !checkImage;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (databaseManager == null) {
            databaseManager = DatabaseManager.INSTANCE;
            databaseManager.init(getApplicationContext());
        } else if (databaseManager.isDatabaseClosed()) {
            databaseManager.init(getApplicationContext());
        }
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!databaseManager.isDatabaseClosed())
            databaseManager.closeDatabase();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        sensorManager.unregisterListener(this);
        if (databaseManager != null) databaseManager.closeDatabase();

    }

    public class SaveTask extends AsyncTask<String, String, String> {

        private Context context;
        private ProgressDialog pDialog;
        URL myFileUrl;
        Bitmap bmImg = null;
        File file;

        public SaveTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            pDialog = new ProgressDialog(context);
            pDialog.setMessage(getResources().getString(R.string.downloading));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected String doInBackground(String... args) {
            String as[] = null;
            try {
                myFileUrl = new URL(args[0]);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {

                String path = myFileUrl.getPath();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsolutePath() + "/" + getResources().getString(R.string.saved_folder_name) + "/");
                dir.mkdirs();
                String fileName = "Image_" + "_" + idStr;
                file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bmImg.compress(CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
                as = new String[1];
                as[0] = file.toString();

                MediaScannerConnection.scanFile(ActivitySlideImage.this, as, null, new android.media.MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String s1, Uri uri) {
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(String args) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
        }
    }

    public class ShareTask extends AsyncTask<String, String, String> {
        private Context context;
        private ProgressDialog pDialog;
        String image_url;
        URL myFileUrl;
        String myFileUrl1;
        Bitmap bmImg = null;
        File file;

        public ShareTask(Context context) {
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

            try {

                myFileUrl = new URL(args[0]);
                HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                bmImg = BitmapFactory.decodeStream(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {

                String path = myFileUrl.getPath();
                String idStr = path.substring(path.lastIndexOf('/') + 1);
                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsolutePath() + "/" + getResources().getString(R.string.saved_folder_name) + "/");
                dir.mkdirs();
                String fileName = idStr;
                file = new File(dir, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                bmImg.compress(CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String args) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
            startActivity(Intent.createChooser(share, "Share Image"));
            pDialog.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void loadInterstitialAd() {
        Log.d("TAG", "showAd");
        interstitialAd = new InterstitialAd(ActivitySlideImage.this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void showInterstitialAd() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

}
