package com.simcoder.test;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener{

    private ResideMenu resideMenu;
    private Context mContext;
    private ResideMenuItem itemHome,itemPolice,itemPhrase,itemHistory,itemLogout;
    private ResideMenuItem itemProfile;
    private ResideMenuItem itemLocation;
    private ResideMenuItem itemContacts;
    private Intent ReceiverIntent;
    private ReceiveSOS ReceiverService;
    final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private int choice;
    private Boolean rationale=false;
    private Bundle bundle;
    private String userID;
    private DatabaseReference value;
    public static String trigger_phrase;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        mContext = this; bundle=getIntent().getExtras();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        value =FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("trigger_phrase");
        String s;
        setUpMenu();
        if(bundle!=null){
            choice= bundle.getInt("frag");
            if(choice==1){changeFragment(new ReceiveSosFragment());}
            else if(choice==2){resideMenu.clearIgnoredViewList();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fragment, new HomeFragment(), "fragment")
                        .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }
        } else if( bundle == null ){LocationFragment fragment = new LocationFragment();
            Bundle arguments = new Bundle();
            arguments.putInt( "choice" , 1);
            fragment.setArguments(arguments);
            resideMenu.clearIgnoredViewList();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment, fragment, "fragment")
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();}
        if(value!=null){
            value.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()&&dataSnapshot.getValue()!=null){
                        trigger_phrase=(dataSnapshot.getValue().toString());ReceiverService = new ReceiveSOS(trigger_phrase);
                        doStuff();
                    }
                    else{trigger_phrase="strawberry horse";ReceiverService = new ReceiveSOS(trigger_phrase);
                         doStuff();}
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else{trigger_phrase="strawberry horse"; ReceiverService = new ReceiveSOS(trigger_phrase);
        doStuff();}
    }
    private void doStuff(){
        ReceiverIntent = new Intent(this, ReceiverService.getClass());
        if ((ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED)
                ||(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED)) {
            if (!rationale) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("We need you to grant these permissions. Without them most of our functionality won't work.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface dialog, int id) {
                                rationale=true; doStuff();return;
                            }});
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_SMS,
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.CALL_PHONE,
                                Manifest.permission.INTERNET,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.MODIFY_AUDIO_SETTINGS}, REQUEST_ACCESS_FINE_LOCATION);
            }
        }
        else{if (!isMyServiceRunning(ReceiverService.getClass())) {
            startService(ReceiverIntent);}
            checkGPS();
        }
    }
    private void checkGPS(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please turn your location on so that our application can work properly.")
                    .setCancelable(false)
                    .setPositiveButton("Okay", new android.content.DialogInterface.OnClickListener() {
                        public void onClick(android.content.DialogInterface dialog, int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }})
                    .setNegativeButton("No",new android.content.DialogInterface.OnClickListener() {
                        public void onClick(android.content.DialogInterface dialog, int id) {

                        }});
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0) {
                    Boolean accepted=true;
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            accepted=false; break;
                        }
                    }
                    if(accepted){if (!isMyServiceRunning(ReceiverService.getClass())) {
                        startService(ReceiverIntent);} checkGPS();}
                    else{Toast.makeText(getApplicationContext(), "Some permissions were denied so some functionality may not work", Toast.LENGTH_SHORT).show();}

                }
                else {
                    Toast.makeText(getApplicationContext(), "Permissions denied. Most of our functionality won't work", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip.
        resideMenu.setScaleValue(0.4f);

        itemHome     = new ResideMenuItem(this, R.drawable.icon_home,     "Home");
        itemProfile  = new ResideMenuItem(this, R.drawable.profile,  "Profile");
        itemLocation = new ResideMenuItem(this, R.drawable.location,      "Location");
        itemContacts = new ResideMenuItem(this, R.drawable.contacts, "Special Contacts");
        itemPolice = new ResideMenuItem(this, R.drawable.police, "Police Contacts");
        itemPhrase = new ResideMenuItem(this, R.drawable.micicon, "Trigger Phrase");
        itemHistory = new ResideMenuItem(this, R.drawable.history, "History");
        itemLogout = new ResideMenuItem(this, R.drawable.logout, "Logout");
        itemHome.setOnClickListener(this);
        itemProfile.setOnClickListener(this);
        itemLocation.setOnClickListener(this);
        itemContacts.setOnClickListener(this);
        itemPolice.setOnClickListener(this);
        itemPhrase.setOnClickListener(this);
        itemHistory.setOnClickListener(this);
        itemLogout.setOnClickListener(this);

        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemProfile, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemPhrase, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemHistory, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemLocation, ResideMenu.DIRECTION_RIGHT);
        resideMenu.addMenuItem(itemContacts, ResideMenu.DIRECTION_RIGHT);
        resideMenu.addMenuItem(itemPolice, ResideMenu.DIRECTION_RIGHT);
        resideMenu.addMenuItem(itemLogout, ResideMenu.DIRECTION_RIGHT);
        // You can disable a direction by setting ->
        // resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
        findViewById(R.id.title_bar_right_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_RIGHT);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {

        if (view == itemHome){
            changeFragment(new HomeFragment());
        }else if (view == itemProfile){
            changeFragment(new ProfileFragment());
        }else if (view == itemLocation){
            LocationFragment fragment = new LocationFragment();
            Bundle arguments = new Bundle();
            arguments.putInt( "choice" , 2);
            fragment.setArguments(arguments);
            resideMenu.clearIgnoredViewList();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment, fragment, "fragment")
                    .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(null)
                    .commit();
        }else if (view == itemContacts){
            changeFragment(new SpecialContacts());
        }else if (view == itemPolice){
            changeFragment(new PoliceContactsFragment());
        }else if (view == itemPhrase){
            changeFragment(new TriggerPhraseFragment());
        }else if (view == itemHistory){
            changeFragment(new HistoryFragment());
        }else if (view == itemLogout){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Logout or Exit?")
                    .setCancelable(true)
                    .setPositiveButton("Logout", new android.content.DialogInterface.OnClickListener() {
                        public void onClick(android.content.DialogInterface dialog, int id) {
                            if(ReceiverIntent!=null){ReceiveSOS.Logout=true; stopService(ReceiverIntent);}
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                        }})
                    .setNegativeButton("Exit",new android.content.DialogInterface.OnClickListener() {
                        public void onClick(android.content.DialogInterface dialog, int id) {
                            finish();
                        }});
            AlertDialog alert = builder.create();
            alert.show();
        }

        resideMenu.closeMenu();
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
        }

        @Override
        public void closeMenu() {
        }
    };

    private void changeFragment(Fragment targetFragment){
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
        //return;
    }

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu(){
        return resideMenu;
    }
    @Override
    protected void onDestroy() {
        if(ReceiverIntent!=null)stopService(ReceiverIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();
    }
}

