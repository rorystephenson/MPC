package thelollies.mpc.models;

import java.util.List;

import mpc.MPCMusicMeta;
import thelollies.mpc.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter for the listView which displays types that implement MPCMusicMeta.
 * It can only display one of sub-type at a time, if multiple 
 * are required in a single list the MPCMultiplTypeAdapter should be used
 * @author Rory Stephenson
 *
 */
public class MPCSingleTypeAdapter<T extends MPCMusicMeta> extends ArrayAdapter<T> {

	private List<T> songs;
	private final int layout;
	private final LayoutInflater inflater;
	private int flashIndex = -1;

	public MPCSingleTypeAdapter(Context context, int textViewResourceId, List<T> items) {
		super(context, textViewResourceId, items);
		this.songs = items;
		this.layout = textViewResourceId;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		MPCMusicMeta song = songs.get(position);

		// Cannot recycle view, create a new one
		if(convertView == null){
			convertView = inflater.inflate(layout, null);
			holder = new ViewHolder();
			holder.textViewTop = (TextView) convertView.findViewById(R.id.toptext);
			holder.textViewBottom = (TextView) convertView.findViewById(R.id.bottomtext);
			convertView.setTag(holder);
		}else{
			// recycle view
			holder = (ViewHolder)convertView.getTag();
		}

		holder.textViewTop.setText(song.getName());
		holder.textViewBottom.setText(song.getDescription());

		// Shows a flashing animation on a row if it's position matches flashIndex
		if(position == flashIndex){
			new HighlightRunnable(convertView, (Activity)getContext()).start();
			flashIndex = -1;
		}

		return convertView;
	}

	public int indexOf(MPCMusicMeta music){
		return songs.indexOf(music);
	}

	/**
	 * Mark a row to show a flashing animation, must call notifyDataSetChanged()
	 * after for it to work.
	 * @param index of row to animate
	 */
	public void flashItem(int index){
		this.flashIndex = index;
		notifyDataSetChanged();
	}

	/* Holds the components of a row to be reused in scrolling */	
	private static class ViewHolder {
		public TextView textViewTop;
		public TextView textViewBottom;
	}

	/*Handles the actual execution of the animation. It does so by running a 
	 * transition and undoing it twice. The transitions are fired on the
	 * UI thread. */
	private class HighlightRunnable extends Thread{
		private View view;
		private Activity activity;

		public HighlightRunnable(View view, Activity activity){
			this.view = view;
			this.activity = activity;
		}

		/* Runs the two flashes */		
		@Override
		public void run() {
			TransitionDrawable t = (TransitionDrawable)view.getBackground();
			oneFlash(t);
			oneFlash(t);
		}

		/* Executes a flash. This involves firing a transition,
		 * pausing for 300ms so it can complete, and then reversing it before
		 * waiting 300ms again. The transitions are fired on the UI Thread	 */
		private void oneFlash(final TransitionDrawable t){
			activity.runOnUiThread(new Runnable(){
				public void run(){
					t.startTransition(300);
				}
			});
			try{Thread.sleep(300);} catch (InterruptedException e) {}
			activity.runOnUiThread(new Runnable(){
				public void run(){
					t.reverseTransition(300);
				}
			});
			try{Thread.sleep(300);} catch (InterruptedException e) {}
		}
	}
}