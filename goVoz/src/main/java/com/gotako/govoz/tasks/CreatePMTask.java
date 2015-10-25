package com.gotako.govoz.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.gotako.govoz.ActivityCallback;
import com.gotako.govoz.R;
import com.gotako.govoz.VozCache;
import com.gotako.govoz.VozConstant;
import com.gotako.util.Utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nam on 10/25/2015.
 */
public class CreatePMTask extends AsyncTask<String, Void, Boolean> {

    protected ActivityCallback<Boolean> mCallback;
    private CustomProgressDialog mProgressDialog;
    private Context mContext;

    public CreatePMTask(ActivityCallback<Boolean> callback) {
        this.mCallback = callback;
        this.mContext = (Context) callback;
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog = new CustomProgressDialog(mContext, R.drawable.loadday1);
        mProgressDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String pmRecipient = params[0];
        String pmTitle = params[1];
        String pmContent = params[2];
        String pmReplyLink = params[3];
        String securityToken = params[4];
        boolean doNewPM = false;
        try {
            if(Utils.isNullOrEmpty(pmReplyLink) || Utils.isNullOrEmpty(securityToken)) {
                doNewPM = true;
            }
            if(doNewPM) {
                Document document = Jsoup.connect(VozConstant.VOZ_LINK + "/private.php?do=newpm")
                        .cookies(VozCache.instance().getCookies())
                        .timeout(20000)
                        .get();
                securityToken = getValue(document.select("input[name*=securitytoken]").first());
            }

            Connection connection = Jsoup.connect(VozConstant.VOZ_LINK + "/pmReplyLink");
            if(doNewPM) connection = Jsoup.connect(VozConstant.VOZ_LINK + "/private.php?do=insertpm&pmid=");
            connection = connection.timeout(30000)
                            .cookies(VozCache.instance().getCookies())
                            .data("message", pmContent)
                            .data("wysiwyg", "0")
                            .data("styleid", "0")
                            .data("fromquickreply", "1")
                            .data("s", " ")
                            .data("securitytoken", securityToken)
                            .data("do", "insertpm")
                            .data("parseurl", "1")
                            .data("title", pmTitle)
                            .data("recipients", pmRecipient)
                            .data("forward", "0")
                            .data("savecopy", "1")
                            .data("sbutton", "Submit Message");
            if(doNewPM) connection = connection.data("iconid", "0");
            else {
                String[] splits = pmReplyLink.split("=");
                String pmId = splits[splits.length - 1];
                connection = connection.data("pmid", pmId).data("loggedinuser", params[5]); // params[5] should be logged in user
            }

            Connection.Response localResponse = connection.method(Connection.Method.POST).execute();
            Document localDocument = localResponse.parse();
            String abc = localDocument.text();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private String getValue(Element element) {
        return element.attr("value");
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(mProgressDialog !=null) {
            mProgressDialog.dismiss();
        }
        List<Boolean> boo = new ArrayList<Boolean>();
        boo.add(result);
        mCallback.doCallback(boo, null);
    }
}
