package thelollies.mpc.views;

import thelollies.mpc.R;
import thelollies.mpc.database.SongDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SearchFragment extends SherlockListFragment implements MPCFragment, TextWatcher{

	private boolean dbRenewed = false;
	private static SongDatabase songDatabase;

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
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		EditText searchString = (EditText)view.findViewById(R.id.search_text);
		searchString.addTextChangedListener(this);
		return view;
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// TODO
	}

	public void refreshList(){
		// TODO
	}

	public boolean navigateUp(){
		// TODO 
		return false;
	}

	public void navigateTop(){
		// TODO 
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO create the search functionality here
		// Implement a listviewadapter which can show albums/artists/songs above
		// (perhaps generalise the other ones)
	}

	@Override
	public void afterTextChanged(Editable s) {}

}