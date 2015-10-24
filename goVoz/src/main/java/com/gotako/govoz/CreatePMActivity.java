package com.gotako.govoz;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.annotation.BindingField;
import com.gotako.govoz.tasks.PostReplyTask;
import com.gotako.util.Utils;

import java.util.List;

public class CreatePMActivity extends VozFragmentActivity implements ActivityCallback<Boolean>, ExceptionCallback {

    @BindingField(id = R.id.toAddress, twoWay = true)
    String toAddress = "";
    @BindingField(id = R.id.pmContent, twoWay = true)
    String pmContent = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_post);

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.activity_create_pm, null);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(layout);

        GoFastEngine.initialize(this);

        doTheming();
        layout.findViewById(R.id.pmCreateRootLayout).setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
    }

    public void post(View view) {
        CreatePMTask task = new CreatePMTask(this);
        task.execute(tString, answerText, titleText, replyLink);
    }

    @Override
    public void doCallback(List<Boolean> result, Object... extra) {

    }
}
