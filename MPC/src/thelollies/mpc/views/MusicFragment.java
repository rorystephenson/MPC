package thelollies.mpc.views;

import java.util.List;

import thelollies.mpc.R;
import thelollies.mpc.database.SongDatabase;
import thelollies.mpc.models.ListState;
import mpc.*;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

public class ListFragment extends SherlockListFragment{

	private ListState currentState;
	private boolean dbRenewed = false;
	private static SongDatabase songDatabase;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (songDatabase == null) songDatabase = new SongDatabase(getSherlockActivity());
		
		Bundle extras = getArguments();

		// Create list of songs/artist names/albums specified in displaying query
		String type = extras.getString(TabContainer.TAB);
		if(type.equals("albums")){
			currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_ALBUMS));
		}
		else if(type.equals("artists")){
			currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_ARTISTS));
		}
		else{
			currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_SONGS));
		}


		refreshList();
	}

	public void dbRenewed(){
		dbRenewed = true;
	}
	
	@Override
	public void onResume() {
		super.onResume();

		// Refresh the list if the database was updated
		if(dbRenewed){
			refreshList();
			dbRenewed = false;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.activity_song_list, container, false);
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
				LayoutInflater vi = (LayoutInflater)getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				LayoutInflater vi = (LayoutInflater)getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	private class MPCArtistAdapter extends ArrayAdapter<String> {

		private List<String> artists;
		private int layout;

		public MPCArtistAdapter(Context context, int textViewResourceId, List<String> items) {
			super(context, textViewResourceId, items);
			this.artists = items;
			this.layout = textViewResourceId;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Object o = l.getItemAtPosition(position);

		SherlockFragmentActivity activity = getSherlockActivity();

		if(o instanceof MPCSong){
			if(!currentState.query.equals(TabContainer.playing)){
				List<MPCSong> songs = new SongDatabase(activity).processSongQuery(currentState.query);
				TabContainer.mpc.enqueSongs(songs);
				TabContainer.playing = currentState;
			}
			TabContainer.mpc.play(position);
		}
		else if(o instanceof MPCAlbum){
			MPCAlbum album = (MPCAlbum) o;

			currentState.setY(getListView().getFirstVisiblePosition());
			if(album.isAll()){
				currentState = new ListState(currentState, new MPCQuery(MPCQuery.SONGS_BY_ARTIST, album.artist));
			}
			else{
				currentState = new ListState(currentState, new MPCQuery(MPCQuery.SONGS_BY_ALBUM_ARTIST, album.artist, album.title));
			}

			refreshList();
		}
		else if(o instanceof String){
			String artist = (String) o;
			currentState.setY(getListView().getFirstVisiblePosition());

			currentState = new ListState(currentState, new MPCQuery(MPCQuery.ALBUMS_BY_ARTIST, artist));

			refreshList();
		}
		TabContainer.mpc.requestStatus();
		super.onListItemClick(l, v, position, id);super.onListItemClick(l, v, position, id);
	}

	public void refreshList(){
		int type = currentState.query.getType();
		Context context = getSherlockActivity();


		if(type == MPCQuery.ALL_SONGS || 
				type == MPCQuery.SONGS_BY_ALBUM_ARTIST ||
				type == MPCQuery.SONGS_BY_ARTIST){
			List<MPCSong> songs = new SongDatabase(context).processSongQuery(currentState.query);
			MPCSongAdapter adapter = new MPCSongAdapter(context, R.layout.song_row, songs);
			setListAdapter(adapter);
		}
		else if(type == MPCQuery.ALL_ARTISTS){
			List<String> artists = new SongDatabase(context).processArtistQuery(currentState.query);
			MPCArtistAdapter adapter = new MPCArtistAdapter(context, R.layout.artist_row, artists);
			setListAdapter(adapter);
		}
		else{
			List<MPCAlbum> albums = new SongDatabase(context).processAlbumQuery(currentState.query);
			MPCAlbumAdapter adapter = new MPCAlbumAdapter(context, R.layout.album_row, albums);
			setListAdapter(adapter);
		}

	}

	public boolean navigateUp(){
		if(currentState.parent != null){
			currentState = currentState.parent;
			refreshList();
			getListView().setSelection(currentState.getY());
			return true;
		}
		return false;
	}
	
	public void navigateTop(){
		if(currentState.parent == null) return;
		else if(currentState.parent.parent != null){
			currentState = currentState.parent.parent;
		}
		else{
			currentState = currentState.parent;
		}
		refreshList();
		getListView().setSelection(0);
	}

}