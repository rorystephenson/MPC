package thelollies.mpc.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import mpc.*;

/**
 * SongDatabase is an interface for the database of songs held
 * on the android device. Stores all of the server's song information
 * and allows interaction with it.
 * 
 * @author thelollies
 */

public class SongDatabase extends SQLiteOpenHelper implements MusicDatabase{

	// Database name and version
	private static final String DATABASE_NAME = "songDatabase.db";
	private static final int DATABASE_VERSION = 3;

	// Contacts table name
	private static final String TABLE_SONGS = "songs";

	// Contacts Table Columns names
	private static final String KEY_FILE = "file";
	private static final String KEY_TIME = "time";
	private static final String KEY_ARTIST = "artist";
	private static final String KEY_TITLE = "title";
	private static final String KEY_ALBUM = "album";
	private static final String KEY_TRACK_NO = "track_number";

	private SQLiteDatabase addSongDB;
	
	/**
	 * Creates an instance of SongDatabase in the context of the
	 * specified activity.
	 * 
	 * @param context the activity from where the handler is called
	 */
	public SongDatabase(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Creates the database tables if they don't already exist.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SONGS + " ("
				+ KEY_FILE + " TEXT, "
				+ KEY_TIME + " INTEGER, " + KEY_ARTIST + " TEXT, "
				+ KEY_TITLE + " TEXT, " + KEY_ALBUM + " TEXT, "
				+ KEY_TRACK_NO + " INTEGER" + ")";

		try{db.execSQL(CREATE_CONTACTS_TABLE);}catch(Exception e){e.printStackTrace();}
	}

	/**
	 * Creates a new table when the database version is changed.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
		
		// Create tables again
		onCreate(db);
	}

	/**
	 * Queries the database for all songs and returns a list of the songs in
	 * alphabetical order (by songs name)
	 * 
	 * @return List<MPCSong> of all songs
	 */
	private List<MPCSong> getSongsAlphabetical(){
		List<MPCSong> songList = new ArrayList<MPCSong>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SONGS + " ORDER BY " + KEY_TITLE + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCSong song = new MPCSong(cursor.getString(0), cursor.getInt(1), 
						cursor.getString(2), cursor.getString(3), cursor.getString(4), 
						cursor.getInt(5));

				songList.add(song);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// return contact list
		return songList;
	}

	/**
	 * Queries the database for all artist names and returns a list
	 * in alphabetical order.
	 * 
	 * @return List<String> of artist names (no duplicates)
	 */
	private List<String> getArtistsAlphabetical(){
		List<String> artistList = new ArrayList<String>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SONGS + " ORDER BY " + KEY_ARTIST + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				if(artistList.contains(cursor.getString(2))){continue;} // Avoid duplicate entries
				artistList.add(cursor.getString(2));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
		// return contact list
		return artistList;
	}

	/**
	 * Queries the database for all albums and returns a List<MPCAlbum>
	 * with duplicates excluded.
	 * 
	 * @return List<MPCAlbum> of all albums (no duplicates)
	 */
	private List<MPCAlbum> getAlbumsAlphabetical(){
		List<MPCAlbum> albumList = new ArrayList<MPCAlbum>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SONGS + " ORDER BY " + KEY_ALBUM + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCAlbum album = new MPCAlbum(cursor.getString(2), cursor.getString(4));

				if(albumList.contains(album)){continue;} // Avoid duplicate entries
				albumList.add(album);
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
		// return contact list
		return albumList;
	}

	/**
	 * Queries the database for all songs by the specified artist. A List<MCPSong>
	 * is returned with the songs in alphabetical order by song name.
	 * 
	 * @param artist the artist to filter songs by
	 * @return List<MPCSong> of songs by the specified artist
	 */
	private List<MPCSong> getArtistSongsAlphabetical(String artist) {
		artist = DatabaseUtils.sqlEscapeString(artist);
		
		List<MPCSong> songList = new ArrayList<MPCSong>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST + 
				"=" + artist + " ORDER BY " + KEY_TITLE + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCSong song = new MPCSong(cursor.getString(0), cursor.getInt(1), 
						cursor.getString(2), cursor.getString(3), cursor.getString(4), 
						cursor.getInt(5));

				songList.add(song);
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
		// return contact list
		return songList;
	}

