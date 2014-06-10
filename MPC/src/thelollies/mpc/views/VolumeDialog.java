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

/**
 * VolumeDialog is a dialog which mimics the appearance/behavior of the
 * stock Android volume dialog. It passes volume changes on to the
 * MPClient and reflects changes in volume via a seek bar.
 * @author Rory Stephenson
 *
 */
public class VolumeDialog extends Dialog implements OnSeekBarChangeListener{

	// The last time the volume was shown, 0 indicates a dialog is not showing
	private static long volumeShownAt = 0;
	private OnSeekBarChangeListener volumeChangeListener;
	private final long VOLUME_DELAY; // How long to show volume bar
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

	/** Swallow keyUp events **/
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return true;
	}

	/** Hide volume dialog when back button is pressed **/
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
					// While dialog is showing, check if we have reached end of hide delay
					while((lastShown = VolumeDialog.volumeShownAt) != 0){
						if(System.currentTimeMillis() - lastShown >= VOLUME_DELAY){
							VolumeDialog.volumeShownAt = 0;

							activity.runOnUiThread(new Runnable(){
								public void run(){hide();}});
						}
					}
				}
			}.start();
		}
		else{ // Record when we started showing VolumeDialog
			volumeShownAt = System.currentTimeMillis();
		}
	}

	/**
	 * Provides access to the seek bar
	 * @return VolumeDialog's SeekBar
	 */
	public SeekBar volumeBar(){
		return (SeekBar)findViewById(R.id.volume_seek);
	}

	/**
	 * Set listener which is fired when the volume is changed
	 * @param volumeChangeListener
	 */
	public void setVolumeChangeListener(OnSeekBarChangeListener volumeChangeListener) {
		this.volumeChangeListener = volumeChangeListener;
	}

	/** 
	 * SeekBar change, update volume
	 */
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
