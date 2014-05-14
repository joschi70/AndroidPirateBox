package de.fun2code.android.piratebox.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.R;

public class PirateUtil {
	
	/**
	 * Calculates the current number of uploaded files
	 * 
	 * @param context	Context to use
	 * @return			number of uploaded files
	 */
	public static int calculateUploads(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String storageDir = preferences.getString(Constants.PREF_STORAGE_DIR, context.getResources().getString(R.string.pref_storage_dir_default));
		File storageDirFile = new File(storageDir + "/uploads");
		
		int uploads = 0;
		
		if(storageDirFile.isDirectory()) {
			for(File file : storageDirFile.listFiles()) {
				if(file.isFile()) {
					uploads++;
				}
			}
		}
		
		return uploads;
	}
	
	/**
	 * Calculates the current number of shout/chat messages
	 * 
	 * @param context	Context to use
	 * @return			number of shout/chat messages
	 */
	public static int calculateMessages(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String storageDir = preferences.getString(Constants.PREF_STORAGE_DIR, context.getResources().getString(R.string.pref_storage_dir_default));
		File chatFile = new File(storageDir + "/chat/data.bso");
		
		int messages = 0;
		
		if(chatFile.exists()) {
			BufferedReader br = null;
			
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(chatFile));
				br = new BufferedReader(new InputStreamReader(in));
				
				while (br.readLine() != null)   {
				  messages++;
				}
			}
			catch(IOException e) {
				Log.e(Constants.TAG, "Unable to count messages");
			}
			finally {
				if(br != null) {
					try {
						br.close();
					} catch (IOException e) {
						// Game over, ignore
					}
				}
			}
		}
		
		return messages;
	}
}
