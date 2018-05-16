package com.simcoder.test;

/**
 * Created by ismail on 7/12/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;


public class SmsReciever extends BroadcastReceiver {

    public static Boolean requestBool=false;
    public static ArrayList<String> contacts=new ArrayList<>();
    public static Location mLastLocation;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i ("<<<<<<CAPTION>>>>>>", "SMSReceiver");
        Bundle pudsBundle = intent.getExtras();
        Object[] pdus = (Object[]) pudsBundle.get("pdus");
        SmsMessage messages =SmsMessage.createFromPdu((byte[]) pdus[0]);
        Log.i("body",  messages.getMessageBody());
        String number=messages.getOriginatingAddress();
        String message1=messages.getMessageBody().trim();
        if(requestBool&&contacts.contains(number)&&message1.equals("Isitoq")){
            String message ="https://www.google.com/maps/dir/?api=1&destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED)  {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(number, null, message, null, null);
            }
            else {android.widget.Toast.makeText(context, "Permission denied to send text", android.widget.Toast.LENGTH_LONG).show(); }
        }
        /*Intent smsIntent=new Intent(context,SMS_Receive.class);
        smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        smsIntent.putExtra("MessageNumber", messages.getOriginatingAddress());
        smsIntent.putExtra("Message", messages.getMessageBody());
        context.startActivity(smsIntent);*/
        Toast.makeText(context, "SMS Received From :"+messages.getOriginatingAddress()+"\n"+ messages.getMessageBody(), Toast.LENGTH_LONG).show();
    }

}
