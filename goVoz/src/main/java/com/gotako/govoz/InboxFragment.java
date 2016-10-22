package com.gotako.govoz;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
public class InboxFragment extends Fragment implements
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
        loadPrivateMessages();
    }

    private void loadPrivateMessages() {
        PMDownloadTask task = new PMDownloadTask(this);
        String pmHttpsLink = VOZ_LINK + "/" + "private.php";
        // load threads for forum
        task.setShowProcessDialog(true);
        task.setContext(getActivity());
        task.execute(pmHttpsLink);
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
        LinearLayout threadsPlaceholder = (LinearLayout) pmLayout.findViewById(R.id.linearThreads);

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
        threadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onPMClicked(thread.pmLink);
            }
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
