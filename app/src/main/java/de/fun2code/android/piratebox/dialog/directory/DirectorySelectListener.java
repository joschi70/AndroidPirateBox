package de.fun2code.android.piratebox.dialog.directory;

public interface DirectorySelectListener {
	/**
	 * Called when a directory has been selected from the DirectoryDialog
	 * 
	 * @param directory		Selected directory
	 */
	public void onDirectorySelected(String directory);
}
