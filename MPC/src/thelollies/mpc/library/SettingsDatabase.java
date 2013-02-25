package thelollies.mpc.library;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SettingsDatabase is an interface for the database of settings held
 * on the android device. Stores all of the connection information.
 * 
 * @author thelollies
 */

public class SettingsDatabase extends SQLiteOpenHelper{

	// Database name and version
	private static final String DATABASE_NAME = "settingsDatabase.db";
	private static final int DATABASE_VERSION = 1;

	// Contacts table name
	private static final String TABLE_SETTINGS = "songs";

	// Contacts Table Columns names
	private static final String KEY_ADDRESS = "address";
	private static final String KEY_PORT = "port";

	
	/**
	 * Creates an instance of SettingsDatabase in the context of the
	 * specified activity.
	 * 
	 * @param context the activity from where the handler is called
	 */
	public SettingsDatabase(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Creates the database tables if they don't already exist.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + " ("
				+ KEY_ADDRESS + " TEXT, "
				+ KEY_PORT + " INTEGER" + ")";

		try{db.execSQL(CREATE_CONTACTS_TABLE);}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * Creates a new table when the database version is changed.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);

		// Create tables again
		onCreate(db);
	}

	/**
	 * Updates the settings, clearing the old ones before saving new values.
	 * 
	 * @param address String representing the new address
	 * @param port int representing the new port
	 */
	public void updateSettings(String address, int port) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		// Delete old settings
		db.delete(TABLE_SETTINGS, null, null);
		
		ContentValues values = new ContentValues();
		values.put(KEY_ADDRESS, address);
		values.put(KEY_PORT, port);

		// Inserting Row
		db.insert(TABLE_SETTINGS, null, values);
		db.close(); // Closing database connection
	}
	
	/**
	 * Gets the saved address from the database
	 * 
	 * @return String address value
	 */
	public String getAddress(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SETTINGS, null);
		
		String address = null;
		if(cursor.moveToFirst()){
			address = cursor.getString(0);
		}
		
		db.close();
		return address;
	}
	
	/**
	 * Gets the saved port from the database
	 * 
	 * @return int port value
	 */
	public int getPort(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SETTINGS, null);
		
		int port = 0;
		if(cursor.moveToFirst()){
			port = cursor.getInt(1);
		}
		
		db.close();
		return port;
	}

}
