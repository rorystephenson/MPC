package thelollies.mpc.models;

import java.util.List;

import mpc.MPCArtist;
import mpc.MPCMusicMeta;
import mpc.MPCSong;
import thelollies.mpc.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * Adapter for the listView which holds songs, artists and albums.
 * @author thelollies
 *
 */
public class MPCMultipleTypeAdapter extends BaseAdapter {

	private List<MPCMusicMeta> music;
	private final LayoutInflater inflater;

	public MPCMultipleTypeAdapter(Context context, List<MPCMusicMeta> items) {
		this.music = items;
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getItemViewType(int position) {
		MPCMusicMeta item = music.get(position);
		if(item instanceof MPCSong) return 0;
		else if(item instanceof MPCArtist) return 1;
		else return 2;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		MPCMusicMeta song = music.get(position);
		int viewType = getItemViewType(position);
		
		// Create new view if we cannot recycle
		if(convertView == null){
			switch(viewType){
			case 0: convertView = inflater.inflate(R.layout.row_song, null); break;
			case 1: convertView = inflater.inflate(R.layout.row_artist, null); break;
			default: convertView = inflater.inflate(R.layout.row_album, null); break;
			}
			
			// Uses holder pattern to implement recycling of views
			holder = new ViewHolder();
			holder.textViewTop = (TextView) convertView.findViewById(R.id.toptext);
			holder.textViewBottom = (TextView) convertView.findViewById(R.id.bottomtext);
			convertView.setTag(holder);
		}else{
			// Recycle invisible view
			holder = (ViewHolder)convertView.getTag();
		}

		holder.textViewTop.setText(song.getName());
		holder.textViewBottom.setText(song.getDescription());

		return convertView;
	}

	@Override
	public int getCount() {
		return music.size();
	}

	@Override
	public Object getItem(int position) {
		return music.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Holder is simply a container for row information, used to recycle views.
	 * @author Rory Stephenson
	 *
	 */
	private static class ViewHolder {
		public TextView textViewTop;
		public TextView textViewBottom;
	}

	public void setData(List<MPCMusicMeta> music) {
		this.music = music;
		notifyDataSetChanged();
	}
}
