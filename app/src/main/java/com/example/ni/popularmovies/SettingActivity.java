package com.example.ni.popularmovies;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.Window;

/**
 * Created by ni on 16-8-18.
 */
public class SettingActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        // use action bar here
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

//        getActionBar().setDisplayHomeAsUpEnabled(true);

    }



    public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.rank_key)));
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(),""));
        }

        public boolean onPreferenceChange(Preference preference, Object value) {
            String string = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(string);
                if (prefIndex > 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }else {
                    preference.setSummary(string);
                }
            }
            return true;
        }
    }
}
