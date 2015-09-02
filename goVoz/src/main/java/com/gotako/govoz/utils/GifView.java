/**
 * 
 */
package com.gotako.govoz.utils;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Nam
 *
 */
public class GifView extends View {
	private Movie movie = null;
	private int id;
	private long movieStart;
	private boolean isInitialized = false;

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
		// R.drawable.loader - our animated GIF
		if (id != 0) {
			InputStream is = getContext().getResources().openRawResource(id);
			movie = Movie.decodeStream(is);
			isInitialized = true;
			movieStart = 0;
		}
	}

	public void setImageResource(int id) {
		this.id = id;
		isInitialized = false; // set to false so it is reinitialized when draw
		initializeView();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		initializeView();
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
		}
	}

}
