package com.yingyang.wallx.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yingyang.wallx.Config;
import com.yingyang.wallx.R;
import com.yingyang.wallx.adapters.AdapterItemByCategory;
import com.yingyang.wallx.json.JsonConfig;
import com.yingyang.wallx.json.JsonUtils;
import com.yingyang.wallx.models.ItemWallpaperByCategory;
import com.yingyang.wallx.utilities.DatabaseHandlerCateList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityWallpaperByCategory extends AppCompatActivity {

    GridView gridView;
    List<ItemWallpaperByCategory> itemWallpaperByCategories;
    AdapterItemByCategory adapterItemByCategory;
    ArrayList<String> list_image, image_cat_name, image_id;
    String[] str_list_image, str_image_cat_name, str_image_id;
    private int columnWidth;
    JsonUtils util;
    AdView mAdView;
    public DatabaseHandlerCateList databaseHandlerCateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_by_category);
        setTitle(JsonConfig.CATEGORY_TITLE);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHandlerCateList = new DatabaseHandlerCateList(ActivityWallpaperByCategory.this);

        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
            }

            @Override
            public void onAdFailedToLoad(int error) {
                mAdView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }
        });

        gridView = (GridView) findViewById(R.id.category_grid);
        itemWallpaperByCategories = new ArrayList<ItemWallpaperByCategory>();

        list_image = new ArrayList<String>();
        image_cat_name = new ArrayList<String>();
        image_id = new ArrayList<String>();

        str_list_image = new String[list_image.size()];
        str_image_cat_name = new String[image_cat_name.size()];
        str_image_id = new String[image_id.size()];

        util = new JsonUtils(getApplicationContext());

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Intent intent = new Intent(getApplicationContext(), ActivitySlideImage.class);
                intent.putExtra("POSITION_ID", position);
                intent.putExtra("IMAGE_ARRAY", str_list_image);
                intent.putExtra("IMAGE_CATNAME", str_image_cat_name);
                intent.putExtra("ITEMID", str_image_id);

                startActivity(intent);

            }
        });

        if (JsonUtils.isNetworkAvailable(ActivityWallpaperByCategory.this)) {
            new MyTask().execute(Config.SERVER_URL + "/api.php?cat_id=" + JsonConfig.CATEGORY_ID);
        } else {
            itemWallpaperByCategories = databaseHandlerCateList.getFavRow(JsonConfig.CATEGORY_ID);
            if (itemWallpaperByCategories.size() == 0) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_first_load), Toast.LENGTH_SHORT).show();
            }
            setAdapterToListView();
            for (int j = 0; j < itemWallpaperByCategories.size(); j++) {

                ItemWallpaperByCategory objCategoryBean = itemWallpaperByCategories.get(j);

                list_image.add(objCategoryBean.getItemImageurl());
                str_list_image = list_image.toArray(str_list_image);

                image_cat_name.add(objCategoryBean.getItemCategoryName());
                str_image_cat_name = image_cat_name.toArray(str_image_cat_name);

                image_id.add(objCategoryBean.getItemCatId());
                str_image_id = image_id.toArray(str_image_id);

            }
        }
    }

    private class MyTask extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ActivityWallpaperByCategory.this);
            pDialog.setMessage(getResources().getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                ActivityWallpaperByCategory.this.finish();
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConfig.CATEGORY_ITEM_ARRAY);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemWallpaperByCategory objItem = new ItemWallpaperByCategory();

                        databaseHandlerCateList.AddtoFavoriteCateList(new ItemWallpaperByCategory(objJson.getString(JsonConfig.CATEGORY_ITEM_CATNAME), objJson.getString(JsonConfig.CATEGORY_ITEM_IMAGEURL), objJson.getString(JsonConfig.CATEGORY_ITEM_CATID)));
                        Log.e("og", "" + objJson.getString(JsonConfig.CATEGORY_ITEM_CATNAME));
                        Log.e("og", "" + objJson.getString(JsonConfig.CATEGORY_ITEM_IMAGEURL));
                        Log.e("og", "" + objJson.getString(JsonConfig.CATEGORY_ITEM_CATID));
                        objItem.setItemCategoryName(objJson.getString(JsonConfig.CATEGORY_ITEM_CATNAME));
                        objItem.setItemImageurl(objJson.getString(JsonConfig.CATEGORY_ITEM_IMAGEURL));
                        objItem.setItemCatId(objJson.getString(JsonConfig.CATEGORY_ITEM_CATID));

                        itemWallpaperByCategories.add(objItem);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int j = 0; j < itemWallpaperByCategories.size(); j++) {

                    ItemWallpaperByCategory objCategoryBean = itemWallpaperByCategories.get(j);

                    list_image.add(objCategoryBean.getItemImageurl());
                    str_list_image = list_image.toArray(str_list_image);

                    image_cat_name.add(objCategoryBean.getItemCategoryName());
                    str_image_cat_name = image_cat_name.toArray(str_image_cat_name);

                    image_id.add(objCategoryBean.getItemCatId());
                    str_image_id = image_id.toArray(str_image_id);

                }

                setAdapterToListView();
            }
        }
    }

    public void setAdapterToListView() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adapterItemByCategory = new AdapterItemByCategory(ActivityWallpaperByCategory.this, R.layout.lsv_item_grid_wallpaper, itemWallpaperByCategories, columnWidth);
        } else {
            adapterItemByCategory = new AdapterItemByCategory(ActivityWallpaperByCategory.this, R.layout.lsv_item_grid_wallpaper_pre, itemWallpaperByCategories, columnWidth);
        }
            gridView.setAdapter(adapterItemByCategory);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    protected void onPause() {
        mAdView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }

}
