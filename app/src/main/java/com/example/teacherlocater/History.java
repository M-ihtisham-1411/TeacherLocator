package com.example.teacherlocater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {
    Adaptar_historyRecyclerView adaptar_historyRecyclerView;
    RecyclerView history_recyclerView;

    TextView name_textView;
    DatabaseReference historyRef;
    DatabaseReference nameRef;
    List<String> statusList,timeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        name_textView = findViewById(R.id.name_textView);

        statusList = new ArrayList<>();
        timeList = new ArrayList<>();

        nameRef = FirebaseDatabase.getInstance().getReference("Teachers/"+ PreferenceManager.getDefaultSharedPreferences(History.this).getString("UN", ""));
        historyRef = FirebaseDatabase.getInstance().getReference("teacher_history/"+ PreferenceManager.getDefaultSharedPreferences(History.this).getString("UN", ""));

        history_recyclerView = findViewById(R.id.history_recyclerView);
        history_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name_textView.setText(snapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println(snapshot);
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    statusList.add(snapshot1.child("action").getValue().toString());
                    timeList.add(snapshot1.child("time").getValue().toString());
                    System.out.println(snapshot1.child("action").getValue().toString());
                }

                adaptar_historyRecyclerView = new Adaptar_historyRecyclerView(statusList,timeList);
                history_recyclerView.setAdapter(adaptar_historyRecyclerView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}