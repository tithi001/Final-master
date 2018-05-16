package com.simcoder.test;

import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.special.ResideMenu.ResideMenu;

import java.util.ArrayList;

public class HistoryFragment extends Fragment {
    private View parentView;
    private ResideMenu resideMenu;
    private DatabaseReference dref;
    private String userID;
    private ListView listview;
    private ArrayList<String> list=new ArrayList<>();
    private Button button;
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_history, container, false);
        resideMenu = ((MainActivity2)getActivity()).getResideMenu();
        HorizontalScrollView ignored_view = (HorizontalScrollView) parentView.findViewById(R.id.scroll1);
        resideMenu.addIgnoredView(ignored_view);
        userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        button=(Button)parentView.findViewById(R.id.clear_history);
        listview=(ListView)parentView.findViewById(R.id.history_list);
        dref= FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("History");
        if(dref!=null)
        {
            dref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            String temp=ds.getKey().toString()+"  "+ds.getValue().toString();
                            list.add(temp);
                        }
                        adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,list);
                        listview.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });}

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Clear all history entries?")
                        .setPositiveButton("Okay", new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface dialog, int id) {
                                try{dref.removeValue();}catch(Exception e){}
                                list.clear();
                                adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,list);
                                listview.setAdapter(adapter);
                            }})
                        .setNegativeButton("Cancel",new android.content.DialogInterface.OnClickListener() {
                            public void onClick(android.content.DialogInterface dialog, int id) {
                            }});
                AlertDialog alert = builder.create();
                alert.show();
            }});
        return parentView;
    }

    @Override
    public void onDestroy() {
        resideMenu.clearIgnoredViewList();
        super.onDestroy();
    }
}
