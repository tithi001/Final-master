package com.simcoder.test;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText mNameField, Age,PhoneField;
    private TextView BirthDate;
    private Spinner Gender,Country;
    private static final int CHOOSE_IMAGE = 101;
    private Button mConfirm;
    private ImageButton edit1,edit2,edit3,edit4,edit5;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String userID;
    private String mName,gender,country,date,age,phone;
    private int countryCode;
    private String mProfileImageUrl;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Uri resultUri;
    private ArrayAdapter<String> genderlist,countrylist;
    private View parentView;
    //private ResideMenu resideMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_profile, container, false);
        mNameField = (EditText) parentView.findViewById(R.id.name);
        PhoneField = (EditText) parentView.findViewById(R.id.phone);
        Age = (EditText) parentView.findViewById(R.id.age);
        BirthDate = (TextView) parentView.findViewById(R.id.DateofBirth);
        Gender = (Spinner) parentView.findViewById(R.id.gender);
        Country=(Spinner) parentView.findViewById(R.id.country);
        mProfileImage = (ImageView) parentView.findViewById(R.id.profileImage);
        edit1=(ImageButton)parentView.findViewById(R.id.edit1) ;
        edit2=(ImageButton)parentView.findViewById(R.id.edit2) ;
        edit3=(ImageButton)parentView.findViewById(R.id.edit3) ;
        edit4=(ImageButton)parentView.findViewById(R.id.edit4) ;
        edit5=(ImageButton)parentView.findViewById(R.id.edit5) ;
        mConfirm = (Button) parentView.findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        mNameField.setEnabled(false);
        Age.setEnabled(false);
        Gender.setEnabled(false);
        Country.setEnabled(false);
        BirthDate.setEnabled(false);
        PhoneField.setEnabled(false);
        TelephonyManager tm = (TelephonyManager)getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
        countryCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion((tm.getNetworkCountryIso()).toUpperCase());
        Selection.setSelection(PhoneField.getText(), PhoneField.getText().length());
        PhoneField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().startsWith(""+countryCode)){
                    PhoneField.setText(""+countryCode);
                    Selection.setSelection(PhoneField.getText(), PhoneField.getText().length());

                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        genderlist= new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_layout, getResources().getStringArray(R.array.Gender));
        genderlist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Gender.setAdapter(genderlist);
        Gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Gender.setSelection(i);
                switch(i) {
                    case 0:
                        gender = "Female";
                        return;
                    case 1:
                        gender = "Male";
                        return;
                    default:
                        gender = "";
                        return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        BirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        getActivity(),
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d("SignUpActivity", "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                date = month + "/" + day + "/" + year;
                BirthDate.setText(date);
            }
        };
        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (country.trim().length()>0 && !countries.contains(country)) {
                countries.add(country);
            }
        }
        Collections.sort(countries);
        countrylist= new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_layout, countries);
        countrylist.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Country.setAdapter(countrylist);
        Country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                country=Country.getItemAtPosition(i).toString();
                Country.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
                mNameField.setEnabled(false);
                Gender.setEnabled(false);
                BirthDate.setEnabled(false);
                Country.setEnabled(false);
                PhoneField.setEnabled(false);
            }
        });
        edit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNameField.setEnabled(true);
            }});
        edit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gender.setEnabled(true);
            }});
        edit3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BirthDate.setEnabled(true);
            }});
        edit4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Country.setEnabled(true);
            }});
        edit5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneField.setEnabled(true);
            }});
        return parentView;
    }
    private void getUserInfo(){

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("gender")!=null) {
                        gender = map.get("gender").toString();
                        Gender.setSelection(genderlist.getPosition(gender));
                    }
                    if(map.get("dateOfBirth")!=null){
                        date=map.get("dateOfBirth").toString();
                        BirthDate.setText(date);
                        /*try {
                            Date date1=new SimpleDateFormat("MM/dd/yyyy").parse(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }*/
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
                        Age.setText(age);
                    }
                    if(map.get("country")!=null){
                        country=map.get("country").toString();
                        Country.setSelection(countrylist.getPosition(country));
                    }
                    if(map.get("phone number")!=null){
                        phone = map.get("phone number").toString();
                        PhoneField.setText(phone);
                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        if(isAdded())
                        {Glide.with(ProfileFragment.this).load(mProfileImageUrl).into(mProfileImage);}
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void saveUserInformation() {
        mName = mNameField.getText().toString().trim();
        phone = PhoneField.getText().toString().trim();Log.i("profile", "button");
        if(mName.isEmpty()){
            mNameField.setError("Name is required");
            mNameField.requestFocus();
            return;
        }if(phone.equals(""+countryCode)){
            PhoneField.setError("Contact number is required");
            PhoneField.requestFocus();
            return;
        }if ((BirthDate.getText()).equals("Date of Birth")) {
            BirthDate.setError("Date of birth is required");
            return;
        }
        if (gender.equals("Gender")) {
            Toast.makeText(getActivity(), "Gender is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (country.equals("Country")) {
            Toast.makeText(getActivity(), "Country is required", Toast.LENGTH_SHORT).show();
            return;
        }Log.i("profile", "button2");
        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("gender", gender);
        userInfo.put("dateOfBirth", date);
        userInfo.put("country", country);
        userInfo.put("phone number", phone);
        userDatabase.updateChildren(userInfo);
        Log.i("profile", "button3");
        if(resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplication().getContentResolver(), resultUri);
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
                    getActivity().finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    userDatabase.updateChildren(newImage);

                    getActivity().finish();
                    return;
                }
            });
        }else{
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_IMAGE && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }
}
