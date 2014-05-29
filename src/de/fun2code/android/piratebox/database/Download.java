package de.fun2code.android.piratebox.database;

/**
 * Class which contains information about a download file
 * 
 * @author joschi
 *
 */
public class Download {
	private String url;
	private int counter;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getCounter() {
		return counter;
	}
	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	public String toString() {
		return "URL: " + url + " / Counter:" + counter;
	}
	
}
