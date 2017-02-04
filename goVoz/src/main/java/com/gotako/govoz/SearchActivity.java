package com.gotako.govoz;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.ExceptionCallback;
import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.ReactiveCollectionField;
import com.gotako.gofast.annotation.BindingCollection;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.Thread;
import com.gotako.govoz.tasks.VozForumSearchTask;
import com.gotako.util.Utils;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.android.segment.SegmentedGroup;

import static com.gotako.govoz.VozConstant.FORUM_POSITION;
import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;
import static com.gotako.govoz.VozConstant.VOZ_LINK;

public class SearchActivity extends VozFragmentActivity implements
        ActivityCallback<Thread>, OnItemClickListener, ExceptionCallback {
    private List<Forum> forums;
    ListView list;
    @BindingCollection(id = R.id.threadsList, layout = R.layout.thread_item, twoWay = true)
    private List<Thread> threads;
    private int firstPage = 1;
    private int lastPage = 1;
    private ListView listView;
    private String forumName = VozConstant.VOZ_SIGN;
    private int forumId;
    private int forumPage;
    private SegmentedGroup segmentedGroup;


    @SuppressWarnings("rawtypes")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VozCache.instance().setCanShowReplyMenu(false);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View layout = mInflater.inflate(R.layout.activity_forum, null);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        frameLayout.addView(layout);

        forums = new ArrayList<Forum>();
        threads = new ArrayList<Thread>();
        GoFastEngine.initialize(this);
        ReactiveCollectionField field = GoFastEngine.instance()
                .getBindingObject(this, "threads",
                        ReactiveCollectionField.class);
        field.getAdapter().setBindingActionListener(this);
        list = (ListView) layout.findViewById(R.id.threadsList);
        list.setOnItemClickListener(this);
        processSearch(this.getIntent().getStringExtra("searchString"));
        updateNavigationPanel();
        doTheming();
        segmentedGroup = (SegmentedGroup) layout.findViewById(R.id.navigation_group);
        if (segmentedGroup != null) {
            segmentedGroup.setTintColor(Utils.getColorByTheme(this, R.color.white, R.color.voz_front_color),
                    Utils.getColorByTheme(this, R.color.black, R.color.white));
        }
        LinearLayout navigationRootPanel = (LinearLayout) layout.findViewById(R.id.navigationRootPanel);
        if (navigationRootPanel != null)
            navigationRootPanel.setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
    }

    private void processSearch(String searchString) {
        firstPage = 1;
        lastPage = 1;
        VozForumSearchTask task = new VozForumSearchTask(this);
        // load threads for forum
        task.setShowProcessDialog(true);
        task.setContext(this);
        task.setRetries(1);
        task.execute(searchString);
    }

    private void updateStatus() {
        setTitle("Search result");
        updateNavigationPanel();
        listView = (ListView) findViewById(R.id.threadsList);
        listView.setSelection(0);
        final SegmentedGroup navigationGroup = (SegmentedGroup) findViewById(R.id.navigation_group);
    }

    public void loadThreads() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void doCallback(List<Thread> result, Object... extra) {
        if (result == null || result.size() == 0) {
            Toast.makeText(this,
                    "Cannot access to VozForum. Please try again later.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        threads = result;
        forums = (List<Forum>) extra[0];
        lastPage = (Integer) extra[1];
        forumName = (String) extra[2];
        forumPage = VozCache.instance().getCurrentForumPage();
        insertForumToThreads();
        GoFastEngine.notify(this, "threads");
        updateStatus();
    }

    private void updateNavigationPanel() {
        final SegmentedGroup navigationGroup = (SegmentedGroup) findViewById(R.id.navigation_group);
        navigationGroup.removeAllViews();
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        int currentPage = VozCache.instance().getCurrentForumPage();
        if (currentPage > 2) {
            RadioButton first = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            first.setText("<<");
            first.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goFirst();
                }
            });
            navigationGroup.addView(first);
        }
        int prevStart = currentPage - 1;
        if (prevStart < 1) prevStart = 1;
        int extraRight = 0;
        for (int i = prevStart; i <= currentPage; i++) {
            RadioButton prevPage = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            prevPage.setText(String.valueOf(i));
            prevPage.setTag(i);
            final int page = i;
            if (i == currentPage) prevPage.setChecked(true);
            prevPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPage(page);
                }
            });
            navigationGroup.addView(prevPage);
            extraRight++;
        }
        int nextEnd = currentPage + 1;
        if (nextEnd < 4) nextEnd = 4;
        if (nextEnd > lastPage) nextEnd = lastPage;
        for (int i = currentPage + 1; i <= nextEnd; i++) {
            RadioButton nextPage = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            nextPage.setText(String.valueOf(i));
            nextPage.setTag(i);
            final int page = i;
            nextPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToPage(page);
                }
            });
            navigationGroup.addView(nextPage);
        }
        if (nextEnd < lastPage) {
            RadioButton last = (RadioButton) mInflater.inflate(R.layout.navigation_button, null);
            last.setText(">>");
            last.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goLast();
                }
            });
            navigationGroup.addView(last);
        }
        navigationGroup.updateBackground();
        navigationGroup.requestLayout();
        navigationGroup.invalidate();
    }

    private void insertForumToThreads() {
        List<Thread> newThreads = new ArrayList<Thread>();
        for (Forum forum : forums) {
            Thread th = new Thread();
            th.setTitle(forum.getForumName());
            th.setPoster("");
            th.setLastUpdate("");
            th.setSubForum(true);
            newThreads.add(th);
        }
        newThreads.addAll(threads);
        threads = newThreads;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
                            int selectedIndex, long arg3) {
        Thread currentThread = threads.get(selectedIndex);
        if (currentThread.isDeleted()) return;
        VozCache.instance().cache().put(FORUM_POSITION, selectedIndex);
        if (!currentThread.isSubForum()) {
            VozCache.instance().setCurrentThread(currentThread.getId());
            VozCache.instance().setCurrentThreadPage(1);
            String url = VOZ_LINK + "/"
                    + currentThread.getThreadUrl()
                    + "&page="
                    + String.valueOf(VozCache.instance().getCurrentThreadPage());
            VozCache.instance().navigationList.add(url);
            Intent intent = new Intent(this, ThreadActivity.class);
            startActivity(intent);
        } else {
            VozCache.instance().setCurrentThread(-1);
            Forum subForum = null;
            for (Forum forum : forums) {
                if (currentThread.getTitle().equals(forum.getForumName())) {
                    subForum = forum;
                    break;
                }
            }
            if (subForum != null) {
                String forumUrl = FORUM_URL_F + subForum.getId() + FORUM_URL_ORDER + "1";
                VozCache.instance().navigationList.add(forumUrl);
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
//                forums = new ArrayList<Forum>();
//                threads = new ArrayList<Thread>();
//                GoFastEngine.notify(this, "threads");
//                VozCache.instance().setCurrentParentForum(VozCache.instance().getCurrentForum());
//                VozCache.instance().setCurrentForum(Integer.parseInt(subForum.getId()));
//                subForum.setParent(VozCache.instance().getCurrentParentForum());
//                String forumUrl = FORUM_URL_F + subForum.getId() + FORUM_URL_ORDER + "1";
                //VozCache.instance().setCurrentForumPage(1);
                //loadThreads(Integer.parseInt(subForum.getId()), 1);
            }
        }
    }

    @Override
    public void preProcess(int position, View convertView, Object... extra) {
        Thread currentThread = threads.get(position);
        View viewHeader = convertView.findViewById(R.id.subForumHeader);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            viewHeader.setBackground(Utils.getDrawableByTheme(this, R.drawable.gradient, R.drawable.gradient_light));
        } else {
            viewHeader.setBackgroundColor(Utils.getColorByTheme(this, R.color.background_material_light_dark, R.color.background_material_light_light));
        }
        View viewSection = convertView.findViewById(R.id.forumSection);
        viewSection.findViewById(R.id.tableRow2).setVisibility(View.GONE);
        viewSection.setBackgroundColor(Utils.getColorByTheme(this, R.color.black, R.color.voz_back_color));
        if (currentThread.isSubForum()) {
            if (position == 0) {
                viewHeader.setVisibility(View.VISIBLE);
                TextView textView = (TextView) viewHeader
                        .findViewById(R.id.forumGroupName);
                textView.setText("Sub-Forums : ");
            } else {
                viewHeader.setVisibility(View.GONE);
            }

        } else {
            if (position == 0 || threads.get(position - 1).isSubForum()) {
                viewHeader.setVisibility(View.VISIBLE);
                TextView textView = (TextView) viewHeader
                        .findViewById(R.id.forumGroupName);
                textView.setText("Threads in Forum : ");
            } else {
                viewHeader.setVisibility(View.GONE);
            }
        }
        // must set value manually since we want support prefix in a beautiful way
        TextView title = (TextView) viewSection.findViewById(R.id.titleWithPrefix);
        String titleText = currentThread.getTitle();
        if (currentThread.isDeleted()) {
            //title.setTextColor(Color.LTGRAY);
            titleText = "<b><font color=\"#D3D3D3\">" + titleText + "</font></b>";
            viewSection.setBackgroundColor(getResources().getColor(R.color.delete_thread_color));
        } else {
            if (currentThread.isSticky()) { // set red background for text
                //title.setTextColor(Color.RED);
                titleText = "<b><font color=\"red\">" + titleText + "</font></b>";
            } else {
                //title.setTextColor(Utils.getColorByTheme(this, R.color.white, R.color.voz_front_color));
                if (VozConfig.instance().isDarkTheme()) {
                    titleText = "<b><font color=\"#e7e7e7\">" + titleText + "</font></b>";
                } else {
                    titleText = "<b><font color=\"#23497C\">" + titleText + "</font></b>";
                }
            }
        }
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, VozConfig.instance()
                .getFontSize());
        title.setText(Html.fromHtml(titleText));
        String subTitle = currentThread.getSubTitle();
        if(subTitle != null && subTitle.length() > 0) {
            viewSection.findViewById(R.id.tableRow2).setVisibility(View.VISIBLE);
            ((TextView)viewSection.findViewById(R.id.subThreadTitle)).setText(subTitle);
        }
        if (!Utils.isNullOrEmpty(currentThread.prefix)) {
            String prefix = "<b><font color=\"" + currentThread.prefixColor + "\">" + currentThread.prefix + "</font></b></a> - ";
            int i1 = prefix.indexOf("[");
            int i2 = prefix.indexOf("]");
            title.setText(Html.fromHtml(prefix + titleText));
        }
        TextView poster = (TextView) viewSection.findViewById(R.id.poster);
        poster.setTextColor(Utils.getColorByTheme(this, R.color.white, R.color.black));
        poster.setTextSize(TypedValue.COMPLEX_UNIT_SP, VozConfig.instance()
                .getFontSize() - 2);
        ((TextView) viewSection.findViewById(R.id.lastUpdate)).setTextColor(Utils.getColorByTheme(this, R.color.light_gray, R.color.black));
    }

    public void goFirst() {
        VozCache.instance().setCurrentForumPage(1);
        loadThreads();

    }

    public void goToPage(int page) {
        VozCache.instance().setCurrentForumPage(page);
        loadThreads();
    }

    public void goLast() {
        VozCache.instance().setCurrentForumPage(lastPage);
        loadThreads();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_bookmark).setVisible(true);
        return ret;
    }

    @Override
    public void doBookmark() {
        VozCache.instance().pinItemForumList.add(buildForumItem(forumName, String.valueOf(forumId)));
        savePinForumsList();
        Toast.makeText(this, "Shortcut " + forumName + " đã được thêm", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void doOnResume() {
        refreshActionBarIcon();
        updateNavigationPanel();
        VozCache.instance().setCurrentForumPage(forumPage);
        VozCache.instance().setCurrentForum(forumId);
    }

    public int getLastPage() {
        return lastPage;
    }

    @Override
    public void refresh() {
        if (VozCache.instance().getCurrentForum() != forumId)
            VozCache.instance().setCurrentForum(forumId);
        if (VozCache.instance().getCurrentForumPage() != forumPage)
            VozCache.instance().setCurrentForumPage(forumPage);
        loadThreads();
    }

    protected boolean canShowPinnedMenu() {
//		if (navDrawerItems != null) {
//			String forumId = String.valueOf(VozCache.instance().getCurrentForum());
//			String forumUrl = FORUM_URL_F + forumId + FORUM_URL_ORDER
//					+ String.valueOf(VozCache.instance().getCurrentForumPage());
//			Forum forum = VozCache.instance().getCurrentForum();
//			NavDrawerItem item = new NavDrawerItem(forum.getForumName(),
//					forumUrl);
//
//			if (navDrawerItems.contains(item)) {
//				return true;
//			} else {
//				return false;
//			}
//		}
        return false;
    }

    protected boolean canShowUnpinnedMenu() {
//		if (navDrawerItems != null) {
//			String forumId = VozCache.instance().getCurrentForum().getId();
//			String forumUrl = FORUM_URL_F + forumId + FORUM_URL_ORDER
//					+ String.valueOf(VozCache.instance().getCurrentForumPage());
//			Forum forum = VozCache.instance().getCurrentForum();
//			NavDrawerItem item = new NavDrawerItem(forum.getForumName(),
//					forumUrl, "forum");
//
//			if (navDrawerItems.contains(item)) {
//				return false;
//			} else {
//				return true;
//			}
//		}
        return true;
    }

    protected void doPin() {

    }

    protected void doUnpin() {

    }
}
