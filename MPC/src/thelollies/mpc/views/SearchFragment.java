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

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

/**
 * SearchFragment contains a search box and a results list which is populated
 * incrementally based on the results of the entered query. Selecting results
 * fires navigation to the selected item in the appropriate music tab.
 * @author Rory Stephenson
 *
 */
public class SearchFragment extends SherlockListFragment implements 
MPCFragment, TextWatcher, OnSharedPreferenceChangeListener{

	private static SongDatabase songDatabase;
	private static List<MPCMusicMeta> results = new ArrayList<MPCMusicMeta>();
	private static MPCMultipleTypeAdapter adapter;
	private int searchLimit = 10;
	private boolean searchLimitChanged = false;

	/**
	 * This constructor is called for instantiation in the ViewPager
	 * @return
	 */
	public static SearchFragment newInstance(){
		SearchFragment listFragment = new SearchFragment();
		return listFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get a handle to the music database
		if (songDatabase == null) 
			songDatabase = new SongDatabase(getSherlockActivity());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Create list adapter
		adapter = new MPCMultipleTypeAdapter(getSherlockActivity(), results);
		setListAdapter(adapter);
		
		// Load search limit preference
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		searchLimit = Integer.parseInt(prefs.getString("searchLimit", "10"));
		
		// Listener for search limit changing
		prefs.registerOnSharedPreferenceChangeListener(this);
		// Register the ListView to receive long-press actions and react to them
		registerForContextMenu(getListView());
		getListView().setOnCreateContextMenuListener(this);
	}


	@Override
	public void onResume() {
		// Save the current search text
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

		if(o instanceof MPCSong){// Navigate to song in Songs tab
			((TabContainer)getActivity()).showInSongs((MPCSong)o);
		}
		else if (o instanceof MPCArtist){// Navigate to artist in artists tab
			((TabContainer)getActivity()).showInArtists((MPCArtist)o);
		}
		else if(o instanceof MPCAlbum){// Navigate to album in Albums tab
			((TabContainer)getActivity()).showInAlbums((MPCAlbum)o);
		}	
	}

	// Required by MPCFragment, have no meaning here
	@Override	public boolean navigateUp(){return false;}
	@Override	public void navigateTop(){}
	@Override	public void dbRenewed(){}

	/**
	 * Fires a new query and updates the results list every time a change is made
	 * in the search text
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		String query = s.toString();
		if(query.length() == 0) results = new ArrayList<MPCMusicMeta>();
		else results = songDatabase.search(query, searchLimit);

		adapter.setData(results);
	}

	/**
	 * Update the query if the search limit is changed
	 */
	@Override
	public void onAttach(Activity activity) {
		if(searchLimitChanged){
			EditText searchText = (EditText)activity.findViewById(R.id.search_text);
			setSearchText(searchText.getText().toString());
			searchLimitChanged = false;
		}
		super.onAttach(activity);
	}

	/**
	 * Internal helper method which sets the search text and places the cursor at the 
	 * end of the text
	 * @param text
	 */
	private void setSearchText(final String text){
		final EditText searchText = (EditText)getActivity().findViewById(R.id.search_text);
		searchText.setText(text);

		// Place the cursor at the end
		searchText.setText(text);
		searchText.post(new Runnable() {
		         @Override
		         public void run() {
		             searchText.setSelection(text.length());
		         }
		});
	}
	
	// Unused methods required by TextWatcher
	@Override	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
	@Override	public void afterTextChanged(Editable s) {}

	/**
	 * Refresh the search results if the search limit is changed
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("searchLimit")){
			searchLimit = Integer.parseInt(sharedPreferences.getString("searchLimit", "10"));
			if(isAdded()){
				SherlockFragmentActivity activity = getSherlockActivity();
				EditText searchText = (EditText)activity.findViewById(R.id.search_text);
				setSearchText(searchText.getText().toString());
			}else{
				searchLimitChanged = true;
			}
		}
	}

	/**
	 * Save the current search text
	 */
	@Override
	public void onPause() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		EditText text = (EditText)getActivity().findViewById(R.id.search_text);
		Editor edit = prefs.edit();
		edit.putString("searchText", text.getText().toString());
		edit.commit();

		super.onPause();
	}

	/**
	 * Give the options for long-clicking an MPCSong
	 */
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
	
	/**
	 * React to a context menu item selection
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		
		// Make sure it is an MPCSong that was long-clicked originally
		Object clicked = getListAdapter().getItem(info.position);
		if(!(clicked instanceof MPCSong)) return true;
	
		MPCSong song = (MPCSong) clicked;
	

		if(item.getItemId() == 0) // Show in Arists selected
			((TabContainer)getActivity()).showInArtists(song);
		else // Show in Albums selected
			((TabContainer)getActivity()).showInAlbums(song);
		
		return true;
	}

}