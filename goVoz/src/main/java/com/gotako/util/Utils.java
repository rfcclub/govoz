package com.gotako.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.gotako.govoz.VozConfig;

public class Utils {
    private static Random random = new Random(System.nanoTime());

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

    public static String flatMap(Map<String, String> cookies) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = cookies.keySet().iterator();
        int count = 0;
        while(iter.hasNext()) {
            if(count > 0) builder.append("; ");
            count++;
            String key = iter.next();
            String value = cookies.get(key);
            builder.append(key +"=" + value);
        }
        return builder.toString();
    }

    public static String getRandomMessage(String[] inboxMessages) {
        return inboxMessages[random.nextInt(inboxMessages.length-1)];
    }

    public static String getPath(String url) {
        int index = url.indexOf("/","https://".length());
        return url.substring(index);
    }

    public static String getContentType(String url) {
        if(url.endsWith("png")) return "image/png";
        else if(url.endsWith("jpg")) return "image/jpg";
        else if(url.endsWith("gif")) return "image/gif";
        else if(url.endsWith("jpeg")) return "image/jpeg";
        else if(url.endsWith("bmp")) return "image/bmp";
        else return "image/jpeg";
    }

    public static long convertToMinutes(long l) {
        return TimeUnit.MILLISECONDS.toMinutes(l);
    }
}
