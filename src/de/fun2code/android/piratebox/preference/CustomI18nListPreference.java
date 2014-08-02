package de.fun2code.android.piratebox.preference;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import de.fun2code.android.piratebox.Constants;

public class CustomI18nListPreference extends ListPreference {

	public CustomI18nListPreference(Context context) {
		super(context);
	}

	public CustomI18nListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		//setEntries(new CharSequence[] { "English", "German"});
		//setEntryValues(new CharSequence[] { "en", "de"});
		String i18nDir = Constants.getInstallDir(context) + "/html/i18n";
		
		File[] i18nFiles = new File(i18nDir).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().matches("^.*_[a-z]{2}\\.properties$");
			}
		});
		
		Arrays.sort(i18nFiles);
		
		List<CharSequence> entriesList = new ArrayList<CharSequence>();
		List<CharSequence> valuesList = new ArrayList<CharSequence>();
		
		for(File file : i18nFiles) {
			String lang = file.getName().replaceAll("^.*_([a-z]{2})\\.properties$", "$1");
			String langDisp = new Locale(lang).getDisplayLanguage(Locale.getDefault());
			
			entriesList.add(langDisp);
			valuesList.add(lang);
		}
		
		setEntries(entriesList.toArray(new CharSequence[entriesList.size()]));
		setEntryValues(valuesList.toArray(new CharSequence[valuesList.size()]));
	}

}
