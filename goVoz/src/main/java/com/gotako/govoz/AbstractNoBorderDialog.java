package com.gotako.govoz;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.TextView;

/**
 * Created by Nam on 9/12/2015.
 */
public abstract class AbstractNoBorderDialog extends android.support.v7.app.AppCompatDialogFragment {
    protected Activity activity;
    protected EditText editText;
    protected String title;

    public AbstractNoBorderDialog() {
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

        final View view = inflater.inflate(R.layout.select_page_layout, container);

        editText = (EditText) view.findViewById(R.id.txtSearchString);

        ((TextView) view.findViewById(R.id.txtTitle)).setText(title);

        ((Button) view.findViewById(R.id.btnGo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                doOkAction(editText.getText().toString());
            }
        });

        ((Button) view.findViewById(R.id.btnCancel)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                cancel();
            }
        });

        return view;
    }

    public abstract void doOkAction(String... param);



    public void cancel() {
        doCancelAction();
    }

    private void doCancelAction() {
        // do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getDialog().getWindow().getAttributes());

        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;

        lp.width = (int) (width * 0.9);
        getDialog().getWindow().setAttributes(lp);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
