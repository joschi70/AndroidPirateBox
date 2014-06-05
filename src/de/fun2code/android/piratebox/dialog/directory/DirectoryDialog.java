package de.fun2code.android.piratebox.dialog.directory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import de.fun2code.android.piratebox.R;
import de.fun2code.android.piratebox.util.DialogUtil;

/**
 * Directory chooser dialog
 * 
 * @author joschi
 *
 */
public class DirectoryDialog extends Dialog implements OnItemClickListener,
		DialogInterface.OnCancelListener {
	private DirectoryDialog dialog;

	private List<File> fileList;
	private List<NavBackStackInfo> navBackStack;
	private int navDirection;

	private DirectoryAdapter adapter;
	private Handler handler;

	private ListView listView;
	private Button buttonCancel;
	private Button buttonOk;
	private TextView currentDirText;
	DirectorySelectListener directorySelectListener;

	private String currentDir;
	private String topDir = "/";
	private boolean showOnlyWritableDirs = false;

	private Context context;
	private boolean showCreateButton = true;

	private int NAV_NEUTRAL = 0;
	private int NAV_UP = 1;
	private int NAV_DOWN = 2;

	/**
	 * Constructs a directory chooser dialog
	 * 
	 * @param context				{@code Context} to use
	 * @param showCreateButton		show create directory button
	 */
	public DirectoryDialog(Context context, boolean showCreateButton) {
		super(context);
		this.context = context;
		this.showCreateButton = showCreateButton;
		init();
	}

	/**
	 * Constructs a directory chooser dialog
	 * 
	 * @param context				Constructs a directory chooser dialog
	 * @param theme					theme to use
	 * @param showCreateButton		show create directory button
	 */
	public DirectoryDialog(Context context, int theme, boolean showCreateButton) {
		super(context, theme);
		this.context = context;
		this.showCreateButton = showCreateButton;
		init();
	}

	/**
	 * Initializes the layout
	 */
	private void init() {
		handler = new Handler();
		navBackStack = new ArrayList<NavBackStackInfo>();
		navDirection = NAV_NEUTRAL;

		setContentView(R.layout.dialog_directory);

		dialog = this;
		handler = new Handler();

		currentDirText = (TextView) findViewById(R.id.currentDirectory);
		currentDirText.setText(currentDir == null ? "" : currentDir);

		buttonCancel = (Button) findViewById(R.id.directory_dialog_cancel);
		buttonOk = (Button) findViewById(R.id.directory_dialog_ok);

		buttonCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (directorySelectListener != null) {
					directorySelectListener.onDirectorySelected(null);
				}
				dismiss();
			}
		});

		Button buttonCreateDirectory = (Button) findViewById(R.id.createDirectory);
		buttonCreateDirectory.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				createDirectory();
			}
		});
		
		buttonCreateDirectory.setVisibility(showCreateButton ? View.VISIBLE : View.GONE);

		buttonOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (directorySelectListener != null) {
					directorySelectListener.onDirectorySelected(currentDir);
					dismiss();
				}
			}
		});

		setOnCancelListener(this);

		if (fileList == null) {
			fileList = new ArrayList<File>();
			adapter = new DirectoryAdapter(context, fileList);
			refresh();
		}

		listView = (ListView) findViewById(R.id.directory_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (directorySelectListener != null) {
			directorySelectListener.onDirectorySelected(null);
		}
		dismiss();

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		File f = fileList.get((int) id);
		if (f.getName().equals("..")) {
			currentDir = new File(currentDir).getParentFile().getAbsolutePath();
			navDirection = NAV_UP;
			updateListInfo();
		} else if (f.isDirectory()) {
			currentDir = f.getAbsolutePath();
			navDirection = NAV_DOWN;
			pushNavBackStack();
			updateListInfo();
		}

	}

	/**
	 * Notifies the adapter that data has changed
	 */
	private void notifyAdapter() {
		if (adapter == null) {
			return;
		}

		handler.post(new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
				if (navDirection == NAV_UP) {
					popNavBackStack();
				} else {
					// Set list to top position
					listView.setSelectionFromTop(0, 0);
				}
			}
		});
	}

	/**
	 * Sets the {@code DirectorySelectListener} that is called when a
	 * directory has been selected
	 * 
	 * @param listener	specifies the {@code DirectorySelectListener}
	 */
	public void setOnDirectorySelectListener(DirectorySelectListener listener) {
		directorySelectListener = listener;
	}

	/**
	 * Set start directory
	 * 
	 * @param dir	directory to use
	 */
	public void setCurrentDirectory(String dir) {
		currentDir = dir;
	}

	/**
	 * Returns the start directory
	 * 	
	 * @return	name of the start directory
	 */
	public String getCurrentDirectory() {
		return currentDir;
	}
	
	/**
	 * Sets the top directory, default is {@literal /}
	 * 
	 * @param dir	name of top directory
	 */
	public void setTopDirectory(String dir) {
		topDir = dir;
	}

	/**
	 * Returns the top directory
	 * 
	 * @return	name of top directory
	 */
	public String getTopDirectory() {
		return topDir;
	}
	
	/**
	 * Defines if only writable directories should be displayed
	 * 
	 * @param state		if {@code true} only writable directories are displayed,
	 * 					otherwise all directories are shown
	 */
	public void showOnlyWritableDirs(boolean state) {
		showOnlyWritableDirs = state;
	}

	/**
	 * Updates the title to display the currently selected directory
	 */
	private void updateInfoText() {
		handler.post(new Runnable() {
			public void run() {
				currentDirText.setText(currentDir);
			}
		});
	}

	/**
	 * Updates file and directory lists before the adapter is updated
	 */
	private void updateListInfo() {
		if (currentDir == null) {
			return;
		}

		List<File> dirs = new ArrayList<File>();
		List<File> files = new ArrayList<File>();

		File[] allFiles = new File(currentDir).listFiles();

		if (allFiles == null) {
			return;
		}

		fileList.clear();
		for (File f : allFiles) {
			if (f.isDirectory() && !f.getName().startsWith(".")) {
				// If showOnlyWritableDirs is true, show only writable directories
				if(!showOnlyWritableDirs || f.canWrite()) {
					dirs.add(f);
				}
			} else {
				files.add(f);
			}
		}

		orderFileNames(dirs);
		fileList.addAll(dirs);

		if (!currentDir.equals(topDir)
				&& new File(currentDir).getParent() != null) {
			fileList.add(0, new File(".."));
		}

		notifyAdapter();
		updateInfoText();
	}

	/**
	 * Refreshes the list information
	 */
	public void refresh() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				updateListInfo();

			}
		});
	}

	/**
	 * Sorts the file names
	 * 
	 * @param list	{@code File} list to sort
	 */
	private void orderFileNames(List<File> list) {
		Collections.sort(list, new Comparator<File>() {

			public int compare(File f1, File f2) {
				return f1.getName().toLowerCase()
						.compareTo(f2.getName().toLowerCase());
			}

		});
	}

	/**
	 * Pushes the current scroll position to the navigation stack
	 * 
	 * Taken from:
	 * http://stackoverflow.com/questions/3014089/scroll-to-a-position
	 * -in-a-listview
	 */
	private void pushNavBackStack() {
		int index = listView.getFirstVisiblePosition();
		View v = listView.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();

		NavBackStackInfo nbsi = new NavBackStackInfo();
		nbsi.index = index;
		nbsi.top = top;

		navBackStack.add(0, nbsi);
	}

	/**
	 * Pops an entry from the navigation stack and restores the scroll position
	 */
	private void popNavBackStack() {
		if (navBackStack.size() > 0) {
			NavBackStackInfo info = navBackStack.remove(0);
			listView.setSelectionFromTop(info.index, info.top);
		}
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
	private void showInputDialog(final CharSequence title,
			final CharSequence body, final EditText input, final int iconId,
			final DialogInterface.OnClickListener positiveListener,
			final DialogInterface.OnClickListener negativeListener) {
		handler.post(new Runnable() {
			public void run() {
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
		});
	}

	/**
	 * Shows the create directory dialog
	 */
	private void createDirectory() {
		final EditText dirName = new EditText(context);
		handler.post(new Runnable() {
			@Override
			public void run() {
				DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newDir = currentDir + File.separator
								+ dirName.getText();
						if (new File(newDir).mkdir()) {
							currentDir = newDir;
							navDirection = NAV_DOWN;
							pushNavBackStack();
							updateListInfo();
						}
					}
				};

				DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing
					}
				};

				DialogUtil.showInputDialog(context,
						context.getText(R.string.dialog_title_create_directory),
						context.getText(R.string.dialog_label_name), dirName,
						R.drawable.directory, positiveListener,
						negativeListener);
			}

		});
	}

	/**
	 * Navigation back stack class
	 * 
	 * @author joschi
	 *
	 */
	private class NavBackStackInfo {
		int index;
		int top;
	}

}