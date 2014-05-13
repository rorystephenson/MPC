package thelollies.mpc.views;

import java.util.HashMap;

import mpc.MPC;
import mpc.MPCListener;
import mpc.MPCStatus;
import thelollies.mpc.R;
import thelollies.mpc.database.SongDatabase;
import thelollies.mpc.models.ListState;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

// TODO NOW:
// Check whether MPD is completing it's db update before read the new songs in

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

	@Override
	protected void onPause() {
		super.onPause();
		if(volumeDialog != null)
			volumeDialog.dismiss();
		volumeDialog = null;
	}


	@Override
	public void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		mpc.requestStatus();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(TAB, mPager.getCurrentItem());
		super.onSaveInstanceState(outState);
	}

	/**
	 * This is a helper class that implements a generic mechanism for
	 * associating fragments with the tabs in a tab host.  It relies on a
	 * trick.  Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show.  This is not sufficient for switching
	 * between fragments.  So instead we make the content part of the tab host
	 * 0dp high (it is not shown) and the TabManager supplies its own dummy
	 * view to show as the tab content.  It listens to changes in tabs, and takes
	 * care of switch to the correct fragment shown in a separate content area
	 * whenever the selected tab changes.
	 */
	public static class TabManager implements TabHost.OnTabChangeListener {
		private final FragmentActivity mActivity;
		private final TabHost mTabHost;
		private final int mContainerId;
		private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
		TabInfo mLastTab;

		static final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;
			private MusicFragment fragment;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		static class DummyTabFactory implements TabHost.TabContentFactory {
			private final Context mContext;

			public DummyTabFactory(Context context) {
				mContext = context;
			}

			@Override
			public View createTabContent(String tag) {
				View v = new View(mContext);
				v.setMinimumWidth(0);
				v.setMinimumHeight(0);
				return v;
			}
		}

		public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
			mActivity = activity;
			mTabHost = tabHost;
			mContainerId = containerId;
			mTabHost.setOnTabChangedListener(this);
		}

		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
			tabSpec.setContent(new DummyTabFactory(mActivity));
			String tag = tabSpec.getTag();

			TabInfo info = new TabInfo(tag, clss, args);

			// Check to see if we already have a fragment for this tab, probably
			// from a previously saved state.  If so, deactivate it, because our
			// initial state is that a tab isn't shown.
			info.fragment = (MusicFragment)mActivity.getSupportFragmentManager().findFragmentByTag(tag);
			if (info.fragment != null && !info.fragment.isDetached()) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				ft.detach(info.fragment);
				ft.commit();
			}

			mTabs.put(tag, info);
			mTabHost.addTab(tabSpec);
		}

		@Override
		public void onTabChanged(String tabId) {
			TabInfo newTab = mTabs.get(tabId);
			if (mLastTab != newTab) {
				FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
				if (mLastTab != null) {
					if (mLastTab.fragment != null) {
						ft.detach(mLastTab.fragment);
					}
				}
				if (newTab != null) {
					if (newTab.fragment == null) {
						newTab.fragment = (MusicFragment)Fragment.instantiate(mActivity,
								newTab.clss.getName(), newTab.args);
						ft.add(mContainerId, newTab.fragment, newTab.tag);
					} else {
						ft.attach(newTab.fragment);
					}
				}

				mLastTab = newTab;
				ft.commit();
				mActivity.getSupportFragmentManager().executePendingTransactions();
			}
		}

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

	public void returnToPlaying(View v){
		((MPCFragment)mPagerAdapter.getFragment(mPager.getCurrentItem())).navigateUp();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
			return true;
		return super.onKeyUp(keyCode, event);
	}

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

	private void changeVolume(int change){
		mpc.changeVolume(change);
		lastVolChange = System.currentTimeMillis();
		mpc.requestStatus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSherlock().getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

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

	@Override
	public void databaseUpdated() {
		for(int i = 0; i < mPagerAdapter.getCount(); i++){
			MPCFragment current = (MPCFragment)mPagerAdapter.getFragment(i);
			if(current != null)
				current.dbRenewed();
		}
	}

	@Override
	public void statusUpdate(final MPCStatus newStatus) {
		runOnUiThread(new Runnable(){
			@Override public void run(){
				if(newStatus == null){return;}

				View playPauseView = findViewById(R.id.playPauseButton);
				if(newStatus.playing){
					playPauseView.setBackgroundResource(R.drawable.pause);
					playPauseView.setTag("pause");
				} else{
					playPauseView.setBackgroundResource(R.drawable.play);
					playPauseView.setTag("play");
				}

				View shuffleBtn = findViewById(R.id.shuffleButton);
				if(newStatus.shuffling){
					shuffleBtn.setBackgroundResource(R.drawable.shuffle);
					shuffleBtn.setTag("on");
				} else{
					shuffleBtn.setBackgroundResource(R.drawable.shuffle_off);
					shuffleBtn.setTag("off");
				}

				if(System.currentTimeMillis() - lastVolChange <= 1000){
					if(volumeDialog == null) return;
					final SeekBar volumeBar = volumeDialog.volumeBar();
					volumeBar.setProgress(newStatus.volume);
					volumeDialog.show();
				}
			}
		});

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mPager.setCurrentItem(tab.getPosition());
	}

	// Navigate to the top level of a tan when it is reselected
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		((MPCFragment)mPagerAdapter.getFragment(tab.getPosition())).navigateTop();
	}

	@Override	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}


}
