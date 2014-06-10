package thelollies.mpc.views;


import mpc.MPCDatabaseListener;
import thelollies.mpc.R;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * This activity displays the app's settings and allows them to be 
 * changed and saved by the user. 
 * @author thelollies
 */

public class Settings extends SherlockPreferenceActivity implements 
MPCDatabaseListener, OnPreferenceClickListener, OnSharedPreferenceChangeListener{

	private ProgressDialog databaseRenewDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.fragment_settings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		TabContainer.mpc.setMPCDatabaseListener(this);

		// Create click listener for database renewal
		findPreference("renewDatabase").setOnPreferenceClickListener(this);

		// Register this activity as a preference change listener
		PreferenceManager.getDefaultSharedPreferences(this).
		registerOnSharedPreferenceChangeListener(this);
	}
	

	/**
	 * Home is pressed in the ActionBar, trigger back press
	 */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            super.onBackPressed();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	/**
	 * Database update selected
	 */
	@Override
	public boolean onPreferenceClick(Preference arg0){
		// Create progress dialog
		databaseRenewDialog = new ProgressDialog(Settings.this);
		databaseRenewDialog.setMessage("Renewing Database...");
		databaseRenewDialog.setCancelable(false);
		databaseRenewDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		databaseRenewDialog.show();
		
		// Renew the database
		TabContainer.mpc.renewDatabase();
		return true;
	}
	
	/**
	 * Called when database update is complete, dismisses update progress dialog.
	 */
	@Override
	public void databaseUpdated() {
		if(databaseRenewDialog != null){
			databaseRenewDialog.dismiss();
			databaseRenewDialog = null;
		}
	}

	/**
	 * Called by MPClient when database update progress changes, reflects the
	 * new progress in the progress dialog.
	 */
	@Override
	public void databaseUpdateProgressChanged(int progress) {
		databaseRenewDialog.setProgress(progress);
	}

	/**
	 * Called when connection fails in MPClient. Dismisses update dialog if one
	 * exists and shows a Toast message indicating the failed connection.
	 */
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

	/**
	 * Updates the MPClient if the address/port change
	 */
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
