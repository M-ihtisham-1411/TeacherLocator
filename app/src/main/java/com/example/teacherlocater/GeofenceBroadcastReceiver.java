package com.example.teacherlocater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    DatabaseReference teacherHistoryRef;
    DatabaseReference notificationRef;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        System.out.println("Geofence triggered!");

        teacherHistoryRef = FirebaseDatabase.getInstance().getReference();
        notificationRef = FirebaseDatabase.getInstance().getReference();

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()){
            System.out.println("onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        int transitionType = geofencingEvent.getGeofenceTransition();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMMM/yyyy 'at' hh:mm:ss a");
        String currentDateandTime;
        String pushID;

        switch (transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                System.out.println("Enter");
                currentDateandTime = sdf.format(new Date());
                pushID = teacherHistoryRef.push().getKey();
                teacherHistoryRef.child("teacher_history").child(PreferenceManager.getDefaultSharedPreferences(context).getString("UN", "")).child(pushID).child("time").setValue(currentDateandTime);
                teacherHistoryRef.child("teacher_history").child(PreferenceManager.getDefaultSharedPreferences(context).getString("UN", "")).child(pushID).child("action").setValue("entered");

                notificationRef.child("notification").child(pushID).child("user_name").setValue(PreferenceManager.getDefaultSharedPreferences(context).getString("UN", ""));
                notificationRef.child("notification").child(pushID).child("time").setValue(currentDateandTime);
                notificationRef.child("notification").child(pushID).child("action").setValue("entered");
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                System.out.println("Dwell");
                Toast.makeText(context.getApplicationContext(), "Dwell", Toast.LENGTH_SHORT).show();
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context.getApplicationContext(), "Exit", Toast.LENGTH_SHORT).show();
                System.out.println("Exit");
                pushID = teacherHistoryRef.push().getKey();
                currentDateandTime = sdf.format(new Date());
                teacherHistoryRef.child("teacher_history").child(PreferenceManager.getDefaultSharedPreferences(context).getString("UN", "")).child(pushID).child("time").setValue(currentDateandTime);
                teacherHistoryRef.child("teacher_history").child(PreferenceManager.getDefaultSharedPreferences(context).getString("UN", "")).child(pushID).child("action").setValue("exit");

                notificationRef.child("notification").child(pushID).child("user_name").setValue(PreferenceManager.getDefaultSharedPreferences(context).getString("UN", ""));
                notificationRef.child("notification").child(pushID).child("time").setValue(currentDateandTime);
                notificationRef.child("notification").child(pushID).child("action").setValue("exit");
                break;


        }

    }
}