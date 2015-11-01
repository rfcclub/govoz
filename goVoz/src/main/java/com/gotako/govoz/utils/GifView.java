/**
 * 
 */
package com.gotako.govoz.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.gotako.govoz.data.GifUrlDrawable;
import com.gotako.govoz.data.UrlDrawable;

/**
 * @author Nam
 *
 */
public class GifView extends ImageView {
	private Movie movie = null;
	private int id;
	private long movieStart;
	private boolean isInitialized = false;
    private Drawable drawable;
    private boolean isShowAsBitmap;

    public GifView(Context context) {
		super(context);
	}

	public GifView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GifView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private void initializeView() {
        GifUrlDrawable urlDrawable = (GifUrlDrawable)drawable;
        File file = new File(urlDrawable.drawableUrl);
        try {
            movie = Movie.decodeStream(new BufferedInputStream(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        isInitialized = true;
		movieStart = 0;
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
        this.drawable = drawable;
	}

	@Override
	protected void onDraw(Canvas canvas) {
        if(drawable instanceof GifUrlDrawable) {
            if(!isInitialized) initializeView();
            canvas.drawColor(Color.TRANSPARENT);
            super.onDraw(canvas);
            long now = android.os.SystemClock.uptimeMillis();
            if (movieStart == 0) {
                movieStart = now;
            }
            if (movie != null) {
                int relTime = (int) ((now - movieStart) % movie.duration());
                movie.setTime(relTime);
                movie.draw(canvas, getWidth() - movie.width(),
                        getHeight() - movie.height());
                this.invalidate();
            } else {
                if(!isShowAsBitmap) {
                    try {
                        GifUrlDrawable urlDrawable = (GifUrlDrawable) drawable;
                        Bitmap bm = BitmapFactory.decodeStream(new BufferedInputStream(new FileInputStream(urlDrawable.drawableUrl)));
                        setImageBitmap(bm);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    isShowAsBitmap = true;
                }
            }
        } else {
            super.onDraw(canvas);
        }
	}
}
