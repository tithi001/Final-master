package com.simcoder.test;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.special.ResideMenu.ResideMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PoliceContactsFragment extends Fragment implements com.google.android.gms.location.LocationListener {
    private View parentView;
    private DatabaseReference dref;
    private String userID;
    private Button Filter;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private ListView listview;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private GeoQuery geoQuery;
    private GeoFire geoFire;
    private int radius=5;
    private Boolean filter=false;
    private ResideMenu resideMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_police_contacts, container, false);
        resideMenu = ((MainActivity2)getActivity()).getResideMenu();
        HorizontalScrollView ignored_view = (HorizontalScrollView) parentView.findViewById(R.id.scroll2);
        resideMenu.addIgnoredView(ignored_view);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listview = (ListView) parentView.findViewById(R.id.policelistview);
        dref = FirebaseDatabase.getInstance().getReference().child("Police Contacts");
        adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,list);
        adapter.setNotifyOnChange(true);
        listview.setAdapter(adapter);
        final GoogleApiClient GAC=new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .build();
        GAC.connect();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        Filter = (Button) parentView.findViewById(R.id.filter);
        ValueLis();
        Filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!filter){
                    list.clear();adapter.notifyDataSetChanged();
                    filter=true; Filter.setText("All");radius=5;count=0;
                    geoFire = new GeoFire(dref);
                    mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;}
                    LocationServices.FusedLocationApi.requestLocationUpdates(GAC, mLocationRequest, PoliceContactsFragment.this);
                }
                else{
                    list.clear();adapter.notifyDataSetChanged();
                    filter=false; Filter.setText("Filter");
                    ValueLis();
                }
            }});
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String data=(String)listview.getItemAtPosition(position);
                String[] split=data.split("Contact: ");
                final String contactNumber=split[1].trim();
                String Location=split[0];
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(contactNumber)
                        .setPositiveButton("Call", new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface dialog, int id) {
                                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
                                Intent intent = new Intent(Intent.ACTION_CALL);
                                intent.setData(Uri.parse("tel:" + contactNumber));
                                startActivity(intent);}
                                else{android.widget.Toast.makeText(getActivity(), "Permission denied to call", android.widget.Toast.LENGTH_LONG).show();}
                            }})
                        .setNegativeButton("Cancel",new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface dialog, int id) {
                            }});
                AlertDialog alert = builder.create();
                alert.show();

                //Toast.makeText(getActivity(),contactNumber,Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(),Location,Toast.LENGTH_SHORT).show();
            }


        });
        return parentView;
    }
    private void ValueLis(){
        if(dref!=null) {
            dref=null;
            dref = FirebaseDatabase.getInstance().getReference().child("Police Contacts");
            dref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                        list.clear();adapter.notifyDataSetChanged();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            final String temp=ds.getKey().toString();
                            dref.child(temp).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                                        Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                                        if(map.get("contact")!=null){
                                            String temp2=temp+" Contact: "+map.get("contact").toString();
                                            if(!filter){list.add(temp2);Log.i("onLocaChang",temp2);
                                                Collections.sort(list);
                                            adapter.notifyDataSetChanged();}
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        //if(filter){adapter.notifyDataSetChanged();}
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });}
    }
    private int count=0;
    private void enList(final Location location){
        geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), radius);
        geoQuery.removeAllListeners(); list.clear();adapter.notifyDataSetChanged();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                final String temp=key; count++;
                dref.child(temp).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                            Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                            if(map.get("contact")!=null){
                                String temp2=temp+" Contact: "+map.get("contact").toString();
                                list.add(temp2);Log.i("onLocaChang",temp2);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });}

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,list);
                //listview.setAdapter(adapter);
   /*3,20*/             if (filter&&(count<2))
                {
                    radius++;
                    enList(location);Log.i("onLocaChang","radius++");
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.i("LocationFragmentgCR", "GeoQuery failed!");
            }
        });
        return;
    }
    @Override
    public void onLocationChanged(Location location) {
        Log.i("onLocaChang","blahblah");
        enList(location);
        //mLocationManager.removeUpdates(this);
    }
}
