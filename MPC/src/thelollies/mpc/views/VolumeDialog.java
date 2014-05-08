package thelollies.mpc.views;

import thelollies.mpc.R;
import android.app.Activity;
import android.app.Dialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class VolumeDialog extends Dialog implements OnSeekBarChangeListener{

	private static long volumeShownAt = 0;
	private OnSeekBarChangeListener volumeChangeListener;
	private final long VOLUME_DELAY;
	private Activity activity;

	public VolumeDialog(final Activity activity, long dialogDismissDelay) {
		super(activity);
		this.activity = activity;
		this.VOLUME_DELAY = dialogDismissDelay;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		
		View view = getLayoutInflater().inflate(R.layout.dialog_volume, 
				(ViewGroup)findViewById(R.id.volume_dialog_root));
		SeekBar volumeBar = (SeekBar)view.findViewById(R.id.volume_seek);
		volumeBar.setOnSeekBarChangeListener(this);

		setContentView(view);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) return super.onKeyDown(keyCode, event);
		return activity.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		VolumeDialog.volumeShownAt = 0;
	}
	
	@Override
	public void show() {
		if(volumeShownAt == 0){

			// No dialog is showing yet
			super.show();
			volumeShownAt = System.currentTimeMillis();
			new Thread(){
				@Override public void run(){
					long lastShown;
					while((lastShown = VolumeDialog.volumeShownAt) != 0){
						if(System.currentTimeMillis() - lastShown >= VOLUME_DELAY){
							VolumeDialog.volumeShownAt = 0;
							hide();
						}
					}
				}
			}.start();
		}
		else{
			volumeShownAt = System.currentTimeMillis();
		}
	}

	public SeekBar volumeBar(){
		return (SeekBar)findViewById(R.id.volume_seek);
	}

	public void setVolumeChangeListener(OnSeekBarChangeListener volumeChangeListener) {
		this.volumeChangeListener = volumeChangeListener;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		volumeChangeListener.onProgressChanged(seekBar, progress, fromUser);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		volumeChangeListener.onStartTrackingTouch(seekBar);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		volumeChangeListener.onStopTrackingTouch(seekBar);
	}
}
