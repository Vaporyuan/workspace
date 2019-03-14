package com.meig.logkit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.os.Environment;

import android.os.ServiceManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

/**
 * the Utils of logkit
 */
public class LogkitUtils {

	private static final String TAG = "MeigLogKit";

	private static final String SAVE_LOCATON_TAG = "save_location";

	public static final String LOG_PAHT_TAG = "log_path";

	public static final String LOGS_PATH = "/ULogs/logs/";
	public static final String CONFIG_PATH = "/ULogs/config/";

	public static final String ADB_LOG_PATH = LOGS_PATH + "adb/";
	public static final String QXDM_LOG_PATH = LOGS_PATH + "qxdm/";
	public static final String IP_LOG_PATH = LOGS_PATH + "ip/";
	public static final String DMESG_LOG_PATH = LOGS_PATH + "dmesg/";
	public static final String DUMP_STATE_LOG_PATH = LOGS_PATH + "dumpstate/";
	public static final String DUMP_SYS_LOG_PATH = LOGS_PATH + "dumpsys/";
	public static final String CRASH_LOG_PATH = LOGS_PATH + "crash/";

	public static final String KEY_ADB_MAIN_TOGGLE = "main_tag";
	public static final String KEY_ADB_RADIO_TOGGLE = "radio_tag";
	public static final String KEY_ADB_SYSTEM_TOGGLE = "system_tag";
	public static final String KEY_ADB_EVENT_TOGGLE = "event_tag";
	public static final String KEY_QXDM_TOGGLE = "qxdm_tag";
	public static final String KEY_IP_TOGGLE = "ip_tag";
	public static final String KEY_DMESG_TOGGLE = "dmesg_tag";
	public static final String KEY_DUMP_STATE_TOGGLE = "dumpstate_tag";
	public static final String KEY_DUMP_SYS_TOGGLE = "dumpsys_tag";
	public static final String KEY_CRASH_TOGGLE = "crash_tag";

	public static final String KEY_ADB_LOG = "adb_log";
	public static final String KEY_QXDM_LOG = "qxdm_log";
	public static final String KEY_IP_LOG = "ip_log";
	public static final String KEY_STATE_LOG = "state_log";
	public static final String KEY_CRASH_LOG = "crash_log";

	public static final String ACTION_SETTING_UPDATE = "meig.intent.action.SETTING_UPDATE";
	public static final String ACTION_ROOT_PATH_CHANGE = "meig.intent.action.ROOT_PAHT_CHANGE";
	public static final String ACTION_MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";
	public static final String ACTION_SERVICE_STOP = "android.intent.action.SERVICE_STOP";
	public static final String ACTION_SERVICE_START = "android.intent.action.SERVICE_START";
    public static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";

	public static final String ACTION_KEY_SD_PATH = "sd_path";

	public static final boolean LOGKIT_DEFAULT_ENABLE = SystemProperties
			.getBoolean("ro.meig.logkit.default_enable", false);

	public static String ROOT_PATH = null;

	public static boolean LOGKIT_SREVICE_IS_RUNNING = false;

	public static String QXDM_CONFIG_FILE = "default_diag_mask.cfg";

	// Default external sd mounted
	/* public static String DEFAULT_SD_PATH = "/storage/sdcard1"; */
	// Default internal sd mounted
	public static String DEFAULT_SD_PATH = "/storage/emulated/0";

	public static ArrayList<String> AVAILABLE_SD_PATHS = new ArrayList<String>();

	public static void LogE(String msg) {
		Log.e(TAG, msg + "");
	}

	public static void Logw(String msg) {
		Log.w(TAG, msg + "");
	}

	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences("com.meig.logkit_preferences",
				Activity.MODE_PRIVATE);
	}

	public static String getSDPath(Context context) {
		return getSharedPreferences(context).getString(SAVE_LOCATON_TAG, null);
	}

	public static void setSDPath(Context context, String sdPath) {
		getSharedPreferences(context).edit()
				.putString(SAVE_LOCATON_TAG, sdPath).commit();
	}

	public static String getQXDMConfigPath() {
		return ROOT_PATH + CONFIG_PATH + QXDM_CONFIG_FILE;
	}

	public static boolean checkSDMounted(Context context) {
		LogE("checkSDMounted");
		try {
			// external sd mounted
			AVAILABLE_SD_PATHS.clear();
			if (sdCardPhoneExist(context)) {
				AVAILABLE_SD_PATHS.add(getPhoneSDPath(context));
			} else {
				AVAILABLE_SD_PATHS.add("");
			}
			// internal sd mounted
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				AVAILABLE_SD_PATHS.add(Environment
						.getExternalStorageDirectory().getCanonicalPath());
			} else {
				AVAILABLE_SD_PATHS.add("");
			}
			String rootPath = getSDPath(context);
			String defaultSdPath = null;
			if (TextUtils.isEmpty(rootPath)) {
				defaultSdPath = DEFAULT_SD_PATH;
			} else {
				defaultSdPath = rootPath;
			}

			LogkitUtils.Logw(" PATH : --> " + AVAILABLE_SD_PATHS.toString());

			// checkout sd path
			for (String sd : AVAILABLE_SD_PATHS) {
				if (defaultSdPath.equals(sd)) {
					ROOT_PATH = sd;
					setSDPath(context, sd);
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			LogkitUtils.LogE("GET PATH ERROR .......");
			return false;
		}
	}

	/**
	 * If need to start servier
	 * 
	 * @return
	 */
	public static boolean isStartService(Context context) {
		SharedPreferences sp = getSharedPreferences(context);
		if (sp.getBoolean(LogkitUtils.KEY_ADB_MAIN_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_ADB_RADIO_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_ADB_SYSTEM_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_ADB_EVENT_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_QXDM_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_IP_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_DMESG_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_DUMP_STATE_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_DUMP_SYS_TOGGLE, false)
				|| sp.getBoolean(LogkitUtils.KEY_CRASH_TOGGLE, false)) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean sdCardPhoneExist(Context context) {
		boolean ret = false;
		StorageManager mStorageManager = (StorageManager) context
				.getSystemService(Context.STORAGE_SERVICE);
		if (mStorageManager.getVolumeState(getPhoneSDPath(context)).equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			ret = true;
		}
		return ret;
	}

	/**
	 * Get sd path
	 * 
	 * @param context
	 * @return
	 */
	public static String getPhoneSDPath(Context context) {
		String sd = null;
		StorageManager mStorageManager = (StorageManager) context
				.getSystemService(Context.STORAGE_SERVICE);
		StorageVolume[] volumes = mStorageManager.getVolumeList();
		if (volumes.length < 2) {// not have SD card
			sd = volumes[0].getPath();
		} else {
			for (int i = 0; i < volumes.length; i++) {
				if (volumes[i].isRemovable() /* && volumes[i].allowMassStorage() */
						&& volumes[i].getDescription(context).contains("SD")) {
					sd = volumes[i].getPath();
				}
			}
		}

		return sd;
	}
}
