package com.yingyang.wallx.adapters;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yingyang.wallx.Config;
import com.yingyang.wallx.R;
import com.yingyang.wallx.utilities.Pojo;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterFavorite extends BaseAdapter {

    LayoutInflater layoutInflater;
    Activity activity;
    private List<Pojo> data;
    Pojo object;
    private int imageWidth;

    public AdapterFavorite(List<Pojo> contactList, Activity activity, int columnWidth) {
        this.activity = activity;
        this.data = contactList;
        layoutInflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        this.imageWidth = columnWidth;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class GroupItem {
        public ImageView img_fav;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        object = data.get(position);

        View v = null;
        final GroupItem item = new GroupItem();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v = layoutInflater.inflate(R.layout.lsv_item_grid_wallpaper, null);
        } else {
            v = layoutInflater.inflate(R.layout.lsv_item_grid_wallpaper_pre, null);
        }

        item.img_fav = (ImageView) v.findViewById(R.id.item);

        Picasso
                .with(activity)
                .load(Config.SERVER_URL + "/upload/thumbs/" + object.getImageurl())
                .placeholder(R.drawable.ic_thumbnail)
                .into(item.img_fav);

        return v;
    }


}
