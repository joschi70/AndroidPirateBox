package de.fun2code.android.piratebox.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import de.fun2code.android.piratebox.util.ServerConfigUtil;

public class CustomServerSettingEditTextPreference extends EditTextPreference {
	// Must match the namespace in preferences.xml
	private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
	private static final String PIRATEBOX_NAMESPACE = "http://fun2code.de/apk/res/piratebox";
	private String pawSetting = null;

	// Standard constructors
	public CustomServerSettingEditTextPreference(Context context) {
		super(context);
	}

	public CustomServerSettingEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		pawSetting = attrs.getAttributeValue(PIRATEBOX_NAMESPACE, "setting");
	}

	public CustomServerSettingEditTextPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		pawSetting = attrs.getAttributeValue(PIRATEBOX_NAMESPACE, "setting");
	}
	
	@Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		setText(ServerConfigUtil.getServerSetting(pawSetting, getContext()));
    }
	
	
	@Override
	public boolean persistString(String value) {
		ServerConfigUtil.storeServerSetting(pawSetting, value, getContext());
		return true;
	}
	

}
