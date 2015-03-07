package de.fun2code.android.piratebox.handler;

import java.io.IOException;
import java.util.Locale;

import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.PirateBoxService;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Allow Droopy like file uploads on port 8080
 * 
 * @author joschi
 *
 */
public class DroopyHandler implements Handler {
	private String prefix;
	private SharedPreferences preferences;
	private String fupURL;
	private static final String FUP_URL = "fup";
	private static final String FUP_URL_DEFAULT= "/fup.xhtml";

	@Override
	public boolean init(Server server, String prefix) {
		this.prefix = prefix;
		fupURL = server.props.getProperty(prefix + FUP_URL, FUP_URL_DEFAULT);
		preferences = PreferenceManager.getDefaultSharedPreferences(PirateBoxService.getService());
		
		return true;
	}

	@Override
	public boolean respond(Request request) throws IOException {
		String contentType = request.headers.get("Content-Type");
		
		/*
		 * Check if URL is base URL (empty or "/")
		 * Only handle these URLs.
		 */
		boolean isBaseUrl = request.url.matches("^[/]{0,1}$");
		
		
		if(contentType != null && preferences.getBoolean(Constants.PREF_EMULATE_DROOPY, true)
				&& contentType.toLowerCase(Locale.US).contains("multipart") &&
				isBaseUrl) {
			/*
			 * Rewrite current and original url.
			 */
			request.url = fupURL;
			request.props.setProperty("url.orig", fupURL);
		}
		
		// always return false
		return false;
	}

}
