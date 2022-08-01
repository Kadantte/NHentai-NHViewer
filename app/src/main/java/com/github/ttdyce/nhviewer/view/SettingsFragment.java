package com.github.ttdyce.nhviewer.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.github.ttdyce.nhviewer.BuildConfig;
import com.github.ttdyce.nhviewer.R;
import com.microsoft.appcenter.distribute.Distribute;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        showVersionName();
        setVersionOnClick();

        PreferenceScreen proxyPreference = findPreference(MainActivity.KEY_PREF_PROXY);
        proxyPreference.setOnPreferenceClickListener(preference -> {
            NavController navController = Navigation.findNavController(getActivity(), R.id.fragmentNavHost);
            navController.navigate(R.id.proxySettingsFragment);
            return true;
        });
        setCheckUpdateOnClick();
    }

    private void setCheckUpdateOnClick() {

        SwitchPreference checkUpdatePreference = findPreference(MainActivity.KEY_PREF_CHECK_UPDATE);
        checkUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(requireContext(), R.string.remind_restart_after_setting, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {


    }

    private void setVersionOnClick() {
        PreferenceScreen versionPreference = findPreference(MainActivity.KEY_PREF_VERSION);
        versionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(requireContext(), "Checking latest version...", Toast.LENGTH_SHORT).show();
                Distribute.checkForUpdate();

                return true;
            }
        });
    }

    private void showVersionName() {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        Log.i("SettingsFragment", "onCreate: version name=" + versionName);
        Log.i("SettingsFragment", "onCreate: version code=" + versionCode);

        PreferenceScreen editTextPreference = findPreference(MainActivity.KEY_PREF_VERSION);
        editTextPreference.setSummary(versionName);
    }




    public enum Language{
        all(0), chinese(1), english(2), japanese(3), notSet(-1);

        int id;

        Language(int i) {
            id = i;
        }

        public int getInt() {
            return id;
        }

        public String toString(){
            return String.valueOf(id);
        }

    }

}
