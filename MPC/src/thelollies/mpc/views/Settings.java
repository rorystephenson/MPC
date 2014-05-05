package thelollies.mpc.views;


import mpc.MPCDatabaseListener;
import thelollies.mpc.R;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * This activity displays the app's settings and allows them to be 
 * changed and saved by the user. 
 * @author thelollies
 */

public class Settings extends PreferenceActivity implements MPCDatabaseListener, OnPreferenceClickListener{

	private ProgressDialog databaseRenewDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);
		
		TabContainer.mpc.setMPCDatabaseListener(this);
		
		// Create click listener for database renewal
		findPreference("renewDatabase").setOnPreferenceClickListener(this);
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
		if(databaseRenewDialog != null) databaseRenewDialog.dismiss();
	}

	@Override
	public void databaseUpdateProgressChanged(int progress) {
		databaseRenewDialog.setProgress(progress);
	}

	@Override
	public void connectionFailed(final String message) {
		if(databaseRenewDialog != null) databaseRenewDialog.dismiss();
		this.runOnUiThread(new Runnable(){
			@Override public void run() {
				Toast.makeText(Settings.this, message, Toast.LENGTH_LONG).show();
			}});		
	}

	
	
}
