package com.gotako.govoz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by Nam on 9/12/2015.
 */
public class SearchDialog extends AbstractNoBorderDialog {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.quick_search_page_layout, container);
        editText = (EditText) view.findViewById(R.id.txtSearchString);
        RadioButton rdoShowPosts = (RadioButton) view.findViewById(R.id.rdoShowPosts);
        ((TextView) view.findViewById(R.id.txtTitle)).setText(title);
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


}
