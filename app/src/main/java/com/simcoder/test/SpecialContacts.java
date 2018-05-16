package com.simcoder.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.widget.ListPopupWindow.WRAP_CONTENT;

public class SpecialContacts extends Fragment {

    //private GridLayout mainGrid;
    private LinearLayout mainGrid;
    private String name,number,imageUrl;
    private boolean edit=false;
    private String userID;
    private DatabaseReference SC;
    private DatabaseReference dbr[];
    private FloatingActionButton add;
    private Uri resultUri;
    private View parentView;
    private PopupWindow popup;
    private static final int CHOOSE_IMAGE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.special_contacts, container, false);
        mainGrid=(LinearLayout)parentView.findViewById(R.id.lalalala);
        //mainGrid = (GridLayout) parentView.findViewById(R.id.mainGrid);
        userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        SC=FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("Special_Contacts");
        dbr=new DatabaseReference[4];
        add=(FloatingActionButton) parentView.findViewById(R.id.floatingActionButton4);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(count<4){ count++;
                    dbr[count-1] = (DatabaseReference) SC.child(""+count);
                    dbr[count-1].setValue(true);
                    //edit=true;
                    CardView cardView = (CardView) mainGrid.getChildAt(count-1);
                    cardView.setVisibility(View.VISIBLE);
                    cardView.setEnabled(true);

                    ViewGroup vg1=(ViewGroup)cardView.getChildAt(0);
                    ViewGroup vg2=(ViewGroup)cardView.getChildAt(1);
                    ((ImageView)vg2.getChildAt(0)).setEnabled(true);
                    ((EditText)vg2.getChildAt(1)).setEnabled(true);
                    ((EditText)vg2.getChildAt(2)).setEnabled(true);
                    ((ImageButton) vg1.getChildAt(1)).setImageResource(R.drawable.ic_check_black_24dp);
                }
                else{
                    Toast.makeText(getActivity(), "Can't add more than four contacts!", Toast.LENGTH_SHORT).show();
                }*/
                int i;
                for (i = 0; i < mainGrid.getChildCount(); i++) {
                    CardView cardView = (CardView) mainGrid.getChildAt(i);
                    if(!cardView.isEnabled()&& dbr[i]==null){
                        dbr[i] = (DatabaseReference) SC.child(""+(i+1));
                        dbr[i].setValue(true);
                        cardView.setVisibility(View.VISIBLE);
                        cardView.setEnabled(true); return;}
                }
                if(i>3){Toast.makeText(getActivity(), "Can't add more than four contacts!", Toast.LENGTH_SHORT).show();}
            }});
        setSingleEvent(mainGrid);
        if(SC!=null){
        getContacts();}
        return parentView;
    }

    private void getContacts(){
        SC.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dss) {
                for(int index=0;index<4;index++) {

                    if(dss.hasChild(""+(index+1))){
                        dbr[index] = (DatabaseReference) SC.child(""+(index+1));
                        final CardView cardView = (CardView) mainGrid.getChildAt(index);
                        cardView.setVisibility(View.VISIBLE);
                        cardView.setEnabled(true);
                        final ViewGroup vg=(ViewGroup)cardView.getChildAt(1);
                        dbr[index].addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                    if (map.get("contact_name") != null) {
                                        EditText tv=(EditText)vg.getChildAt(0);
                                        tv.setText(map.get("contact_name").toString());
                                    }
                                    if (map.get("contact_number") != null) {
                                        EditText tv=(EditText)vg.getChildAt(1);
                                        tv.setText(map.get("contact_number").toString());
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
                        final CardView cardView = (CardView) mainGrid.getChildAt(index);
                        cardView.setVisibility(View.GONE);
                        cardView.setEnabled(false);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        }

    private void setToggleEvent(GridLayout mainGrid) {
        //Loop all child item of Main Grid
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            //You can see , all child item is CardView , so we just cast object to CardView
            final CardView cardView = (CardView) mainGrid.getChildAt(i);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cardView.getCardBackgroundColor().getDefaultColor() == -1) {
                        //Change background color
                        cardView.setCardBackgroundColor(Color.parseColor("#FF6F00"));
                        Toast.makeText(getActivity(), "State : True", Toast.LENGTH_SHORT).show();

                    } else {
                        //Change background color
                        cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                        Toast.makeText(getActivity(), "State : False", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setSingleEvent(LinearLayout mainGrid) {
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            CardView cardView = (CardView) mainGrid.getChildAt(i);
            cardView.setVisibility(View.GONE);
            cardView.setEnabled(false); final int index=i;
            dbr[index]=null;
            final ViewGroup vg1=(ViewGroup)cardView.getChildAt(0);
            final ViewGroup vg2=(ViewGroup)cardView.getChildAt(1);
            //((ImageView)vg2.getChildAt(0)).setEnabled(false);
            ((EditText)vg2.getChildAt(0)).setEnabled(false);
            ((EditText)vg2.getChildAt(1)).setEnabled(false);
            /*((ImageView)vg2.getChildAt(0)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
                }
            });*/
            ((ImageButton) vg1.getChildAt(0)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
                    int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

                    LinearLayout viewGroup = (LinearLayout) getActivity().findViewById(R.id.ll);
                    LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = layoutInflater.inflate(R.layout.popup, viewGroup);

                    final PopupWindow popup = new PopupWindow(getActivity());
                    popup.setContentView(layout);
                    popup.setWidth(popupWidth);
                    popup.setHeight(popupHeight);
                    popup.showAtLocation(layout, Gravity.CENTER, 0,0);
                    Button cancel = (Button) layout.findViewById(R.id.cancel);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popup.dismiss();
                        }
                    });
                    Button delete = (Button) layout.findViewById(R.id.delete);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dbr[index].removeValue(); dbr[index]=null;
                            ((EditText)vg2.getChildAt(0)).setText("");
                            ((EditText)vg2.getChildAt(1)).setText("");
                            popup.dismiss();}
                    });
                }
            });
            ((ImageButton) vg1.getChildAt(1)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!edit)  { edit=true;
                        //((ImageView)vg2.getChildAt(0)).setEnabled(true);
                        ((EditText)vg2.getChildAt(0)).setEnabled(true);
                        ((EditText)vg2.getChildAt(1)).setEnabled(true);
                        ((ImageButton) vg1.getChildAt(1)).setImageResource(R.drawable.ic_check_black_24dp);}
                    else if(edit){ edit=false;
                        name=((EditText)vg2.getChildAt(0)).getText().toString();
                        number=((EditText)vg2.getChildAt(1)).getText().toString();
                        Map userInfo = new HashMap();
                        userInfo.put("contact_name", name);
                        userInfo.put("contact_number", number);
                        dbr[index].updateChildren(userInfo);
                        /*if(resultUri != null) {

                            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                            byte[] data = baos.toByteArray();
                            UploadTask uploadTask = filePath.putBytes(data);

                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    finish();
                                    return;
                                }
                            });
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                    Map newImage = new HashMap();
                                    newImage.put("contact_picture", downloadUrl.toString());
                                    dbr[index].updateChildren(newImage);

                                    finish();
                                    return;
                                }
                            });
                            }else{
                            finish();
                            }*/
                        //((ImageView)vg2.getChildAt(0)).setEnabled(false);
                        ((EditText)vg2.getChildAt(0)).setEnabled(false);
                        ((EditText)vg2.getChildAt(1)).setEnabled(false);
                        ((ImageButton) vg1.getChildAt(1)).setImageResource(R.drawable.ic_edit_black_24dp);
                    }
                }
            });
        }}
    }