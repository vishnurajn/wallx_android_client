package com.yingyang.wallx.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.yingyang.wallx.R;
import com.yingyang.wallx.activities.ActivitySlideImage;
import com.yingyang.wallx.adapters.AdapterFavorite;
import com.yingyang.wallx.json.JsonUtils;
import com.yingyang.wallx.utilities.DatabaseHandler;
import com.yingyang.wallx.utilities.DatabaseHandler.DatabaseManager;
import com.yingyang.wallx.utilities.Pojo;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    GridView gridView;
    DatabaseHandler databaseHandler;
    private DatabaseManager databaseManager;
    AdapterFavorite adapterFavorite;
    ArrayList<String> list_image, image_cat_name;
    String[] str_list_image, str_image_cat_name;
    List<Pojo> listItem;
    TextView textView;
    private int columnWidth;
    JsonUtils jsonUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);

        gridView = (GridView) rootView.findViewById(R.id.favorite_grid);
        textView = (TextView) rootView.findViewById(R.id.textView1);
        databaseHandler = new DatabaseHandler(getActivity());
        databaseManager = DatabaseManager.INSTANCE;
        databaseManager.init(getActivity());
        jsonUtils = new JsonUtils(getActivity());

        listItem = databaseHandler.getAllData();
        adapterFavorite = new AdapterFavorite(listItem, getActivity(), columnWidth);
        gridView.setAdapter(adapterFavorite);
        if (listItem.size() == 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                Intent intent = new Intent(getActivity(), ActivitySlideImage.class);
                intent.putExtra("POSITION_ID", position);
                intent.putExtra("IMAGE_ARRAY", str_list_image);
                intent.putExtra("IMAGE_CATNAME", str_image_cat_name);

                startActivity(intent);

            }
        });

        return rootView;
    }

    public void onDestroyView() {
        if (!databaseManager.isDatabaseClosed())
            databaseManager.closeDatabase();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        listItem = databaseHandler.getAllData();
        adapterFavorite = new AdapterFavorite(listItem, getActivity(), columnWidth);
        gridView.setAdapter(adapterFavorite);
        if (listItem.size() == 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
        list_image = new ArrayList<String>();
        image_cat_name = new ArrayList<String>();

        str_list_image = new String[list_image.size()];
        str_image_cat_name = new String[image_cat_name.size()];

        for (int j = 0; j < listItem.size(); j++) {

            Pojo objAllBean = listItem.get(j);

            list_image.add(objAllBean.getImageurl());
            str_list_image = list_image.toArray(str_list_image);

            image_cat_name.add(objAllBean.getCategoryName());
            str_image_cat_name = image_cat_name.toArray(str_image_cat_name);

        }
        if (databaseManager == null) {
            databaseManager = DatabaseManager.INSTANCE;
            databaseManager.init(getActivity());
        } else if (databaseManager.isDatabaseClosed()) {
            databaseManager.init(getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!databaseManager.isDatabaseClosed())
            databaseManager.closeDatabase();
    }


}
