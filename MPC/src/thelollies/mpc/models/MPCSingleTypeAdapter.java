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
 * Adapter for the listView which displays MPCSongs.
 * @author thelollies
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

		if(convertView == null){
			convertView = inflater.inflate(layout, null);
			holder = new ViewHolder();
			holder.textViewTop = (TextView) convertView.findViewById(R.id.toptext);
			holder.textViewBottom = (TextView) convertView.findViewById(R.id.bottomtext);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		holder.textViewTop.setText(song.getName());
		holder.textViewBottom.setText(song.getDescription());

		if(position == flashIndex){
			new HighlightRunnable(convertView, (Activity)getContext()).start();
			flashIndex = -1;
		}

		return convertView;
	}

	public int indexOf(MPCMusicMeta music){
		return songs.indexOf(music);
	}

	public void flashItem(int index){
		this.flashIndex = index;
		notifyDataSetChanged();
	}
	private static class ViewHolder {
		public TextView textViewTop;
		public TextView textViewBottom;
	}

	private class HighlightRunnable extends Thread{
		private View view;
		private Activity activity;

		public HighlightRunnable(View view, Activity activity){
			this.view = view;
			this.activity = activity;
		}

		@Override
		public void run() {
			TransitionDrawable t = (TransitionDrawable)view.getBackground();
			oneFlash(t);
			oneFlash(t);
		}

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