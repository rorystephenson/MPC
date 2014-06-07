package thelollies.mpc.database;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import mpc.MPCAlbum;
import mpc.MPCArtist;
import mpc.MPCMusicMeta;
import mpc.MPCQuery;
import mpc.MPCSong;
import mpc.MusicDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
	private static final int DATABASE_VERSION = 4;

	// Contacts table name
	private static final String TABLE_SONGS = "songs";
	private static final String TABLE_ARTISTS = "artists";
	private static final String TABLE_ALBUMS = "albums";


	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_TITLE = "title";
	private static final String KEY_TRACK_NO = "track_number";
	private static final String KEY_TIME = "time";
	private static final String KEY_ARTIST_ID = "artist_id";
	private static final String KEY_ALBUM_ID = "album_id";
	private static final String KEY_FILE = "file";

	private static final String KEY_NAME = "name";

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
		String createArtistsTable = "CREATE TABLE " + TABLE_ARTISTS + " ("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_NAME + " TEXT)";

		String createAlbumsTable = "CREATE TABLE " + TABLE_ALBUMS + " ("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_NAME + " TEXT, "
				+ KEY_ARTIST_ID + " INTEGER, "
				+ "FOREIGN KEY("+KEY_ARTIST_ID+") REFERENCES "+TABLE_ARTISTS+"("+KEY_ID+"))";

		String createSongsTable = "CREATE TABLE " + TABLE_SONGS + " ("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_TITLE + " TEXT, "
				+ KEY_TRACK_NO + " INTEGER, "
				+ KEY_TIME + " INTEGER, " 
				+ KEY_ARTIST_ID + " INTEGER, "
				+ KEY_ALBUM_ID + " INTEGER, "
				+ KEY_FILE + " TEXT, "
				+ "FOREIGN KEY("+KEY_ARTIST_ID+") REFERENCES "+TABLE_ARTISTS+"("+KEY_ID+"), "
				+ "FOREIGN KEY("+KEY_ALBUM_ID+") REFERENCES "+TABLE_ALBUMS+"("+KEY_ID+"))";
		db.beginTransaction();
		try{
			db.execSQL(createArtistsTable);
			db.execSQL(createAlbumsTable);
			db.execSQL(createSongsTable);
			db.setTransactionSuccessful();
		}catch(Exception e){db.endTransaction();e.printStackTrace();}
		db.endTransaction();
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
		String query = "SELECT * FROM ("+TABLE_SONGS+" LEFT JOIN "
				+TABLE_ARTISTS+" ON "+TABLE_SONGS+"."+KEY_ARTIST_ID+" = "
				+TABLE_ARTISTS+"."+KEY_ID+") AS Temp LEFT JOIN "
				+TABLE_ALBUMS+" ON Temp."+KEY_ALBUM_ID+" = "+TABLE_ALBUMS+"."
				+KEY_ID+" ORDER BY "+KEY_TITLE+" ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(query, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCSong song = new MPCSong(cursor.getString(6), cursor.getInt(3), 
						cursor.getString(8), cursor.getString(1), cursor.getString(10), 
						cursor.getInt(2));

				songList.add(song);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// return contact list
		return songList;
	}

	/**
	 * Runs a search query with the given parameters
	 */

	/**
	 * Queries the database for all artist names and returns a list
	 * in alphabetical order.
	 * 
	 * @return List<String> of artist names (no duplicates)
	 */
	private List<MPCArtist> getArtistsAlphabetical(){
		List<MPCArtist> artistList = new ArrayList<MPCArtist>();
		// Select All Query
		String selectQuery = "SELECT DISTINCT "+KEY_NAME+" FROM " + TABLE_ARTISTS + " ORDER BY " + KEY_NAME + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				artistList.add(new MPCArtist(cursor.getString(0)));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

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
		String selectQuery = "SELECT "+TABLE_ARTISTS+"."+KEY_NAME+", "
				+TABLE_ALBUMS+"."+KEY_NAME+" FROM " + TABLE_ALBUMS + " LEFT JOIN "
				+ TABLE_ARTISTS +" ON "+KEY_ARTIST_ID+"="+TABLE_ARTISTS+"."+KEY_ID+
				" ORDER BY "+ TABLE_ALBUMS +"."+ KEY_NAME + " ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCAlbum album = new MPCAlbum(cursor.getString(0), cursor.getString(1));

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
		List<MPCSong> songList = new ArrayList<MPCSong>();
		// Select All Query
		String query = "SELECT * FROM ("+TABLE_SONGS+" LEFT JOIN "
				+TABLE_ARTISTS+" ON "+TABLE_SONGS+"."+KEY_ARTIST_ID+" = "
				+TABLE_ARTISTS+"."+KEY_ID+") AS Temp LEFT JOIN "
				+TABLE_ALBUMS+" ON Temp."+KEY_ALBUM_ID+" = "+TABLE_ALBUMS+"."
				+KEY_ID+" WHERE Temp."+KEY_NAME+"=? ORDER BY "+KEY_TITLE+" ASC";

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(query, new String[]{artist});

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCSong song = new MPCSong(cursor.getString(6), cursor.getInt(3), 
						cursor.getString(8), cursor.getString(1), cursor.getString(10), 
						cursor.getInt(2));

				songList.add(song);
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();

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

		// Select All Query
		String selectQuery = String.format("SELECT %s.%s, %s.%s FROM %s LEFT JOIN %s ON "
				+"%s=%s.%s WHERE %s.%s=? ORDER BY %s.%s ASC",
				TABLE_ARTISTS, KEY_NAME, TABLE_ALBUMS, KEY_NAME, TABLE_ALBUMS, TABLE_ARTISTS, 
				KEY_ARTIST_ID, TABLE_ARTISTS, KEY_ID, TABLE_ARTISTS, KEY_NAME, TABLE_ARTISTS, KEY_NAME);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, new String[] {artist});

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			albums.add(new MPCAlbum(artist, "All Songs", true));
			do {
				albums.add(new MPCAlbum(cursor.getString(0), cursor.getString(1)));
			} while (cursor.moveToNext());
		}

		// Don't include "All Songs" if there is only one album
		if(albums.size() == 2) albums.remove(0);

		cursor.close();
		db.close();

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
		List<MPCSong> songList = new ArrayList<MPCSong>();
		// Select All Query
		String query = "SELECT * FROM ("+TABLE_SONGS+" LEFT JOIN "
				+TABLE_ARTISTS+" ON "+TABLE_SONGS+"."+KEY_ARTIST_ID+" = "
				+TABLE_ARTISTS+"."+KEY_ID+") AS Temp LEFT JOIN "
				+TABLE_ALBUMS+" ON Temp."+KEY_ALBUM_ID+" = "+TABLE_ALBUMS+"."
				+KEY_ID+" WHERE Temp."+KEY_NAME+"=? AND "+TABLE_ALBUMS+"."+KEY_NAME+"=? "
				+"ORDER BY "+KEY_TITLE+" ASC";

		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(query, new String[]{artist,album});

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCSong song = new MPCSong(cursor.getString(6), cursor.getInt(3), 
						cursor.getString(8), cursor.getString(1), cursor.getString(10), 
						cursor.getInt(2));

				songList.add(song);
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
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
			return getAlbumSongsByTrack(query.getAlbum().artist, query.getAlbum().title);
		case(MPCQuery.SONGS_BY_ARTIST): 
			return getArtistSongsAlphabetical(query.getAlbum().artist);
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
			return getArtistAlbumsAlphabetical(query.getArtist().title);
		}
		return null;
	}

	/**
	 * Processes an artist query (artist names)
	 * @param query
	 * @return artist names which match the query
	 */
	public List<MPCArtist> processArtistQuery(MPCQuery query) {
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
		db.delete(TABLE_ALBUMS, null, null);
		db.delete(TABLE_ARTISTS, null, null);

		// Don't close the database since we are about to write to it
		if(!renewingDB) db.close();		
	}

	private Integer findArtistPK(String artist){
		Cursor cursor = addSongDB.rawQuery("SELECT "+KEY_ID+" FROM " + TABLE_ARTISTS + 
				" WHERE name=?", new String[]{artist});
		int result;
		if(cursor.moveToFirst()){
			result = cursor.getInt(0);
		}else{
			ContentValues values = new ContentValues(2);
			values.put(KEY_NAME, artist);
			result = (int)addSongDB.insert(TABLE_ARTISTS, null, values);
		}
		cursor.close();
		return result;
	}

	private Integer findAlbumPK(String album, int artistFK){
		int result = -1;

		// Loop through the albums of the same name and stop if you find one
		// with the same artist
		Cursor cursorAlbum = addSongDB.rawQuery("SELECT "+KEY_ID+" FROM " + TABLE_ALBUMS + 
				" WHERE "+KEY_NAME+"=? AND " + KEY_ARTIST_ID + "=?", new String[] {album, String.valueOf(artistFK)});

		if(cursorAlbum.moveToFirst()){
			result = cursorAlbum.getInt(0);
		}else{
			ContentValues values = new ContentValues(2);
			values.put(KEY_NAME, album);
			values.put(KEY_ARTIST_ID, artistFK);
			result = (int)addSongDB.insert(TABLE_ALBUMS, null, values);
		}
		cursorAlbum.close();

		return result;
	}

	@Override
	public void addSong(MPCSong song) {

		// Creates the artist/ablum if they don't exist 
		// and gets a reference to them
		int artistFK = findArtistPK(song.artist);
		int albumFK = findAlbumPK(song.album, artistFK);

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, song.title);
		values.put(KEY_TRACK_NO, song.track);
		values.put(KEY_TIME, song.time);
		values.put(KEY_ARTIST_ID, artistFK);
		values.put(KEY_ALBUM_ID, albumFK);
		values.put(KEY_FILE, song.file);

		// Inserting Row
		addSongDB.insert(TABLE_SONGS, null, values);
	}



	public List<MPCMusicMeta> search(String query, int limit){
		SQLiteDatabase db = getReadableDatabase();
		List<MPCMusicMeta> musicMeta = new ArrayList<MPCMusicMeta>();

		String songQuery = "SELECT * FROM (songs LEFT JOIN artists ON "
				+"songs.artist_id=artists.id) AS Temp LEFT JOIN "
				+"albums ON Temp.album_id=albums.id WHERE "
				+"title LIKE ? ORDER BY title ASC LIMIT ?";

		String artistQuery = "SELECT name FROM artists WHERE name LIKE ? "
				+"ORDER BY name ASC LIMIT ?";

		String albumQuery = "SELECT albums.name, artists.name FROM "
				+ "albums LEFT JOIN artists ON albums.artist_id=artists.id"
				+" WHERE albums.name LIKE ? ORDER BY "
				+ "albums.name ASC, artists.name ASC LIMIT ?";

		String[] parameters = 
				new String[]{"%"+query+"%", String.valueOf(limit)};

		Cursor cursor = db.rawQuery(songQuery, parameters);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MPCSong song = new MPCSong(cursor.getString(6), cursor.getInt(3), 
						cursor.getString(8), cursor.getString(1), cursor.getString(10),	cursor.getInt(2));
				musicMeta.add(song);
			} while (cursor.moveToNext());
		}
		cursor.close();

		// Get ready for next query if we haven't exceeded limit
		limit -= musicMeta.size();
		if(limit == 0){db.close();return musicMeta;}
		parameters[1] = String.valueOf(limit);
		cursor = db.rawQuery(artistQuery, parameters);

		if (cursor.moveToFirst()) {
			do {
				MPCArtist artist = new MPCArtist(cursor.getString(0));
				musicMeta.add(artist);
			} while (cursor.moveToNext());
		}
		cursor.close();

		// Get ready for next query if we haven't exceeded limit
		limit -= musicMeta.size();
		if(limit == 0){db.close();return musicMeta;}
		parameters[1] = String.valueOf(limit);
		cursor = db.rawQuery(albumQuery, parameters);

		if (cursor.moveToFirst()) {
			do {
				MPCAlbum album = new MPCAlbum(cursor.getString(1), cursor.getString(0));
				musicMeta.add(album);
			} while (cursor.moveToNext());
		}
		cursor.close();

		db.close();

		return musicMeta;
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
