/**
 * 
 */
package com.gotako.govoz.adapter;

import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gotako.govoz.BaseFragmentActivity;
import com.gotako.govoz.R;
import com.gotako.govoz.VozConfig;
import com.gotako.govoz.data.NavDrawerItem;
import com.gotako.govoz.tasks.IgnoreUserTask;
import com.gotako.util.Utils;

/**
 * @author lnguyen66
 *
 */
public class NavDrawerListAdapter extends BaseAdapter {

	private BaseFragmentActivity baseFragmentActivity;
    private List<NavDrawerItem> navDrawerItems;
     
    public NavDrawerListAdapter(BaseFragmentActivity context, List<NavDrawerItem> navDrawerItems){
        this.baseFragmentActivity = context;
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
                    baseFragmentActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        if(VozConfig.instance().isDarkTheme()) {
            imgIcon.setImageResource(R.drawable.ic_delete_white_18dp);
        } else {
            imgIcon.setImageResource(R.drawable.ic_delete_black_18dp);
        }
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);

        txtTitle.setText(navDrawerItems.get(position).title);
        
        imgIcon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                new AlertDialog.Builder(baseFragmentActivity)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.remove_shortcut)
                        .setMessage(Utils.getString(baseFragmentActivity, R.string.really_want_to_remove_shotcut) + "?")
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                navDrawerItems.remove(position);
                                NavDrawerListAdapter.this.notifyDataSetChanged();
                                baseFragmentActivity.savePinForumsList();
                                baseFragmentActivity.savePinThreadsList();
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
			}
		});
        
        return convertView;
    }

}
