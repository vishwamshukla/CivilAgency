package com.example.civilagency;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputLayout email;
    private Button Continue;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    ImageView image;
    TextView logoText, slogantext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        image = findViewById(R.id.logo_image);
        logoText = findViewById(R.id.logo_name);
        slogantext = findViewById(R.id.slogan_name);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email4);
        Continue = (Button) findViewById(R.id.submit_button_reset);
        progressBar = findViewById(R.id.progress_bar);
        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getEditText().getText().toString();
                String emailpattern = "[a-zA=z0-9._-]+@[a-z]+\\.+[a-z]+";
                if(userEmail.isEmpty()){
                    email.setError("Field can't be empty");
                }
                else if(userEmail.matches(emailpattern)){
                    email.setError("Email address is not valid");
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(ResetPasswordActivity.this, "Instruction to reset your password has been sent to you email", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            }
                            else {
                                progressBar.setVisibility(View.VISIBLE);
                                String message = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, "Error occured"+ message , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}