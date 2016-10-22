package com.gotako.govoz;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.Thread;
import com.gotako.govoz.tasks.VozForumDownloadTask;
import com.gotako.util.Utils;

import java.util.List;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ForumFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForumFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForumFragment extends Fragment implements ActivityCallback<Thread>, PageNavigationListener {
    private OnFragmentInteractionListener mListener;

    private List<Forum> mForums;
    private List<Thread> mThreads;
    private String mForumName = VozConstant.VOZ_SIGN;
    private int mForumId;

    public ForumFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ForumFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ForumFragment newInstance() {
        ForumFragment fragment = new ForumFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processNavigationLink();
    }

    private void updateNavigationPanel() {
        if (mListener != null) mListener.updateNavigationPanel(true);
    }

    private void processNavigationLink() {
        // last element of list should be forum link
        NavigationItem currentNavigationItem = VozCache.instance().currentNavigateItem();
        String forumLink = currentNavigationItem.mLink;
        String[] parameters = forumLink.split("\\?")[1].split("\\&");
        String firstParam = parameters[0];
        mForumId = Integer.parseInt(firstParam.split("=")[1]);
        int currentForumId = VozCache.instance().getCurrentForum();
        if (currentForumId == mForumId) {
            loadThreads();
        } else {
            int foundIndex = -1;
            for (int i = 1; i < parameters.length; i++) {
                if (parameters[i].startsWith("page=")) {
                    foundIndex = i;
                    break;
                }
            }
            int page = 1;
            if (foundIndex > -1) page = Integer.parseInt(parameters[foundIndex].split("\\=")[1]);
            loadThreads(mForumId, page);
        }
    }

    public void loadThreads() {
        NavigationItem item = VozCache.instance().currentNavigateItem();
        loadThreads(VozCache.instance().getCurrentForum(), item.mCurrentPage);
    }

    public void loadThreads(int forumId, int page) {
        VozForumDownloadTask task = new VozForumDownloadTask(this);
        int _forumId = forumId;
        if (_forumId <= 0) { // invalid forum id
            _forumId = VozCache.instance().getCurrentForum();
        } else { // set current forum
            VozCache.instance().setCurrentForum(_forumId);
        }
        int _page = page;
        if (_page == 0) { // invalid page number
            _page = VozCache.instance().currentNavigateItem().mCurrentPage;
        } else { // set current page
            VozCache.instance().currentNavigateItem().mCurrentPage = _page;
        }

        // load mThreads for forum
        task.setShowProcessDialog(true);
        task.setContext(getActivity());
        task.setRetries(1);
        String forumUrl = FORUM_URL_F + _forumId + FORUM_URL_ORDER
                + String.valueOf(_page);
        task.setForumId(String.valueOf(_forumId));
        task.execute(forumUrl);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void doCallback(List<Thread> result, Object... extra) {
        if (result == null || result.size() == 0) {
            Toast.makeText(getActivity(),
                    "Cannot access to VozForum. Please try again later.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mThreads = result;
        mForums = (List<Forum>) extra[0];
        VozCache.instance().currentNavigateItem().mLastPage = (Integer) extra[1];
        mForumName = (String) extra[2];

        // Fill data to layout
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        LinearLayout parent = (LinearLayout) getView().findViewById(R.id.linearMain);
        parent.removeAllViews();
        updateThread(parent, layoutInflater);
        parent.invalidate();
        updateStatus();
    }

    private void updateThread(LinearLayout parent, LayoutInflater layoutInflater) {
        LinearLayout forumLayout = (LinearLayout) layoutInflater.inflate(R.layout.forum_thread_item, null);
        LinearLayout forumsPlaceholder = (LinearLayout) forumLayout.findViewById(R.id.linearSubForum);
        LinearLayout threadsPlaceholder = (LinearLayout) forumLayout.findViewById(R.id.linearThreads);
        TextView textThreadsTitle = (TextView) forumLayout.findViewById(R.id.textThreadsTitle);
        textThreadsTitle.setText(textThreadsTitle.getText() + " " + mForumName);
        // sub-forum insertion
        if (mForums != null && mForums.size() > 0) {
            forumsPlaceholder.removeAllViews();
            for (Forum forum : mForums) {
                // update child forum
                View view = createChildForum(forum, layoutInflater);
                forumsPlaceholder.addView(view);
            }

        } else {
            forumsPlaceholder.setVisibility(View.GONE);
            forumLayout.findViewById(R.id.textSubForums).setVisibility(View.GONE);
        }

        // thread insertion
        for (Thread thread : mThreads) {
            View view = createThread(thread, layoutInflater);
            threadsPlaceholder.addView(view);
        }
        parent.addView(forumLayout);
    }

    private View createChildForum(Forum forum, LayoutInflater layoutInflater) {
        final LinearLayout subForumLayout = (LinearLayout) layoutInflater.inflate(R.layout.neo_forum_item, null);
        final String forumId = forum.getId();
        ((TextView) subForumLayout.findViewById(R.id.textForumCode)).setText("f" + forumId);
        ((TextView) subForumLayout.findViewById(R.id.textForumName)).setText(forum.getForumName());
        if(!Utils.isNullOrEmpty(forum.getViewing())) {
            ((TextView) subForumLayout.findViewById(R.id.textViewing)).setText(forum.getViewing());
        } else {
            subForumLayout.findViewById(R.id.textViewing).setVisibility(View.GONE);
        }
        subForumLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onForumClicked(forumId);
            }
        });
        return subForumLayout;
    }

    private View createThread(final Thread thread, final LayoutInflater layoutInflater) {
        final LinearLayout threadLayout = (LinearLayout) layoutInflater.inflate(R.layout.neo_thread_item, null);
        TextView title = (TextView) threadLayout.findViewById(R.id.textTitle);
        String titleText = thread.getTitle();
        if (thread.isDeleted()) {
            titleText = "<b><font color=\"#D3D3D3\">" + titleText + "</font></b>";
            threadLayout.setBackgroundColor(getResources().getColor(R.color.delete_thread_color));
        } else {
            if (thread.isSticky()) { // set red background for text
                titleText = "<b><font color=\"red\">" + titleText + "</font></b>";
            } else {
                if (VozConfig.instance().isDarkTheme()) {
                    titleText = "<b><font color=\"#e7e7e7\">" + titleText + "</font></b>";
                } else {
                    titleText = "<b><font color=\"#23497C\">" + titleText + "</font></b>";
                }
            }
        }
        title.setText(Html.fromHtml(titleText));
        String subTitle = thread.getSubTitle();
        if (!Utils.isNullOrEmpty(subTitle)) {
            TextView subTitleView = (TextView) threadLayout.findViewById(R.id.textSubtile);
            subTitleView.setVisibility(View.VISIBLE);
            subTitleView.setText(subTitle);
            if(titleText.length() >= 30) subTitleView.setMaxLines(1);
        }
        if (!Utils.isNullOrEmpty(thread.prefix)) {
            String prefix = "<b><font color=\"" + thread.prefixColor + "\">" + thread.prefix + "</font></b></a> - ";
            int i1 = prefix.indexOf("[");
            int i2 = prefix.indexOf("]");
            title.setText(Html.fromHtml(prefix + titleText));
        }

        TextView poster = (TextView) threadLayout.findViewById(R.id.textPostUser);
        poster.setText(thread.getPoster());
        ((TextView) threadLayout.findViewById(R.id.textLastUpdate)).setText(thread.getLastUpdate());
        ((TextView) threadLayout.findViewById(R.id.textReplies)).setText(thread.replies);
        if(!thread.isDeleted()) {
            threadLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null) {
                        mListener.onThreadClicked(thread);
                    }
                }
            });
        }
        return threadLayout;
    }

    private void updateStatus() {
        getActivity().setTitle(mForumName);
        updateNavigationPanel();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainNeoActivity)getActivity()).mFragment = this;
        processNavigationLink();
    }

    @Override
    public void goFirst() {
        VozCache.instance().currentNavigateItem().mCurrentPage = 1;
        loadThreads();
    }

    @Override
    public void goLast() {
        VozCache.instance().currentNavigateItem().mCurrentPage = VozCache.instance().currentNavigateItem().mLastPage;
        loadThreads();
    }

    @Override
    public void goToPage(int page) {
        VozCache.instance().currentNavigateItem().mCurrentPage = page;
        loadThreads();
    }

    public interface OnFragmentInteractionListener {
        void onThreadClicked(Thread thread);

        void updateNavigationPanel(boolean visible);

        void onForumClicked(String forumId);
    }
}
