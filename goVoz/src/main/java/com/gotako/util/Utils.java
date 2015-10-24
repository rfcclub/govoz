package com.gotako.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.gotako.govoz.ThreadActivity;
import com.gotako.govoz.VozConfig;

public class Utils {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String md5(String paramString) {
        StringBuffer localStringBuffer = new StringBuffer();
        try {
            byte[] arrayOfByte = MessageDigest.getInstance("MD5").digest(paramString.getBytes());
            System.out.println(arrayOfByte);
            for (int i = 0; i < arrayOfByte.length; i++)
                localStringBuffer.append(Integer.toHexString(0x100 | 0xFF & arrayOfByte[i]).substring(1, 3));
        } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
        }
        return localStringBuffer.toString();
    }

    public static int convertInt(String value, int defaultValue) {
        if (isEmpty(value)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getFirstText(Elements eles) {
        if (eles != null && eles.size() > 0) {
            return eles.first().text();
        } else {
            return "";
        }
    }

    public static String getFirstOwnText(Elements eles) {
        if (eles != null && eles.size() > 0) {
            return eles.first().ownText();
        } else {
            return "";
        }
    }

    public static String getFirstHtml(Elements eles) {
        if (eles != null && eles.size() > 0) {
            return eles.first().html();
        } else {
            return "";
        }
    }

    public static Element getFirstElement(Elements eles) {
        if (eles != null && eles.size() > 0) {
            return eles.first();
        } else {
            return null;
        }
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static void writeToFile(FileOutputStream stream, Object content) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(content);
            oos.close();
        } catch (Exception e) {
        }
    }

    public static Object readFromFile(FileInputStream stream) {
        try {
            ObjectInputStream oos = new ObjectInputStream(stream);
            Object result = oos.readObject();
            oos.close();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean checkNetworkConnection(Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    public static String getString(Context context, int id) {
        return context.getString(id);
    }

    public static Element getElementAt(Elements trs, int i) {
        Element ele = null;
        try {
            ele = trs.get(i);
        } catch (Exception ex) {
            // do nothing
        }
        return ele;
    }

    public static int getValueByTheme(int a, int b) {
        if (VozConfig.instance().isDarkTheme()) return a;
        else return b;
    }

    public static int getColorByTheme(Context ctx, int black, int white) {
        return ctx.getResources().getColor(getValueByTheme(black, white));
    }

    public static Drawable getDrawableByTheme(Context ctx, int black, int white) {
        return ctx.getResources().getDrawable(getValueByTheme(black, white));
    }
}
