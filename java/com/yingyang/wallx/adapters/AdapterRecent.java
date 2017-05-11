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
import com.yingyang.wallx.models.ItemRecent;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterRecent extends ArrayAdapter<ItemRecent> {

    private Activity activity;
    private List<ItemRecent> itemsLatest;
    ItemRecent object;
    private int row;
    private int imageWidth;

    public AdapterRecent(Activity act, int resource, List<ItemRecent> arrayList, int columnWidth) {
        super(act, resource, arrayList);
        this.activity = act;
        this.row = resource;
        this.itemsLatest = arrayList;
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

        if ((itemsLatest == null) || ((position + 1) > itemsLatest.size()))
            return view;

        object = itemsLatest.get(position);


        holder.imageView = (ImageView) view.findViewById(R.id.item);

        Picasso
                .with(getContext())
                .load(Config.SERVER_URL + "/upload/thumbs/" + object.getImageurl())
                .placeholder(R.drawable.ic_thumbnail)
                .into(holder.imageView);

        return view;

    }

    public class ViewHolder {

        public ImageView imageView;

    }

}
