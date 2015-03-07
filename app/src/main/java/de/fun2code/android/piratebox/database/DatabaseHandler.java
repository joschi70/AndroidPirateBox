package de.fun2code.android.piratebox.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.fun2code.android.piratebox.Constants;

/**
 * Handles the creation/modification of the statistics database
 * <br/>
 * This class contains methods to fill the statistics tables for visitors and
 * downloads and to retrieve statistics data from the database.
 * 
 * @author joschi
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	public DatabaseHandler(Context context) {
        super(context, Constants.STATS_DATABASE_NAME, null, Constants.STATS_DATABASE_VERSION);
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlVcTable = "CREATE TABLE IF NOT EXISTS " + Constants.STATS_TABLE_VISITORS + " ( day text, visitor text,  PRIMARY KEY ( day , visitor ) ON CONFLICT IGNORE  )";
		String sqlDlTable = "CREATE TABLE IF NOT EXISTS " + Constants.STATS_TABLE_DOWNLOADS + " ( url text  PRIMARY KEY ASC, counter int )";
		
		db.execSQL(sqlVcTable);
		db.execSQL(sqlDlTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		 // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Constants.STATS_TABLE_VISITORS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.STATS_TABLE_DOWNLOADS);
 
        // Create tables again
        onCreate(db);
		
	}
	
	/**
	 * Inserts a visitor into the database
	 * <br/>
	 * If the visitor entry already exists for the given {@code day} the entry
	 * is silently ignored.
	 * 
	 * @param day		specifies the day
	 * @param visitor	visitor to insert, this is normally a {@literal SHA-1} hex value
	 */
	public void insertVisitor(String day, String visitor) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
	    values.put("day", day);
	    values.put("visitor", visitor);
		
		db.insertWithOnConflict(Constants.STATS_TABLE_VISITORS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		db.close();
	}
	
	/**
	 * Returns the {@code Visitors} object for the given date
	 * 
	 * @param date		date for which the visitor info should be retrieved
	 * @return			{@code Visitors} object
	 */
	public Visitors getVisitors(Date date) {
		String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
		SQLiteDatabase db = this.getReadableDatabase();
		Visitors visitors = new Visitors();
		visitors.setDay(day);
		 
		Cursor cursor= db.rawQuery("select count(*) from " + Constants.STATS_TABLE_VISITORS + " where day=?", new String[] {day});
		if(cursor.moveToFirst()) {
			visitors.setCount(cursor.getInt(0));
			cursor.close();
		}
		else {
			visitors.setCount(0);
		}
		
		return visitors;
	}
	
	/**
	 * Inserts a download URL into the database
	 * <br/>
	 * If an entry for the specified URL already exists, the counter is 
	 * incremented by one.
	 * 
	 * @param url	URL to insert
	 */
	public void insertUrl(String url) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Check if insert or update
		Cursor cursor= db.rawQuery("select url from " + Constants.STATS_TABLE_DOWNLOADS + " where url=?", new String[] {url});
		
		// Update
		if(cursor.moveToFirst()) {
			cursor.close();
			db.execSQL("UPDATE " + Constants.STATS_TABLE_DOWNLOADS + " SET counter=counter+1 WHERE url=?", new String[] {url});
		}
		// Insert
		else { 
			ContentValues values = new ContentValues();
		    values.put("url", url);
		    values.put("counter", 1);
			
			db.insert(Constants.STATS_TABLE_DOWNLOADS, null, values);
		}
		
		db.close();
	}
	
	/**
	 * Returns the top downloads 
	 * 
	 * @param limit		limits the list to the specified number of entries
	 * @return			{@code List} of {@code Download} objects
	 */
	public List<Download> getTopDownloads(int limit) {
		List<Download> downloads = new ArrayList<Download>();
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor= db.rawQuery("select url, counter from " + Constants.STATS_TABLE_DOWNLOADS + " order by counter desc limit ?", new String[] {String.valueOf(limit)});
		
		if(cursor.moveToFirst()) {
			do {
				Download download = new Download();
				download.setUrl(cursor.getString(cursor.getColumnIndex("url")));
				download.setCounter(cursor.getInt(cursor.getColumnIndex("counter")));
				downloads.add(download);
			} while(cursor.moveToNext());
		}
		
		cursor.close();
		db.close();
		
		return downloads;
	}
	
	/**
	 * Deletes all database table entries
	 */
	public void clearTables() {
		SQLiteDatabase db = this.getWritableDatabase();
		
		db.execSQL("DELETE FROM " + Constants.STATS_TABLE_VISITORS);
		db.execSQL("DELETE FROM " + Constants.STATS_TABLE_DOWNLOADS);
		
		db.close();
	}

}
