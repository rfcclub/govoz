package com.gotako.govoz;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import com.gotako.util.Utils;

public class SettingActivity extends Activity {

	private CheckBox loadImageByDemand;
	private CheckBox autoReloadForum;
	private CheckBox supportLongAvatar;
	private EditText fontSize;
	private VozConfig config;
	private RadioButton currentCheckedRadio,darkThemeRadio,lightThemeRadio;
	private CheckBox chkShowSign;
	private CheckBox hardwareAccelerated;
	private CheckBox useBackgroundService;
	private CheckBox isPreloadForumsAndThreads;
	private CheckBox useDnsOverVpn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		VozConfig.instance().load(this);
		if (VozConfig.instance().isDarkTheme()) {
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.AppTheme_Light);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		setTitle("Setting");	
		config = VozConfig.instance();
		config.load(this);

        loadImageByDemand = findViewById(R.id.loadImageByDemand);
		autoReloadForum = findViewById(R.id.autoReloadForum);
		supportLongAvatar = findViewById(R.id.supportLongAvatar);
		chkShowSign = findViewById(R.id.chkShowSign);
		fontSize = findViewById(R.id.fontSize);
		useDnsOverVpn = findViewById(R.id.usingVpn);
		fontSize.setText(String.valueOf(config.getFontSize()));
		loadImageByDemand.setChecked(config.isLoadImageByDemand());
		autoReloadForum.setChecked(config.isAutoReloadForum());
		supportLongAvatar.setChecked(config.isSupportLongAvatar());
		chkShowSign.setChecked(config.isShowSign());

		darkThemeRadio = (RadioButton)findViewById(R.id.darkThemeRadio);
        lightThemeRadio = (RadioButton)findViewById(R.id.lightThemeRadio);
        if(config.isDarkTheme()) {
            darkThemeRadio.setChecked(true);
            lightThemeRadio.setChecked(false);
        } else {
            darkThemeRadio.setChecked(false);
            lightThemeRadio.setChecked(true);
        }
		//currentCheckedRadio = getRadioForDrawable(config.getLoadingDrawable());
		//currentCheckedRadio.setChecked(true);

		hardwareAccelerated = (CheckBox)findViewById(R.id.hardwareAccelerated);
		hardwareAccelerated.setChecked(config.isHardwareAccelerated());
		useBackgroundService = (CheckBox) findViewById(R.id.useBackgroundService);
		useBackgroundService.setChecked(config.isUseBackgroundService());
		isPreloadForumsAndThreads = (CheckBox) findViewById(R.id.preloadThings);
		isPreloadForumsAndThreads.setChecked(config.isPreloadForumsAndThreads());
		useDnsOverVpn.setChecked(config.isUsingDnsOverVpn());
        View rootView = findViewById(R.id.rootSettingLayout);
        rootView.setBackgroundColor(Utils.getColorByTheme(this, R.color.background_material_light_dark, R.color.voz_back_color));
	}

	private RadioButton getRadioForDrawable(int loadingDrawable) {
		RadioButton button = null;		
		switch(loadingDrawable) {
			case R.drawable.load159:
					button = (RadioButton)findViewById(R.id.radio159);
					break;
			case R.drawable.load257:
					button = (RadioButton)findViewById(R.id.radio257);
					break;			
			case R.drawable.load35:
				button = (RadioButton)findViewById(R.id.radio35);
					break;
			case R.drawable.load715:
				button = (RadioButton)findViewById(R.id.radio715);
					break;
			case R.drawable.loadday1:
				button = (RadioButton)findViewById(R.id.radioday1);
					break;
			case R.drawable.load278:
			default:
				button = (RadioButton)findViewById(R.id.radio278);
				
		}
		return button;
	}

	private int parseDrawableFromRadio(int id) {
		int result = R.drawable.load278;
		switch (id) {
		case R.id.radio159:
			result = R.drawable.load159;
			break;
		case R.id.radio257:
			result = R.drawable.load257;
			break;
		case R.id.radio35:
			result = R.drawable.load35;
			break;
		case R.id.radio715:
			result = R.drawable.load715;
			break;
		case R.id.radioday1:
			result = R.drawable.loadday1;
			break;
		case R.id.radio278:
		default:
			result = R.drawable.load278;
		}		
		return result;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void saveConfig(View view) {
		config.setLoadImageByDemand(loadImageByDemand.isChecked());
		config.setFontSize(Integer.parseInt(fontSize.getText().toString()));
		config.setAutoReloadForum(autoReloadForum.isChecked());		
		config.setSupportLongAvatar(supportLongAvatar.isChecked());
		//config.setLoadingDrawable(parseDrawableFromRadio(currentCheckedRadio.getId()));
		config.setShowSign(chkShowSign.isChecked());
		config.setHardwareAccelerated(hardwareAccelerated.isChecked());
		config.setUseBackgroundService(useBackgroundService.isChecked());
		config.setPreloadForumsAndThreads(isPreloadForumsAndThreads.isChecked());
		config.setUsingDnsOverVpn(useDnsOverVpn.isChecked());
        if(darkThemeRadio.isChecked()) {
            config.setDarkTheme(true);
        } else {
            config.setDarkTheme(false);
        }
		config.save(this);		
		this.finish();
	}
	

	public void animationChoosing(View view) {
		if (view instanceof RadioButton) {
			RadioButton button = (RadioButton)view;
			if(button.isChecked()) {				
				currentCheckedRadio.setChecked(false);				
				currentCheckedRadio = button;
			}
		}
	}

    @Override
    public void onBackPressed() {
        saveConfig(null);
    }
}
