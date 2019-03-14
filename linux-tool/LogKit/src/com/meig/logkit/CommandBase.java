package com.meig.logkit;

/**
 * Base commands of log
 */

public class CommandBase {
    public static final String ADB_MAIN_COMMAND = "logcat -b main -v time -f %s";
    public static final String ADB_RADIO_COMMAND = "logcat -b radio -v time -f %s";
    public static final String ADB_SYSTEM_COMMAND = "logcat -b system -v time -f %s";
    public static final String ADB_EVENTS_COMMAND = "logcat -b events -v time -f %s";
    public static final String QXDM_COMMAND = "diag_mdlog -ce -s 100 -n 20 -o %s -f %s";
    public static final String IP_COMMAND = "tcpdump -i any -p -s 0 -w %s";
    public static final String DMESG_COMMAND = "dmesg";
    public static final String DUMP_STATE_COMMAND = "dumpstate";
    public static final String DUMP_SYS_COMMAND = "dumpsys";
    public static final String CRASH_COMMAND = "";

    public static boolean CRASH_LOG_ENABLE = false;
    public static boolean DUMP_STATE_LOG_ENABLE = false;
    public static boolean DUMP_SYS_LOG_ENABLE = false;
}
