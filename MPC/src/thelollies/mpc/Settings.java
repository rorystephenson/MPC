package thelollies.mpc;


import thelollies.mpc.library.MPC;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * This activity displays the app's settings and allows them to be 
 * changed and saved by the user. 
 * @author thelollies
 */

public class Settings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_settings);


		// Create click listener for database renewal
		findPreference("renewDatabase").		
		setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) { 
				new MPC(Settings.this).renewDatabase(Settings.this);
				return true;
			}
		});
	}

	
	
}
