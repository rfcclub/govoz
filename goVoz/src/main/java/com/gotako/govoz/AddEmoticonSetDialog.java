package com.gotako.govoz;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.gotako.govoz.data.EmoticonSetObject;
import com.gotako.govoz.tasks.RatingThreadTask;

/**
 * Created by Nam on 9/12/2015.
 */
public class AddEmoticonSetDialog extends Dialog {
    protected Activity activity;
    protected EditText editText;
    protected String title;
    private Context ctx;
    public AddEmoticonSetDialog(@NonNull Context context) {
        super(context);
        ctx = context;
     }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_emoticon_set_layout);
        Button button = findViewById(R.id.btnSave);
        button.setOnClickListener(view1 -> {
            String name = ((EditText)findViewById(R.id.emoticonSetName)).getText().toString();
            String location = ((EditText)findViewById(R.id.emoticonSetLink)).getText().toString();
            EmoticonSetObject emoticonSetObject = new EmoticonSetObject(name, location);

            VozConfig.getEmoticonSet().add(emoticonSetObject);
            VozConfig.instance().save(ctx);
            AddEmoticonSetDialog.this.dismiss();
        });
        Button cancelButton = findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(view1 -> {
            AddEmoticonSetDialog.this.dismiss();
        });
    }

}
