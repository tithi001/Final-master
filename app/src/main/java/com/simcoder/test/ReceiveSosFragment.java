package com.simcoder.test;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReceiveSosFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,GoogleMap.OnMarkerClickListener , RoutingListener {

    private MapView mMapView;
    private GoogleMap googlemap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private Button mLogout, mRequest;
    private Boolean requestBool = false;
    private String UserID,UserName;
    private MainActivity2 parentActivity;
    private ResideMenu resideMenu;
    private View parentView;
    private DatabaseReference CurrLocDB,UserDB,SosDB,TempDB;
    private View layout;
    private ArrayList<Marker> senderMarkers;
    private int markcount;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_receive_sos, container, false);
        polylines = new ArrayList<>();
        senderMarkers = new ArrayList<>();
        UserName="" ;
        UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserDB=FirebaseDatabase.getInstance().getReference().child("Users").child(UserID);
        SosDB=UserDB.child("SOS");
        TempDB=UserDB.child("Temp");
        parentActivity = (MainActivity2) getActivity();
        resideMenu = parentActivity.getResideMenu();
        mRequest = (Button) parentView.findViewById(R.id.sos2);
        mMapView = (MapView) parentView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        CurrLocDB =FirebaseDatabase.getInstance().getReference().child("Users").child(UserID).child("l");
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
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
                    googlemap.setOnMarkerClickListener(ReceiveSosFragment.this);
                    LatLng sydney = new LatLng(-34, 151);
                    googlemap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                    googlemap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
        if(UserDB.child("Confirmation")!=null){
            UserDB.child("Confirmation").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()&&dataSnapshot.hasChildren()){
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            final String senderID = ds.getKey();
                            if(ds.getValue().equals("received"))
                            {final DatabaseReference senderDB=FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
                            UserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                    if(map.get("name")!=null){
                                        UserName=map.get("name").toString();
                                        Log.i("testingtesting",UserName);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            senderDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                        String name;
                                        if(map.get("name")!=null){
                                            name=map.get("name").toString();
                                        }
                                        else{name="";}
                                        Log.i("testingtesting",name);
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    final String x=name;
                                    builder.setMessage("Did you help "+name +" ID : "+senderID)
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                                                public void onClick(android.content.DialogInterface dialog, int id) {
                                                    UserDB.child("Confirmation").child(senderID).removeValue();
                                                    UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Helped "+x+" ID: "+senderID);
                                                    FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Helped "+x +", ID: "+senderID);
                                                    senderDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Was Helped By "+UserName+" ID: "+UserID);
                                                    FirebaseDatabase.getInstance().getReference().child("History").child(senderID).child(""+Calendar.getInstance().getTime()).setValue("Was Helped By "+UserName +", ID: "+UserID);
                                                }})
                                            .setNegativeButton("No",new android.content.DialogInterface.OnClickListener() {
                                                public void onClick(android.content.DialogInterface dialog, int id) {
                                                    UserDB.child("Confirmation").child(senderID).removeValue();
                                                }});
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }}

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
        CheckNoti();
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (requestBool){


                }else{
                }
            }
        });

        return parentView;
    }

    private void CheckNoti(){
        try{
            if(TempDB!=null){TempDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            final String senderID = ds.getKey();

                                DatabaseReference senderDB=FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
                                int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
                                int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

                                LinearLayout viewGroup = (LinearLayout) getActivity().findViewById(R.id.ll);
                                LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                layout = layoutInflater.inflate(R.layout.popup2, viewGroup);

                                final PopupWindow popup2 = new PopupWindow(getActivity());
                                popup2.setContentView(layout);
                                popup2.setWidth(popupWidth);
                                popup2.setHeight(popupHeight);
                                popup2.showAtLocation(layout, Gravity.CENTER, 0,0);
                                senderDB.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                Button accept = (Button) layout.findViewById(R.id.cancel2);
                                accept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        popup2.dismiss();
                                        TempDB.child(senderID).removeValue();
                                        SosDB.child(senderID).setValue(true);
                                        DatabaseReference senderDB= FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
                                        senderDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                                    if(map.get("name")!=null){
                                                        final String x=map.get("name").toString();
                                                        UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Accepted SOS from "+x +" ID: "+senderID);
                                                        FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Accepted SOS from "+x +", ID: "+senderID);
                                                    }}}

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });
                                Button ignore = (Button) layout.findViewById(R.id.delete2);
                                ignore.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        popup2.dismiss();
                                        TempDB.child(senderID).removeValue();
                                        try{
                                            FirebaseDatabase.getInstance().getReference().child("Users").child(senderID).child("Receivers").child(UserID).removeValue();
                                        }catch (Exception e){}
                                        DatabaseReference senderDB= FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
                                        senderDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                                    if(map.get("name")!=null){
                                                        final String x=map.get("name").toString();
                                                        UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Rejected SOS from "+x +" ID: "+senderID);
                                                        FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Rejected SOS from "+x +", ID: "+senderID);
                                                    }}}

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                });
                            }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });return;}
        }catch(Exception e){CheckNoti();return;}
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
        Log.i("ReceiveSosFrag", "onLocationChanged");
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        DatabaseReference lat=CurrLocDB.child("0"); lat.setValue(location.getLatitude());
        DatabaseReference lng=CurrLocDB.child("1"); lng.setValue(location.getLongitude());
        if(SosDB!=null){googlemap.clear();
            final ArrayList<Marker> markers=new ArrayList<>();
            SosDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        final String senderID = ds.getKey();
                        DatabaseReference senderLocDB= FirebaseDatabase.getInstance().getReference().child("Users").child(senderID).child("l");
                        senderLocDB.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                markers.add(googlemap.addMarker(new MarkerOptions().position(tempLL).title(senderID).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup))));
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
        mLocationRequest.setInterval(5*1000);
        mLocationRequest.setFastestInterval(5*1000);
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
        final String senderID= marker.getTitle();
        final DatabaseReference senderDB= FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
        senderDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    String name=""; final String[] Sen_phone= new String[1];
                    if(map.get("name")!=null){
                        name=map.get("name").toString();}
                    if(map.get("phone number")!=null){
                        Sen_phone[0]=map.get("phone number").toString();
                    }
                        final String x=name;
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("Sender : "+x)
                                .setPositiveButton("Stop Pursuit?", new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(android.content.DialogInterface dialog, int id) {
                                        UserDB.child("History").child(""+Calendar.getInstance().getTime()).setValue("Stopped pursuing "+x +" ID: "+senderID);
                                        FirebaseDatabase.getInstance().getReference().child("History").child(UserID).child(""+Calendar.getInstance().getTime()).setValue("Stopped pursuing "+x +", ID: "+senderID);
                                        try{SosDB.child(senderID).removeValue();}
                                        catch(Exception e){}
                                        try{
                                            FirebaseDatabase.getInstance().getReference().child("Users").child(senderID).child("Receivers").child(UserID).removeValue();
                                        }catch (Exception e){}
                                    }})
                                .setNegativeButton("Help Done?",new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(android.content.DialogInterface dialog, int id) {
                                        senderDB.child("Confirmation").child(UserID).setValue("sent");
                                        try{SosDB.child(senderID).removeValue();}
                                        catch(Exception e){}
                                        try{
                                            FirebaseDatabase.getInstance().getReference().child("Users").child(senderID).child("Receivers").child(UserID).removeValue();
                                        }catch (Exception e){}
                                    }})
                                .setNeutralButton("Call",new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(android.content.DialogInterface dialog, int id) {
                                        if(!Sen_phone[0].equals(null)){
                                            Intent intent = new Intent(Intent.ACTION_CALL);
                                            intent.setData(Uri.parse("tel:" + Sen_phone[0]));
                                            if(ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
                                                startActivity(intent);
                                            }
                                            else{Toast.makeText(getActivity(), "Permission denied to call",Toast.LENGTH_LONG).show();}
                                        }else{Toast.makeText(getActivity(), "Receiver's phone number not provided", Toast.LENGTH_LONG).show();}
                                    }});
                        AlertDialog alert = builder.create();
                        alert.show();
                    }}

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                        googlemap.setOnMarkerClickListener(ReceiveSosFragment.this);
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
        /*if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }*/

        polylines = new ArrayList<>();
        //add route(s) to the map.
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

    /*@Override
    public void onStop() {

        super.onStop();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        String userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userID);
    }*/
}
