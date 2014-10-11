package de.fun2code.android.piratebox.preference;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
		
		String i18nDir = Constants.getInstallDir(context) + "/html/i18n";
		
		File[] i18nFiles = new File(i18nDir).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().matches("^.*_[a-z]{2}\\.properties$");
			}
		});
		
		
		if(i18nFiles != null) {
			final String langRegEx = "^.*_([a-z]{2})\\.properties$";
			
			Arrays.sort(i18nFiles, new Comparator<File>(){

				@Override
				public int compare(File lhs, File rhs) {
					String lang1 = lhs.getName().replaceAll(langRegEx, "$1");
					String langDisp1 = new Locale(lang1).getDisplayLanguage(Locale.getDefault());
					
					String lang2 = rhs.getName().replaceAll(langRegEx, "$1");
					String langDisp2 = new Locale(lang2).getDisplayLanguage(Locale.getDefault());
					
					return langDisp1.compareTo(langDisp2);
				}
				
			});
			
			List<CharSequence> entriesList = new ArrayList<CharSequence>();
			List<CharSequence> valuesList = new ArrayList<CharSequence>();
			
			for(File file : i18nFiles) {
				String lang = file.getName().replaceAll(langRegEx, "$1");
				String langDisp = new Locale(lang).getDisplayLanguage(Locale.getDefault());
				
				entriesList.add(langDisp);
				valuesList.add(lang);
			}
			
			setEntries(entriesList.toArray(new CharSequence[entriesList.size()]));
			setEntryValues(valuesList.toArray(new CharSequence[valuesList.size()]));
		}
		else {
			setEntries(new CharSequence[0]);
			setEntryValues(new CharSequence[0]);
		}
		
	}

}
