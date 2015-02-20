package de.fun2code.android.piratebox.handler;

import java.io.IOException;

import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.PirateBoxService;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * iOS sends a WISPr request to check if connected to the internet.
 * This handler answers those request.
 * 
 * A request looks like this:
 * 
 * GET /library/test/success.html HTTP/1.
 * Host: www.apple.com
 * User-Agent: CaptiveNetworkSupport/1.0 wisp
 * Connection: close
 * 
 * @author joschi
 *
 */
public class IosWisprHandler implements Handler {
	private String prefix;
	private String userAgentRegExp = ".*CaptiveNetworkSupport.*";
	private String html = "<!DOCTYPE HTML PUBLIC “-//W3C//DTD HTML 3.2//EN”>\n<HTML>\n"+
							"<HEAD>\n<TITLE>Success</TITLE>\n</HEAD>\n<BODY>\nSuccess\n</BODY>\n</HTML>";
	private SharedPreferences preferences;

	@Override
	public boolean init(Server server, String prefix) {
		this.prefix = prefix;
		preferences = PreferenceManager.getDefaultSharedPreferences(PirateBoxService.getService());
		
		return true;
	}

	@Override
	public boolean respond(Request request) throws IOException {
		String ua = request.headers.get("User-Agent");
		
		if(preferences.getBoolean(Constants.PREF_IOS_WISPR_SUPPORT, true) && ua.matches(userAgentRegExp)) {
			request.sendResponse(html, "text/html");
			return true;
		}
		return false;
	}

}
