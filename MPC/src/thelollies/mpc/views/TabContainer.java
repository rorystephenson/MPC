package thelollies.mpc.views;

import mpc.MPC;
import mpc.MPCListener;
import mpc.MPCMusicMeta;
import mpc.MPCSong;
import mpc.MPCStatus;
import thelollies.mpc.R;
import thelollies.mpc.database.SongDatabase;
import thelollies.mpc.models.ListState;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

// TODO 
// Check whether MPD is completing it's db update before read the new songs in

/**
 * TabContainer holds the tabs and facilitates navigation between them.
 * @author Rory Stephenson
 *
 */
public class TabContainer extends SherlockFragmentActivity implements MPCListener, TabListener{

	// Keeps track of the last system time the connection error toast message
	// was shown so that it isn't shown if it is already showing
	public static long connErrLastShown = 0;

	// Holds the current query being played
	public static ListState playing;

	// Tab related variables
	private static final String TAB = "tab";
	private ViewPager mPager;
	private ViewPagerAdapter mPagerAdapter;

	// Volume related variables
	private final static long VOLUME_DELAY = 4000;
	private VolumeDialog volumeDialog;
	private long lastVolChange = 0;

	// MPC library instance for communicating with MPD server
	protected static MPC mpc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_tabs);

		// Create volume dialog
		createVolumeDialog();

		// Handle to action bar instance
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);

		// Set up the ViewPager and ViewPagerAdapter
		mPager = (ViewPager)findViewById(R.id.pager);
		mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(3);
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// Checks the tab isn't already pointing to the right
				// Page otherwise selecting it will cause tabReselected to be fired
				ActionBar actionBar = getSupportActionBar();
				int selectedNavInd = actionBar.getSelectedNavigationIndex();
				if(selectedNavInd != position){
					actionBar.setSelectedNavigationItem(position);
				}
			}
		});


		// Create tabs in action bar
		actionBar.addTab(actionBar.newTab().setText("Songs").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Artists").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Albums").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("Search").setTabListener(this));

		// Set the last tab if it was saved
		if (savedInstanceState != null) 
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt(TAB));


		// Set up mpc instance
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String address = sharedPref.getString("address", "");
		int port = 0;
		try{
			port = Integer.parseInt(sharedPref.getString("port", "0"));
		}catch(NumberFormatException e){}
		if (mpc == null) mpc = new MPC(address, port, 1000, new SongDatabase(this));
		mpc.setMPCListener(this);


	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up volume controller
		if(volumeDialog == null){
			createVolumeDialog();
		}
	}

	private void createVolumeDialog(){
		volumeDialog = new VolumeDialog(this, VOLUME_DELAY);

		// Add the drag listener on the volume bar
		volumeDialog.setVolumeChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					if(mpc == null) return;
					mpc.setVolume(progress);
					lastVolChange = System.currentTimeMillis();
					mpc.requestStatus();
				}
			}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}

	/**
	 * Dismiss the volume dialog on pause if it is showing
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if(volumeDialog != null)
			volumeDialog.dismiss();
		volumeDialog = null;
	}

	/**
	 * Request the MPD server status on start
	 */
	@Override
	public void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		mpc.requestStatus();
	}

	/**
	 * Save the current tab
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(TAB, mPager.getCurrentItem());
		super.onSaveInstanceState(outState);
	}

	/**
	 * Fired when the rewind button is clicked. Skips back a song in the playlist.
	 * @param v
	 */
	public void rewind(View v){
		mpc.previous();
		mpc.requestStatus();
	}

	/**
	 * Fires when fast forward button is clicked. Skips to the next song in the playlist.
	 * @param v
	 */
	public void fastForward(View v){
		mpc.next();
		mpc.requestStatus();
	}

	/**
	 * Handles clicking of the play/pause button. Passing the appropriate action to
	 * the server.
	 * @param v
	 */
	public void playPause(View v){

		if(v.getTag().equals("play")){
			mpc.play();
		}
		else{
			mpc.pause();
		}

		mpc.requestStatus();
	}

	/**
	 *  Toggles shuffle mode on the MPD server, fired by shuffle button.
	 *  @param v
	 */
	public void shuffleToggle(View v){
		if(v.getTag().equals("on")){
			mpc.shuffle(false);
		}
		else{
			mpc.shuffle(true);
		}

		mpc.requestStatus();
	}

	/**
	 * Navigates up one level in the current tab
	 * @param v
	 */
	public void navigateUp(View v){
		((MPCFragment)mPagerAdapter.getFragment(mPager.getCurrentItem())).navigateUp();
	}

	/**
	 * Fire volume change
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
			return true;
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * Swallows volume keyDown actions as they are caught on keyUp. 
	 * If the action is a back press it attempts to navigate up a level
	 * in the tab and otherwise passes on the back press to its parent.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Ignore releasing of buttons
		if(event.getAction() != KeyEvent.ACTION_DOWN) return true;

		switch(keyCode){
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			changeVolume(-5);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			changeVolume(5);
			return true;
		case KeyEvent.KEYCODE_BACK:
			if(!((MPCFragment)mPagerAdapter.getFragment(mPager.getCurrentItem())).navigateUp()){
				super.onBackPressed();
				return false;
			}
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Requests a volume change of the specified amount, records when the
	 * change was made and requests the MPD server to update its status.
	 * @param change
	 */
	private void changeVolume(int change){
		mpc.changeVolume(change);
		lastVolChange = System.currentTimeMillis();
		mpc.requestStatus();
	}

	/**
	 * Shows options menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSherlock().getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Handles selection of items in the options menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case R.id.settings:
			startActivity(new Intent(TabContainer.this, Settings.class));
			return true;
		default:
			return super.onOptionsItemSelected(item); 
		}
	}

	/**
	 * Shows a toast message when the connection fails if one is not already
	 * showing.
	 */
	@Override
	public void connectionFailed(final String message) {
		this.runOnUiThread(new Runnable(){
			@Override public void run() {
				if(System.currentTimeMillis() - connErrLastShown > 2000){
					Toast.makeText(TabContainer.this, message, Toast.LENGTH_SHORT).show();
					connErrLastShown = System.currentTimeMillis();
				}
			}});
	}

	/**
	 * Passes the database update indication onto the active fragment.
	 */
	@Override
	public void databaseUpdated() {
		for(int i = 0; i < mPagerAdapter.getCount(); i++){
			MPCFragment current = (MPCFragment)mPagerAdapter.getFragment(i);
			if(current != null)
				current.dbRenewed();
		}
	}

	/**
	 * Updates the UI elements and the volume indicator when the status
	 * is updated (triggered by MPClient). Will only show volume indicator
	 * if the user recently changed the volume. This prevents it from
	 * being shown when another client changes the MPD server's volume.
	 */
	@Override
	public void statusUpdate(final MPCStatus newStatus) {
		runOnUiThread(new Runnable(){
			@Override public void run(){
				if(newStatus == null){return;}

				// Update play/pause button
				View playPauseView = findViewById(R.id.playPauseButton);
				if(newStatus.playing){
					playPauseView.setBackgroundResource(R.drawable.pause);
					playPauseView.setTag("pause");
				} else{
					playPauseView.setBackgroundResource(R.drawable.play);
					playPauseView.setTag("play");
				}

				// update shuffle button
				View shuffleBtn = findViewById(R.id.shuffleButton);
				if(newStatus.shuffling){
					shuffleBtn.setBackgroundResource(R.drawable.shuffle);
					shuffleBtn.setTag("on");
				} else{
					shuffleBtn.setBackgroundResource(R.drawable.shuffle_off);
					shuffleBtn.setTag("off");
				}

				// Show volume indicator if user recently update volume
				if(System.currentTimeMillis() - lastVolChange <= 1000){
					if(volumeDialog == null) return;
					final SeekBar volumeBar = volumeDialog.volumeBar();
					volumeBar.setProgress(newStatus.volume);
					volumeDialog.show();
				}
			}
		});

	}

	/**
	 * Change to the selected tab's fragment
	 */
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mPager.setCurrentItem(tab.getPosition());
	}

	/**
	 * Navigate to the top level of the current fragment when it's tab is
	 * reselected.
	 */
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		((MPCFragment)mPagerAdapter.getFragment(tab.getPosition())).navigateTop();
	}

	@Override	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}

	/**
	 * Show the specified song in the songs tab
	 * @param song
	 */
	public void showInSongs(MPCSong song){
		hideKeyboard();
		MusicFragment songFragment = mPagerAdapter.getMusicFragment(0);
		songFragment.showInSongs(song);
		mPager.setCurrentItem(0);
	}
	
	/**
	 * Show the specified music item in the artists tab (supports MPCsong or MPCArtist)
	 * @param music
	 */
	public void showInArtists(MPCMusicMeta music){
		hideKeyboard();
		MusicFragment artistFragment = mPagerAdapter.getMusicFragment(1);
		artistFragment.showInArtists(music);
		mPager.setCurrentItem(1);
	}
	
	/**
	 * Shows the specified music item in the albums tab (supports MPCSong or MPCAlbum)
	 * @param music
	 */
	public void showInAlbums(MPCMusicMeta music){
		hideKeyboard();
		MusicFragment albumFragment = mPagerAdapter.getMusicFragment(2);
		albumFragment.showInAlbums(music);
		mPager.setCurrentItem(2);
	}
	
	/**
	 * Hide the on screen keyboard if it is showing
	 */
	private void hideKeyboard(){
		InputMethodManager inputManager = (InputMethodManager) this
	            .getSystemService(Context.INPUT_METHOD_SERVICE);

	    //check if no view has focus:
	    View v=this.getCurrentFocus();
	    if(v==null)
	        return;

	    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
}
