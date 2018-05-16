package com.simcoder.test;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.speech.RecognizerIntent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import edu.cmu.pocketsphinx.SpeechRecognizer;


public class TriggerPhraseFragment extends Fragment {
    private View parentView;
    private TextView txvResult;
    private TextView txvTemp;
    private Button btn_save;
    private ImageView btnSpeak;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase,value;
    private String userID;
    private static final String KWS_SEARCH = "wakeup";
    private String mphrase;
    private SpeechRecognizer recognizer1;
    final int REQUEST_FROM_TRIGGER_PHRASE = 2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_trigger_phrase, container, false);
        txvResult = (TextView) parentView.findViewById(R.id.txvResult);
        txvTemp = (TextView) parentView.findViewById(R.id.txvTemp);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        value= userDatabase.child("trigger_phrase");
        if(value!=null){
            value.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!=null)txvResult.setText(dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        btn_save = (Button) parentView.findViewById(R.id.btn_save);
        btnSpeak = (ImageView) parentView.findViewById(R.id.btnSpeak);
        if(ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            btnSpeak.setEnabled(false);
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_FROM_TRIGGER_PHRASE);
        }
        btn_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Map userInfo = new HashMap();
                userInfo.put("trigger_phrase", mphrase);
                userDatabase.updateChildren(userInfo);
                //value.setValue(mphrase);
            }
        });
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    recognizer1=new ReceiveSOS().getRec();
                    try{if(recognizer1!=null)recognizer1.stop();}catch (Exception e){Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();}
                    startActivityForResult(intent, 10);
                } else {
                    Toast.makeText(getActivity(), "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return parentView;
    }

    /*public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(getActivity(), "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FROM_TRIGGER_PHRASE: {
                if (grantResults.length > 0) {
                    if (android.support.v4.content.ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        btnSpeak.setEnabled(true);
                    }
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Can't record audio without permission.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mphrase=result.get(0);
                    txvTemp.setText(result.get(0));
                    try{if(recognizer1!=null)recognizer1.startListening(KWS_SEARCH);}catch (Exception e){Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();}
                }
                break;
        }
    }

}
