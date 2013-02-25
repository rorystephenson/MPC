package thelollies.mpc;

import java.util.List;

import thelollies.mpc.R;

import thelollies.mpc.library.SongDatabase;
import thelollies.mpc.library.MPC;
import thelollies.mpc.library.MPCAlbum;
import thelollies.mpc.library.MPCQuery;
import thelollies.mpc.library.MPCSong;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This activity handles the song, artist and album views. It displays
 * the songs specified in SongList.DISLP intent which is an MPCQuery.
 * 
 * Provides navigation buttons to other categories and implements
 * browsing and library playback.
 * 
 * @author thelollies
 *
 */

public class SongList  extends ListActivity {

	private MPCQuery playing; // Describes criteria for playing playlist
	private MPCQuery displaying; // Describes criteria for displayed list
	private int pressedButton;

	// Intent constants
	public final static String PLAY = "p";
	public final static String DISPL = "d";
	public final static String CLICK = "c";

	// Array of button id's for highlighting clicked button
	public final static int[] buttons = {R.id.songsButton, R.id.artistsButton, R.id.albumsButton, R.id.settingsButton};


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Bundle extras = getIntent().getExtras();

		// Handle missing intents
		if(extras == null){
			extras = new Bundle();
			extras.putInt(CLICK, R.id.songsButton);
			MPCQuery query = new MPCQuery(MPCQuery.ALL_SONGS);
			extras.putParcelable(PLAY, query);
			extras.putParcelable(DISPL, query);
		}
		
		
		// Read intents
		pressedButton = extras.getInt(CLICK);
		playing = (MPCQuery)extras.get(PLAY);
		displaying = (MPCQuery)extras.get(DISPL);

		
		// Create list of songs/artist names/albums specified in displaying query
		if(displaying.getType() <= 3){ // Return type is List<MPCSong>
			List<MPCSong> songs = new SongDatabase(this).processSongQuery(displaying);
			MPCSongAdapter adapter = new MPCSongAdapter(this, R.layout.song_row, songs);
			setListAdapter(adapter);
		}
		else if(displaying.getType() <= 5){ // Return type is List<MPCAlbum>
			List<MPCAlbum> albums = new SongDatabase(this).processAlbumQuery(displaying);
			MPCAlbumAdapter adapter = new MPCAlbumAdapter(this, R.layout.album_row, albums);
			setListAdapter(adapter);
		}
		else if(displaying.getType() == 6){ // Return type is List<String>
			List<String> artists = new SongDatabase(this).processStringQuery(displaying);
			StringAdapter adapter = new StringAdapter(this, R.layout.artist_row, artists);
			setListAdapter(adapter);
		}

		// Set layout
		setContentView(R.layout.activity_song_list);


