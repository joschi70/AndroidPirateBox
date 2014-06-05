package de.fun2code.android.piratebox.dialog.directory;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.fun2code.android.piratebox.R;

/**
 * ListView adapter that displays file and directory information
 * 
 * @author joschi
 *
 */
public class DirectoryAdapter extends BaseAdapter {
	private Context context;

	private List<File> listFileEntries;

	public DirectoryAdapter(Context context, List<File> listFileEntries) {
		this.context = context;
		this.listFileEntries = listFileEntries;
	}

	public int getCount() {
		return listFileEntries.size();
	}

	public Object getItem(int position) {
		return listFileEntries.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View view, ViewGroup viewGroup) {
		File entry = listFileEntries.get(position);
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.directory_files_row, null);
		}

		ImageView imageType = (ImageView) view.findViewById(R.id.fileImageType);
		TextView textInfo = (TextView) view.findViewById(R.id.textInfo);

		if (entry.getName().equals("..")) {
			imageType.setImageResource(R.drawable.back);
			textInfo.setVisibility(View.VISIBLE);
			textInfo.setText("");
		} else if (entry.isDirectory()) {
			
			try {
				Bitmap image = BitmapFactory.decodeStream(context
					.getResources().getAssets()
					.open("file_icons/directory.png"));
				imageType.setImageBitmap(image);
			}
			catch(Exception e) {
				imageType.setImageResource(R.drawable.directory);
			}
			
			textInfo.setVisibility(View.VISIBLE);
			//textInfo.setText(new Date(entry.lastModified()).toLocaleString());
			textInfo.setText(DateFormat.getDateTimeInstance().format(new Date(entry.lastModified())));
		} else {
			textInfo.setVisibility(View.VISIBLE);
			imageType.setImageResource(R.drawable.file);
			textInfo.setText(String.format("%.2f MB",
					entry.length() / 1024.0 / 1024.0));

			String ext = "default";

			if (entry.getName().matches(".*\\.[0-9A-Za-z]{3,4}$")) {
				ext = entry.getName().replaceAll(".*\\.([0-9A-Za-z]{3,4})$",
						"$1").toLowerCase(Locale.getDefault());
			}
			
			// Check if file exists
			try {
				context.getResources().getAssets()
						.open("file_icons/" + ext + ".png");
			} catch (IOException e) {
				ext = "default";
			}

			if (ext != null) {
				try {
					Bitmap image = BitmapFactory.decodeStream(context
							.getResources().getAssets()
							.open("file_icons/" + ext + ".png"));
					imageType.setImageBitmap(image);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		TextView textResource = (TextView) view.findViewById(R.id.textName);
		textResource.setText(entry.getName());

		return view;
	}
}
