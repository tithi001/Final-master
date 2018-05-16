package com.simcoder.test;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.special.ResideMenu.ResideMenu;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LocationFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,GoogleMap.OnMarkerClickListener,RoutingListener {

    private MapView mMapView;
    private GoogleMap googlemap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation,CurrLoc;
    private Marker mCurrLocationMarker;
    private Button mRequest;
    public static Boolean requestBool = false;
    private String UserID,UserName;
    private MainActivity2 parentActivity;
    private ResideMenu resideMenu;
    private View parentView,layout;
    private DatabaseReference SC, CurrLocDB,UserDB,RecDB;
    private DatabaseReference dbr[];
    private TelephonyManager mTM;
    private int num,count,count2=0;
    private Timer timer;
    private TimerTask timerTask;
    private Boolean ready=false, flag=true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_location, container, false);
        Bundle arguments = getArguments();
        int choice= arguments.getInt("choice");
        UserName="";
        UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserDB=FirebaseDatabase.getInstance().getReference().child("Users").child(UserID);
        RecDB=UserDB.child("Receivers");
        parentActivity = (MainActivity2) getActivity();
        resideMenu = parentActivity.getResideMenu();
        //destinationLatLng = new LatLng(0.0,0.0);
        mRequest = (Button) parentView.findViewById(R.id.sos2);
        mMapView = (MapView) parentView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        CurrLocDB =FirebaseDatabase.getInstance().getReference().child("Users").child(UserID).child("l");
        mMapView.onResume(); // needed to get the map to display immediately
        CurrLoc=new Location("");
        final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(UserDB.child("Confirmation")!=null){
            UserDB.child("Confirmation").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()&&dataSnapshot.hasChildren()){
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            final String senderID = ds.getKey();
                            if(ds.getValue().equals("sent"))
                            {final DatabaseReference senderDB=FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
                                UserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                        if(map.get("name")!=null){
                                            UserName=map.get("name").toString();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                senderDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                        String name;
                                        if(map.get("name")!=null){
                                            name=map.get("name").toString();
                                        }
                                        else{name="";}
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        final String x=name;
                                        builder.setMessage("Did receive help from "+name +" ID : "+senderID+" ?")
                                                .setCancelable(false)
                                                .setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                                                    public void onClick(android.content.DialogInterface dialog, int id) {
                                                        UserDB.child("Confirmation").child(senderID).removeValue();
                                                        UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Was Helped By "+x+" ID: "+senderID);
                                                        FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Was Helped By "+x +", ID: "+senderID);
                                                        senderDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Helped "+UserName+" ID: "+UserID);
                                                        FirebaseDatabase.getInstance().getReference().child("History").child(senderID).child(""+Calendar.getInstance().getTime()).setValue("Helped "+UserName +", ID: "+UserID);
                                                    }})
                                                .setNegativeButton("No",new android.content.DialogInterface.OnClickListener() {
                                                    public void onClick(android.content.DialogInterface dialog, int id) {
                                                        UserDB.child("Confirmation").child(senderID).removeValue();
                                                    }});
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });}
                        }}
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googlemap = mMap;
                if ((ActivityCompat.checkSelfPermission(getActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                    ||(ActivityCompat.checkSelfPermission(getActivity(),android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_FROM_LOC_FRAG_LOC);
                }
                else{
                    googlemap.setMyLocationEnabled(true);
                    googlemap.setOnMarkerClickListener(LocationFragment.this);
                    LatLng sydney = new LatLng(-34, 151);
                    googlemap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                    googlemap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requestBool){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Did you receive help?")
                            .setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                                public void onClick(android.content.DialogInterface dialog, int id) {
                                    final ArrayList<String> receivers=new ArrayList<>();
                                    final ArrayList<String> receiverIDs=new ArrayList<>();
                                    if(RecDB!=null){
                                        RecDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                                        final String receiverID = ds.getKey(); receiverIDs.add(receiverID);
                                                        DatabaseReference R=FirebaseDatabase.getInstance().getReference().child("Users").child(receiverID);
                                                        R.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                                                                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                                                    String name="";
                                                                    if(map.get("name")!=null){
                                                                        name=map.get("name").toString();}
                                                                    receivers.add(name);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }}}

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });}
                                    final ArrayAdapter<String> adp = new ArrayAdapter<String>(getActivity(),
                                            android.R.layout.simple_list_item_1, receivers);
                                    final Spinner sp = new Spinner(getActivity());
                                    sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    sp.setAdapter(adp);
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setMessage("Who helped you?")
                                            .setCancelable(false)
                                            .setView(sp)
                                            .setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                                                public void onClick(android.content.DialogInterface dialog, int id) {
                                                    String rid= receiverIDs.get(sp.getSelectedItemPosition());
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(rid).child("Confirmation").child(UserID).setValue("received");
                                                }})
                                            .setNegativeButton("Cancel",new android.content.DialogInterface.OnClickListener() {
                                                public void onClick(android.content.DialogInterface dialog, int id) {
                                                }});
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }})
                            .setNegativeButton("No",new android.content.DialogInterface.OnClickListener() {
                                public void onClick(android.content.DialogInterface dialog, int id) {
                                }});
                    AlertDialog alert = builder.create();
                    alert.show();
                    endRequest();
                }else{
                    fire();
                }
            }
        });
        if(choice==1) {//fire();
        Log.i("SOS","in choice 1");}
    return parentView;
    }
    private ArrayList <String> contacts=new ArrayList<>();
    private void fire(){
        requestBool = true; SmsReciever.requestBool=requestBool;
        UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Sent SOS");
        FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Sent SOS");
        SC=FirebaseDatabase.getInstance().getReference().child("Users").child(UserID).child("Special_Contacts");
        dbr=new DatabaseReference[4];
        mRequest.setText("Cancel SOS");
        ready=true;

    }
    private void getContacts(){
        SC.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dss) {
                contacts.clear();
                for(int index=0;index<4;index++) {
                    final int Temp_index=index;
                    if(dss.hasChild(""+(index+1))){
                        dbr[index] = (DatabaseReference) SC.child(""+(index+1));
                        dbr[index].addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                    if (map.get("contact_number") != null) {
                                        contacts.add(map.get("contact_number").toString());
                                        if(Temp_index==3){
                                            SmsReciever.contacts=contacts;
                                            emergencyCall();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                    else{
                        dbr[index]=null;
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void emergencyCall(){
        num=contacts.size();
        mTM = (TelephonyManager) getActivity().getSystemService(getActivity().getApplicationContext().TELEPHONY_SERVICE);
        for(int i=0;i<num;i++){
            String message ="https://www.google.com/maps/dir/?api=1&destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
            String phoneNo = contacts.get(i);
            if (!TextUtils.isEmpty(message) && !TextUtils.isEmpty(phoneNo)) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED)  {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                }
                else {android.widget.Toast.makeText(getActivity(), "Permission denied to send text", android.widget.Toast.LENGTH_LONG).show(); }
            }
        }
        count=0;
        //doPhoneCall();
        callPolice();
    }
    protected void doPhoneCall(){num=contacts.size();
        final Intent intent = new Intent(Intent.ACTION_CALL);
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE)== PackageManager.PERMISSION_GRANTED) {
            mTM.listen(new PhoneStateListener(){
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if(TelephonyManager.CALL_STATE_RINGING == state) {
                        Log.i("<<<<<<<State>>>>>>>", "RINGING, number: " + incomingNumber);
                        if(count>0 && count<num) timerReject(1);
                    }
                    else if(TelephonyManager.CALL_STATE_OFFHOOK == state) {
                        Log.i("<<<<<<<State>>>>>>>", "OFFHOOK");
                        if(requestBool&&count<num){
                            stopTimerTask(); timerReject(3);
                        }
                    }
                    if(TelephonyManager.CALL_STATE_IDLE == state) {
                        count++; stopTimerTask();
                        if(!requestBool||count>=num){
                            //finish();
                            //startActivity(getIntent());
                            return;
                        }
                        else if(requestBool&&count<num)
                        {intent.setData(Uri.parse("tel:" + contacts.get(count-1)));
                            Log.i("<<<<<<<State>>>>>>>", "IDLE");
                            //intent.putExtra("com.android.phone.force.slot", true);
                            //intent.putExtra("Cdma_Supp", true);
                            //for (String s : simSlotName)
                            //   intent.putExtra(s, 0); //0 or 1 according to sim.......
                            //works only for API >= 21
                            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            //  intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", (Parcelable) " here You have to get phone account handle list by using telecom manger for both sims:- using this method getCallCapablePhoneAccounts()");
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
                                UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Called "+contacts.get(count-1));
                                FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Called "+contacts.get(count-1));
                                startActivity(intent); stopTimerTask();timerReject(2);
                            }
                            else{android.widget.Toast.makeText(getActivity(), "Permission denied to call", android.widget.Toast.LENGTH_LONG).show();}
                            }
                    }
                }}, PhoneStateListener.LISTEN_CALL_STATE);
        }
        else{android.widget.Toast.makeText(getActivity(), "Permission denied to read phone state", android.widget.Toast.LENGTH_LONG).show();}
    }
    public void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    private void timerReject(int choice){
        switch (choice){
            case 1:
                rejectCall();
                return;
            case 2:
                timer= new Timer();
                timerTask= new TimerTask() {
                    @Override
                    public void run() {
                        rejectCall();
                    }
                };
                timer.schedule(timerTask,(20*1000));
                return;
            case 3:
                timer= new Timer();
                timerTask= new TimerTask() {
                    @Override
                    public void run() {
                        rejectCall();
                    }
                };
                timer.schedule(timerTask,(5*1000));
                return;
        }
    }
    private void rejectCall(){
        try {
            Log.i("<<<<<<<Timer>>>>>>>", "task");
            Class<?> classTelephony = Class.forName(mTM.getClass().getName());
            Method method = classTelephony.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            Object telephonyInterface = method.invoke(mTM);
            Class<?> telephonyInterfaceClass =Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");
            methodEndCall.invoke(telephonyInterface);
            //return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int radius = 1,radius2=1;
    private ArrayList<String> receiverFoundID = new ArrayList<>();
    GeoQuery geoQuery,geoQuery2;
    long helpCount=0;
    private void getClosestReceiver(){
        DatabaseReference receiverLocation = FirebaseDatabase.getInstance().getReference().child("Users");
        GeoFire geoFire = new GeoFire(receiverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!key.equals(UserID)){
                if (requestBool){
                    DatabaseReference receiverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
                    final String fkey=key;
                    Log.i("LocationFragmentgCR", "Trial "+"  key = "+fkey);
                    (receiverDatabase.child("Temp").child(UserID)).setValue(true);
                    (RecDB.child(fkey)).setValue(true);
                    Log.i("LocationFragmentgCR", "helpCount =   "+"  key = "+fkey);
            }
            }}

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (requestBool&&radius<101)
                {
                    RecDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                                helpCount=dataSnapshot.getChildrenCount();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    if(helpCount<5){radius++;
                    Log.i("LocationFragmentgCR", "helpCount = "+helpCount+"  radius = "+radius);
                    if(radius==101){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("No receiver found. Try again?")
                                .setCancelable(false)
                                .setPositiveButton("Okay", new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(android.content.DialogInterface dialog, int id) {
                                        endRequest();fire();
                                    }})
                                .setNegativeButton("End Request",new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(android.content.DialogInterface dialog, int id) {
                                        endRequest();
                                    }});
                        final AlertDialog alert = builder.create();
                        alert.show();
                        new Timer().schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        alert.dismiss();endRequest();fire();
                                    }
                                },30*1000
                        );
                    }
                    else if(radius<100) getClosestReceiver();}
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.i("LocationFragmentgCR", "GeoQuery failed!");
            }
        });
    }
    private void callPolice(){
        final DatabaseReference policeDB = FirebaseDatabase.getInstance().getReference().child("Police Contacts");
        GeoFire geoFire=new GeoFire(policeDB);
        geoQuery2 = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius2);
        geoQuery2.removeAllListeners();
        geoQuery2.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(requestBool&&count2<1)
                {final String temp=key; count2++;
                policeDB.child(temp).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                            Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                            if(map.get("contact")!=null){
                                String temp2=map.get("contact").toString().trim();
                                UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Called Police Station "+temp);
                                FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Called Police Station "+temp);
                                String message ="https://www.google.com/maps/dir/?api=1&destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude();
                                if (!TextUtils.isEmpty(temp2)) {
                                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS)== PackageManager.PERMISSION_GRANTED)  {
                                        SmsManager smsManager = SmsManager.getDefault();
                                        smsManager.sendTextMessage(temp2, null, message, null, null);
                                    }
                                    else {android.widget.Toast.makeText(getActivity(), "Permission denied to send text", android.widget.Toast.LENGTH_LONG).show(); }
                                contacts.add(temp2); doPhoneCall();
                                /*Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse("tel:" + temp2));
                                if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
                                    startActivity(intent); timerReject(2);
                                }
                                else{android.widget.Toast.makeText(getActivity(), "Permission denied to call", android.widget.Toast.LENGTH_LONG).show();}*/
                            }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });}}

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
   /*3,20*/             if (requestBool&&(count2<1))
                {
                    radius2++;
                    callPolice();Log.i("LocationFragmentgCR","radius++");
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.i("LocationFragmentgCR", "GeoQuery failed!");
            }
        });
        return;
    }
    private void endRequest(){
        requestBool = false; SmsReciever.requestBool=requestBool;ready=false;
        if(geoQuery!=null){geoQuery.removeAllListeners();}
        UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Ended SOS");
        FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Ended SOS");
        Log.i("LocationFragmenteR", "endRequest()");

        RecDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        final String receiverID = ds.getKey();
                        DatabaseReference receiverDB=FirebaseDatabase.getInstance().getReference().child("Users").child(receiverID);
                        try{
                            receiverDB.child("Temp").child(receiverID).removeValue();
                        }catch(Exception e){
                            Log.i("LocationFragmenteR", e.getMessage());
                        }
                        try{
                            receiverDB.child("SOS").child(receiverID).removeValue();
                        }catch(Exception e){
                            Log.i("LocationFragmenteR", e.getMessage());
                        }
                    RecDB.child(receiverID).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        radius2=5;count2=0;
        radius = 1; helpCount=0;mRequest.setText("SOS");googlemap.clear(); return;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (googlemap == null) {
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googlemap=googleMap;
        googlemap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googlemap.setOnMarkerClickListener(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (android.support.v4.content.ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                googlemap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            googlemap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int REQUEST_FROM_LOC_FRAG_LOC = 3;

    private void checkLocationPermission() {
        if (android.support.v4.content.ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    private CameraPosition cameraPosition;
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location; SmsReciever.mLastLocation=mLastLocation;
        if(requestBool&&ready){
            getClosestReceiver(); getContacts(); ready=false;
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        DatabaseReference lat=CurrLocDB.child("0"); lat.setValue(location.getLatitude());
        DatabaseReference lng=CurrLocDB.child("1"); lng.setValue(location.getLongitude());
        if(RecDB!=null){googlemap.clear();
            final ArrayList<Marker> markers=new ArrayList<>();
            RecDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            final String receiverID = ds.getKey();
                            DatabaseReference receiverLocDB= FirebaseDatabase.getInstance().getReference().child("Users").child(receiverID).child("l");
                            receiverLocDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                        List<Object> map = (List<Object>) dataSnapshot.getValue();
                                        double locationLat = 0;
                                        double locationLng = 0;
                                        if(map.get(0) != null){
                                            locationLat = Double.parseDouble(map.get(0).toString());
                                        }
                                        if(map.get(1) != null){
                                            locationLng = Double.parseDouble(map.get(1).toString());
                                        }
                                        LatLng tempLL = new LatLng(locationLat,locationLng);
                                        markers.add(googlemap.addMarker(new MarkerOptions().position(tempLL).title(receiverID).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup))));
                                        if(markers!=null)(markers.get(markers.size()-1)).setPosition(tempLL);
                                        Log.i("ReceiveSosFrag", "senderLocation");
                                        getRouteToMarker(tempLL);
                                    }}

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }}}

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });}
        if(cameraPosition==null){
            cameraPosition = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(15)
                        .build();}
        else{float temp_zoom=15;LatLng temp_latlng=latLng;
            try{temp_zoom=cameraPosition.zoom;}catch(Exception e){temp_zoom=15;}
            try{temp_latlng=cameraPosition.target;}catch(Exception e){temp_latlng=latLng;}
            cameraPosition = new CameraPosition.Builder()
                    .target(temp_latlng)
                    .zoom(temp_zoom)
                    .build();}
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        if(mCurrLocationMarker==null){mCurrLocationMarker = googlemap.addMarker(markerOptions);}
        else{mCurrLocationMarker.remove(); googlemap.clear();mCurrLocationMarker = googlemap.addMarker(markerOptions);}
        googlemap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10*1000);
        mLocationRequest.setFastestInterval(10*1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (android.support.v4.content.ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public boolean onMarkerClick(final Marker marker){

        String receiverID= marker.getTitle();
        DatabaseReference receiverDB= FirebaseDatabase.getInstance().getReference().child("Users").child(receiverID);
        int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        final String[] Rec_Phone= new String[1];
        LinearLayout viewGroup = (LinearLayout) getActivity().findViewById(R.id.ll);
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = layoutInflater.inflate(R.layout.popup2, viewGroup);
        final PopupWindow popup2 = new PopupWindow(getActivity());
        popup2.setContentView(layout);
        popup2.setWidth(popupWidth);
        popup2.setHeight(popupHeight);
        popup2.showAtLocation(layout, Gravity.CENTER, 0,0);
        ((TextView)layout.findViewById(R.id.textView6)).setText("Receiver Information");
        Button cancel = (Button) layout.findViewById(R.id.cancel2);
        cancel.setText("Cancel");
        Button call = (Button) layout.findViewById(R.id.delete2);
        call.setText("Call");
        receiverDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    String gender="", age="";
                    if(map.get("name")!=null){
                        ((TextView)layout.findViewById(R.id.name)).setText(map.get("name").toString());
                    }
                    if(map.get("gender")!=null) {
                        gender = map.get("gender").toString();
                        ((TextView)layout.findViewById(R.id.info)).setText(gender+" , "+age);
                    }
                    if(map.get("dateOfBirth")!=null){
                        String date=map.get("dateOfBirth").toString();
                        Calendar dob = Calendar.getInstance();
                        Calendar today = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                        try {
                            dob.setTime(sdf.parse(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        int ageYears = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

                        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                            ageYears--;
                        }

                        Integer ageInt = new Integer(ageYears);
                        age = ageInt.toString();
                        ((TextView)layout.findViewById(R.id.info)).setText(gender+" , "+age);
                    }
                    if(map.get("phone number")!=null){
                        Rec_Phone[0]=map.get("phone number").toString();
                    }
                    if(map.get("profileImageUrl")!=null){
                        String mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getActivity().getApplication()).load(mProfileImageUrl).into((ImageView) layout.findViewById(R.id.profileImage2));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup2.dismiss();
}
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup2.dismiss();
                if(!Rec_Phone[0].equals(null)){
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + Rec_Phone[0]));
                    if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
                        startActivity(intent);
                    }
                    else{Toast.makeText(getActivity(), "Permission denied to call",Toast.LENGTH_LONG).show();}
                }else{Toast.makeText(getActivity(), "Receiver's phone number not provided", Toast.LENGTH_LONG).show();}
            }
        });
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (android.support.v4.content.ContextCompat.checkSelfPermission(getActivity(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        googlemap.setMyLocationEnabled(true);
                    }

                } else {
                    android.widget.Toast.makeText(getActivity(), "permission denied", android.widget.Toast.LENGTH_LONG).show();
                }
                return;
            }
            case REQUEST_FROM_LOC_FRAG_LOC:{
                if (grantResults.length > 0) {
                    Boolean accepted=true;
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            accepted=false; break;
                        }
                    }
                    if(accepted){
                        googlemap.setMyLocationEnabled(true);
                        googlemap.setOnMarkerClickListener(LocationFragment.this);
                        LatLng sydney = new LatLng(-34, 151);
                        googlemap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                        googlemap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(), "Some permissions were denied so some functionality may not work", Toast.LENGTH_SHORT).show();}

                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Permissions denied. Most of our functionality won't work", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
    private void getRouteToMarker(LatLng destLatLng) {
        Log.i("ReceiveSosFrag", "getRouteToMarker1");
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), destLatLng)
                .build();
        Log.i("ReceiveSosFrag", "getRouteToMarker2");
        routing.execute();
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            android.widget.Toast.makeText(getActivity(), "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }else {
            android.widget.Toast.makeText(getActivity(), "Something went wrong, Try again", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.material_deep_teal_500};
    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        polylines = new ArrayList<>();
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;
            Log.i("ReceiveSosFrag", "onRoutingSuccess1");
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googlemap.addPolyline(polyOptions);
            Log.i("ReceiveSosFrag", "onRoutingSuccess2");
            polylines.add(polyline);
            Log.i("ReceiveSosFrag", "onRoutingSuccess3");
            android.widget.Toast.makeText(getActivity(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
        android.widget.Toast.makeText(getActivity(),"Routing canceled",android.widget.Toast.LENGTH_SHORT).show();
    }

}
