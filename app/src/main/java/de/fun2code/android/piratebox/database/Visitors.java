package de.fun2code.android.piratebox.database;

/**
 * Class that contains information about the user count on a single day
 * 
 * @author joschi
 *
 */
public class Visitors {
	private String day;
	private int count;
	
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public String toString() {
		return "Day: " + day + " / Count:" + count;
	}
	
}
