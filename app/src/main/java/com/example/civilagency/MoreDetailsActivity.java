package com.example.civilagency;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class MoreDetailsActivity extends AppCompatActivity {

    TextInputLayout name3, username3, phone3;
    Button submitButton;

    FirebaseDatabase rootNode;
    DatabaseReference reference, UsersRef, UsersRef1;
    FirebaseAuth mAuth;
    String currentUserID;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        progressBar = findViewById(R.id.progress_bar);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Civil").child(currentUserID);
        UsersRef1 = FirebaseDatabase.getInstance().getReference().child("users");
        name3 = findViewById(R.id.name3);
        username3= findViewById(R.id.username3);

        phone3 = findViewById(R.id.phone3);

        submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInfoSaved();
            }
        });
    }

    private void userInfoSaved() {
        String name = name3.getEditText().getText().toString();
        String username = username3.getEditText().getText().toString();
        String phone = phone3.getEditText().getText().toString();

        String noWhiteSpaces = "(?=\\s+$)";
        String emailpattern = "[a-zA=z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(name.isEmpty()){
            name3.setError("Field can't be empty");
        }
        else if(username.isEmpty()){
            username3.setError("Field can't be empty");
        }
        else if(phone.isEmpty()){
            phone3.setError("Field can't be empty");
        }
        else if (username.matches(noWhiteSpaces)){
            username3.setError("White spaces are not allowed");
        }
        else if (username.length() > 15){
            username3.setError("Username is too long");
        }
        else if(phone.length() > 10){
            phone3.setError("Phone number is not valid");
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Civil");
        String token_id = FirebaseInstanceId.getInstance().getToken();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("username", username);
        userMap.put("phone", phone);
        userMap.put("email", mAuth.getCurrentUser().getEmail().toString());
        userMap.put("device_token",token_id);
        userMap.put("status","Hi there, I am Civil Member");
        userMap.put("image","default");
        userMap.put("thumb_image","default");
        userMap.put("online","true");
        userMap.put("usertype","Civil Member");
        ref.child(currentUserID).updateChildren(userMap);
        UsersRef1.child(currentUserID).updateChildren(userMap);



        Toast.makeText(MoreDetailsActivity.this, "Thank you", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MoreDetailsActivity.this, HomeActivity.class));
    }
}