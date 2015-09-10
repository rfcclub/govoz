package com.gotako.govoz;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.ReactiveCollectionField;
import com.gotako.gofast.annotation.BindingCollection;
import com.gotako.govoz.R;
import com.gotako.govoz.data.PrivateMessage;
import com.gotako.govoz.tasks.PMDownloadTask;

import java.util.ArrayList;
import java.util.List;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

public class InboxActivity extends VozFragmentActivity implements
        ActivityCallback<PrivateMessage>, AdapterView.OnItemClickListener, ExceptionCallback {

    @BindingCollection(id = R.id.pmList, layout = R.layout.pm_item, twoWay = true)
    private List<PrivateMessage> pmList = new ArrayList<PrivateMessage>();
    private int lastPage = 1;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VozCache.instance().setCanShowReplyMenu(false);

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.activity_inbox, null);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(layout);
        overridePendingTransition(R.animator.right_slide_in, R.animator.zoom_out);
        GoFastEngine.initialize(this);
        // TODO
        ReactiveCollectionField field = GoFastEngine.instance().getBindingObject(this, "pmList", ReactiveCollectionField.class);
        field.getAdapter().setBindingActionListener(this);
        ListView list = (ListView) layout.findViewById(R.id.pmList);
        list.setOnItemClickListener(this);
        loadPrivateMessages();
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
    public void doCallback(List<PrivateMessage> result, Object... extra) {
        pmList = result;
        GoFastEngine.notify(this, "pmList");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
