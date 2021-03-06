package com.gotako.govoz;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// import com.balysv.materialripple.MaterialRippleLayout;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.data.VozDumpObject;
import com.gotako.govoz.tasks.VozMainForumDownloadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends VozFragment implements ActivityCallback<Forum> {

    private OnFragmentInteractionListener mListener;
    // private Map<Integer, Forum> mForumGroups;
    private List<Forum> mForumGroups;
    private Map<Integer, List<Forum>> mForums;
    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VozCache.instance().navigationList.clear();
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null) {
            // getVozForums();
        } else {
            Toast.makeText(getActivity(), "Không có kết nối đến mạng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void doRefresh() {
        getVozForums();
    }

    private void getVozForums() {
        VozDumpObject dumpObject = (VozDumpObject) VozCache.instance().getDataFromCache(VozConstant.VOZ_LINK);
        VozMainForumDownloadTask task = new VozMainForumDownloadTask(this);
        task.setContext(getActivity());
        if (dumpObject != null) {
            List<Forum> forums = task.processResult(dumpObject.document);
            if (forums.size() > 0) doCallback(new CallbackResult.Builder<Forum>().setResult(forums).build());
            else {
                task.setShowProcessDialog(true);
                task.execute(VozConstant.VOZ_LINK);
            }
        } else {
            task.setShowProcessDialog(true);
            task.execute(VozConstant.VOZ_LINK);
        }

    }

    private void updateNavigationPanel() {
        getActivity().setTitle("GoVoz");
        if(mListener !=null) mListener.updateNavigationPanel(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return createViewWithSwipeToRefresh(inflater, R.layout.fragment_layout, container, savedInstanceState);
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
    public void doCallback(CallbackResult<Forum> callbackResult) {
        if (callbackResult.isSessionExpired()) {
            if (mListener != null) mListener.onSessionExpired();
        }
        List<Forum> result = callbackResult.getResult();
        Object[] extra = callbackResult.getExtra();
        if (result == null || result.size() == 0) {
            String errMsg = extra.length > 0 ? (String) extra[0]: null;
            if (errMsg == null)
                Toast.makeText(getActivity(), "Cannot access to VozForum. Please try again later.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getActivity(), errMsg, Toast.LENGTH_LONG).show();
            return;
        }

        mForumGroups = new ArrayList<>();
        mForums = new HashMap<>();
        Integer pos = -1;
        Forum parentForum = null;
        for (Forum forum : result) {
            if (parentForum == null || forum.getForumGroupName() != null) {
                parentForum = forum;
                mForumGroups.add(parentForum);
                pos++;
            }
            if (forum.getForumGroupName() == null) {
                List<Forum> list = mForums.get(pos);
                if (list == null) {
                    list = new ArrayList<Forum>();
                    mForums.put(pos, list);
                }
                list.add(forum);
            }
        }

        // Fill data to layout
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        if (getView() == null) {
            // error here, should quit and create fragment again
            if (mListener != null) mListener.reload();
            else if (getActivity() != null) ((MainNeoActivity)getActivity()).reloadCurrentFragment();
            return;
        }
        LinearLayout parent = (LinearLayout) getView().findViewById(R.id.linearMain);
        parent.removeAllViews();
        updateForum(parent, layoutInflater);
        parent.invalidate();
        updateNavigationPanel();
    }

    private void updateForum(LinearLayout parent, LayoutInflater layoutInflater) {
        int index = 0;
        for(Forum forumGroup : mForumGroups) {
            LinearLayout forumGroupLayout = (LinearLayout) layoutInflater.inflate(R.layout.main_forum_item, null);
            ((TextView) forumGroupLayout.findViewById(R.id.textMainForum)).setText(forumGroup.getForumGroupName());
            LinearLayout forumsPlaceholder = forumGroupLayout.findViewById(R.id.linearSubForum);
            forumsPlaceholder.removeAllViews();
            List<Forum> forums = mForums.get(index);
            for (Forum forum : forums) {
                // update child forum
                View view = createChildForum(forum, layoutInflater);
                forumsPlaceholder.addView(view);
            }
            parent.addView(forumGroupLayout);
            index++;
        }
    }

    private View createChildForum(Forum forum, LayoutInflater layoutInflater) {
        final LinearLayout subForumLayout = (LinearLayout) layoutInflater.inflate(R.layout.neo_forum_item, null);
        final String forumId = forum.getId();
        ((TextView) subForumLayout.findViewById(R.id.textForumCode)).setText("f" + forumId);
        ((TextView) subForumLayout.findViewById(R.id.textForumName)).setText(forum.getForumName());
        ((TextView) subForumLayout.findViewById(R.id.textViewing)).setText(forum.getViewing());
        subForumLayout.findViewById(R.id.ripple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) mListener.onForumClicked(forumId);
            }
        });
        return subForumLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainNeoActivity)getActivity()).mFragment = this;
        // getVozForums();
    }

    @Override
    public void forceRefresh() {
        getVozForums();
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onForumClicked(String forumIndex);
        void updateNavigationPanel(boolean visible);
        void reload();

        void onSessionExpired();
    }
}
