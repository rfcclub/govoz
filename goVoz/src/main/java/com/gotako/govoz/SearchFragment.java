package com.gotako.govoz;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gotako.govoz.data.SearchDumpObject;
import com.gotako.govoz.data.Thread;
import com.gotako.govoz.tasks.VozForumSearchTask;

import java.util.List;

import static com.gotako.govoz.VozConstant.PASSWORD;
import static com.gotako.govoz.VozConstant.SPLIT_SIGN;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends ForumFragment {
    private static final String SEARCH_STRING = "searchString";
    private static final String SHOW_POSTS = "showPosts";

    private String mSearchString = null;
    private String mShowPosts = null;
    private boolean mForceReload = true;
    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param searchString Parameter 1.
     * @param showPosts Parameter 2.
     * @return A new instance of fragment SampleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String searchString, String showPosts) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_STRING, searchString);
        args.putString(SHOW_POSTS, showPosts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void processNavigationLink() {
        loadThreads();
    }

    @Override
    public void loadThreads() {
        // last element of list should be forum link
        NavigationItem currentNavigationItem = VozCache.instance().currentNavigateItem();
        String searchLink = currentNavigationItem.mLink;
        boolean forceReload = VozConfig.instance().isAutoReloadForum() || mForceReload;
        mForceReload = false; // reset force reload immediately
        VozForumSearchTask searchTask = new VozForumSearchTask(this);
        searchTask.setContext(getActivity());
        if (searchLink != null) {
            String key = searchLink + "&page=" + VozCache.instance().currentNavigateItem().mCurrentPage;
            SearchDumpObject searchDumpObject = (SearchDumpObject) VozCache.instance().getDataFromCache(key);
            if (!forceReload && searchDumpObject != null) {
                List<Thread> threads = searchTask.processResult(searchDumpObject.document);
                doCallback(threads, null, searchDumpObject.lastPage, searchTask.getForumName());
            } else {
                if (mSearchString != null && mShowPosts != null) {
                    searchTask.setShowProcessDialog(true);
                    searchTask.execute(mSearchString, mShowPosts);
                }
            }
        } else {
            if (mSearchString != null && mShowPosts != null) {
                searchTask.setShowProcessDialog(true);
                searchTask.execute(mSearchString, mShowPosts);
            } else {
                // TODO
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSearchString = getArguments().getString(SEARCH_STRING);
            mShowPosts = getArguments().getString(SHOW_POSTS);
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
        void onThreadClicked(Thread thread);
        void updateNavigationPanel(boolean visible);
    }
}
