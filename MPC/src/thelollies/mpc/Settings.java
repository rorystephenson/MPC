package thelollies.mpc;


import thelollies.mpc.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import thelollies.mpc.library.SettingsDatabase;
import thelollies.mpc.library.MPC;
import thelollies.mpc.library.MPCQuery;

/**
 * This activity displays the app's settings and allows them to be 
 * changed and saved by the user. Settings are automatically saved
 * when the user navigates back to song views or when they press
 * renew database button.
 * 
 * @author thelollies
 */

public class Settings extends Activity {

	private String address;
	private int port;

	private MPCQuery playing; // Describes criteria for playing playlist

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_settings);


		// Fetch intents (with missing intent handling)
		Bundle extras = getIntent().getExtras();
		playing = (MPCQuery)extras.get(SongList.PLAY);

		// Colour currently selected button
		for (int i = 0; i < 4; i++){
			if(SongList.buttons[i] == R.id.settingsButton){
				findViewById(SongList.buttons[i]).setBackgroundColor(getResources().getColor(R.color.lightGrey));
			}
			else{
				findViewById(SongList.buttons[i]).setBackgroundColor(getResources().getColor(R.color.mediumGrey));
			}
		}


		// Fetch settings and display in the appropriate setting items
		SettingsDatabase settings = new SettingsDatabase(this);
		this.address = settings.getAddress();
		this.port = settings.getPort();

		EditText addressText = (EditText) findViewById(R.id.address);
		EditText portText = (EditText) findViewById(R.id.port);
		addressText.setText(address);
		portText.setText(Integer.toString(port));

	}

	/**
	 * Updates both the database on the android device and the database on the
	 * MPD server. Fired when the renew database button is clicked
	 * @param v
	 */
	public void renewDatabase(View v){
		EditText addressView = (EditText) findViewById(R.id.address); 
		EditText portView = (EditText) findViewById(R.id.port);

		String address = addressView.getText().toString();
		int port = Integer.parseInt(portView.getText().toString());

		if(this.address != address || this.port != port){
			saveSettings();
		}

		MPC mpc = new MPC(this);
		mpc.renewDatabase();

	}

	/**
	 * Saves the connection settings to the settings database
	 */
	private void saveSettings() {
		EditText addressView = (EditText) findViewById(R.id.address); 
		EditText portView = (EditText) findViewById(R.id.port);

		String address = addressView.getText().toString();
		int port = Integer.parseInt(portView.getText().toString());

		SettingsDatabase settings = new SettingsDatabase(this);
		settings.updateSettings(address, port);
	}
	
	/**
	 * Fired when the navigation buttons are clicked. Does nothing if the current
	 * activitie's button is clicked otherwise navigates to the specified activity.
	 * @param view clicked button
	 */
	public void operateButton(View view){
		Intent intent = new Intent(this, thelollies.mpc.SongList.class);
		intent.putExtra(SongList.PLAY, playing);
		intent.putExtra(SongList.CLICK, view.getId());

		switch (view.getId()){
		case R.id.songsButton:
			intent.putExtra(SongList.DISPL,
					new MPCQuery(MPCQuery.ALL_SONGS));
			break;
		case R.id.artistsButton: 
			intent.putExtra(SongList.DISPL,
					new MPCQuery(MPCQuery.ALL_ARTISTS));
			break;
		case R.id.albumsButton: 
			intent.putExtra(SongList.DISPL,
					new MPCQuery(MPCQuery.ALL_ALBUMS));
			break;
		case R.id.settingsButton: 
			return;
		default: return;
		}

		saveSettings();
		startActivity(intent);
	}

}
