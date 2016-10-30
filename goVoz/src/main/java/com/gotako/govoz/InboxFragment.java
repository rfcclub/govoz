package com.gotako.govoz;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.gotako.govoz.data.PrivateMessage;
import com.gotako.govoz.tasks.PMDownloadTask;

import java.util.ArrayList;
import java.util.List;

import static com.gotako.govoz.VozConstant.VOZ_LINK;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InboxFragment extends VozFragment implements
        ActivityCallback<PrivateMessage> {

    private InboxFragment.OnFragmentInteractionListener mListener;

    private List<PrivateMessage> mPMList = new ArrayList<PrivateMessage>();

    public InboxFragment() {
        // Required empty public constructor
    }

    public static InboxFragment newInstance() {
        InboxFragment fragment = new InboxFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPrivateMessages(0);
    }

    private void loadPrivateMessages(int folderId) {
        PMDownloadTask task = new PMDownloadTask(this);
        String pmHttpsLink = VOZ_LINK + "/" + "private.php?s=&pp=50&folderid=" + String.valueOf(folderId);
        // load threads for forum
        task.setShowProcessDialog(true);
        task.setContext(getActivity());
        task.execute(pmHttpsLink);
    }

    @Override
    protected void doRefresh() {
        loadPrivateMessages(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_layout, container, false);
    }


    @Override
    public void doCallback(List<PrivateMessage> result, Object... extra) {
        mPMList = result;
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        LinearLayout parent = (LinearLayout) getView().findViewById(R.id.linearMain);
        parent.removeAllViews();

        LinearLayout pmLayout = (LinearLayout) layoutInflater.inflate(R.layout.inbox_pm_item, null);
        pmLayout.findViewById(R.id.inbox).setOnClickListener((view) -> {
            loadPrivateMessages(0);
        });
        pmLayout.findViewById(R.id.sentitems).setOnClickListener((view) -> {
            loadPrivateMessages(-1);
        });
        LinearLayout threadsPlaceholder = (LinearLayout) pmLayout.findViewById(R.id.linearThreads);
        threadsPlaceholder.removeAllViews();
        // pm insertion
        for (PrivateMessage thread : mPMList) {

            View view = createPM(thread, layoutInflater);
            threadsPlaceholder.addView(view);
        }
        parent.addView(pmLayout);

        parent.invalidate();
        updateNavigationPanel();
    }

    private View createPM(final PrivateMessage thread, final LayoutInflater layoutInflater) {
        final LinearLayout threadLayout = (LinearLayout) layoutInflater.inflate(R.layout.neo_inbox_item, null);
        ((TextView)threadLayout.findViewById(R.id.pmSender)).setText(thread.pmSender);
        ((TextView)threadLayout.findViewById(R.id.pmTitle)).setText(thread.pmTitle);
        ((TextView)threadLayout.findViewById(R.id.pmDate)).setText(thread.pmDate);
        threadLayout.findViewById(R.id.ripple).setOnClickListener(v -> {
            if (mListener != null) mListener.onPMClicked(thread.pmLink);
        });
        return threadLayout;
    }

    private void updateNavigationPanel() {
        if (mListener != null) mListener.updateNavigationPanel(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InboxFragment.OnFragmentInteractionListener) {
            mListener = (InboxFragment.OnFragmentInteractionListener) context;
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

    public interface OnFragmentInteractionListener {
        void updateNavigationPanel(boolean visible);

        void onPMClicked(String pmLink);
    }


}