	/**
	 * Queries the database for all albums by the specified artist. The albums
	 * are ordered by album name alphabetically.
	 * 
	 * @param artist artist to filter albums by
	 * @return List<MPCAlbum> albums by the artist
	 */
	private List<MPCAlbum> getArtistAlbumsAlphabetical(String artist) {
		List<MPCAlbum> albums = new ArrayList<MPCAlbum>();
		String artistEscaped = DatabaseUtils.sqlEscapeString(artist);

		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST + 
				"=" + artistEscaped + " ORDER BY " + KEY_TITLE + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			albums.add(new MPCAlbum(artist, "All Songs", true));
			do {
				MPCAlbum album = new MPCAlbum(cursor.getString(2), cursor.getString(4));
				if(albums.contains(album)){continue;} // Avoid duplicate entries
				albums.add(album);
			} while (cursor.moveToNext());
		}

		// Don't include "All Songs" if there is only one album
		if(albums.size() == 2) albums.remove(0);
		
		cursor.close();
		db.close();
		// return contact list
		return albums;
	}

	/**
	 * Queries the database for all albums in the specified album by the specified
	 * artist. The list is ordered by track number.
	 * 
	 * @param artist album to filter songs by
	 * @param album album to filter songs by
	 * @return List<MPCSong> songs by the specified artist in the specified album
	 */
	private List<MPCSong> getAlbumSongsByTrack(String artist, String album) {
		artist = DatabaseUtils.sqlEscapeString(artist);
		album = DatabaseUtils.sqlEscapeString(album);
		List<MPCSong> songList = new ArrayList<MPCSong>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_SONGS + " WHERE (" + KEY_ARTIST + 
				"=" + artist + " AND " + KEY_ALBUM + 
				"=" + album + ")" + " ORDER BY " + KEY_TRACK_NO + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCSong song = new MPCSong(cursor.getString(0), cursor.getInt(1), 
						cursor.getString(2), cursor.getString(3), cursor.getString(4), 
						cursor.getInt(5));

				songList.add(song);
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
		// return contact list
		return songList;
	}

	/**
	 * Processes a song query
	 * @param query
	 * @return list of songs which meet the query
	 */
	public List<MPCSong> processSongQuery(MPCQuery query) {

		switch(query.getType()){
		case(MPCQuery.ALL_SONGS):
			return getSongsAlphabetical();
		case(MPCQuery.SONGS_BY_ALBUM_ARTIST): 
			return getAlbumSongsByTrack(query.getArtist(), query.getAlbum());
		case(MPCQuery.SONGS_BY_ARTIST): 
			return getArtistSongsAlphabetical(query.getArtist());
		}

		return null;
	}

	/**
	 * Processes an album query
	 * 
	 * @param query
	 * @return list of songs which meet the query
	 */
	public List<MPCAlbum> processAlbumQuery(MPCQuery query) {
		switch(query.getType()){
		case(MPCQuery.ALL_ALBUMS): 
			return getAlbumsAlphabetical();
		case(MPCQuery.ALBUMS_BY_ARTIST): 
			return getArtistAlbumsAlphabetical(query.getArtist());
		}
		return null;
	}

	/**
	 * Processes an artist query (artist names)
	 * @param query
	 * @return artist names which match the query
	 */
	public List<String> processArtistQuery(MPCQuery query) {
		if(query.getType() == MPCQuery.ALL_ARTISTS){
			return getArtistsAlphabetical();
		}
		return null;
	}

	@Override
	public void clear() {
		SQLiteDatabase db;
		boolean renewingDB = false;
		if(addSongDB != null){
			db = addSongDB;
			renewingDB = true;
		}
		else{
			db = getWritableDatabase();
		}
		// Delete all music
		db.delete(TABLE_SONGS, null, null);
		
		// Don't close the database since we are about to write to it
		if(!renewingDB) db.close();		
	}

	@Override
	public void addSong(MPCSong song) {

		ContentValues values = new ContentValues();
		values.put(KEY_FILE, song.file);
		values.put(KEY_TIME, song.time);
		values.put(KEY_ARTIST, song.artist);
		values.put(KEY_ALBUM, song.album);
		values.put(KEY_TITLE, song.title);
		values.put(KEY_TRACK_NO, song.track);

		// Inserting Row
		addSongDB.insert(TABLE_SONGS, null, values);
	}

	@Override
	public void startTransaction() {
		addSongDB = getWritableDatabase();
		addSongDB.beginTransaction();
	}

	@Override
	public void setTransactionSuccessful() {
		addSongDB.setTransactionSuccessful();
	}

	@Override
	public void endTransaction() {
		addSongDB.endTransaction();
		addSongDB.close(); // Closing database connection
		addSongDB = null;
	}

}
