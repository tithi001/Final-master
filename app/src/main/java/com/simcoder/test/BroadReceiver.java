package com.simcoder.test;

import android.*;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.Context.POWER_SERVICE;

public class BroadReceiver extends BroadcastReceiver {
    private static int countPowerOff = 0;

    public BroadReceiver () {}
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i ("<<<<<<CAPTION>>>>>>", "BroadcastReceiver Before");
        if (intent.getAction().equals("android.intent.action.SCREEN_OFF"))
        {
            Log.e("In on receive", "In Method:  ACTION_SCREEN_OFF");
            countPowerOff++;
        }
        else if (intent.getAction().equals("android.intent.action.SCREEN_ON"))
        {
            Log.e("In on receive", "In Method:  ACTION_SCREEN_ON");
        }
        else if(intent.getAction().equals("android.intent.action.USER_PRESENT"))
        {
            Log.e("In on receive", "In Method:  ACTION_USER_PRESENT");
            if (countPowerOff > 2)
            {
                PowerManager.WakeLock screenLock = ((PowerManager)context.getSystemService(POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                screenLock.acquire();
                KeyguardManager manager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                KeyguardManager.KeyguardLock lock = manager.newKeyguardLock("abc");
                lock.disableKeyguard();
                countPowerOff=0;
                Toast.makeText(context, "MAIN ACTIVITY IS BEING CALLED ", Toast.LENGTH_LONG).show();
                Intent i = new Intent(context, MainActivity2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            }
        }
        else{
            context.startService(new Intent(context, ReceiveSOS.class));
            Log.i ("<<<<<<CAPTION>>>>>>", "BroadcastReceiver After");
        }
    }
}
