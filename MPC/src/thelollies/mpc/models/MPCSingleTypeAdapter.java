package thelollies.mpc.models;

import java.util.List;

import mpc.MPCMusicMeta;
import thelollies.mpc.R;
import android.content.Context;
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

		return convertView;
	}

	private static class ViewHolder {
		public TextView textViewTop;
		public TextView textViewBottom;
	}
}