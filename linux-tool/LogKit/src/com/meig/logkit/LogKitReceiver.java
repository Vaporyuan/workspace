package com.meig.logkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.android.internal.telephony.TelephonyIntents;

public class LogKitReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogkitUtils.LogE(intent.getAction());
        if (LogkitUtils.ACTION_MEDIA_MOUNTED.equals(intent.getAction())
                && LogkitUtils.isStartService(context)) {
            // when media mounted and run logcat service
            Intent it = new Intent(context, LogKitService.class);
            it.putExtra(LogkitUtils.ACTION_KEY_SD_PATH, intent.getData().getPath());
            context.startService(it);
        } else if (LogkitUtils.SECRET_CODE_ACTION.equals(intent.getAction())) {
            // according secret code and goto settings
            Uri uri = intent.getData();
            if ("android_secret_code".equals(uri.getScheme())) {
                if ("3333".equals(uri.getHost())) {
                    Intent it = new Intent(context, LogkitSetings.class);
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(it);
                }
            }
        } else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && LogkitUtils.isStartService(context)
                    && !LogkitUtils.LOGKIT_SREVICE_IS_RUNNING) {
            context.startService(new Intent(context, LogKitService.class));
        } else if(LogkitUtils.ACTION_SERVICE_STOP.equals(intent.getAction())
                && !LogkitUtils.isStartService(context)
                    && LogkitUtils.LOGKIT_SREVICE_IS_RUNNING){
            context.stopService(new Intent(context, LogKitService.class));
        }else if(LogkitUtils.ACTION_SERVICE_START.equals(intent.getAction())
                && LogkitUtils.isStartService(context)
                    && !LogkitUtils.LOGKIT_SREVICE_IS_RUNNING){
            context.startService(new Intent(context, LogKitService.class));
        }
    }

}
