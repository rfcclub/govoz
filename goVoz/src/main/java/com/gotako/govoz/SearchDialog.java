package com.gotako.govoz;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gotako.govoz.AbstractNoBorderDialog;
import com.gotako.govoz.ForumActivity;
import com.gotako.govoz.R;
import com.gotako.govoz.VozCache;

import static com.gotako.govoz.VozConstant.FORUM_URL_F;
import static com.gotako.govoz.VozConstant.FORUM_URL_ORDER;

/**
 * Created by Nam on 9/12/2015.
 */
public class SearchDialog extends AbstractNoBorderDialog {

    protected Activity activity;
    protected EditText editText;
    protected String title;

    public SearchDialog() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = new AppCompatDialog(getActivity(), getTheme());
        return dialog;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        switch (style) {
            case STYLE_NO_INPUT:
                dialog.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // fall through...
            case STYLE_NO_FRAME:
            case STYLE_NO_TITLE:
                ((AppCompatDialog) dialog).supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.quick_search_page_layout, container);
        editText = (EditText) view.findViewById(R.id.txtSearchString);
        RadioButton rdoShowPosts = (RadioButton) view.findViewById(R.id.rdoShowPosts);
        ((TextView) view.findViewById(R.id.txtTitle)).setText("Seach forums");
        (view.findViewById(R.id.btnGo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                doOkAction(editText.getText().toString(), rdoShowPosts.isChecked() ? "1" : "0");
            }
        });

        (view.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        view.findViewById(R.id.btnAdvanceSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                goToAdvanceSearch();
            }
        });
        return view;
    }

    @Override
    public void doOkAction(String... param) {
        if(activity instanceof MainNeoActivity) {
            ((MainNeoActivity)activity).quickSearch(param[0], param[1]);
        }
    }

    private void goToAdvanceSearch() {

    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getDialog().getWindow().getAttributes());

        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;

        lp.width = (int) (width * 0.95);
        getDialog().getWindow().setAttributes(lp);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


}
