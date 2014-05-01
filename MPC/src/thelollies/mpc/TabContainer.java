package thelollies.mpc;

import java.util.HashMap;

import thelollies.mpc.ReclickableTabHost.ClickSameTabListener;
import thelollies.mpc.TabContainer.TabManager.TabInfo;
import thelollies.mpc.library.MPC;
import thelollies.mpc.library.MPCStatus;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

// NOW:
// Make tabs refresh when settings changed
// If tab pressed when already in that tab, go back to top level of tab

// FUTURE:
// TODO add password ability to MPC

public class TabContainer extends SherlockFragmentActivity{

	// Holds the current query being played
	public static ListState playing;

	// Tab containers
	ReclickableTabHost mTabHost;
	TabManager mTabManager;

	private final static long VOLUME_DELAY = 4000;

	private Dialog seekDialog;
	private final Handler seekDialogHandler = new Handler();

	public static final String TAB = "tab";

	private OnSharedPreferenceChangeListener dbRenewListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Sherlock_Light_NoActionBar);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fragment_tabs);
		mTabHost = (ReclickableTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);

		Bundle bndlSongs = new Bundle();
		bndlSongs.putString(TAB, "songs");

		Bundle bndlArtists = new Bundle();
		bndlArtists.putString(TAB, "artists");

		Bundle bndlAlbums = new Bundle();
		bndlAlbums.putString(TAB, "albums");

		mTabManager.addTab(mTabHost.newTabSpec("songs").setIndicator("Songs"),
				ListFragment.class, bndlSongs);
		mTabManager.addTab(mTabHost.newTabSpec("artists").setIndicator("Artists"),
				ListFragment.class, bndlArtists);
		mTabManager.addTab(mTabHost.newTabSpec("albums").setIndicator("Albums"),
				ListFragment.class, bndlAlbums);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}

		seekDialog = new Dialog(this);
		seekDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.seek_dialog, (ViewGroup)findViewById(R.id.your_dialog_root_element));
		seekDialog.setContentView(layout);

		seekDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				return TabContainer.this.onKeyDown(keyCode, event);
			}});

		// Add the drag listener on the seek bar
		SeekBar bar = (SeekBar)seekDialog.findViewById(R.id.volume_seek);
		bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser){
					Integer newVol = new MPC(TabContainer.this).setVolume(progress);
					if(newVol != null) seekBar.setProgress(newVol);
					seekDialogHandler.removeCallbacksAndMessages(null);
					seekDialogHandler.postDelayed(new Thread(){
						@Override public void run() {
							seekDialog.hide();
						}}, VOLUME_DELAY);
				}
			}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
		});


		// Reload current tab
		dbRenewListener = new OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if(key.equals("renewDatabase"))
					for(TabInfo ti : mTabManager.mTabs.values()){
						if(ti != null && ti.fragment != null)
							ti.fragment.dbRenewed();
					}
			}
		};
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(dbRenewListener);


		mTabHost.setClickSameTabListener(new ClickSameTabListener() {

			@Override
			public void clickSameTab() {
				ListFragment frag = mTabManager.mLastTab.fragment;
				frag.navigateTop();
			}
		});
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		updateButtons();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
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
			private ListFragment fragment;

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
			info.fragment = (ListFragment)mActivity.getSupportFragmentManager().findFragmentByTag(tag);
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
						newTab.fragment = (ListFragment)Fragment.instantiate(mActivity,
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
		new MPC(this).previous();
	}

	/**
	 * Fires when fast forward button is clicked. Skips to the next song in the playlist.
	 * @param v
	 */
	public void fastForward(View v){
		new MPC(this).next();
	}

	/**
	 * Handles clicking of the play/pause button. Passing the appropriate action to
	 * the server.
	 * @param v
	 */
	public void playPause(View v){

		MPC mpc = new MPC(this);
		Object tag = v.getTag(R.id.playPauseButton);
		if(tag == null) {
			updateButtons();
			tag = v.getTag(R.id.playPauseButton);
		}

		if(v.getTag(R.id.playPauseButton).equals("play")){
			mpc.play();
		}
		else{
			mpc.pause();
		}

		updateButtons();
	}

	/**
	 *  Toggles shuffle mode on the MPD server, fired by shuffle button.
	 *  @param v
	 */
	public void shuffleToggle(View v){
		MPC mpc = new MPC(this);

		if(v.getTag(R.id.shuffleButton).equals("on")){
			mpc.shuffle(false);
		}
		else{
			mpc.shuffle(true);
		}

		updateButtons();
	}

	public void returnToPlaying(View v){
		mTabManager.mLastTab.fragment.navigateUp();
	}

	/**
	 * If music is playing show the pause button, if paused show the play button.
	 */
	public void updateButtons() {
		MPC mpc = new MPC(this);
		MPCStatus status = mpc.getStatus();

		if(status == null){return;}

		View playPauseView = findViewById(R.id.playPauseButton);
		if(status.playing){
			playPauseView.setBackgroundResource(R.drawable.pause);
			playPauseView.setTag(R.id.playPauseButton, "pause");
		} else{
			playPauseView.setBackgroundResource(R.drawable.play);
			playPauseView.setTag(R.id.playPauseButton, "play");
		}

		View shuffleBtn = findViewById(R.id.shuffleButton);
		if(status.shuffling){
			shuffleBtn.setBackgroundResource(R.drawable.shuffle);
			shuffleBtn.setTag(R.id.shuffleButton, "on");

		} else{
			shuffleBtn.setBackgroundResource(R.drawable.shuffle_off);
			shuffleBtn.setTag(R.id.shuffleButton, "off");
		}
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//super.onKeyDown(keyCode, event);
		switch(keyCode){
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			changeVolume(-5);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			changeVolume(5);
			return true;
		case KeyEvent.KEYCODE_BACK:
			if(!mTabManager.mLastTab.fragment.navigateUp()){
				super.onBackPressed();
				return false;
			}
			return true;
		default:
			return false;
		}
	}

	private void changeVolume(int change){
		Integer vol = new MPC(this).changeVolume(change);
		if(vol != null){
			SeekBar bar = (SeekBar)seekDialog.findViewById(R.id.volume_seek);
			bar.setProgress(vol);
			seekDialog.show();

			if(seekDialog.isShowing()){seekDialogHandler.removeCallbacksAndMessages(null);}
			seekDialogHandler.postDelayed(new Thread(){
				@Override public void run() {
					seekDialog.hide();
				}}, VOLUME_DELAY);
		}
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

}
