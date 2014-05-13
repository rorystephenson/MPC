package thelollies.mpc.views;

import java.io.Serializable;
import java.util.List;

import mpc.MPCAlbum;
import mpc.MPCArtist;
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

public class MusicFragment extends SherlockListFragment implements MPCFragment{

	private ListState currentState;
	private boolean dbRenewed = false;
	private static SongDatabase songDatabase;
	public final static String TAB_TYPE = "tabtype";
	private final static String STATE = "state";

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

		if (songDatabase == null) songDatabase = new SongDatabase(getSherlockActivity());

		Serializable s;
		if(savedInstanceState != null &&
				(s = savedInstanceState.getSerializable(STATE)) != null) {
			currentState = (ListState)s;
		}
		else{
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
		return inflater.inflate(R.layout.fragment_music, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Object o = l.getItemAtPosition(position);

		if(o instanceof MPCSong){
			if(!currentState.query.equals(TabContainer.playing)){
				List<MPCSong> songs = songDatabase.processSongQuery(currentState.query);
				TabContainer.mpc.enqueSongs(songs);
				TabContainer.playing = currentState;
			}
			TabContainer.mpc.play(position);
		}
		else if(o instanceof MPCAlbum){
			MPCAlbum album = (MPCAlbum) o;

			currentState.setY(getListView().getFirstVisiblePosition());
			if(album.isAll()){
				currentState = new ListState(currentState, new MPCQuery(MPCQuery.SONGS_BY_ARTIST, album));
			}
			else{
				currentState = new ListState(currentState, new MPCQuery(MPCQuery.SONGS_BY_ALBUM_ARTIST, album));
			}

			refreshList();
		}
		else if(o instanceof MPCArtist){
			MPCArtist artist = (MPCArtist) o;
			currentState.setY(getListView().getFirstVisiblePosition());

			currentState = new ListState(currentState, new MPCQuery(MPCQuery.ALBUMS_BY_ARTIST, artist));

			refreshList();
		}
		TabContainer.mpc.requestStatus();
		super.onListItemClick(l, v, position, id);
	}

	public void refreshList(){
		int type = currentState.query.getType();
		Context context = getSherlockActivity();

		if(type == MPCQuery.ALL_SONGS || 
				type == MPCQuery.SONGS_BY_ALBUM_ARTIST ||
				type == MPCQuery.SONGS_BY_ARTIST){
			List<MPCSong> songs = songDatabase.processSongQuery(currentState.query);
			MPCSingleTypeAdapter<MPCSong> adapter = 
					new MPCSingleTypeAdapter<MPCSong>(context, R.layout.row_song, songs);
			setListAdapter(adapter);
		}
		else if(type == MPCQuery.ALL_ARTISTS){
			List<MPCArtist> artists = new SongDatabase(context).processArtistQuery(currentState.query);
			MPCSingleTypeAdapter<MPCArtist> adapter = 
					new MPCSingleTypeAdapter<MPCArtist>(context, R.layout.row_artist, artists);
			setListAdapter(adapter);
		}
		else{
			List<MPCAlbum> albums = new SongDatabase(context).processAlbumQuery(currentState.query);
			MPCSingleTypeAdapter<MPCAlbum> adapter = 
					new MPCSingleTypeAdapter<MPCAlbum>(context, R.layout.row_album, albums);
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