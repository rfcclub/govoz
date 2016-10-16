package com.gotako.govoz;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gotako.gofast.GoFastEngine;
import com.gotako.gofast.adapter.ExpandableAdapter;
import com.gotako.govoz.data.Forum;
import com.gotako.govoz.tasks.VozMainForumDownloadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
public class MainFragment extends Fragment implements ActivityCallback<Forum> {

    private OnFragmentInteractionListener mListener;
    private Map<Integer, Forum> mForumGroups;
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
            doLoginAndGetVozForums();
        } else {
            Toast.makeText(getActivity(), "Không có kết nối đến mạng", Toast.LENGTH_SHORT).show();
        }
    }

    private void doLoginAndGetVozForums() {
        SharedPreferences prefs = getActivity().getSharedPreferences(VozConstant.VOZINFO, Context.MODE_PRIVATE);
        boolean autoLogin = false;
        if (prefs.contains(VozConstant.USERNAME) && prefs.contains(VozConstant.PASSWORD)) {
            // if not login so do login
            if (VozCache.instance().getCookies() == null) {
                String username = prefs.getString(VozConstant.USERNAME, "guest");
                String password = prefs.getString(VozConstant.PASSWORD, "guest");
                AutoLoginBackgroundService albs = new AutoLoginBackgroundService(getActivity());
                albs.doLogin(username, password);
            }
        }

        if (!autoLogin) {
            getVozForums();
        }
    }

    private void getVozForums() {
        VozMainForumDownloadTask task = new VozMainForumDownloadTask(this);
        task.setContext(getActivity());
        task.setShowProcessDialog(true);
        task.execute(VozConstant.VOZ_LINK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void doCallback(List<Forum> result, Object... extra) {
        if (result == null || result.size() == 0) {
            Toast.makeText(getActivity(), "Cannot access to VozForum. Please try again later.", Toast.LENGTH_SHORT).show();
        }

        mForumGroups = new HashMap<>();
        mForums = new HashMap<>();
        Integer pos = -1;
        for (Forum forum : result) {
            if (forum.getForumGroupName() != null) {
                pos++;
                mForumGroups.put(pos, forum);
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

        // TODO: Fill data to layout
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        LinearLayout parent = (LinearLayout) getView().findViewById(R.id.linearMain);
        updateForum(parent, layoutInflater);
        parent.invalidate();
    }

    private void updateForum(LinearLayout parent, LayoutInflater layoutInflater) {
        Iterator<Integer> iterator = mForumGroups.keySet().iterator();
        while (iterator.hasNext()) {
            int index = iterator.next();
            Forum forumGroup = mForumGroups.get(index);
            LinearLayout forumGroupLayout = (LinearLayout) layoutInflater.inflate(R.layout.main_forum_item, null);
            ((TextView) forumGroupLayout.findViewById(R.id.textMainForum)).setText(forumGroup.getForumGroupName());
            LinearLayout forumsPlaceholder = (LinearLayout) forumGroupLayout.findViewById(R.id.linearSubForum);
            forumsPlaceholder.removeAllViews();
            List<Forum> forums = mForums.get(index);
            for (Forum forum : forums) {
                // update child forum
                View view = createChildForum(forum, layoutInflater);
                forumsPlaceholder.addView(view);
            }
            parent.addView(forumGroupLayout);
        }
    }

    private View createChildForum(Forum forum, LayoutInflater layoutInflater) {
        LinearLayout subForumLayout = (LinearLayout) layoutInflater.inflate(R.layout.sub_forum_item, null);
        ((TextView) subForumLayout.findViewById(R.id.textForumCode)).setText(forum.getId());
        ((TextView) subForumLayout.findViewById(R.id.textForumName)).setText(forum.getForumName());
        ((TextView) subForumLayout.findViewById(R.id.textViewing)).setText(forum.getViewing());
        return subForumLayout;
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
        void onFragmentInteraction(Uri uri);
    }
}