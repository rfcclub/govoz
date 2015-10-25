package com.gotako.govoz;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.annotation.BindingField;
import com.gotako.govoz.tasks.CreatePMTask;
import com.gotako.govoz.tasks.PostReplyTask;
import com.gotako.util.Utils;

import java.util.List;

public class CreatePMActivity extends VozFragmentActivity implements ActivityCallback<Boolean>, ExceptionCallback {

    @BindingField(id = R.id.toAddress, twoWay = true)
    String toAddress = "";
    @BindingField(id = R.id.pmContent, twoWay = true)
    String pmContent = "";
    @BindingField(id = R.id.pmTitle, twoWay = true)
    String pmTitle = "";
    private String pmReplyLink;
    private boolean doReply = false;
    private String securityToken;
    private String loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_post);

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.activity_create_pm, null);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(layout);
        GoFastEngine.initialize(this);
        String address = getIntent().getStringExtra("pmRecipient");
        toAddress = (address == null)? "": address;
        GoFastEngine.notify(this, "toAddress");
        pmReplyLink = getIntent().getStringExtra("pmReplyLink");
        securityToken = getIntent().getStringExtra("securityToken");
        loggedInUser = getIntent().getStringExtra("loggedInUser");
        String quote = getIntent().getStringExtra("pmQuote");
        pmContent = (quote == null ? "" : quote);
        GoFastEngine.notify(this, "pmContent");
        String title  = getIntent().getStringExtra("pmTitle");
        pmTitle = (title == null ? "" : title);
        GoFastEngine.notify(this, "pmTitle");
        if(pmReplyLink == null || pmReplyLink.trim() == "") doReply = true;
        EditText pmContentEdit = (EditText)layout.findViewById(R.id.pmContent);
        pmContentEdit.setSelection(pmContentEdit.getText().length());
        doTheming();
        layout.findViewById(R.id.pmCreateRootLayout).setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
    }

    public void post(View view) {
        CreatePMTask task = new CreatePMTask(this);
        task.execute(toAddress, pmTitle, pmContent, pmReplyLink, securityToken, loggedInUser);
    }

    @Override
    public void doCallback(List<Boolean> result, Object... extra) {
        if(doReply) setResult(2);
        else setResult(1);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(0);
        finish();
    }
}
