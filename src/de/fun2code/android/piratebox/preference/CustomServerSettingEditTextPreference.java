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
	private boolean isNumeric;
	private Integer numericDivider;

	// Standard constructors
	public CustomServerSettingEditTextPreference(Context context) {
		super(context);
	}

	public CustomServerSettingEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		readAttributes(attrs);
	}

	public CustomServerSettingEditTextPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		readAttributes(attrs);
	}
	
	@Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if(isNumeric && numericDivider != null) {
			int valueInt = Integer.valueOf(ServerConfigUtil.getServerSetting(pawSetting, getContext()));
			valueInt /= numericDivider;
			setText(String.valueOf(valueInt));
		}
		else {
			setText(ServerConfigUtil.getServerSetting(pawSetting, getContext()));
		}
    }
	
	
	@Override
	public boolean persistString(String value) {
		if(isNumeric && numericDivider != null) {
			int valueInt = Integer.valueOf(value);
			valueInt *= numericDivider;
			ServerConfigUtil.storeServerSetting(pawSetting, String.valueOf(valueInt), getContext());
		}
		else {
			ServerConfigUtil.storeServerSetting(pawSetting, value, getContext());
		}
		
		return true;
	}
	
	/**
	 * Reads attribute values and initializes members
	 * 
	 * @param attrs attributes to read from
	 */
	private void readAttributes(AttributeSet attrs) {
		pawSetting = attrs.getAttributeValue(PIRATEBOX_NAMESPACE, "setting");
		isNumeric = attrs.getAttributeValue(ANDROID_NAMESPACE, "numeric") != null ? true : false;
		try {
			numericDivider = Integer.valueOf(attrs.getAttributeValue(PIRATEBOX_NAMESPACE, "numericDivider"));
		}
		catch(Exception e) {
			numericDivider = null;
		}
	}
	

}
