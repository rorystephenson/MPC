package thelollies.mpc.views;

import java.util.ArrayList;
import java.util.List;

import mpc.MPCAlbum;
import mpc.MPCArtist;
import mpc.MPCMusicMeta;
import mpc.MPCSong;
import thelollies.mpc.R;
import thelollies.mpc.database.SongDatabase;
import thelollies.mpc.models.MPCMultipleTypeAdapter;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

public class SearchFragment extends SherlockListFragment implements 
MPCFragment, TextWatcher, OnSharedPreferenceChangeListener{

	private boolean dbRenewed = false;
	private static SongDatabase songDatabase;
	private static List<MPCMusicMeta> results = new ArrayList<MPCMusicMeta>();
	private static MPCMultipleTypeAdapter adapter;
	private int searchLimit = 10;
	private boolean searchLimitChanged = false;

	public static SearchFragment newInstance(){
		SearchFragment listFragment = new SearchFragment();
		return listFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (songDatabase == null) 
			songDatabase = new SongDatabase(getSherlockActivity());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new MPCMultipleTypeAdapter(getSherlockActivity(), results);
		setListAdapter(adapter);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		searchLimit = Integer.parseInt(prefs.getString("searchLimit", "10"));
		prefs.registerOnSharedPreferenceChangeListener(this);
		registerForContextMenu(getListView());
		getListView().setOnCreateContextMenuListener(this);
	}

	public void dbRenewed(){
		dbRenewed = true;
	}

	@Override
	public void onResume() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		setSearchText(prefs.getString("searchText", ""));

		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		EditText searchString = (EditText)view.findViewById(R.id.search_text);
		searchString.addTextChangedListener(this);
		return view;
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Object o = l.getItemAtPosition(position);

		if(o instanceof MPCSong){
			((TabContainer)getActivity()).showInSongs((MPCSong)o);
		}
		else if (o instanceof MPCArtist){
			((TabContainer)getActivity()).showInArtists((MPCArtist)o);
		}
		else if(o instanceof MPCAlbum){
			((TabContainer)getActivity()).showInAlbums((MPCAlbum)o);
		}	
	}

	public boolean navigateUp(){
		return false;
	}
	public void navigateTop(){}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String query = s.toString();
		if(query.length() == 0) results = new ArrayList<MPCMusicMeta>();
		else results = songDatabase.search(query, searchLimit);

		adapter.setData(results);
	}

	@Override
	public void onAttach(Activity activity) {
		if(searchLimitChanged){
			TextView searchText = (TextView)activity.findViewById(R.id.search_text);
			setSearchText(searchText.getText().toString());
			searchLimitChanged = false;
		}
		super.onAttach(activity);
	}

	private void setSearchText(String text){
		TextView searchText = (TextView)getActivity().findViewById(R.id.search_text);
		searchText.setText(text);

		// Place the cursor at the end
		searchText.append("");
	}
	
	// Unused methods required by TextWatcher
	@Override	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
	@Override	public void afterTextChanged(Editable s) {}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("searchLimit")){
			searchLimit = Integer.parseInt(sharedPreferences.getString("searchLimit", "10"));
			if(isAdded()){
				SherlockFragmentActivity activity = getSherlockActivity();
				TextView searchText = (TextView)activity.findViewById(R.id.search_text);
				setSearchText(searchText.getText().toString());
			}else{
				searchLimitChanged = true;
			}
		}
	}

	@Override
	public void onPause() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		EditText text = (EditText)getActivity().findViewById(R.id.search_text);
		Editor edit = prefs.edit();
		edit.putString("searchText", text.getText().toString());
		edit.commit();

		super.onPause();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		Object item = getListAdapter().getItem(info.position);
		
		// Make sure it is a song that has been long clicked
		if(!(item instanceof MPCSong)) return;
		MPCSong song = (MPCSong) item;
		
		menu.setHeaderTitle(song.getName());
		
		String[] menuItems = getResources().getStringArray(R.array.search_song_menu);
		for (int i = 0; i<menuItems.length; i++) {
			menu.add(Menu.NONE, i, i, menuItems[i]);
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		
		Object clicked = getListAdapter().getItem(info.position);
		if(!(clicked instanceof MPCSong)) return true;
	
		MPCSong song = (MPCSong) clicked;
	
		if(item.getItemId() == 0)
			((TabContainer)getActivity()).showInArtists(song);
		else
			((TabContainer)getActivity()).showInAlbums(song);
		
		return true;
	}

}