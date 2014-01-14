package de.fun2code.android.piratebox;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import de.fun2code.android.pawserver.PawServerActivity;
import de.fun2code.android.piratebox.util.NetworkUtil;
import de.fun2code.android.piratebox.util.ShellUtil;

/**
 * Main PirateBox Activity
 * 
 *
 */
public class PirateBoxActivity extends PawServerActivity implements StateChangedListener {
	@SuppressWarnings("unused")
	private Handler handler;
	
	// View that displays the server URL
	private TextView txtVersion, txtInfo;
	private ImageView imgServer, imgAp, imgNetwork;
	private ImageButton btnSwitch;
	private String version = null;
	private SharedPreferences preferences;
	private AlphaAnimation  blinkAnimation;
	private boolean supportedDevice;
	private Activity activity;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		INSTALL_DIR = Constants.getInstallDir(this.getApplicationContext());
		
		activity = this;

		/*
		 * Turn the PawServerActivity into runtime mode.
		 * Otherwise an error may occur if some things special to the
		 * original PAW server are not available.
		 */
		calledFromRuntime = true;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		handler = new Handler();
		
		txtVersion = (TextView) findViewById(R.id.version);
		txtInfo = (TextView) findViewById(R.id.info);
		imgServer = (ImageView) findViewById(R.id.imageServer);
		imgAp = (ImageView) findViewById(R.id.imageAp);
		imgNetwork = (ImageView) findViewById(R.id.imageNetwork);
		btnSwitch = (ImageButton) findViewById(R.id.buttonSwitch);
		
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			txtVersion.setText(version);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			version = null;
		}
		
		// Handle on/off button
		btnSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(PirateBoxService.isRunning()) {
					btnSwitch.setEnabled(false);
					btnSwitch.setImageResource(R.drawable.switch_off);
					
					stopService();
				}
				else {
					btnSwitch.setEnabled(false);
					btnSwitch.setImageResource(R.drawable.switch_on);
					
					txtInfo.setText(getText(R.string.msg_setting_up_networking));
					btnSwitch.startAnimation(blinkAnimation);
					
					startService();
					
				}
				
			}
			
		});
		
		/* Check installation and extract ZIP if necessary */
		checkInstallation();

		/*
		 * Register handler This is needed in order to get dialogs etc. to work.
		 */
		messageHandler = new MessageHandler(this);
		PirateBoxService.setActivityHandler(messageHandler);

		/*
		 * Register activity with service.
		 */
		PirateBoxService.setActivity(this);
		
		/*
		 * Blink animation
		 */
		blinkAnimation = new AlphaAnimation(1, 0.1F);
		blinkAnimation.setDuration(1000);
		blinkAnimation.setRepeatCount(Animation.INFINITE);
		blinkAnimation.setRepeatMode(Animation.REVERSE);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// Set views to correct state
		imgServer.setVisibility(PirateBoxService.isRunning() ? View.VISIBLE : View.INVISIBLE);
		imgNetwork.setVisibility(PirateBoxService.isNetworkRunning() ? View.VISIBLE : View.INVISIBLE);
		imgAp.setVisibility(PirateBoxService.isApRunning() ? View.VISIBLE : View.INVISIBLE);
		
		if(!PirateBoxService.isStartingUp()) {
			btnSwitch.setImageResource(PirateBoxService.isRunning() ?  R.drawable.switch_on : R.drawable.switch_off);
			
			checkStatus();
		}
		else {
			btnSwitch.setImageResource(R.drawable.switch_on);
			btnSwitch.startAnimation(blinkAnimation);
			txtInfo.setText(R.string.msg_setting_up_networking);
		}
		
		checkPreRequisites();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		PirateBoxService.registerChangeListener(this);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		PirateBoxService.unregisterChangeListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return supportedDevice ? super.onPrepareOptionsMenu(menu) : false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.menu_preferences:
	    		Intent preferencesActivity = new Intent(getBaseContext(),
						PreferencesActivity.class);
				startActivity(preferencesActivity);
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * Stops the service
	 */
	@Override
	public void stopService() {
		Intent serviceIntent = new Intent(this.getApplicationContext(),
				PirateBoxService.class);
		stopService(serviceIntent);
	}

	/**
	 * Starts the service
	 */
	@Override
	public void startService() {
		/*
		 * Do nothing, if service is already running.
		 */
		if (PirateBoxService.isRunning()) {
			return;
		}
		
		Intent serviceIntent = new Intent(PirateBoxActivity.this,
				PirateBoxService.class);

		startService(serviceIntent);
	}
	
	/**
	 * Checks the installation and extracts the content.zip file
	 * to INSTALL_DIR if needed
	 */
	private void checkInstallation() {
		String pref_version = preferences.getString(Constants.PREF_VERSION, null);
		boolean update = preferences.getBoolean(Constants.PREF_UPDATE, true);
		
		/*
		 * If installation directory exists and updates should not
		 * be installed automatically -> return
		 */
		
		if(new File(INSTALL_DIR).exists() && !update) {
			return;
		}
		
		if(!new File(INSTALL_DIR).exists() || (version == null || pref_version == null) || !pref_version.equals(version)) {
			// Create directories
			new File(INSTALL_DIR).mkdirs();
			
			final ProgressDialog progress = new ProgressDialog(this);
			progress.setMessage("Please wait...");
			progress.setCancelable(false);
			progress.setCanceledOnTouchOutside(false);
			progress.show();
			
			new Thread() {
				public void run() {
					// Files not to overwrite
					HashMap<String, Integer> keepFiles = new HashMap<String, Integer>();
					
					// Extract ZIP file form assets
					try {
						extractZip(getAssets().open("content.zip"),
								INSTALL_DIR, keepFiles);
						
						// Write new version to preferences
						Editor edit = preferences.edit();
						edit.putString(Constants.PREF_VERSION, version);
						edit.commit();
					} catch (IOException e) {
						Log.e(Constants.TAG, e.getMessage());
					}
					finally {
						activity.runOnUiThread(new Runnable() {				
							@Override
							public void run() {
								progress.dismiss();
							}
						});
					}
				}
			}.start();		
		}
	}

	@Override
	public void apEnabled(boolean autoStartup) {
		System.out.println("apEnabled");
		imgAp.setVisibility(View.VISIBLE);
		imgAp.setAlpha(autoStartup ? 255 : 50);
		txtInfo.setText(getText(R.string.msg_ap_up));

		checkStatus();
	}

	@Override
	public void apDisabled(boolean autoStartup) {
		System.out.println("apDisabled");
		imgAp.setVisibility(View.INVISIBLE);
		txtInfo.setText(getText(R.string.msg_ap_down));
		checkStatus();
	}

	@Override
	public void networkUp() {
		System.out.println("networkUp");
		imgNetwork.setVisibility(View.VISIBLE);
		txtInfo.setText(getText(R.string.msg_networking_up));
		checkStatus();
	}
	

	@Override
	public void serverUp(boolean success) {
		imgServer.setVisibility(View.VISIBLE);
		txtInfo.setText(getText(R.string.msg_webserver_down));

		checkStatus();
	}

	@Override
	public void serverDown(boolean success) {
		imgServer.setVisibility(View.INVISIBLE);
		txtInfo.setText(getText(R.string.msg_webserver_down));
		checkStatus();
		
	}

	@Override
	public void networkDown() {
		System.out.println("networkDown");
		imgNetwork.setVisibility(View.INVISIBLE);
		//btnSwitch.setEnabled(true);
		txtInfo.setText(getText(R.string.msg_networking_down));
		checkStatus();
	}
	
	/**
	 * Checks the PirateBox status and disables/enables the on/off switch
	 * accordingly
	 */
	private void checkStatus() {
		int status = imgNetwork.getVisibility() + imgAp.getVisibility()
				+ imgServer.getVisibility();
		switch (status) {
		case 3 * View.VISIBLE:
			txtInfo.setText(getText(R.string.msg_piratebox_up));
			btnSwitch.clearAnimation();
			btnSwitch.setEnabled(true);
			break;
		case 3 * View.INVISIBLE:
			txtInfo.setText(getText(R.string.msg_piratebox_down));
			btnSwitch.setEnabled(true);
			break;
		}
	}
	
	/**
	 * Checks if all prerequisites are met.
	 * Prerequisites are: rooted device, dnsmasq available and iptables available.
	 * If prerequisites are not me a message is displayed and buttons and menu
	 * are disabled.
	 */
	private void checkPreRequisites() {
		Map<String, String> checks = new HashMap<String, String>();
		String message = "";
		
		checks.put(ShellUtil.SU_BIN, getString(R.string.chk_not_rooted));
		checks.put(NetworkUtil.DNSMASQ_BIN, getString(R.string.chk_missing_dnsmasq));
		checks.put(NetworkUtil.IPTABLES_BIN, getString(R.string.chk_missing_iptables));
		
		ShellUtil shellUtil = new ShellUtil();
		for(String cmd : checks.keySet()) {
			if(!shellUtil.isCommandAvailable(cmd)) {
				message += "- " + checks.get(cmd) + "\n";
			}
		}
		
		if(message.length() > 0) {
			message = "Unsupported device!\n" + message;
			supportedDevice = false;
			btnSwitch.setVisibility(View.GONE);
			txtInfo.setTextColor(Color.RED);
			txtInfo.setText(message);
		}
		else {
			supportedDevice = true;
		}
	}

}