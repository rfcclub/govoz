/**
 * 
 */
package com.gotako.govoz.adapter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.util.Utils;

/**
 * @author lnguyen66
 *
 */
public class NavDrawerListAdapter extends BaseAdapter {

	private Context context;
    private List<NavDrawerItem> navDrawerItems;
     
    public NavDrawerListAdapter(Context context, List<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }
 
    @Override
    public int getCount() {
        return navDrawerItems.size();
    }
 
    @Override
    public Object getItem(int position) {      
        return navDrawerItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        txtTitle.setText(navDrawerItems.get(position).title);
        
        imgIcon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				navDrawerItems.remove(position);
				NavDrawerListAdapter.this.notifyDataSetChanged();
                // TODO update list to shared preferences
			}
		});
        
        return convertView;
    }

}
