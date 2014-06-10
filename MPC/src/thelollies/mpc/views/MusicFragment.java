package thelollies.mpc.views;

import java.io.Serializable;
import java.util.List;

import mpc.MPCAlbum;
import mpc.MPCArtist;
import mpc.MPCMusicMeta;
import mpc.MPCQuery;
import mpc.MPCSong;
import thelollies.mpc.R;
import thelollies.mpc.database.SongDatabase;
import thelollies.mpc.models.ListState;
import thelollies.mpc.models.MPCSingleTypeAdapter;
import thelollies.mpc.views.ViewPagerAdapter.TabType;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * MusicFragment is a fragment containing a list of either MPCSong, MPCArtist or
 * MPCAlbum. It allows navigation around the music in the SongDatabase as
 * well as playback when a song is selected.
 * @author Rory Stephenson
 *
 */
// Suppress the warnings around casting the ListAdapter, as we always know what type it is
@SuppressWarnings("unchecked")
public class MusicFragment extends SherlockListFragment implements MPCFragment{

	private ListState currentState;
	private boolean dbRenewed = false;
	private static SongDatabase songDatabase;
	public final static String TAB_TYPE = "tabtype";
	private final static String STATE = "state";

	/**
	 * The constructor that should be used when creating a new MusicFragment
	 * @param tabType
	 * @return
	 */
	public static MusicFragment newInstance(TabType tabType){
		MusicFragment listFragment = new MusicFragment();

		// Supply tab type as an argument.
		Bundle args = new Bundle();
		args.putInt(TAB_TYPE, tabType.ordinal());
		listFragment.setArguments(args);


		return listFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get database instance
		if (songDatabase == null) songDatabase = new SongDatabase(getSherlockActivity());

		// Loads ListState if one exists
		Serializable s;
		if(savedInstanceState != null &&
				(s = savedInstanceState.getSerializable(STATE)) != null) {
			currentState = (ListState)s;
		}
		else{
			// Create a new ListState, initialisation depends on the type of tab.
			switch(TabType.values()[getArguments().getInt(TAB_TYPE)]){
			case SONGS:
				currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_SONGS));
				break;
			case ARTISTS:
				currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_ARTISTS));
				break;
			case ALBUMS:
				currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_ALBUMS));
				break;
			default:
				currentState = null;
				Log.e("MusicFragment", "Unknown tab type");
				break;
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		refreshList();
	}

	public void dbRenewed(){
		dbRenewed = true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Save the ListState
		if(currentState != null)
			outState.putSerializable(STATE, currentState);
		super.onSaveInstanceState(outState);
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
		// Create the View
		return inflater.inflate(R.layout.fragment_music, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Object o = l.getItemAtPosition(position);

		// Song clicked, play it
		if(o instanceof MPCSong){
			if(!currentState.query.equals(TabContainer.playing)){
				List<MPCSong> songs = songDatabase.processSongQuery(currentState.query);
				TabContainer.mpc.enqueSongs(songs);
				TabContainer.playing = currentState;
			}
			TabContainer.mpc.play(position);
		}
		// Album clicked, navigate into it
		else if(o instanceof MPCAlbum){
			MPCAlbum album = (MPCAlbum) o;

			// Store scroll position
			currentState.setY(getListView().getFirstVisiblePosition());
			if(album.isAll()){ // "All Songs" album
				currentState = new ListState(currentState, new MPCQuery(MPCQuery.SONGS_BY_ARTIST, album));
			}
			else{ // Normal album
				currentState = new ListState(currentState, new MPCQuery(MPCQuery.SONGS_BY_ALBUM_ARTIST, album));
			}

			refreshList();
		}
		// Album clicked, navigate into it
		else if(o instanceof MPCArtist){
			MPCArtist artist = (MPCArtist) o;
			currentState.setY(getListView().getFirstVisiblePosition());

			currentState = new ListState(currentState, new MPCQuery(MPCQuery.ALBUMS_BY_ARTIST, artist));

			refreshList();
		}
		
		// Request status of MPD server, playback buttons updated via the response
		TabContainer.mpc.requestStatus();
		super.onListItemClick(l, v, position, id);
	}

	/**
	 * Refreshes the ListView according to the currentState's query.
	 */
	public void refreshList(){
		int type = currentState.query.getType();
		Context context = getSherlockActivity();

		// Songs tab refresh
		if(type == MPCQuery.ALL_SONGS || 
				type == MPCQuery.SONGS_BY_ALBUM_ARTIST ||
				type == MPCQuery.SONGS_BY_ARTIST){
			List<MPCSong> songs = songDatabase.processSongQuery(currentState.query);
			MPCSingleTypeAdapter<MPCSong> adapter = 
					new MPCSingleTypeAdapter<MPCSong>(context, R.layout.row_song, songs);
			setListAdapter(adapter);
		}
		// Artists tab refresh
		else if(type == MPCQuery.ALL_ARTISTS){
			List<MPCArtist> artists = new SongDatabase(context).processArtistQuery(currentState.query);
			MPCSingleTypeAdapter<MPCArtist> adapter = 
					new MPCSingleTypeAdapter<MPCArtist>(context, R.layout.row_artist, artists);
			setListAdapter(adapter);
		}
		// Albums tab refresh
		else{
			List<MPCAlbum> albums = new SongDatabase(context).processAlbumQuery(currentState.query);
			MPCSingleTypeAdapter<MPCAlbum> adapter = 
					new MPCSingleTypeAdapter<MPCAlbum>(context, R.layout.row_album, albums);
			setListAdapter(adapter);
		}
	}

	/**
	 * See interface
	 */
	public boolean navigateUp(){
		if(currentState.parent != null){
			currentState = currentState.parent;
			refreshList();
			getListView().setSelection(currentState.getY());
			return true;
		}
		return false;
	}

	/**
	 * See interface
	 */
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

	/**
	 * Shows the specified song in the ListView under the songs tab
	 * @param song
	 */
	public void showInSongs(MPCSong song){
		MPCSingleTypeAdapter<MPCSong> adapter = (MPCSingleTypeAdapter<MPCSong>)getListAdapter();
		int index = adapter.indexOf(song);

		currentState.setY(index);

		// Flash the item to indicate it
		adapter.flashItem(index);

		getListView().setSelection(index);	
	}

	/**
	 * Shows the specified music in the Artists tab ListView
	 * @param music
	 */
	public void showInArtists(MPCMusicMeta music){
		if(music instanceof MPCArtist){
			MPCArtist artist = (MPCArtist) music;

			currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_ARTISTS));
			// set the y position of the parent list state
			setStateYToItem(currentState, artist);
			currentState = new ListState(currentState, new MPCQuery(MPCQuery.ALBUMS_BY_ARTIST, artist));

			refreshList();
		}
		else if(music instanceof MPCSong){
			MPCSong song = (MPCSong)music;

			currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_ARTISTS));
			// set the y position of the parent list state
			setStateYToItem(currentState, new MPCArtist(song.artist));
			currentState = new ListState(currentState, new MPCQuery(MPCQuery.ALBUMS_BY_ARTIST, new MPCArtist(song.artist)));
			// set the y position of the parent list state
			setStateYToItem(currentState, new MPCAlbum(song.artist, song.album));
			currentState =  new ListState(currentState, 
					new MPCQuery(MPCQuery.SONGS_BY_ARTIST, new MPCAlbum(song.artist, song.album, true)));

			refreshList();

			MPCSingleTypeAdapter<MPCSong> adapter = (MPCSingleTypeAdapter<MPCSong>)getListAdapter();
			int index = adapter.indexOf(song);

			currentState.setY(index);

			adapter.flashItem(index);
			getListView().setSelection(index);	
		}
	}

	/**
	 * Show the specified music item in the Album tab's ListView
	 * @param music
	 */
	public void showInAlbums(MPCMusicMeta music){
		if(music instanceof MPCAlbum){
			MPCAlbum album = (MPCAlbum) music;

			currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_ALBUMS));
			// set the y position of the parent list state
			setStateYToItem(currentState, album);
			currentState = new ListState(currentState, new MPCQuery(MPCQuery.SONGS_BY_ALBUM_ARTIST, album));

			refreshList();
		}
		else if(music instanceof MPCSong){
			MPCSong song = (MPCSong)music;

			currentState = new ListState(null, new MPCQuery(MPCQuery.ALL_ALBUMS));
			// set the y position of the parent list state
			setStateYToItem(currentState, new MPCAlbum(song.artist, song.album));
			currentState = new ListState(currentState, new MPCQuery(MPCQuery.SONGS_BY_ALBUM_ARTIST, 
					new MPCAlbum(song.artist, song.album)));

			refreshList();

			MPCSingleTypeAdapter<MPCSong> adapter = (MPCSingleTypeAdapter<MPCSong>)getListAdapter();
			int index = adapter.indexOf(song);

			currentState.setY(index);

			adapter.flashItem(index);
			getListView().setSelection(index);
		}
	}

	/**
	 * Helper method to set the Y position of a ListState to position of the specfied
	 * MPCMusicMeta
	 * @param state
	 * @param music
	 */
	private void setStateYToItem(ListState state, MPCMusicMeta music){

		MPCSingleTypeAdapter<MPCMusicMeta> adapter = (MPCSingleTypeAdapter<MPCMusicMeta>)getListAdapter();
		state.setY(adapter.indexOf(music));

	}

}