package com.simcoder.test;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;


public class ReceiveSOS extends Service implements
        RecognitionListener , com.google.android.gms.location.LocationListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String KWS_SEARCH = "wakeup";
    private static Boolean destroy=true;
    public static Boolean Logout=false;
    private Context con;
    private String KEYPHRASE;
    private static SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    private String userID;
    private DatabaseReference UserDB,value,TempDB;
    private AsyncTask asyncTask;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    public ReceiveSOS(String s) { Logout=false;
        //KEYPHRASE=s;
    }
    public ReceiveSOS(){Logout=false;}
    public SpeechRecognizer getRec(){
        return recognizer;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        destroy=true; Logout=false;
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserDB=FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        TempDB=UserDB.child("Temp");
        value =UserDB.child("trigger_phrase");
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        KEYPHRASE= MainActivity2.trigger_phrase;
        buildGoogleApiClient();
        Log.i ("<<<<<<CAPTION>>>>>>", "Preparing the recognizer "+ MainActivity2.trigger_phrase);
        if(TempDB!=null){TempDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        final Boolean value = (Boolean) ds.getValue();
                        //DatabaseReference senderDB=FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
                        if(value){TempDB.child((String)ds.getKey()).setValue(false);
                            Intent intent1=new Intent(ReceiveSOS.this,MainActivity2.class);
                        intent1.putExtra("frag",1);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent=PendingIntent.getActivity(ReceiveSOS.this, 0, intent1, 0);
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(ReceiveSOS.this)
                                        .setSmallIcon(R.drawable.eye)
                                        .setContentTitle("Isitoq")
                                        .setContentText("You received "+dataSnapshot.getChildrenCount()+" SOS!")
                                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                                        .setLights(android.graphics.Color.RED, 3000, 3000)
                                        .setAutoCancel(true)
                                        .setContentIntent(pendingIntent);
                        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        mBuilder.setSound(alarmSound);
                        NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        // When you issue multiple notifications about the same type of event,
                        // it’s best practice for your app to try to update an existing notification
                        // with this new information, rather than immediately creating a new notification.
                        // If you want to update this notification at a later date, you need to assign it an ID.
                        // You can then use this ID whenever you issue a subsequent notification.
                        // If the previous notification is still visible, the system will update this existing notification,
                        // rather than create a new one. In this example, the notification’s ID is 001//

                        mNotificationManager.notify(001, mBuilder.build());
                        PowerManager.WakeLock screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
                        screenLock.acquire();
                        KeyguardManager manager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                        KeyguardManager.KeyguardLock lock = manager.newKeyguardLock("abc");
                        lock.disableKeyguard();}
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });}
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(ReceiveSOS.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.i ("<<<<<<CAPTION>>>>>>", "Failed to init recognizer");
                } else {
                    reset();
                }
            }
        }.execute();
        listener();
        //else{onDestroy();}
        return START_STICKY;
    }

    private void listener(){
        if(value!=null){
            value.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String temp="";
                    if(dataSnapshot.exists()&&dataSnapshot.getValue()!=null){
                        temp=(dataSnapshot.getValue().toString());
                        if(!temp.equals(MainActivity2.trigger_phrase)){Log.i ("<<<<<<CAPTION>>>>>>", "onDataChanged");
                            MainActivity2.trigger_phrase=temp;
                            KEYPHRASE=MainActivity2.trigger_phrase;
                            try{recognizer.stop();}catch(Exception e){}
                            try{recognizer.cancel();}catch(Exception e){}
                            try{recognizer.shutdown();}catch(Exception e){}
                            try{recognizer=null;}catch(Exception e){}
                            new AsyncTask<Void, Void, Exception>() {
                                @Override
                                protected Exception doInBackground(Void... params) {
                                    try {
                                        Assets assets = new Assets(ReceiveSOS.this);
                                        File assetDir = assets.syncAssets();
                                        setupRecognizer(assetDir);
                                    } catch (IOException e) {
                                        return e;
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Exception result) {
                                    if (result != null) {
                                        Log.i ("<<<<<<CAPTION>>>>>>", "Failed to init recognizer");
                                    } else {
                                        reset();
                                    }
                                }
                            }.execute();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if(destroy){try{recognizer.stop();}catch(Exception e){}
        try{recognizer.cancel();}catch(Exception e){}
        try{recognizer.shutdown();}catch(Exception e){}
        try{recognizer=null;}catch(Exception e){}
        value=null; stopSelf();
        Log.i("CAPTION", "ondestroy!");
        if(!Logout){Intent intent= new Intent(ReceiveSOS.this,ReceiveSOS.class);
        startService(intent);}
        super.onDestroy();}
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        destroy=false;
        Log.i ("<<<<<<CAPTION>>>>>>", "on Task removed");
        try{recognizer.stop();}catch(Exception e){}
        try{recognizer.cancel();}catch(Exception e){}
        try{recognizer.shutdown();}catch(Exception e){}
        try{recognizer=null;}catch(Exception e){}
        value=null;
        Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {}

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if(text.equals(KEYPHRASE)){
            Log.i ("<<<<<<CAPTION>>>>>>", text);
            /*Intent intent1=new Intent(ReceiveSOS.this,MainActivity2.class);
            intent1.putExtra("frag",2);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);*/
            }
        }
    }

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onEndOfSpeech() {
        reset();
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir)
                .setKeywordThreshold(1e-1f)
                .setBoolean("-allphone_ci", true)
                .getRecognizer();
        recognizer.addListener(this);

        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
    }

    @Override
    public void onError(Exception error) {
        Log.i ("<<<<<<CAPTION>>>>>>", error.getMessage());
        reset();
    }

    @Override
    public void onTimeout() {
        reset();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void reset()
    {
        try{
            recognizer.stop();
            recognizer.startListening(KWS_SEARCH);
        }catch(Exception e){}
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!LocationFragment.requestBool){UserDB.child("l").child("0").setValue(location.getLatitude());
        UserDB.child("l").child("1").setValue(location.getLongitude());}
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60*1000);
        mLocationRequest.setFastestInterval(60*1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (android.support.v4.content.ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"Connection Suspended",Toast.LENGTH_SHORT);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,connectionResult.getErrorMessage(),Toast.LENGTH_SHORT);
    }
}