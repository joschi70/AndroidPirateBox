package de.fun2code.android.piratebox.handler;

import sunlabs.brazil.server.Server;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.fun2code.android.piratebox.Constants;
import de.fun2code.android.piratebox.PirateBoxService;
import de.fun2code.android.piratebox.R;

/**
 * Wrapper class for the original PAW @{code PirateBoxFileHandler} to supper
 * the PirateBox {@literal domainName} preference
 * 
 * @author joschi
 *
 */
public class PirateBoxFileHandler extends org.paw.handler.PirateBoxFileHandler {
	private static final String DOMAIN_NAME_PATTERN = "^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

	@Override
	public boolean init(Server server, String prefix) {
		
		/*
		 * Do some sanity check
		 * If domain name is valid set the DOMAIN property
		 */
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PirateBoxService.getService());
		String domain = prefs.getString(Constants.PREF_DOMAIN_NAME,
				PirateBoxService.getService().getResources().getString(R.string.pref_domain_name_default));
		
		if(domain.matches(DOMAIN_NAME_PATTERN)) {		
			server.props.setProperty(prefix + DOMAIN, domain);
		}
		
		return super.init(server, prefix);
	}

}
