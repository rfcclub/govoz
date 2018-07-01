/**
 * 
 */
package com.gotako.govoz.tasks;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.gotako.govoz.R;
import com.gotako.govoz.utils.GifView;
import com.gotako.util.Utils;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * @author Nam
 *
 */
public class CustomProgressDialog extends Dialog {
	private ImageView iv;
    AVLoadingIndicatorView aiv;
	public CustomProgressDialog(Context context, int theme) {
		super(context, com.gotako.govoz.R.style.TransparentProgressDialog);
		WindowManager.LayoutParams wlmp = getWindow().getAttributes();
		wlmp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
		getWindow().setAttributes(wlmp);
		setTitle(null);
		setCancelable(true);
		setOnCancelListener(null);
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

//		iv = new ImageView(context);
//		iv.setImageResource(theme);
//		layout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//		layout.addView(iv, params);
        aiv = new AVLoadingIndicatorView(context);
        aiv.setIndicator("BallGridPulseIndicator");
        aiv.setIndicatorColor(Color.DKGRAY);
        layout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		layout.addView(aiv, params);
		addContentView(layout, layoutParams);
	}

	@Override
	public void show() {
		super.show();		
//		RotateAnimation anim = new RotateAnimation(0.0f, 360.0f,
//				Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
//				.5f);
//		anim.setInterpolator(new LinearInterpolator());
//		anim.setRepeatCount(Animation.INFINITE);
//		anim.setDuration(500);
//		iv.setAnimation(anim);
//		iv.startAnimation(anim);
        aiv.show();
	}

	@Override
    public void hide() {
	    super.hide();
	    aiv.hide();
    }
}
