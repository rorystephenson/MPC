package thelollies.mpc.views;

import java.util.ArrayList;
import java.util.List;

import mpc.MPCMusicMeta;
import thelollies.mpc.R;
import thelollies.mpc.database.SongDatabase;
import thelollies.mpc.models.MPCMultipleTypeAdapter;
import android.os.Bundle;
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

import com.actionbarsherlock.app.SherlockListFragment;

public class SearchFragment extends SherlockListFragment implements 
	MPCFragment, TextWatcher, OnItemLongClickListener{

	private boolean dbRenewed = false;
	private static SongDatabase songDatabase;
	private static List<MPCMusicMeta> results = new ArrayList<MPCMusicMeta>();
	private static MPCMultipleTypeAdapter adapter;
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
	}

	public void dbRenewed(){
		dbRenewed = true;
	}

	@Override
	public void onResume() {
		super.onResume();

		// Refresh the list if the database was updated
		if(dbRenewed){
			((TextView)getSherlockActivity().findViewById(R.id.search_text)).setText("");
			dbRenewed = false;
		}
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
		// Click song -> navigate to song in Songs tab (flashing selection)
		// Click artist -> navigate into artists in Artist tab
		// Click album -> navigate into album in Albums tab
		// TODO
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
		else results = songDatabase.search(query, -1);

		adapter.setData(results);
	}

	// Unused methods required by TextWatcher
	@Override	public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
	@Override	public void afterTextChanged(Editable s) {}

}