package com.gotako.govoz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.ReactiveCollectionField;
import com.gotako.gofast.annotation.BindingCollection;
import com.gotako.govoz.R;
import com.gotako.govoz.data.PrivateMessage;
import com.gotako.govoz.tasks.PMDownloadTask;
import com.gotako.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

public class InboxActivity extends VozFragmentActivity implements
        ActivityCallback<PrivateMessage>, AdapterView.OnItemClickListener {

    @BindingCollection(id = R.id.pmList, layout = R.layout.pm_item, twoWay = true)
    private List<PrivateMessage> pmList = new ArrayList<PrivateMessage>();
    private int lastPage = 1;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VozCache.instance().setCanShowReplyMenu(false);
        overridePendingTransition(R.animator.right_slide_in,
                R.animator.left_slide_out);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.activity_inbox, null);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(layout);
        GoFastEngine.initialize(this);
        // TODO
        ReactiveCollectionField field = GoFastEngine.instance().getBindingObject(this, "pmList", ReactiveCollectionField.class);
        field.getAdapter().setBindingActionListener(this);
        ListView list = (ListView) layout.findViewById(R.id.pmList);
        list.setOnItemClickListener(this);
        loadPrivateMessages();
        ReactiveCollectionField pmListField = GoFastEngine.instance()
                .getBindingObject(this, "pmList",
                        ReactiveCollectionField.class);
        pmListField.getAdapter().setBindingActionListener(this);
        doTheming();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        if (VozCache.instance().isLoggedIn()) {
            menu.findItem(R.id.action_reply).setVisible(true);
        }
        return ret;
    }

    private void loadPrivateMessages() {
        PMDownloadTask task = new PMDownloadTask(this);
        String pmHttpsLink = VOZ_LINK + "/" + "private.php";
        // load threads for forum
        task.setShowProcessDialog(true);
        task.setContext(this);
        task.execute(pmHttpsLink);
    }

    @Override
    public void doCallback(CallbackResult<PrivateMessage> callbackResult) {
        pmList = callbackResult.getResult();
        GoFastEngine.notify(this, "pmList");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PrivateMessage message = pmList.get(position);
        String httpLink = VOZ_LINK + "/" + message.pmLink;
        VozCache.instance().navigationList.add(httpLink);
        Intent intent = new Intent(this, PMViewActivity.class);
        startActivity(intent);
    }

    @Override
    public void preProcess(int position, View convertView, Object... extra) {
        convertView.findViewById(R.id.pmSection).setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
        ((TextView)convertView.findViewById(R.id.pmTitle)).setTextColor(Utils.getColorByTheme(this, R.color.light_gray, R.color.voz_front_color));
        ((TextView)convertView.findViewById(R.id.pmSender)).setTextColor(Utils.getColorByTheme(this, R.color.light_gray, R.color.black));
        ((TextView)convertView.findViewById(R.id.pmDate)).setTextColor(Utils.getColorByTheme(this, R.color.light_gray, R.color.black));
    }

    @Override
    public void doRep() {
        String httpLink = VOZ_LINK + "/private.php?do=newpm";
        VozCache.instance().navigationList.add(httpLink);
        Intent intent = new Intent(this, CreatePMActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            refresh();
        }
    }

    @Override
    public void refresh() {
        loadPrivateMessages();
    }
}
