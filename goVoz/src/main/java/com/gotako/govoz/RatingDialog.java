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
import android.widget.RadioButton;
import android.widget.TextView;

import com.gotako.govoz.tasks.RatingThreadTask;

/**
 * Created by Nam on 9/12/2015.
 */
public class RatingDialog extends android.support.v7.app.AppCompatDialogFragment {
    protected Activity activity;
    protected EditText editText;
    protected String title;
    private RadioButton star5,star4,star3,star2,star1;
    public RatingDialog() {
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

        final View view = inflater.inflate(R.layout.rating_dialog, container);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        star5 = (RadioButton) view.findViewById(R.id.rating5);
        star4 = (RadioButton) view.findViewById(R.id.rating4);
        star3 = (RadioButton) view.findViewById(R.id.rating3);
        star2 = (RadioButton) view.findViewById(R.id.rating2);
        star1 = (RadioButton) view.findViewById(R.id.rating1);
        ((Button) view.findViewById(R.id.btnGo)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                doRating();
            }
        });
        return view;
    }

    private void doRating() {
        RatingThreadTask ratingThreadTask = new RatingThreadTask();
        ratingThreadTask.execute(String.valueOf(VozCache.instance().getCurrentThread()),
                getVote(),
                String.valueOf(VozCache.instance().getCurrentThreadPage()));
    }

    private String getVote() {
        if(star1.isChecked()) {
            return "1";
        } else if(star5.isChecked()) {
            return "5";
        } else if(star3.isChecked()) {
            return "3";
        } else if(star4.isChecked()) {
            return "4";
        } else {
            return "2";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getDialog().getWindow().getAttributes());

        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;

        lp.width = (int) (width * 0.65);
        getDialog().getWindow().setAttributes(lp);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
