package com.yingyang.wallx.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yingyang.wallx.Config;
import com.yingyang.wallx.R;
import com.yingyang.wallx.models.ItemCategory;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterCategory extends ArrayAdapter<ItemCategory> {

    private Activity activity;
    private List<ItemCategory> listItem;
    ItemCategory object;
    private int row;

    public AdapterCategory(Activity act, int resource, List<ItemCategory> arrayList) {
        super(act, resource, arrayList);
        this.activity = act;
        this.row = resource;
        this.listItem = arrayList;
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

        if ((listItem == null) || ((position + 1) > listItem.size()))
            return view;

        object = listItem.get(position);
        holder.txt = (TextView) view.findViewById(R.id.txt_allphotos_categty);
        holder.img_cat = (ImageView) view.findViewById(R.id.image_category);
        holder.txt.setText(object.getCategoryName());

        Picasso
                .with(getContext())
                .load(Config.SERVER_URL + "/upload/category/" + object.getCategoryImage())
                .placeholder(R.drawable.ic_thumbnail)
                .into(holder.img_cat);

        return view;

    }

    public class ViewHolder {

        public TextView txt;
        public ImageView img_cat;

    }

}
