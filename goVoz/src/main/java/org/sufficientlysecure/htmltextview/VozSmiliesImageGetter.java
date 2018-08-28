/*
 * Copyright (C) 2016 Daniel Passos <daniel@passos.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sufficientlysecure.htmltextview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Assets Image Getter
 *
 * Load image from assets folder
 *
 * @author <a href="mailto:daniel@passos.me">Daniel Passos</a>
 */
public class VozSmiliesImageGetter implements Html.ImageGetter {

    private  Context context;
    private  TextView textView;
    public VozSmiliesImageGetter(Context context, TextView textView) {
        this.textView = textView;
        this.context = context;
    }

    private float getScale(Drawable drawable) {
        View container = textView;
        if (container == null) {
            return 1f;
        }

        float maxWidth = container.getWidth();
        float originalDrawableWidth = drawable.getIntrinsicWidth();

        return maxWidth / originalDrawableWidth;
    }
    @Override
    public Drawable getDrawable(String source) {

        try {
            InputStream inputStream = context.getAssets().open(source);
            Bitmap bm = BitmapFactory.decodeStream(inputStream);
            Drawable d = new BitmapDrawable(context.getResources(), bm);
//            Drawable d = Drawable.createFromStream(inputStream, null);
            d.setBounds(0, 0, d.getIntrinsicWidth() * 2, d.getIntrinsicHeight() * 2);
            textView.postDelayed(()-> {
                textView.invalidate();
                textView.setText(textView.getText());
            }, 50);
            return d;
        } catch (IOException e) {
            // prevent a crash if the resource still can't be found
            Log.e(HtmlTextView.TAG, "source could not be found: " + source);
            return null;
        }

    }

}
