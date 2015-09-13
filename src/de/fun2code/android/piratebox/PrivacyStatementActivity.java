package de.fun2code.android.piratebox;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class PrivacyStatementActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.privacy_statement);
		
		WebView webView = (WebView) findViewById(R.id.webView);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setBuiltInZoomControls(true);
		
		InputStream is = getResources().openRawResource(R.raw.privacy_statement);

		StringBuilder sb = new StringBuilder();
		String line;

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"));
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			is.close();
			
			String html = sb.toString();
			webView.loadData(html, "text/html", "utf-8");
		} catch (Exception e) {
			Log.e(Constants.TAG, "Error loading privacy-statement resource: " + e.getMessage());
		} 
		    
	}

}
