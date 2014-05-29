package de.fun2code.android.piratebox.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import android.app.Service;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.PirateBoxService;
import de.fun2code.android.piratebox.R;

/**
 * Handler that delivers files from the storage directory if
 * the URL matches the specified {@code prefix} specified in {@literal handerl.xml}
 * 
 * @author joschi
 *
 */
public class StorageHandler implements Handler {
	private static final String URL_PREFIX = "prefix";
	private String storageDir;
	private String urlPrefix;
	private Server server;
	
	private final static String MIME_OCTET_STREAM = "application/octet-stream";
	
	@Override
	public boolean init(Server server, String prefix) {
		this.server = server;
		
		try {
			/*
			 * Read the storage location and get the URL prefix
			 */
			Service service = PirateBoxService.getService();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(service);
			String storageDefault = service.getResources().getString(R.string.pref_storage_dir_default);
			storageDir =  prefs.getString(Constants.PREF_STORAGE_DIR, storageDefault);
			
			urlPrefix = server.props.getProperty(prefix + URL_PREFIX, "/");
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean respond(Request request) throws IOException {
		File file = new File(storageDir + Uri.decode(request.url));

		/*
		 * If the URl does not start with the prefix, skipt the handler
		 */
		if(!request.url.startsWith(urlPrefix) || !file.exists()) {
			return false;
		}
				
		try {
			String ext = file.getName().toLowerCase(Locale.US).substring(file.getName().lastIndexOf(".") + 1);
			String mime = (String) server.props.get("mime." + ext);
		    
			/*
			 * Unlike the standard Brazil handler files with unknown mime types
			 * are also delivered. In such a case the mimetype is set to
			 * 'application/octet-stream'.
			 */
			if(mime == null) {
				mime = MIME_OCTET_STREAM;
			}

			if(mime.equals(MIME_OCTET_STREAM)) {
				request.responseHeaders.put("Content-disposition", "attachment; filename=\"" + file.getName() + "\"");
			}

			InputStream fis = new FileInputStream(file);

			request.sendResponse(fis, (int)file.length(), mime, 200);
			fis.close();
		}
		catch(Exception e) {
			return false;
		}
		
		return true;

	}

}
