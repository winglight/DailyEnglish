package net.yihabits.english.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EnglishDBOpenHelper extends SQLiteOpenHelper {
	
	public  static final int DATABASE_VERSION = 2;
    public  static final String ENGLISH_TABLE_NAME = "english";
	public  static final String URL = "URL";
	public  static final String NAME = "NAME";
	public  static final String CATEGORY = "CATEGORY";
	public  static final String CONTENT = "CONTENT";
	public  static final String REPORT_LOCATION = "REPORT_LOCATION";
	public  static final String WORDS_LOCATION = "WORDS_LOCATION";
	public  static final String REPORT_URL = "REPORT_URL";
	public  static final String WORDS_URL = "WORDS_URL";
	public  static final String PUBLISHED_AT = "PUBLISHED_AT";
	public  static final String SOURCE = "SOURCE";
	
    public  static final String RINGTONE_TABLE_CREATE =
                "CREATE TABLE " + ENGLISH_TABLE_NAME + " (" +
                "_id integer primary key autoincrement," +
                CATEGORY + " TEXT, " +
                URL + " TEXT, " +
                REPORT_URL + " TEXT, " +
                WORDS_URL + " TEXT, " +
                REPORT_LOCATION + " TEXT, " +
                WORDS_LOCATION + " TEXT, " +
                CONTENT + " TEXT, " +
                PUBLISHED_AT + " TEXT, " +
                SOURCE + " INTEGER, " +
    NAME + " TEXT);";
	public static final String DATABASE_NAME = "english";

    EnglishDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RINGTONE_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		android.util.Log.w("Constants", "Upgrading database, which will destroy all old	data");
				db.execSQL("DROP TABLE IF EXISTS " + ENGLISH_TABLE_NAME);
				onCreate(db);
		
	}

}
