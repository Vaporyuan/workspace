package com.meig.logkit;

import java.io.File;

import com.meig.logkit.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.PreferenceGroup;

/**
 * the setting activity of logs
 */
public class LogkitSetings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    // control services stop or start
    public static final int CONTROLSERVICES = 1;
    // adb switch
    SwitchPreference mAdbMainToggle = null;
    SwitchPreference mAdbRadioToggle = null;
    SwitchPreference mAdbSystemToggle = null;
    SwitchPreference mAdbEventToggle = null;

    // qxdm switch
    SwitchPreference mQXDMToggle = null;

    // ip switch
    SwitchPreference mIPToggle = null;

    // state switch
    SwitchPreference mDmesgToggle = null;
    SwitchPreference mDumpStateToggle = null;
    SwitchPreference mDumpSysToggle = null;

    // crash switch
    SwitchPreference mCrashToggle = null;

    private Handler handler = new Handler() {  
        public void handleMessage(android.os.Message msg) {  
            switch (msg.what) {  
            case CONTROLSERVICES: 
                ControlService();
            }  
        };  
    };  
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init all preferences
        initPreferences();
        ControlService();
        Intent intent = getIntent();
        String isAllOn = intent.getStringExtra("on");
        if(isAllOn != null && isAllOn.equals("on")){
        	openAllLogSwitch();
        }
        if(isAllOn != null && isAllOn.equals("off")){
        	closeAllLogSwitch();
        }
        if(!LogkitUtils.checkSDMounted(this)){/*
            new AlertDialog.Builder(this)
            .setMessage(R.string.no_sd_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.dismiss();
                    // if no sd and exit this activity
                    LogkitSetings.this.finish();
                }
            }).create().show();
        */}
    }

     // init all log switch preferences
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void initPreferences() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.logkit_main);
        root = getPreferenceScreen();
        SharedPreferences share = LogkitUtils.getSharedPreferences(this);
        mAdbMainToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_ADB_MAIN_TOGGLE));
        mAdbMainToggle.setChecked(share.getBoolean(
                LogkitUtils.KEY_ADB_MAIN_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mAdbMainToggle.setOnPreferenceChangeListener(this);
        if (!getResources().getBoolean(R.bool.main_tag)) {
            ((PreferenceGroup)findPreference(LogkitUtils.KEY_ADB_LOG))
                .removePreference(findPreference(LogkitUtils.KEY_ADB_MAIN_TOGGLE));
        }
        mAdbRadioToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_ADB_RADIO_TOGGLE));
        mAdbRadioToggle.setChecked(share.getBoolean(
                LogkitUtils.KEY_ADB_RADIO_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mAdbRadioToggle.setOnPreferenceChangeListener(this);
        if (!getResources().getBoolean(R.bool.radio_tag)) {
            ((PreferenceGroup)findPreference(LogkitUtils.KEY_ADB_LOG))
                .removePreference(findPreference(LogkitUtils.KEY_ADB_RADIO_TOGGLE));
        }
        mAdbSystemToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_ADB_SYSTEM_TOGGLE));
        mAdbSystemToggle.setChecked(share.getBoolean(
                LogkitUtils.KEY_ADB_SYSTEM_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mAdbSystemToggle.setOnPreferenceChangeListener(this);
        if (!getResources().getBoolean(R.bool.system_tag)) {
            ((PreferenceGroup)findPreference(LogkitUtils.KEY_ADB_LOG))
                .removePreference(findPreference(LogkitUtils.KEY_ADB_SYSTEM_TOGGLE));
        }
        mAdbEventToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_ADB_EVENT_TOGGLE));
        mAdbEventToggle.setChecked(share.getBoolean(
                LogkitUtils.KEY_ADB_EVENT_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mAdbEventToggle.setOnPreferenceChangeListener(this);
        if (!getResources().getBoolean(R.bool.event_tag)) {
            ((PreferenceGroup)findPreference(LogkitUtils.KEY_ADB_LOG))
                .removePreference(findPreference(LogkitUtils.KEY_ADB_EVENT_TOGGLE));
        }
        mQXDMToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_QXDM_TOGGLE));
        mQXDMToggle.setChecked(share.getBoolean(LogkitUtils.KEY_QXDM_TOGGLE,
                LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mQXDMToggle.setOnPreferenceChangeListener(this);
        mIPToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_IP_TOGGLE));
        mIPToggle.setChecked(share.getBoolean(LogkitUtils.KEY_IP_TOGGLE,
                LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mIPToggle.setOnPreferenceChangeListener(this);
        mDmesgToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_DMESG_TOGGLE));
        mDmesgToggle.setChecked(share.getBoolean(LogkitUtils.KEY_DMESG_TOGGLE,
                LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mDmesgToggle.setOnPreferenceChangeListener(this);
        mDumpStateToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_DUMP_STATE_TOGGLE));
        mDumpStateToggle.setChecked(share.getBoolean(
                LogkitUtils.KEY_DUMP_STATE_TOGGLE,
                LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mDumpStateToggle.setOnPreferenceChangeListener(this);
        mDumpSysToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_DUMP_SYS_TOGGLE));
        mDumpSysToggle.setChecked(share.getBoolean(
                LogkitUtils.KEY_DUMP_SYS_TOGGLE,
                LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mDumpSysToggle.setOnPreferenceChangeListener(this);
        PreferenceCategory pc = (PreferenceCategory) root
                .findPreference(LogkitUtils.KEY_STATE_LOG);
        if (null != pc) {
            if (null != mDumpStateToggle && !CommandBase.DUMP_STATE_LOG_ENABLE) {
                pc.removePreference(mDumpStateToggle);
            }
            if (null != mDumpSysToggle && !CommandBase.DUMP_SYS_LOG_ENABLE) {
                pc.removePreference(mDumpSysToggle);
            }
        }
        mCrashToggle = ((SwitchPreference) root
                .findPreference(LogkitUtils.KEY_CRASH_TOGGLE));
        mCrashToggle.setChecked(share.getBoolean(LogkitUtils.KEY_CRASH_TOGGLE,
                LogkitUtils.LOGKIT_DEFAULT_ENABLE));
        mCrashToggle.setOnPreferenceChangeListener(this);
        if (!CommandBase.CRASH_LOG_ENABLE) {
            root.removePreference(root.findPreference(LogkitUtils.KEY_CRASH_LOG));
        }
        // hide log items
        if (!getResources().getBoolean(R.bool.adb_log)) {
            root.removePreference(root.findPreference(LogkitUtils.KEY_ADB_LOG));
        }
        if (!getResources().getBoolean(R.bool.qxdm_log)) {
            root.removePreference(root.findPreference(LogkitUtils.KEY_QXDM_LOG));
        }
        if (!getResources().getBoolean(R.bool.ip_log)) {
            root.removePreference(root.findPreference(LogkitUtils.KEY_IP_LOG));
        }
        if (!getResources().getBoolean(R.bool.state_log)) {
            root.removePreference(root.findPreference(LogkitUtils.KEY_STATE_LOG));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        // send setting change broadcast
        sendSettingUpdateBroadcast();
        
        Message message = new Message();  
        message.what = CONTROLSERVICES; 
        handler.sendMessageDelayed(message, 1);
        
        return true;
    }

    public void ControlService(){
        if(LogkitUtils.isStartService(this)
                && !LogkitUtils.LOGKIT_SREVICE_IS_RUNNING){
            sendBroadcast(new Intent(LogkitUtils.ACTION_SERVICE_START));
        }else if(!LogkitUtils.isStartService(this)
                && LogkitUtils.LOGKIT_SREVICE_IS_RUNNING){
            sendBroadcast(new Intent(LogkitUtils.ACTION_SERVICE_STOP));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsPrefence.class));
            break;
        case R.id.action_clean_logs:
            // clear all Logs
            confirmClearLogs();
            break;
        }
        return true;
    }

    // delete all logs files
    private void clear(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                LogkitUtils.LogE(file.getAbsolutePath());
                LogkitUtils.LogE(file.getPath());
                if ((LogkitUtils.ROOT_PATH + LogkitUtils.CONFIG_PATH)
                        .equals(file.getAbsolutePath() + "/")) {
                    return;
                }
                File[] files = file.listFiles();
                for (File f : files) {
                    clear(f);
                }
            }
        }
    }

    // confirm whether clear logs
    private void confirmClearLogs() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.clean_logs_msg)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,int which) {
                                dialog.dismiss();
                                // the progress dialog of delete files
                                final ProgressDialog pd = ProgressDialog
                                        .show(LogkitSetings.this,null,
                                                getResIdString(R.string.clear_logs_ing));
                                pd.show();
                                closeAllLogSwitch();
                                new Thread() {
                                    public void run() {
                                        clear(new File(LogkitUtils.ROOT_PATH + LogkitUtils.LOGS_PATH));
                                        pd.dismiss();
                                    }
                                }.start();

                                // the dialog of clear logs success
                                new AlertDialog.Builder(LogkitSetings.this)
                                        .setMessage(
                                                R.string.clear_logs_file_success)
                                        .setCancelable(false)
                                        .setPositiveButton(
                                                R.string.ok,
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(
                                                            DialogInterface dialog,
                                                            int id) {
                                                        sendSettingUpdateBroadcast();
                                                    }
                                                }).create().show();
                            }
                        }).setNegativeButton(R.string.no, null).show();
    }

    // send update broadcast
    private void sendSettingUpdateBroadcast() {
        sendBroadcast(new Intent(LogkitUtils.ACTION_SETTING_UPDATE));
    }

    // close all log switch
    private void closeAllLogSwitch() {
        closeLogSwitch(mAdbMainToggle);
        closeLogSwitch(mAdbRadioToggle);
        closeLogSwitch(mAdbSystemToggle);
        closeLogSwitch(mAdbEventToggle);
        closeLogSwitch(mQXDMToggle);
        closeLogSwitch(mIPToggle);
        closeLogSwitch(mDmesgToggle);
        closeLogSwitch(mDumpStateToggle);
        closeLogSwitch(mDumpSysToggle);
        closeLogSwitch(mCrashToggle);
    }

    private void closeLogSwitch(SwitchPreference sp) {
        if (null != sp) {
            sp.setChecked(false);
        }
    }
    
	// open all log switch
	private void openAllLogSwitch() {
		openLogSwitch(mAdbMainToggle);
		openLogSwitch(mAdbRadioToggle);
		openLogSwitch(mAdbSystemToggle);
		openLogSwitch(mAdbEventToggle);
		openLogSwitch(mQXDMToggle);
		openLogSwitch(mIPToggle);
		openLogSwitch(mDmesgToggle);
		openLogSwitch(mDumpStateToggle);
		openLogSwitch(mDumpSysToggle);
		openLogSwitch(mCrashToggle);
	}

	private void openLogSwitch(SwitchPreference sp) {
		if (null != sp) {
			sp.setChecked(true);
		}
	}

    private String getResIdString(int resId) {
        return this.getResources().getString(resId);
    }
}
