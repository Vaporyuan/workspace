package com.meig.logkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.meig.logkit.R;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;
import android.os.SystemProperties;


/*
 * the service of logcat control
 */

public class LogKitService extends Service {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.US);

    // the thread of adb
    private CommandExecThread mAdbMainThread = null;
    private CommandExecThread mAdbRadioThread = null;
    private CommandExecThread mAdbSystemThread = null;
    private CommandExecThread mAdbEventThread = null;

    // the thread of state thread
    private CommandExecThread mDesgThread = null;
    private CommandExecThread mDumpStateThread = null;
    private CommandExecThread mDumpSysThread = null;

    // QXDM thread
    private CommandExecThread mQXDMThread = null;

    //IP thread
    private CommandExecThread mIPThread = null;

    private SharedPreferences sp;

    // the receiver of settings update for setting
    private SettingUpdateReceiver mReceiver = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // register receiver setting update action
        mReceiver = new SettingUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(
                LogkitUtils.ACTION_SETTING_UPDATE);
        intentFilter.addAction(LogkitUtils.ACTION_ROOT_PATH_CHANGE);
        registerReceiver(mReceiver, intentFilter);
        sp = LogkitUtils.getSharedPreferences(this);
        LogkitUtils.LogE("onCreate .................... ");
    }

    // init log path dir
    private void initLogPath(String rootPath) {
        LogkitUtils.LogE(rootPath);
        // log path
        File path = new File(rootPath + LogkitUtils.LOGS_PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
        // config path
        if (getResources().getBoolean(R.bool.qxdm_log)) {
            path = new File(rootPath + LogkitUtils.CONFIG_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
        }else{
            path = new File(rootPath + LogkitUtils.CONFIG_PATH);
            if (path.exists()) {
                path.delete();
            }
        }
        // adb log path
        path = new File(rootPath + LogkitUtils.ADB_LOG_PATH);
        if (!path.exists()) {
            path.mkdirs();
        }
        // qxdm log path
        if (getResources().getBoolean(R.bool.qxdm_log)) {
            path = new File(rootPath + LogkitUtils.QXDM_LOG_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
            // copy config
            path = new File(LogkitUtils.getQXDMConfigPath());
            if(!path.exists()){
                copyConfig();
            }
        }else{
            path = new File(rootPath + LogkitUtils.QXDM_LOG_PATH);
            if (path.exists()) {
                path.delete();
            }
        }
        // ip log path
        if (getResources().getBoolean(R.bool.ip_log)) {
            path = new File(rootPath + LogkitUtils.IP_LOG_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
        }else{
            path = new File(rootPath + LogkitUtils.IP_LOG_PATH);
            if (path.exists()) {
                path.delete();
            }
        }
        // dmesg log path
        if (getResources().getBoolean(R.bool.dmesg_tag)) {
            path = new File(rootPath + LogkitUtils.DMESG_LOG_PATH);
            if (!path.exists()) {
                path.mkdirs();
            }
        }else{
            path = new File(rootPath + LogkitUtils.DMESG_LOG_PATH);
            if (path.exists()) {
                path.delete();
            }
        }
        // dump state log path
        path = new File(rootPath + LogkitUtils.DUMP_STATE_LOG_PATH);
        if (!path.exists() && CommandBase.DUMP_STATE_LOG_ENABLE) {
            path.mkdirs();
        }
        // dump sys log path
        path = new File(rootPath + LogkitUtils.DUMP_SYS_LOG_PATH);
        if (!path.exists() && CommandBase.DUMP_SYS_LOG_ENABLE) {
            path.mkdirs();
        }
        // crash log path
        path = new File(rootPath + LogkitUtils.CRASH_LOG_PATH);
        if (!path.exists() && CommandBase.CRASH_LOG_ENABLE) {
            path.mkdirs();
        }
    }

    // copy the qxdm config to sd
    private void copyConfig() {
        // copy qxdm config file
        InputStream is = getResources()
                .openRawResource(R.raw.default_diag_mask);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(LogkitUtils.getQXDMConfigPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        byte[] buffer = new byte[4096];
        int count = 0;
        try {
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fos)
                    fos.close();
                if (null != is)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // destory all thread
        destoryThread();
        unregisterReceiver(mReceiver);
        LogkitUtils.LOGKIT_SREVICE_IS_RUNNING = false;
        LogkitUtils.LogE("ondestory ................ ");
    }

    // destory all thread
    private void destoryThread() {
        stopLogcat(mAdbMainThread);
        mAdbMainThread = null;
        stopLogcat(mAdbRadioThread);
        mAdbRadioThread = null;
        stopLogcat(mAdbSystemThread);
        mAdbSystemThread = null;
        stopLogcat(mAdbEventThread);
        mAdbEventThread = null;
        stopLogcat(mQXDMThread);
        mQXDMThread = null;
        stopLogcat(mIPThread);
        mIPThread = null;
        stopLogcat(mDesgThread);
        mDesgThread = null;
        stopLogcat(mDumpStateThread);
        mDumpStateThread = null;
        stopLogcat(mDumpSysThread);
        mDumpSysThread = null;
    }

    // stop the logcat thread
    private void stopLogcat(CommandExecThread thread) {
        if (null != thread) {
            thread.stopLogcatProcess();
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // for auto restart when service killed by system
        flags = START_REDELIVER_INTENT;
        /*
         * Environment.MEDIA_MOUNTED.equals(Environment
         * .getExternalStorageState())
         */
        if (null != intent) {
            String sdPath = intent
                    .getStringExtra(LogkitUtils.ACTION_KEY_SD_PATH);
            if(null != sdPath) {
                LogkitUtils.LogE("sdpath=" + sdPath);
                LogkitUtils.AVAILABLE_SD_PATHS.add(sdPath);
                String rootPath = LogkitUtils.getSDPath(this);
                LogkitUtils.LogE("init root=" + rootPath);
                if (TextUtils.isEmpty(rootPath)) {
                    // default sd path is external path
                    rootPath = LogkitUtils.DEFAULT_SD_PATH;
                    LogkitUtils.setSDPath(this, rootPath);
                    if (!rootPath.equals(sdPath)) {
                        return super.onStartCommand(intent, flags, startId);
                    }
                } else if (rootPath.equals(sdPath)) {
                    rootPath = sdPath;
                } else {
                    return super.onStartCommand(intent, flags, startId);
                }
                LogkitUtils.ROOT_PATH = rootPath;
            } else {
                if(!LogkitUtils.checkSDMounted(this)){
                    return super.onStartCommand(intent, flags, startId);
                }
            }
        } else {
            if(!LogkitUtils.checkSDMounted(this)){
                return super.onStartCommand(intent, flags, startId);
            }
        }
        // init all preferences setting
        initSharedPreferences();
        // init log path
        initLogPath(LogkitUtils.ROOT_PATH);
        // update all thread for setting update
        updateLogThread();
        LogkitUtils.LOGKIT_SREVICE_IS_RUNNING = true;
        LogkitUtils.LogE("onStartCommand ......................... ");
        return super.onStartCommand(intent, flags, startId);
    }

    // init all shared preferences of all log setting
    private void initSharedPreferences() {
        if (!sp.contains(LogkitUtils.KEY_ADB_MAIN_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_ADB_MAIN_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
        if (!sp.contains(LogkitUtils.KEY_ADB_RADIO_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_ADB_RADIO_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
        if (!sp.contains(LogkitUtils.KEY_ADB_SYSTEM_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_ADB_SYSTEM_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
        if (!sp.contains(LogkitUtils.KEY_QXDM_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_QXDM_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
        if (!sp.contains(LogkitUtils.KEY_IP_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_IP_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
        if (!sp.contains(LogkitUtils.KEY_DMESG_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_DMESG_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
        if (!sp.contains(LogkitUtils.KEY_DUMP_STATE_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_DUMP_STATE_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
        if (!sp.contains(LogkitUtils.KEY_DUMP_SYS_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_DUMP_SYS_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
        if (!sp.contains(LogkitUtils.KEY_CRASH_TOGGLE)) {
            sp.edit().putBoolean(LogkitUtils.KEY_CRASH_TOGGLE,
                    LogkitUtils.LOGKIT_DEFAULT_ENABLE).commit();
        }
    }

    // update all log thread for setting change
    private void updateLogThread() {
        String datePath = simpleDateFormat.format(new Date());
        // main log
        if (sp.getBoolean(LogkitUtils.KEY_ADB_MAIN_TOGGLE, false)) {
            if (null == mAdbMainThread) {
                mAdbMainThread = new CommandExecThread(String.format(
                        CommandBase.ADB_MAIN_COMMAND, LogkitUtils.ROOT_PATH
                                + LogkitUtils.ADB_LOG_PATH + "main_log_"
                                + datePath + ".log"));
                mAdbMainThread.start();
            }
        } else {
            if (null != mAdbMainThread) {
                stopLogcat(mAdbMainThread);
                mAdbMainThread = null;
            }
        }
        // radio log
        if (sp.getBoolean(LogkitUtils.KEY_ADB_RADIO_TOGGLE, false)) {
            if (null == mAdbRadioThread) {
                mAdbRadioThread = new CommandExecThread(String.format(
                        CommandBase.ADB_RADIO_COMMAND, LogkitUtils.ROOT_PATH
                                + LogkitUtils.ADB_LOG_PATH + "radio_log_"
                                + datePath + ".log"));
                mAdbRadioThread.start();
            }
        } else {
            if (null != mAdbRadioThread) {
                stopLogcat(mAdbRadioThread);
                mAdbRadioThread = null;
            }
        }
        // system log
        if (sp.getBoolean(LogkitUtils.KEY_ADB_SYSTEM_TOGGLE, false)) {
            if (null == mAdbSystemThread) {
                mAdbSystemThread = new CommandExecThread(String.format(
                        CommandBase.ADB_SYSTEM_COMMAND, LogkitUtils.ROOT_PATH
                                + LogkitUtils.ADB_LOG_PATH + "system_log_"
                                + datePath + ".log"));
                mAdbSystemThread.start();
            }
        } else {
            if (null != mAdbSystemThread) {
                stopLogcat(mAdbSystemThread);
                mAdbSystemThread = null;
            }
        }
        // events log
        if (sp.getBoolean(LogkitUtils.KEY_ADB_EVENT_TOGGLE, false)) {
            if (null == mAdbEventThread) {
                mAdbEventThread = new CommandExecThread(String.format(
                        CommandBase.ADB_EVENTS_COMMAND, LogkitUtils.ROOT_PATH
                                + LogkitUtils.ADB_LOG_PATH + "events_log_"
                                + datePath + ".log"));
                mAdbEventThread.start();
            }
        } else {
            if (null != mAdbEventThread) {
                stopLogcat(mAdbEventThread);
                mAdbEventThread = null;
            }
        }
        // qxdm logs
        if (sp.getBoolean(LogkitUtils.KEY_QXDM_TOGGLE, false)) {
            if (null == mQXDMThread) {
                mQXDMThread = new CommandExecThread(String.format(
                        CommandBase.QXDM_COMMAND, LogkitUtils.ROOT_PATH
                                + LogkitUtils.QXDM_LOG_PATH,
                        LogkitUtils.getQXDMConfigPath()));
                mQXDMThread.start();
            }
        } else {
            if (null != mQXDMThread) {
                stopLogcat(mQXDMThread);
                mQXDMThread = null;
            }
        }
        // ip logs
        if (sp.getBoolean(LogkitUtils.KEY_IP_TOGGLE, false)) {
            if (null == mIPThread) {
                mIPThread = new CommandExecThread(String.format(
                        CommandBase.IP_COMMAND, LogkitUtils.ROOT_PATH
                                + LogkitUtils.IP_LOG_PATH + "ip_log_"
                                + datePath + ".cap"));
                mIPThread.start();
            }
        } else {
            if (null != mIPThread) {
                stopLogcat(mIPThread);
                mIPThread = null;
            }
        }
        // dmesg logs
        if (sp.getBoolean(LogkitUtils.KEY_DMESG_TOGGLE, false)) {
            if (null == mDesgThread) {
                SystemProperties.set("persist.sys.en_dmesg", "1");
                mDesgThread = new CommandExecThread(CommandBase.DMESG_COMMAND,
                        LogkitUtils.ROOT_PATH + LogkitUtils.DMESG_LOG_PATH
                                + "dmesg_log_" + datePath + ".log");
                mDesgThread.start();
            }
        } else {
            if (null != mDesgThread) {
                SystemProperties.set("persist.sys.en_dmesg", "0");
                stopLogcat(mDesgThread);
                mDesgThread = null;
            }
        }
        // dump state logs
        if (sp.getBoolean(LogkitUtils.KEY_DUMP_STATE_TOGGLE, false)) {
            if (null == mDumpStateThread) {
                mDumpStateThread = new CommandExecThread(
                        CommandBase.DUMP_STATE_COMMAND, LogkitUtils.ROOT_PATH
                                + LogkitUtils.DUMP_STATE_LOG_PATH
                                + "dumpstate_log_" + datePath + ".log");
                mDumpStateThread.start();
            }
        } else {
            if (null != mDumpStateThread) {
                stopLogcat(mDumpStateThread);
                mDumpStateThread = null;
            }
        }
        // dump sys logs
        if (sp.getBoolean(LogkitUtils.KEY_DUMP_SYS_TOGGLE, false)) {
            if (null == mDumpSysThread) {
                mDumpSysThread = new CommandExecThread(
                        CommandBase.DUMP_SYS_COMMAND, LogkitUtils.ROOT_PATH
                                + LogkitUtils.DUMP_SYS_LOG_PATH
                                + "dumpsys_log_" + datePath + ".log");
                mDumpSysThread.start();
            }
        } else {
            if (null != mDumpSysThread) {
                stopLogcat(mDumpSysThread);
                mDumpSysThread = null;
            }
        }
    }

    // the receiver of setting change
    private class SettingUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // init log path
            initLogPath(LogkitUtils.ROOT_PATH);
            if (LogkitUtils.ACTION_SETTING_UPDATE.equals(intent.getAction())) {
                updateLogThread();
            } else if (LogkitUtils.ACTION_ROOT_PATH_CHANGE.equals(intent.getAction())) {
                destoryThread();
                // update all thread for setting update
                updateLogThread();
                Toast.makeText(getApplicationContext(),
                        R.string.sd_path_switch_success, Toast.LENGTH_LONG).show();
            }
        }

    }
}
