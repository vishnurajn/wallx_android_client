package com.yingyang.wallx.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.yingyang.wallx.Config;
import com.yingyang.wallx.R;
import com.yingyang.wallx.activities.ActivityWallpaperByCategory;
import com.yingyang.wallx.adapters.AdapterCategory;
import com.yingyang.wallx.json.JsonConfig;
import com.yingyang.wallx.json.JsonUtils;
import com.yingyang.wallx.models.ItemCategory;
import com.yingyang.wallx.utilities.DatabaseHandlerCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentCategory extends Fragment {

    ListView listView;
    List<ItemCategory> arrayItemCategory;
    private ArrayList<ItemCategory> arrayListItemCategory;
    AdapterCategory adapterCategory;
    private ItemCategory itemCategory;
    public DatabaseHandlerCategory databaseHandlerCate;
    private InterstitialAd interstitialAd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        setHasOptionsMenu(true);

        loadInterstitialAd();

        listView = (ListView) rootView.findViewById(R.id.lsv_allphotos);

        arrayItemCategory = new ArrayList<ItemCategory>();
        this.arrayListItemCategory = new ArrayList<ItemCategory>();

        databaseHandlerCate = new DatabaseHandlerCategory(getActivity());

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                itemCategory = arrayItemCategory.get(position);
                String Catid = itemCategory.getCategoryId();
                JsonConfig.CATEGORY_ID = itemCategory.getCategoryId();
                Log.e("cat_id", "" + Catid);

                JsonConfig.CATEGORY_TITLE = itemCategory.getCategoryName();

                Intent intent = new Intent(getActivity(), ActivityWallpaperByCategory.class);
                startActivity(intent);

                showInterstitialAd();

            }
        });

        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new MyTask().execute(Config.SERVER_URL + "/api.php");
        } else {

            arrayItemCategory = databaseHandlerCate.getAllData();
            if (arrayItemCategory.size() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.network_first_load), Toast.LENGTH_SHORT).show();
            }
            setAdapterToListView();
        }

        return rootView;
    }

    private class MyTask extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
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
                Toast.makeText(getActivity(), getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConfig.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemCategory objItem = new ItemCategory();

                        databaseHandlerCate.AddtoFavoriteCate(new ItemCategory(objJson.getString(JsonConfig.CATEGORY_CID), objJson.getString(JsonConfig.CATEGORY_NAME), objJson.getString(JsonConfig.CATEGORY_IMAGE_URL)));

                        objItem.setCategoryName(objJson.getString(JsonConfig.CATEGORY_NAME));
                        objItem.setCategoryId(objJson.getString(JsonConfig.CATEGORY_CID));
                        objItem.setCategoryImage(objJson.getString(JsonConfig.CATEGORY_IMAGE_URL));
                        arrayItemCategory.add(objItem);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arrayListItemCategory.addAll(arrayItemCategory);
                setAdapterToListView();
            }

        }
    }

    public void setAdapterToListView() {
        adapterCategory = new AdapterCategory(getActivity(), R.layout.lsv_item_category, arrayItemCategory);
        listView.setAdapter(adapterCategory);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));

        final MenuItem searchMenuItem = menu.findItem(R.id.search);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText.toLowerCase(Locale.getDefault());
                filter(text);

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }
        });
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        arrayItemCategory.clear();
        if (charText.length() == 0) {
            arrayItemCategory.addAll(arrayListItemCategory);
        } else {
            for (ItemCategory filter : arrayListItemCategory) {
                if (filter.getCategoryName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    arrayItemCategory.add(filter);
                }
            }
        }
        setAdapterToListView();
    }

    private void loadInterstitialAd() {
        Log.d("TAG", "showAd");
        interstitialAd = new InterstitialAd(getActivity());
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void showInterstitialAd() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

}
