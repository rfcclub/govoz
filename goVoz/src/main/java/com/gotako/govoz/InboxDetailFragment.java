package com.gotako.govoz;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gotako.govoz.data.PrivateMessageContent;
import com.gotako.govoz.tasks.PMContentDownloadTask;
import com.gotako.govoz.tasks.TaskHelper;
import com.gotako.govoz.utils.DefaultVozWebClient;
import com.gotako.util.Utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.gotako.govoz.VozConstant.VOZ_LINK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InboxDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InboxDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InboxDetailFragment extends VozFragment implements
        ActivityCallback<PrivateMessageContent> {

    private OnFragmentInteractionListener mListener;
    private List<PrivateMessageContent> pmContentList = new ArrayList<PrivateMessageContent>();
    private int lastPage = 1;
    private String pmReplyLink = "";
    private String pmRecipient = "";
    private String pmQuote = "";
    private String securityToken;
    private String loggedInUser;
    private String pmTitle = null;

    public InboxDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InboxDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InboxDetailFragment newInstance() {
        InboxDetailFragment fragment = new InboxDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPrivateMessageContent();
    }

    private void loadPrivateMessageContent() {
        PMContentDownloadTask task = new PMContentDownloadTask(this);
        NavigationItem pmContentLink = VozCache.instance().mNeoNavigationList.get(VozCache.instance().mNeoNavigationList.size() - 1);
        // load threads for private message
        task.setShowProcessDialog(true);
        task.setContext(getActivity());
        task.execute(pmContentLink.mLink);
    }

    @Override
    protected void doRefresh() {
        loadPrivateMessageContent();
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
    public void doCallback(CallbackResult<PrivateMessageContent> callbackResult) {
        pmContentList = callbackResult.getResult();
        Object[] extra = callbackResult.getExtra();
        pmReplyLink = (String)extra[0];
        pmRecipient = (String)extra[1];
        pmQuote = (String)extra[2];
        securityToken = (String)extra[3];
        loggedInUser = (String) extra[4];

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        LinearLayout parent = getView().findViewById(R.id.linearMain);
        parent.removeAllViews();

        LinearLayout pmLayout = (LinearLayout) layoutInflater.inflate(R.layout.pm_detail_item, null);
        LinearLayout threadsPlaceholder = (LinearLayout) pmLayout.findViewById(R.id.linearThreads);

        for (int i = 0; i < pmContentList.size(); i++) {
            View view = layoutInflater.inflate(R.layout.neo_post_item, null);
            PrivateMessageContent post = pmContentList.get(i);
            ((TextView)pmLayout.findViewById(R.id.textThreadsTitle)).setText(post.pmTitle);
            // post date
            TextView postDate = (TextView) view.findViewById(R.id.postDate);
            postDate.setText(post.pmDate);
            // user
            TextView user = (TextView) view.findViewById(R.id.user);
            user.setText(post.pmSender);
            // subtitle
            TextView subTitle = (TextView) view.findViewById(R.id.subTitle);
            if(i> 0) subTitle.setText("Re:" + post.pmTitle);
            else subTitle.setText(post.pmTitle);

            // post count
            ((TextView) view.findViewById(R.id.postCount)).setText("");
            // join date
            ((TextView) view.findViewById(R.id.joinDate)).setText("");
            // rank
            ((TextView) view.findViewById(R.id.rank)).setText(post.pmSenderTitle);
            // posted
            ((TextView) view.findViewById(R.id.posted)).setText("");

            final WebView webView = (WebView) view.findViewById(R.id.content);
            webView.getSettings().setJavaScriptEnabled(true);
            if (webView.isHardwareAccelerated() && VozConfig.instance().isHardwareAccelerated()) {
                webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                webView.setLayerType(View.LAYER_TYPE_NONE, null);
            }
            webView.setDrawingCacheEnabled(false);
            // disable all click listener in webview
            webView.setClickable(false);
            webView.setLongClickable(true);
            webView.setFocusable(false);
            webView.setFocusableInTouchMode(false);
            webView.getSettings().setDefaultFontSize(VozConfig.instance().getFontSize());
            webView.setTag(i); // position
            String utfContent = null;
            try {
                utfContent = new String(post.content.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.d("DEBUG", "UnsupportedEncodingException", e);
            }

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);
            String css = "body{color: #000; background-color: #FFF;}";
            String head = "<head><style type='text/css'>" +
                    css + "\n" +
                    "div#permalink_section\n" +
                    "{\n" +
                    "    white-space: pre-wrap; \n" +
                    "    white-space: -moz-pre-wrap;\n" +
                    "    white-space: -pre-wrap; \n" +
                    "    white-space: -o-pre-wrap;\n" +
                    "    word-wrap: break-word;\n" +
                    "    overflow: hidden;\n" +
                    "}\n" +
                    "</style></head>";
            utfContent = head + "<div style='width="
                    + String.valueOf(outMetrics.widthPixels) + "'>"
                    + utfContent + "</div>";
            post.content = utfContent;

            webView.getSettings().setBuiltInZoomControls(false);
            webView.getSettings().setSupportZoom(false);
            webView.setBackgroundColor(getResources().getColor(Utils.getValueByTheme(R.color.black, R.color.voz_back_color)));
            try {
                TaskHelper.disableSSLCertCheck();
            } catch (Exception e) {
                e.printStackTrace();
            }

            webView.loadDataWithBaseURL(VOZ_LINK + "/", post.content,"text/html", "utf-8", null);
            webView.setWebViewClient(new DefaultVozWebClient(getActivity()));
            threadsPlaceholder.addView(view);
        }
        parent.addView(pmLayout);
        parent.invalidate();
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
        void onLinkClick(String link);
    }
}
