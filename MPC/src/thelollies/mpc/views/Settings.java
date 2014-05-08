package thelollies.mpc.views;


import mpc.MPCDatabaseListener;
import thelollies.mpc.R;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * This activity displays the app's settings and allows them to be 
 * changed and saved by the user. 
 * @author thelollies
 */

public class Settings extends PreferenceActivity implements 
MPCDatabaseListener, OnPreferenceClickListener, OnSharedPreferenceChangeListener{

	private ProgressDialog databaseRenewDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.fragment_settings);

		TabContainer.mpc.setMPCDatabaseListener(this);

		// Create click listener for database renewal
		findPreference("renewDatabase").setOnPreferenceClickListener(this);

		// Register this activity as a preference change listener
		PreferenceManager.getDefaultSharedPreferences(this).
		registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference arg0){
		databaseRenewDialog = new ProgressDialog(Settings.this);
		databaseRenewDialog.setMessage("Renewing Database...");
		databaseRenewDialog.setCancelable(false);
		databaseRenewDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		databaseRenewDialog.show();
		TabContainer.mpc.renewDatabase();
		return true;
	}

	@Override
	public void databaseUpdated() {
		if(databaseRenewDialog != null){
			databaseRenewDialog.dismiss();
			databaseRenewDialog = null;
		}
	}

	@Override
	public void databaseUpdateProgressChanged(int progress) {
		databaseRenewDialog.setProgress(progress);
	}

	@Override
	public void connectionFailed(final String message) {
		if(databaseRenewDialog != null){
			databaseRenewDialog.dismiss();
			databaseRenewDialog = null;
		}
		this.runOnUiThread(new Runnable(){
			@Override public void run() {
				if(TabContainer.connErrLastShown - System.currentTimeMillis() > 2000){
					Toast.makeText(Settings.this, message, Toast.LENGTH_LONG).show();
					TabContainer.connErrLastShown = System.currentTimeMillis();
				}
			}});		
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("address") || key.equals("port")){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String address = prefs.getString("address", "");
			int port = 0;
			try{
				port = Integer.parseInt(prefs.getString("port", "0"));
			}catch(NumberFormatException e){e.printStackTrace();}

			// Update MPC with new settings
			TabContainer.mpc.updateSettings(port, address);
		}
	}

}
