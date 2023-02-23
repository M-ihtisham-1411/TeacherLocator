package com.example.teacherlocater;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.teacherlocater.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GeofencingClient geofencingClient;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private GeofenceHelper geofenceHelper;
    LatLng markerLocation;

    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int ACCESS_BACKGROUND_LOCATION = 100;

    Button history_btn;
    Button myArea_btn;
    Button myLocation_btn;

    Double lat, longi;
    DatabaseReference areaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, ACCESS_BACKGROUND_LOCATION);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 123);

        areaRef = FirebaseDatabase.getInstance().getReference("Teachers/" + PreferenceManager.getDefaultSharedPreferences(MapsActivity.this).getString("UN", "") + "/area");

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userName = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this).getString("UN", "");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        history_btn = findViewById(R.id.history_btn);
        myArea_btn = findViewById(R.id.myArea_btn);
        myLocation_btn = findViewById(R.id.myLocation_btn);


        history_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, History.class);
                startActivity(intent);
            }
        });


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        areaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                lat = Double.parseDouble(String.valueOf(snapshot.child("lat").getValue()));
                longi = Double.parseDouble(String.valueOf(snapshot.child("long").getValue()));

                // Add a marker in Sydney and move the camera
                markerLocation = new LatLng(lat, longi);

                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(markerLocation);
                circleOptions.radius(40);
                circleOptions.strokeColor(Color.argb(255, 17, 207, 207));
                circleOptions.fillColor(Color.argb(64, 17, 207, 207));
                circleOptions.strokeWidth(4);
                mMap.addCircle(circleOptions);


                mMap.addMarker(new MarkerOptions().position(markerLocation).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 16));

                geofenceHelper = new GeofenceHelper(MapsActivity.this);

                Geofence geofence = geofenceHelper.getGeoFence("ID", markerLocation, 40, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
                GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
                PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

                geofencingClient = LocationServices.getGeofencingClient(MapsActivity.this);

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                geofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("called");
                        Toast.makeText(MapsActivity.this, "Geo fence is added", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                        String errormessage = geofenceHelper.getErrorString(e);
                        Toast.makeText(MapsActivity.this, errormessage, Toast.LENGTH_SHORT).show();
                    }
                });


                enableUserLocation();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        myArea_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, 16));
            }
        });

        myLocation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                LocationManager locationManager = (LocationManager) getSystemService(MapsActivity.this.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location != null) {

                            try {
                                Geocoder geocoder = new Geocoder(MapsActivity.this,
                                        Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(
                                        location.getLatitude(), location.getLongitude(), 1
                                );
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()), 15));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });


            }
        });
    }

    private void enableUserLocation() {

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        System.out.println("enableUserLocation");

    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MapsActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{permission}, requestCode);
        } else {
            Toast.makeText(MapsActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == ACCESS_BACKGROUND_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MapsActivity.this, "Background Location Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapsActivity.this, "Background Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}