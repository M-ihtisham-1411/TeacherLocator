package com.example.teacherlocater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    Button login_button;
    EditText userName_editText;
    EditText pin_editText;

    DatabaseReference teacherReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Initialize();
        ClickListners();
    }

    private void Initialize() {
        teacherReference = FirebaseDatabase.getInstance().getReference("Teachers");
        login_button = findViewById(R.id.login_button);
        userName_editText = findViewById(R.id.userName_editText);
        pin_editText = findViewById(R.id.pin_editText);

    }

    private void ClickListners() {
        login_buttonClick();
    }

    private void login_buttonClick() {
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                teacherReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(userName_editText.getText().toString()).exists()){
                            if (snapshot.child(userName_editText.getText().toString()).child("pin").getValue().toString().equals(pin_editText.getText().toString())){
                                PreferenceManager.getDefaultSharedPreferences(Login.this).edit().putString("UN", userName_editText.getText().toString()).apply();
                                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this,MapsActivity.class);
                                startActivity(intent);
                            }else {
                                System.out.println();
                                Toast.makeText(Login.this, "Wrong pin", Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            Toast.makeText(Login.this, "No such user exists", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}