package de.fun2code.android.piratebox.util;

import android.os.Environment;
import android.util.Log;
import de.fun2code.android.piratebox.Constants;

public class FileUtil {
	/**
	 * Waits for external storage to be writable
	 * 
	 * @param tries
	 * 				number of retries
	 * @param pauseBeforRetry
	 * 				time to wait (milis) berfore next try
	 * @return
	 * 				{@code true} on success, otherwise {@code false}
	 */
	public static boolean waitExternalStorageWritable(int tries, int pauseBeforRetry) {
	    boolean externalStorageWriteable = false;
	    int count = 0;

	    do {
	        String state = Environment.getExternalStorageState();
	        if(count > 0) {
	            try {
	                Thread.sleep(pauseBeforRetry);
	            } catch (InterruptedException e) {
	                Log.e(Constants.TAG, e.getMessage(), e);
	            }
	        }
	        if (state.equals(Environment.MEDIA_MOUNTED)) {
	            externalStorageWriteable = true;
	        } 
	        count++;
	    } while (!externalStorageWriteable && (count < tries));
	    

	    return externalStorageWriteable;
	}
}
