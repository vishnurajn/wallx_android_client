package com.yingyang.wallx.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.yingyang.wallx.Config;
import com.yingyang.wallx.R;
import com.yingyang.wallx.activities.ActivitySlideImage;
import com.yingyang.wallx.adapters.AdapterRecent;
import com.yingyang.wallx.json.JsonConfig;
import com.yingyang.wallx.json.JsonUtils;
import com.yingyang.wallx.models.ItemRecent;
import com.yingyang.wallx.utilities.DatabaseHandlerLatest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentRecent extends Fragment {

    GridView gridView;
    List<ItemRecent> listItemRecent;
    AdapterRecent adapterRecent;
    ArrayList<String> allListImage, allListImageCatName;
    String[] allArrayImage, allArrayImageCatName;
    private ItemRecent itemRecent;
    private int columnWidth;
    JsonUtils jsonUtils;
    public DatabaseHandlerLatest databaseHandlerLatest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent_wallpaper, container, false);

        setHasOptionsMenu(true);

        gridView = (GridView) rootView.findViewById(R.id.latest_grid);

        databaseHandlerLatest = new DatabaseHandlerLatest(getActivity());

        listItemRecent = new ArrayList<ItemRecent>();
        allListImage = new ArrayList<String>();
        allListImageCatName = new ArrayList<String>();

        allArrayImage = new String[allListImage.size()];
        allArrayImageCatName = new String[allListImageCatName.size()];

        jsonUtils = new JsonUtils(getActivity());

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Intent intent = new Intent(getActivity(), ActivitySlideImage.class);
                intent.putExtra("POSITION_ID", position);
                intent.putExtra("IMAGE_ARRAY", allArrayImage);
                intent.putExtra("IMAGE_CATNAME", allArrayImageCatName);

                startActivity(intent);

            }
        });
        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new MyTask().execute(Config.SERVER_URL + "/api.php?latest=50");
        } else {
            listItemRecent = databaseHandlerLatest.getAllData();
            if (listItemRecent.size() == 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.network_first_load), Toast.LENGTH_SHORT).show();
            }
            setAdapterToListView();
            for (int j = 0; j < listItemRecent.size(); j++) {

                itemRecent = listItemRecent.get(j);

                allListImage.add(itemRecent.getImageurl());
                allArrayImage = allListImage.toArray(allArrayImage);

                allListImageCatName.add(itemRecent.getCategoryName());
                allArrayImageCatName = allListImageCatName.toArray(allArrayImageCatName);

            }
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
                    JSONArray jsonArray = mainJson.getJSONArray(JsonConfig.LATEST_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemRecent objItem = new ItemRecent();

                        databaseHandlerLatest.AddtoFavoriteLatest(new ItemRecent(objJson.getString(JsonConfig.LATEST_IMAGE_CATEGORY_NAME), objJson.getString(JsonConfig.LATEST_IMAGE_URL)));

                        objItem.setCategoryName(objJson.getString(JsonConfig.LATEST_IMAGE_CATEGORY_NAME));
                        objItem.setImageurl(objJson.getString(JsonConfig.LATEST_IMAGE_URL));

                        listItemRecent.add(objItem);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < listItemRecent.size(); j++) {

                    itemRecent = listItemRecent.get(j);

                    allListImage.add(itemRecent.getImageurl());
                    allArrayImage = allListImage.toArray(allArrayImage);

                    allListImageCatName.add(itemRecent.getCategoryName());
                    allArrayImageCatName = allListImageCatName.toArray(allArrayImageCatName);

                }

                setAdapterToListView();
            }

        }
    }

    public void setAdapterToListView() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            adapterRecent = new AdapterRecent(getActivity(), R.layout.lsv_item_grid_wallpaper, listItemRecent, columnWidth);
        } else {
            adapterRecent = new AdapterRecent(getActivity(), R.layout.lsv_item_grid_wallpaper_pre, listItemRecent, columnWidth);
        }


        gridView.setAdapter(adapterRecent);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

}