		// Colour currently selected button
		for (int i = 0; i < 4; i++){
			if(buttons[i] == pressedButton){
				findViewById(buttons[i]).setBackgroundColor(getResources().getColor(R.color.lightGrey));
			}
			else{
				findViewById(buttons[i]).setBackgroundColor(getResources().getColor(R.color.mediumGrey));
			}
		}
		
	}
	
	@Override
	public void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		updatePlayButton();
	}

	/**
	 * Handles clicking of list items. Song items are played whilst artist/album items 
	 * lead to navigation to the clicked artist/album.
	 */
	@Override
	protected void onListItemClick(ListView lv, View view, int position, long id) {
		Object o = lv.getItemAtPosition(position);

		if(o instanceof MPCSong){
			MPC mpc = new MPC(this);
			if(!displaying.equals(playing)){
				List<MPCSong> songs = new SongDatabase(this).processSongQuery(displaying);
				mpc.enqueSongs(songs);
				playing = displaying;
			}
			mpc.play(position);
			updatePlayButton();
		}
		else if(o instanceof MPCAlbum){
			MPCAlbum album = (MPCAlbum) o;
			if(album.isAll()){
				displaying = new MPCQuery(MPCQuery.SONGS_BY_ARTIST, album.artist);
			}
			else{
				displaying = new MPCQuery(MPCQuery.SONGS_BY_ALBUM_ARTIST, album.artist, album.title);
			}
			Intent intent = new Intent(this, SongList.class);
			intent.putExtra(CLICK, pressedButton);
			intent.putExtra(PLAY, playing);
			intent.putExtra(DISPL, displaying);
			startActivity(intent);
		}
		else if(o instanceof String){
			String artist = (String) o;
			displaying = new MPCQuery(MPCQuery.ALBUMS_BY_ARTIST, artist);
			Intent intent = new Intent(this, SongList.class);
			intent.putExtra(CLICK, pressedButton);
			intent.putExtra(PLAY, playing);
			intent.putExtra(DISPL, displaying);
			startActivity(intent);
		}
		super.onListItemClick(lv, view, position, id);
	}

	/**
	 * If music is playing show the pause button, if paused show the play button.
	 */
	private void updatePlayButton() {
		MPC mpc = new MPC(this);
		
		if(mpc.isPlaying()){
			findViewById(R.id.playPauseButton).setBackgroundResource(R.drawable.pause);
		}
		else{
			findViewById(R.id.playPauseButton).setBackgroundResource(R.drawable.play);
		}
	}

	/**
	 * Fired by navigation buttons. Does nothing if the clicked button corresponds to
	 * the list which is already being played. Otherwise it navigates to that list/settings.
	 * 
	 * @param view clicked button
	 */
	public void operateButton(View view){
		Intent intent = new Intent(this, thelollies.mpc.SongList.class);
		intent.putExtra(PLAY, playing);
		intent.putExtra(CLICK, view.getId());

		switch (view.getId()){
		case R.id.songsButton:
			if(displaying.getType() == MPCQuery.ALL_SONGS){return;}
			intent.putExtra(DISPL,
					new MPCQuery(MPCQuery.ALL_SONGS));
			break;
		case R.id.artistsButton: 
			if(displaying.getType() == MPCQuery.ALL_ARTISTS){return;}
			intent.putExtra(DISPL,
					new MPCQuery(MPCQuery.ALL_ARTISTS));
			break;
		case R.id.albumsButton: 
			if(displaying.getType() == MPCQuery.ALL_ALBUMS){return;}
			intent.putExtra(DISPL,
					new MPCQuery(MPCQuery.ALL_ALBUMS));
			break;
		case R.id.settingsButton: 
			Intent settings = new Intent(this, Settings.class);
			settings.putExtra(PLAY, playing);
			settings.putExtra(CLICK, view.getId());
			settings.putExtra(DISPL, displaying);
			startActivity(settings);
			return;
		default: return;
		}

		startActivity(intent);
	}
	
	/**
	 * Fired when the rewind button is clicked. Skips back a song in the playlist.
	 * @param v
	 */
	public void rewind(View v){
		new MPC(this).previous();
	}
	
	/**
	 * Handles clicking of the play/pause button. Passing the appropriate action to
	 * the server.
	 * @param v
	 */
	public void playPause(View v){
		
		MPC mpc = new MPC(this);
		
		if(v.getBackground().equals(R.drawable.play)){
			mpc.play();
		}
		else{
			mpc.pause();
		}
		
		updatePlayButton();
	}
	
	/**
	 * Fires when fast forward button is clicked. Skips to the next song in the playlist.
	 * @param v
	 */
	public void fastForward(View v){
		new MPC(this).next();
	}

	/**
	 * Adapter for the listView which displays MPCSongs.
	 * @author thelollies
	 *
	 */
	private class MPCSongAdapter extends ArrayAdapter<MPCSong> {

		private List<MPCSong> songs;
		private int layout;

		public MPCSongAdapter(Context context, int textViewResourceId, List<MPCSong> items) {
			super(context, textViewResourceId, items);
			this.songs = items;
			this.layout = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(layout, null);
			}
			MPCSong song = songs.get(position);
			if (song != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(song.title);                            }
				if(bt != null){
					bt.setText(song.artist);
				}
			}
			return v;
		}
	}

	/**
	 * Adapter for the listView which displays MPCAlbums.
	 * @author thelollies
	 *
	 */
	private class MPCAlbumAdapter extends ArrayAdapter<MPCAlbum> {

		private List<MPCAlbum> albums;
		private int layout;

		public MPCAlbumAdapter(Context context, int textViewResourceId, List<MPCAlbum> items) {
			super(context, textViewResourceId, items);
			this.albums = items;
			this.layout = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(layout, null);
			}
			MPCAlbum album = albums.get(position);
			if (album != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(album.title);                            }
				if(bt != null){
					bt.setText(album.artist);
				}
			}
			return v;
		}
	}

	/**
	 * Adapter for the listView which displays artist names.
	 * @author thelollies
	 *
	 */
	private class StringAdapter extends ArrayAdapter<String> {

		private List<String> artists;
		private int layout;

		public StringAdapter(Context context, int textViewResourceId, List<String> items) {
			super(context, textViewResourceId, items);
			this.artists = items;
			this.layout = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(layout, null);
			}
			String album = artists.get(position);
			if (album != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				if (tt != null) {
					tt.setText(album);                            }
				if(bt != null){
					bt.setText("Albums");
				}
			}
			return v;
		}
	}
}


