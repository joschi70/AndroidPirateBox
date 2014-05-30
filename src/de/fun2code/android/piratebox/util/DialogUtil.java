package de.fun2code.android.piratebox.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;

/**
 * Utility class to display dialogue boxes
 * 
 * @author joschi
 *
 */
public class DialogUtil {
	/**
	 * Displays a standard dialogue box with ok button
	 * 
	 * @param activity	Activity context
	 * @param title		title to display
	 * @param message	message to display
	 */
	public static void showDialog(Activity activity, int title, int message) {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(title)
						.setMessage(message)
						.setPositiveButton(android.R.string.ok, null);
				
				AlertDialog dialog = builder.create();
				dialog.show();
	}
	
	/**
	 * Displays a confirmation dialogue
	 * 
	 * @param activity			Activity context
	 * @param title				title to show
	 * @param message			message to display
	 * @param posButtonLabel	positive button label
	 * @param negButtonLabel	negative button label
	 * @param posListener		positive click listener
	 * @param negListener		negative click listener
	 */
	public static void showDialog(Activity activity, int title, int message,
			int posButtonLabel, int negButtonLabel,
			OnClickListener posListener, OnClickListener negListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(title).setMessage(message)
				.setPositiveButton(posButtonLabel, posListener)
				.setNegativeButton(negButtonLabel, negListener);

		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
