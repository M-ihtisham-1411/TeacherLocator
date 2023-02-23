package com.example.teacherlocater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Splash extends AppCompatActivity {

    private static final int ACCESS_FINE_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {


            @Override

            public void run() {
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_FINE_LOCATION);

                Intent i = new Intent(Splash.this, Login.class);

                startActivity(i);

                finish();

            }

        }, 3*1000); // wait for 5 seconds
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(Splash.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(Splash.this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(Splash.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Splash.this, "Location Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(Splash.this, "Location Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
    }

}