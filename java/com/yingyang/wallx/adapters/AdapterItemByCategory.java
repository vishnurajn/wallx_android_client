package com.yingyang.wallx.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.yingyang.wallx.Config;
import com.yingyang.wallx.R;
import com.yingyang.wallx.models.ItemWallpaperByCategory;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterItemByCategory extends ArrayAdapter<ItemWallpaperByCategory> {

    private Activity activity;
    private List<ItemWallpaperByCategory> itemsCategory;
    ItemWallpaperByCategory object;
    private int row;
    private int imageWidth;

    public AdapterItemByCategory(Activity act, int resource, List<ItemWallpaperByCategory> arrayList, int columnWidth) {
        super(act, resource, arrayList);
        this.activity = act;
        this.row = resource;
        this.itemsCategory = arrayList;
        this.imageWidth = columnWidth;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(row, null);

            holder = new ViewHolder();
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if ((itemsCategory == null) || ((position + 1) > itemsCategory.size()))
            return view;

        object = itemsCategory.get(position);


        holder.imageView = (ImageView) view.findViewById(R.id.item);

        Picasso
                .with(getContext())
                .load(Config.SERVER_URL + "/upload/thumbs/" + object.getItemImageurl())
                .placeholder(R.drawable.ic_thumbnail)
                .into(holder.imageView);

        return view;
    }

    public class ViewHolder {

        public ImageView imageView;

    }

}
