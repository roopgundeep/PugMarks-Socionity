package org.socionity.gps.marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DbManager {
	public static final File socionity_directory = new File(Environment
			.getExternalStorageDirectory() , "socionity"); // creating a folder named socionity to store app data
	public static final File app_directory = new File(socionity_directory, "gps");
	public static final String pref_name = "socionity_gps_preferences";
	public static final String pref_key_avlid = "next_avl_id";
	public static final String pref_key_last_record_id = "last_record_id";
	public static final String db_name = "gps_db";
	public static final int db_version = 1;
	public static final String TABLE_RECORDINGS = "recordings";
	public static final String TABLE_DATA = "data";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_RECORD_ID = "record_id";
	public static final String COLUMN_SINGLE_ENTRY = "single_entry";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_TYPE = "type";
	public static final String COLUMN_DATA_TYPE = "type";
	public static final String COLUMN_DATA = "data";
	public static final String[] RECORDINGS_COLUMNS = { COLUMN_ID, COLUMN_TIME,
			COLUMN_NAME,COLUMN_TYPE,COLUMN_SINGLE_ENTRY};
	public static final String[] DATA_COLUMNS = { COLUMN_ID, COLUMN_RECORD_ID,
			COLUMN_TIME, COLUMN_DATA_TYPE, COLUMN_DATA };
	private SharedPreferences pref; //shared preferences are used to shared data btw files

	private class SqlHelper extends SQLiteOpenHelper {

		//Queries:
		public static final String create_recordings_sql = "create table "
				+ TABLE_RECORDINGS + "(" + COLUMN_ID
				+ " integer primary key autoincrement," + COLUMN_TIME
				+ " integer not null," + COLUMN_NAME + " text not null, " + COLUMN_TYPE 
				+ " text, " + COLUMN_SINGLE_ENTRY + " integer) ";
		public static final String create_data_sql = "create table "
				+ TABLE_DATA + " (" + COLUMN_ID
				+ " integer primary key autoincrement," + COLUMN_RECORD_ID
				+ " integer not null," + COLUMN_TIME + " integer not null, "
				+ COLUMN_DATA_TYPE + " text not null, " + COLUMN_DATA
				+ " text not null) ";

		public SqlHelper(Context context) {
			super(context, db_name, null, db_version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d("create_DB",create_recordings_sql); 
			db.execSQL(create_data_sql);  //commmand to ececute SQL queries(Creatin tables)
			db.execSQL(create_recordings_sql);
		}

		//Called when the database needs to be upgraded.
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("sqliteupgrade",
					"Upgrading from" + Integer.toString(oldVersion) + " to "
							+ Integer.toString(newVersion));

			pref.edit().clear().commit();
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDINGS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
			db.execSQL(create_data_sql);
			db.execSQL(create_recordings_sql);
		}

	}
	
	private SQLiteDatabase db;
	private SqlHelper db_helper;

	public DbManager(Context context) {
		//context tells about the present state of app
		pref = context.getSharedPreferences(pref_name, 0);
		db_helper = new SqlHelper(context);
	}

	//Called when the database has been opened.
	public void open() {
		db = db_helper.getWritableDatabase();
	}
	//to close db
	public void close() {
		db.close();
	}

	//Get list of all recordings stored in DB
	public List<RecordingEntry> get_recordings() {
		List<RecordingEntry> l = new ArrayList<RecordingEntry>(); 
		Cursor c = db.query(TABLE_RECORDINGS, RECORDINGS_COLUMNS, null, null,
				null, null, COLUMN_TIME);
		c.moveToFirst();
		//Loop to traverse through all record enteries and store them in a list
		while (!c.isAfterLast()) {
			RecordingEntry r = new RecordingEntry(c.getLong(0), c.getLong(1),
					c.getString(2),c.getString(3),c.getLong(4));
			l.add(r);
			c.moveToNext();
		}
		return l;
	}
	
	//Get the last recording
	public RecordingEntry last_recordings(long recording_id) {
		List<RecordingEntry> l = new ArrayList<RecordingEntry>();
		Cursor c = db.query(TABLE_RECORDINGS, RECORDINGS_COLUMNS, COLUMN_ID + "=?", new String[]{Long.toString(recording_id)},
				null, null, COLUMN_TIME);
		c.moveToFirst(); //the first will be the last recording
		RecordingEntry r = new RecordingEntry(c.getLong(0), c.getLong(1),
					c.getString(2),c.getString(3),c.getLong(4));
		return r;
		
	}

	// Add a new record in DB
	public long add_recording(RecordingEntry entry) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME, entry.name);
		values.put(COLUMN_TYPE, entry.type );
		values.put(COLUMN_SINGLE_ENTRY, entry.single_entry);
		values.put(COLUMN_TIME, System.currentTimeMillis());
		return db.insert(TABLE_RECORDINGS, null, values);
	}
	// Update a specific Recording
	public long update_recording(long single_entry,long id_record){
		ContentValues values= new ContentValues();
		values.put(COLUMN_SINGLE_ENTRY,single_entry);
		return db.update(TABLE_RECORDINGS, values, "id='"+id_record+"'", null);
	}
	
	// Delete Recording
	public long delete_recording(long id_record){
		Log.d("delete","paranoid");
		return db.delete(TABLE_RECORDINGS, "id='" + id_record+"'", null);
		
	}

	//Adding Data
	public long add_data(RecordingDataEntry entry) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_RECORD_ID, entry.record_id);
		values.put(COLUMN_TIME, entry.time);
		values.put(COLUMN_DATA_TYPE, entry.data_type);
		values.put(COLUMN_DATA, entry.data);
		return db.insert(TABLE_DATA, null, values);
	}
	
	//Get the record details for a particular record_id
	public String get_rec_name(long id_record) {
		Cursor c = db.query(TABLE_RECORDINGS, RECORDINGS_COLUMNS, COLUMN_ID + "=?", new String[]{Long.toString(id_record)},
				null, null, COLUMN_TIME);
		c.moveToFirst();
		RecordingEntry r = new RecordingEntry(c.getLong(0), c.getLong(1),
					c.getString(2),c.getString(3),c.getLong(4));
		return c.getString(2);
	}

	// Fetching geopoints from points from DB for a particular ID
	ArrayList<String> get_points(long recording_id){
		Log.i("","query started");
		ArrayList<String> gps_points= new ArrayList<String>();
		Cursor c = db.query(TABLE_DATA, DATA_COLUMNS, COLUMN_RECORD_ID + "=?",
				new String[] { Long.toString(recording_id) }, null, null, null);
		Log.i("","got cursor c");
		c.moveToFirst();
		while (!c.isAfterLast()) {
			String type_data = c.getString(3);
			type_data=type_data+",";
			String r = c.getString(4);
			type_data=type_data+r;
			gps_points.add(type_data);
			c.moveToNext();
		}
		return gps_points;		
	}
	
	//fetching Data from points from DB
	List<RecordingDataEntry> get_data(long recording_id) {
		List<RecordingDataEntry> l = new ArrayList<RecordingDataEntry>();
		Cursor c = db.query(TABLE_DATA, DATA_COLUMNS, COLUMN_RECORD_ID + "=?",
				new String[] { Long.toString(recording_id) }, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			RecordingDataEntry r = new RecordingDataEntry(c.getLong(0),
					c.getLong(1), c.getLong(2), c.getString(3), c.getString(4));
			l.add(r);
			c.moveToNext();
		}
		return l;
	}

	/**
	 * Use this function to get a unique id for using with file names
	 * 
	 * @return returns the next available id which is distinct.
	 */
	public long get_next_id() {
		long id = pref.getLong(pref_key_avlid, 0);
		id++;
		SharedPreferences.Editor ed = pref.edit();
		ed.putLong(pref_key_avlid, id);
		ed.commit();
		return id;
	}

	/**
	 * @return Returns the last used record_id. If no such record , or last
	 *         record was submitted returns 0
	 */
	public long get_last_record_id() {
		return pref.getLong(pref_key_last_record_id, 0);
	}

	public void set_last_record_id(long id) {
		SharedPreferences.Editor ed = pref.edit();
		ed.putLong(pref_key_last_record_id, id);
		ed.commit();
	}

	/**
	 * DO Not call this function. Instead Call purge_db from
	 * {@link SocionityGPSApplication}
	 */
	public void purge_db() {
		pref.edit().clear().commit();
		db.delete(TABLE_DATA, null, null);
		db.delete(TABLE_RECORDINGS, null, null);
	}
}
