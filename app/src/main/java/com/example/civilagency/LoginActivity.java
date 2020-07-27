package com.example.civilagency;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    Button register, login_btn, resetButton;
    ImageView image;
    TextView logoText, slogantext;
    TextInputLayout email2, password2;
    private static int SPLASH_SCREEN = 5000;

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;

    String currentuser;
    private ProgressBar progressBar;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();
//        currentuser = mAuth.getCurrentUser().getUid();

        register = findViewById(R.id.new_user);
        image = findViewById(R.id.logo_image);
        logoText = findViewById(R.id.logo_name);
        slogantext = findViewById(R.id.slogan_name);
        email2 = findViewById(R.id.emaill);
        password2=findViewById(R.id.passswordd);
        login_btn = findViewById(R.id.login_btn);
        resetButton = findViewById(R.id.reset_password_button);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                Pair[] pairs = new Pair[7];
                pairs[0] = new Pair<View, String>(image, "logo_image");
                pairs[1] = new Pair<View, String>(logoText, "logo_text");
                pairs[2] = new Pair<View, String>(slogantext, "logo_desc");
                pairs[3] = new Pair<View, String>(email2, "email_tran");
                pairs[4] = new Pair<View, String>(password2, "password_tran");
                pairs[5] = new Pair<View, String>(login_btn, "button_tran");
                pairs[6] = new Pair<View, String>(register, "register_tran");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
                startActivity(intent, options.toBundle());
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                Pair[] pairs = new Pair[5];
                pairs[0] = new Pair<View, String>(image, "logo_image");
                pairs[1] = new Pair<View, String>(logoText, "logo_text");
                pairs[2] = new Pair<View, String>(slogantext, "logo_desc");
                pairs[3] = new Pair<View, String>(email2, "email_tran");
                pairs[4] = new Pair<View, String>(resetButton, "button_tran");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
                startActivity(intent, options.toBundle());

            }
        });


        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        mAuth = FirebaseAuth.getInstance();


        createRequest();


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn();
            }
        });
    }



    private void createRequest() {


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("562675837506-rs2lviklqp858l1eivcvkekkmem4nupq.apps.googleusercontent.com")
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
                            startActivity(intent);


                        } else {
                            Toast.makeText(LoginActivity.this, "Sorry auth failed.", Toast.LENGTH_SHORT).show();


                        }


                        // ...
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser1 = mAuth.getCurrentUser();
        if (currentUser1 !=  null){

            SendUserToHomeActivity();
        }
    }
    private void SendUserToHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void LoginUser() {
        String email1 = email2.getEditText().getText().toString();
        String password1 = password2.getEditText().getText().toString();
        String emailpattern = "[a-zA=z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(email1.isEmpty()){
            email2.setError("Field can't be empty");
        }
        else if(!email1.matches(emailpattern)){
            email2.setError("Email address is not valid");
        }
        else if(password1.isEmpty()){
            password2.setError("Field can't be empty");
        } else {
            // progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email1, password1)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                SendUserToHomeActivity();
                                Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                                //loadingBar.dismiss();
                                //progressBar.setVisibility(View.INVISIBLE);
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error-" + message, Toast.LENGTH_SHORT).show();
                                //loadingBar.dismiss();
                                //progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        finish();
    }
}