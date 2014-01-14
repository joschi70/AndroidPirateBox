package de.fun2code.android.piratebox.handler;

import java.io.IOException;

import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.PirateBoxService;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;

/**
 * Handles Windows Phone NCSI request
 * Normally Windows Phone devices request the URL 
 * http://www.msftncsi.com/ncsi.txt to check if the device is online.
 * 
 * The standard reply is:
 * Microsoft NCSI
 * 
 * @author joschi
 *
 */
public class WpNcsiHandler implements Handler {
	
	private String prefix;
	private String ncsiUrl = "/ncsi.html";
	private String html = "Microsoft NCSI";
	private SharedPreferences preferences;
	@Override
	public boolean init(Server server, String prefix) {
		this.prefix = prefix;
		preferences = PreferenceManager.getDefaultSharedPreferences(PirateBoxService.getService());
		
		return true;
	}
	
	@Override
	public boolean respond(Request request) throws IOException {
		if(preferences.getBoolean(Constants.PREF_WP_NCSI_SUPPORT, true) && request.url.equals(ncsiUrl)) {
			request.sendResponse(html, "text/html");
			return true;
		}
		return false;
	}
}
