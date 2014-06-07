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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

public class SearchFragment extends SherlockListFragment implements 
MPCFragment, TextWatcher, OnItemLongClickListener, OnSharedPreferenceChangeListener{

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
		getListView().setOnItemLongClickListener(this);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		searchLimit = Integer.parseInt(prefs.getString("searchLimit", "10"));
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	public void dbRenewed(){
		dbRenewed = true;
	}

	@Override
	public void onResume() {
		// Refresh the list if the database was updated
		if(dbRenewed){
			((TextView)getSherlockActivity().findViewById(R.id.search_text)).setText("");
			dbRenewed = false;
		}	
		EditText editText = (EditText) getActivity().findViewById(R.id.search_text);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		editText.setText(prefs.getString("searchText", ""));
		
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
			((TabContainer)getActivity()).show((MPCSong)o);
		}
		else if (o instanceof MPCArtist){
			((TabContainer)getActivity()).show((MPCArtist)o);
		}
		else if(o instanceof MPCAlbum){
			((TabContainer)getActivity()).show((MPCAlbum)o);
		}	
	}


	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// Song menu -> Show in artist, Show in album (show flashing)		
		// TODO
		return false;
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
			searchText.setText(searchText.getText());
			searchLimitChanged = false;
		}
		super.onAttach(activity);
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
				searchText.setText(searchText.getText());
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

}