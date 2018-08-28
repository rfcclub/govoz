package com.gotako.govoz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.gotako.GlideApp;
import com.gotako.govoz.data.Emoticon;
import com.gotako.govoz.data.EmoticonSetObject;
import com.gotako.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VozSmiliesActivity extends AppCompatActivity {

    Spinner emoticonList;
    GridView emoticonGrid;
    EmoticonSpinnerAdapter spinnerAdapter;
    EmoticonGridAdapter gridAdapter;
    List<EmoticonSetObject> emoticonSetList;
    List<Emoticon> emoticons;
    Map<String, String> smiliesMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voz_smilies);
        emoticonList = findViewById(R.id.emoticonList);
        emoticonGrid = findViewById(R.id.emoticonGrid);
        emoticonSetList = new ArrayList<>();
        emoticons = new ArrayList<>();
        spinnerAdapter = new EmoticonSpinnerAdapter(this, emoticonSetList);
        emoticonList.setAdapter(spinnerAdapter);
        gridAdapter = new EmoticonGridAdapter(this, emoticons);
        emoticonGrid.setAdapter(gridAdapter);
        createEmoticonList();
        loadEmoticons(0);
        loadDefaultSmilies();
        emoticonList.setOnItemClickListener((parent, view, position, id) -> {
            loadEmoticons(position);
        });
    }

    private void loadEmoticons(int position) {
        if (position == 0) {
            createVozEmoticons();
        } else { // imgur
            loadImgurAlbum(emoticonSetList.get(position).location);
        }
    }

    private void loadImgurAlbum(String location) {
        
    }

    private void loadDefaultSmilies() {
        try {
            Gson gson = new Gson();
            SharedPreferences prefs = getSharedPreferences(VozConstant.VOZINFO, Context.MODE_PRIVATE);
            String jsonString = prefs.getString("VozSmilies", null);
            if (Utils.isNotEmpty(jsonString)) {
                smiliesMap = gson.fromJson(jsonString, smiliesMap.getClass());
            }
        } finally {
            // do nothing
        }
    }

    private void createVozEmoticons() {
        try {
            String[] files = getAssets().list("");
            for(String file : files) {
                if(file.endsWith("gif") || file.endsWith("png")) {
                    Emoticon emoticon = new Emoticon() {{
                        url = "file:///android_asset/" + file;
                        code = file;
                    }};
                    emoticons.add(emoticon);
                }
            }
            gridAdapter.notifyDataSetChanged();
        } catch(IOException ioExp) {
            Log.e("ERROR", ioExp.getMessage());
        } finally {

        }
    }

    private void createEmoticonList() {
        SharedPreferences prefs = this.getBaseContext().getSharedPreferences(VozConstant.VOZINFO, Context.MODE_PRIVATE);
        String jsonString = prefs.getString("emoticonSets", null);
        if (Utils.isEmpty(jsonString)) {
            emoticonSetList.add(new EmoticonSetObject() {{
                name = "VozForums emoticons";
                location = "assets";
            }});
            emoticonSetList.add(new EmoticonSetObject() {{
                name = "Ếch xanh (Pepe)";
                location = "https://imgur.com/a/PyAepyl";
            }});
            emoticonSetList.add(new EmoticonSetObject() {{
                name = "Ếch lai ong Pebee = (Pepe + Quobee)";
                location = "https://imgur.com/a/0LEvazq";
            }});
            emoticonSetList.add(new EmoticonSetObject() {{
                name = "Voz Hồi giáo";
                location = "https://imgur.com/a/y0xX2";
            }});
        }
        spinnerAdapter.notifyDataSetChanged();
    }

    class EmoticonGridAdapter extends BaseAdapter implements SpinnerAdapter {
        private List<Emoticon> emoticonList;
        private VozSmiliesActivity context;

        public EmoticonGridAdapter(VozSmiliesActivity context, List<Emoticon> objectList) {
            emoticonList = objectList;
            this.context = context;
        }
        @Override
        public int getCount() {
            return emoticonList.size();
        }

        @Override
        public Object getItem(int position) {
            return emoticonList.get(position);
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
                convertView = mInflater.inflate(R.layout.emoticon_grid_item, null);
            }

            ImageView pictureImage = convertView.findViewById(R.id.picture);

            GlideApp.with(context)
                    .load(Uri.parse(emoticonList.get(position).url))
                    .placeholder(R.drawable.user_icon)
                    .into(pictureImage);
            convertView.setOnClickListener((v) -> {
                context.onEmoticonClick(emoticonList.get(position).code);
            });
            return convertView;
        }
    }

    private void onEmoticonClick(String code) {
        String bbCode = code;
        if (emoticonList.getSelectedItemPosition() == 0) {
            bbCode  = smiliesMap.get(code);
        }
        Intent intent = new Intent();
        intent.putExtra("code", bbCode);
        setResult(VozConstant.GET_SMILEY_OK, intent);
        finish();
    }
}

class EmoticonSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
    private List<EmoticonSetObject> setObjectList;
    private VozSmiliesActivity context;

    public EmoticonSpinnerAdapter(VozSmiliesActivity context, List<EmoticonSetObject> objectList) {
        setObjectList = objectList;
        this.context = context;
    }
    @Override
    public int getCount() {
        return setObjectList.size();
    }

    @Override
    public Object getItem(int position) {
        return setObjectList.get(position);
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
            convertView = mInflater.inflate(R.layout.emoticon_list_item, null);
        }

        TextView txtTitle = convertView.findViewById(R.id.title);
        txtTitle.setText(setObjectList.get(position).name);
        TextView txtLocation = convertView.findViewById(R.id.location);
        txtLocation.setText(setObjectList.get(position).location);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        imgIcon.setImageResource(R.drawable.ic_delete_white_18dp);

        TextView txtTitle = convertView.findViewById(R.id.title);
        txtTitle.setText(setObjectList.get(position).name);

        return convertView;
    }
}

