package de.fun2code.android.piratebox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import android.content.Context;
import de.fun2code.android.pawserver.PawServerActivity;
import de.fun2code.android.piratebox.Constants;

public class ServerConfigUtil {
	
	/**
	 * Reads setting form {@literal server.xml} file
	 * 
	 * @param setting	setting to read
	 * @param context	Context to use
	 * @return	{@code true} on success, otherwise {@code false}
	 */
	public static String getServerSetting(String setting, Context context) {
		try {
			SAXBuilder builder = new SAXBuilder(false);
			Document doc = builder.build(new File(Constants.getInstallDir(context) + "/conf/server.xml"));

			// Get root element
			Element root = doc.getRootElement();

			return root.getChild(setting) != null ? root.getChild(
					setting).getText().trim() : "";
		} catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * Writes setting to {@literal server.xml} file
	 * 
	 * @param setting	setting to write
	 * @param value		value to set
	 * @param context	Context to use
	 * @return		{@code true} on success, otherwise {@code false}
	 */
	public static boolean storeServerSetting(String setting, String value, Context context) {
		File confFile = new File(Constants.getInstallDir(context) + "/conf/server.xml");
		byte[] buf = new byte[(int) confFile.length()];
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		String content = null;
		
		try {
			fis = new FileInputStream(confFile);
			fis.read(buf);
			fis.close();
			
			content = new String(buf);
		} catch (IOException e) {
			e.printStackTrace();
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {
				}
			}
			return false;
		}
		
		//content = content.replaceAll("(<" + setting + ">).*?(</" + setting + ">)", "$1" + value + "$2");
		String regex = "(<" + setting + ">).*?(</" + setting + ">)";
		content = Pattern.compile(regex, Pattern.DOTALL).matcher(content).replaceAll("$1" + value + "$2");

		try {
			fos = new FileOutputStream(confFile);
			fos.write(content.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
				}
			}
			return false;
		}
		
		return true;
			
	}
}
