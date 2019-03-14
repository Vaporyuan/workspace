package com.meig.logkit;

import com.meig.logkit.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * the setings for logkit
 */
public class SettingsPrefence extends PreferenceActivity {

    private ListPreference mSDPath;

    // the location of sd
    private static final String KEY_SAVE_LOCATION = "setting_save_location";
    private String[] mSDPathStrs;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.settings);
        root = getPreferenceScreen();
        mSDPathStrs = this.getResources().getStringArray(
                R.array.save_location_options);
        mSDPath = (ListPreference) root.findPreference(KEY_SAVE_LOCATION);
        // add default value for sd path
        SharedPreferences sp = LogkitUtils.getSharedPreferences(this);
        if (TextUtils.isEmpty(sp.getString(KEY_SAVE_LOCATION, ""))) {
            sp.edit().putString(KEY_SAVE_LOCATION, "1").commit();
            mSDPath.setValueIndex(1);
            mSDPath.setSummary(mSDPathStrs[1]);
        }
        else{
        	mSDPath.setSummary(mSDPathStrs[Integer.valueOf(sp.getString(KEY_SAVE_LOCATION, null))]);
        }
        mSDPath.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                    Object newValue) {
                int value = Integer.valueOf((String) newValue);
                String path = getSDPath(value);
                if (!TextUtils.isEmpty(path) && LogkitUtils.AVAILABLE_SD_PATHS.contains(path)) {
                    LogkitUtils.setSDPath(SettingsPrefence.this, path);
                    LogkitUtils.ROOT_PATH = path;
                    mSDPath.setSummary(mSDPathStrs[value]);
                    sendRootPathChangeBroadcast();
                    return true;
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.sd_path_switch_fail, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
    }

    // according the sd path to get sd itme such as internal SD or external SD
    private String getSDItem(String SDPath) {
        if(LogkitUtils.DEFAULT_SD_PATH.equals(SDPath)){
            return mSDPathStrs[0];
        }
        return mSDPathStrs[1];
    }

    // get the path of index
    private String getSDPath(int index) {
        for (String str : LogkitUtils.AVAILABLE_SD_PATHS) {
            if (str.contains(String.valueOf(index))) {
                return str;
            }
        }
        return LogkitUtils.AVAILABLE_SD_PATHS.get(0);
    }

    // send root path change broadcast
    private void sendRootPathChangeBroadcast() {
        Intent intent = new Intent(LogkitUtils.ACTION_ROOT_PATH_CHANGE);
        sendBroadcast(intent);
    }
}
