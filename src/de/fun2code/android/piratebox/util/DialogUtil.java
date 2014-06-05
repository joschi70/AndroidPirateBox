package de.fun2code.android.piratebox.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;

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
	public static void showDialog(Context context, int title, int message) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
	public static void showDialog(Context context, int title, int message,
			int posButtonLabel, int negButtonLabel,
			OnClickListener posListener, OnClickListener negListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(message)
				.setPositiveButton(posButtonLabel, posListener)
				.setNegativeButton(negButtonLabel, negListener);

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * Displays an input dialog
	 * 
	 * @param title					title to display
	 * @param body					message to show
	 * @param input					input to use
	 * @param iconId				icon to use, may be {@code null}
	 * @param positiveListener		listener for positive button
	 * @param negativeListener		listener for negative button
	 */
	public static void showInputDialog(Context context, CharSequence title,
			CharSequence body, EditText input, int iconId,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negativeListener) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(title);
				builder.setIcon(iconId);
				builder.setMessage(body);
				builder.setView(input);

				builder.setPositiveButton(context.getText(android.R.string.ok),
						positiveListener);
				builder.setNegativeButton(
						context.getText(android.R.string.cancel),
						negativeListener);

				AlertDialog ad = builder.create();
				ad.show();
	}
}
