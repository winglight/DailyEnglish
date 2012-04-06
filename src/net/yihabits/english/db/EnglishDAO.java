package net.yihabits.english.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class EnglishDAO {

	private SQLiteDatabase db;
	private final Context context;

	private static EnglishDAO instance;
	private EnglishDBOpenHelper sdbHelper;
	
	private EnglishDAO(Context c) {
		this.context = c;
		this.sdbHelper = new EnglishDBOpenHelper(this.context);
	}

	public void close() {
		db.close();
	}

	public void open() throws SQLiteException {
		try {
			db = sdbHelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			Log.v("Open database exception caught", ex.getMessage());
			db = sdbHelper.getReadableDatabase();
		}
	}

	public static EnglishDAO getInstance(Context c) {
		if (instance == null) {
			instance = new EnglishDAO(c);
		}
		return instance;
	}

	public Cursor getAllEnglishBySource(int source) {
		Cursor c = db.query(EnglishDBOpenHelper.ENGLISH_TABLE_NAME, null, EnglishDBOpenHelper.SOURCE + "=" + source, null,
				 null, null, null);
				
		return c;
	}
	
	public Cursor getEnglishByUrl(String url) {
		Cursor c = db.query(EnglishDBOpenHelper.ENGLISH_TABLE_NAME, null, EnglishDBOpenHelper.URL + " = ?", new String[]{url},
				 null, null, null);
				
		return c;
	}

	public int getMaxId() {
		return 0;
	}

	public long insert(EnglishModel am) {

		try{
			ContentValues newEnglishValue = new ContentValues();
			newEnglishValue.put(EnglishDBOpenHelper.URL, am.getUrl());
			newEnglishValue.put(EnglishDBOpenHelper.SOURCE, am.getSource());
			newEnglishValue.put(EnglishDBOpenHelper.NAME, am.getName());
			newEnglishValue.put(EnglishDBOpenHelper.CATEGORY, am.getCategory());
			newEnglishValue.put(EnglishDBOpenHelper.CONTENT, am.getContent());
			newEnglishValue.put(EnglishDBOpenHelper.REPORT_URL, am.getReportUrl());
			newEnglishValue.put(EnglishDBOpenHelper.WORDS_URL, am.getWordsUrl());
			newEnglishValue.put(EnglishDBOpenHelper.REPORT_LOCATION, am.getReportLocation());
			newEnglishValue.put(EnglishDBOpenHelper.WORDS_LOCATION, am.getWordsLocation());
			newEnglishValue.put(EnglishDBOpenHelper.PUBLISHED_AT, am.getPublishedAt());
			return db.insert(EnglishDBOpenHelper.ENGLISH_TABLE_NAME, null, newEnglishValue);
			} catch(SQLiteException ex) {
				Log.v("Insert into database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long update(EnglishModel em) {

		try{
			ContentValues newEnglishValue = new ContentValues();
//			newServerValue.put("_id", sm.getId());
			newEnglishValue.put(EnglishDBOpenHelper.URL, em.getUrl());
			newEnglishValue.put(EnglishDBOpenHelper.SOURCE, em.getSource());
			newEnglishValue.put(EnglishDBOpenHelper.NAME, em.getName());
			newEnglishValue.put(EnglishDBOpenHelper.CATEGORY, em.getCategory());
			newEnglishValue.put(EnglishDBOpenHelper.CONTENT, em.getContent());
			newEnglishValue.put(EnglishDBOpenHelper.REPORT_URL, em.getReportUrl());
			newEnglishValue.put(EnglishDBOpenHelper.WORDS_URL, em.getWordsUrl());
			if(em.getReportLocation() != null){
				newEnglishValue.put(EnglishDBOpenHelper.REPORT_LOCATION, em.getReportLocation());
			}
			if(em.getWordsLocation() != null){
				newEnglishValue.put(EnglishDBOpenHelper.WORDS_LOCATION, em.getWordsLocation());
			}
			newEnglishValue.put(EnglishDBOpenHelper.PUBLISHED_AT, em.getPublishedAt());
			return db.update(EnglishDBOpenHelper.ENGLISH_TABLE_NAME, newEnglishValue, "_id=" + em.getId(), null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateContent(String url, String content, String reportUrl, String wordsUrl) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(EnglishDBOpenHelper.CONTENT, content);
			newRingtoneValue.put(EnglishDBOpenHelper.REPORT_URL, reportUrl);
			newRingtoneValue.put(EnglishDBOpenHelper.WORDS_URL, wordsUrl);
			return db.update(EnglishDBOpenHelper.ENGLISH_TABLE_NAME, newRingtoneValue, EnglishDBOpenHelper.URL + "='" + url +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateReportLocation(String url, String location) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(EnglishDBOpenHelper.REPORT_URL, location);
			return db.update(EnglishDBOpenHelper.ENGLISH_TABLE_NAME, newRingtoneValue, EnglishDBOpenHelper.URL + "='" + url +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long updateWordsLocation(String url, String location) {

		try{
			ContentValues newRingtoneValue = new ContentValues();
			newRingtoneValue.put(EnglishDBOpenHelper.WORDS_URL, location);
			return db.update(EnglishDBOpenHelper.ENGLISH_TABLE_NAME, newRingtoneValue, EnglishDBOpenHelper.URL + "='" + url +"'", null);
			} catch(SQLiteException ex) {
				Log.v("update database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
	public long delete(long id) {
		try{
			return db.delete(EnglishDBOpenHelper.ENGLISH_TABLE_NAME, "_id=" + id, null);
			} catch(SQLiteException ex) {
				Log.v("delete database exception caught",
						ex.getMessage());
				return -1;
			}
	}
	
}
