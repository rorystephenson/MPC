package thelollies.mpc.views;

import thelollies.mpc.R;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

	public static enum TabType {SONGS, ARTISTS, ALBUMS, SETTINGS};
	
	private final int PAGE_COUNT = TabType.values().length;
	
	private FragmentManager fm;

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
	}

	@Override
	public ListFragment getItem(int index) {
		TabType type = TabType.values()[index];
		if(type == TabType.SETTINGS) return SearchFragment.newInstance();
		return MusicFragment.newInstance(type);
	}

	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	public ListFragment getFragment(int index){
		return (ListFragment)fm.findFragmentByTag(makeFragmentName(R.id.pager, index));
	}

	public MusicFragment getMusicFragment(int index){
		if(index < 0 || index >= 3) return null;
		return (MusicFragment)getFragment(index);
	}
	
	// Gets the tag of a fragment for use with retrieving the fragment
	private static String makeFragmentName(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}

}
