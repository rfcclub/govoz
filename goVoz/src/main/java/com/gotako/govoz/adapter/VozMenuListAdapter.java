package com.gotako.govoz.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gotako.govoz.R;
import com.gotako.govoz.VozConstant;
import com.gotako.govoz.data.VozMenuItem;
import com.gotako.util.Utils;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by Nam on 9/6/2015.
 */
public class VozMenuListAdapter extends BaseAdapter {
    private Context context;
    List<VozMenuItem> itemList;

    public VozMenuListAdapter(Context context, List<VozMenuItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.voz_item_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        imgIcon.setImageResource(itemList.get(position).icon);
        txtTitle.setText(itemList.get(position).title);
        return convertView;
    }
}
