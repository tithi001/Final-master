package com.simcoder.test;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class StartupActivity extends AppCompatActivity {
    private TextView StartupLogin,StartupSignup;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_startup);
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            // Set the content to appear under the system bars so that the
                            // content doesn't resize when the system bars hide and show.
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // Hide the nav bar and status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }



        StartupLogin=(TextView) findViewById(R.id.StartupLogin);
        StartupSignup=(TextView) findViewById(R.id.StartupSignup);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        StartupSignup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(StartupActivity.this, SignUpActivity.class));
                finish();
            }
        });
        StartupLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(StartupActivity.this, MainActivity.class));
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            StartupLogin.setVisibility(View.GONE);
            StartupSignup.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    finish();
                    Intent intent=new Intent(StartupActivity.this, MainActivity2.class);
                    intent.putExtra("frag",2);
                    startActivity(intent);
                }
            }, (4*1000));
        }
    }
}
