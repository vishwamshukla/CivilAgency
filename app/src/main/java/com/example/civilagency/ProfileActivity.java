 package com.example.civilagency;


 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;

 import androidx.appcompat.app.AppCompatActivity;

 import com.google.android.gms.tasks.Continuation;
 import com.google.android.gms.tasks.OnCompleteListener;
 import com.google.android.gms.tasks.Task;
 import com.google.android.material.textfield.TextInputLayout;
 import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.database.DataSnapshot;
 import com.google.firebase.database.DatabaseError;
 import com.google.firebase.database.DatabaseReference;
 import com.google.firebase.database.FirebaseDatabase;
 import com.google.firebase.database.ValueEventListener;
 import com.google.firebase.storage.FirebaseStorage;
 import com.google.firebase.storage.StorageReference;
 import com.google.firebase.storage.StorageTask;
 import com.squareup.picasso.Picasso;
 import com.theartofdev.edmodo.cropper.CropImage;

 import java.util.HashMap;

 import de.hdodenhof.circleimageview.CircleImageView;

 public class ProfileActivity extends AppCompatActivity {

     private CircleImageView profileImageView1;
     TextInputLayout name1, username1, email1, phone1;
     TextView totalPotholeReported;
     Button update;
     RelativeLayout changeProfile;
     private Uri imageUri;
     private String myUrl = "";
     private StorageTask uploadTask;
     private StorageReference storageProfilePrictureRef;
     private ProgressBar progressBar;
     private FirebaseAuth mAuth;
     private DatabaseReference UsersRef, UsersRef1;
     private String checker = "";
     String currentUserID;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_profile);
         storageProfilePrictureRef = FirebaseStorage.getInstance().getReference().child("Civil Agencies Profiles");
         mAuth = FirebaseAuth.getInstance();
         currentUserID = mAuth.getCurrentUser().getUid();
         UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Civil").child(currentUserID);
         UsersRef1 = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserID);

         totalPotholeReported = findViewById(R.id.total_pothole_reported);

         progressBar = findViewById(R.id.progress_bar);

         profileImageView1 = (CircleImageView) findViewById(R.id.image_profile);
         update = findViewById(R.id.update_profile);
         name1 = findViewById(R.id.profile_name);
         username1 = findViewById(R.id.profile_username);
         email1 = findViewById(R.id.profile_email);
         phone1 = findViewById(R.id.profile_phone_number);

         changeProfile = findViewById(R.id.profile_image_layout);

         userInfoDisplay(profileImageView1, name1, username1, email1, phone1);
         update.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view)
             {
                 if (checker.equals("clicked"))
                 {
                     userInfoSaved();
                 }
                 else
                 {
                     updateOnlyUserInfo();
                 }
             }
         });
         changeProfile.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view)
             {
                 checker = "clicked";

                 CropImage.activity(imageUri)
                         .setAspectRatio(1, 1)
                         .start(ProfileActivity.this);
             }
         });

         findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });
     }

     private void updateOnlyUserInfo()
     {
         DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Civil");

         HashMap<String, Object> userMap = new HashMap<>();
         userMap.put("name", name1.getEditText().getText().toString());
         userMap.put("username", username1.getEditText().getText().toString());
         userMap.put("email", email1.getEditText().getText().toString());
         userMap.put("phone", phone1.getEditText().getText().toString());
         ref.child(currentUserID).updateChildren(userMap);

         //startActivity(new Intent(ReceptionProfileActivity.this, ReceptionProfileActivity.class));
         Toast.makeText(ProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
         //finish();
     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         super.onActivityResult(requestCode, resultCode, data);

         if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE  &&  resultCode==RESULT_OK  &&  data!=null)
         {
             CropImage.ActivityResult result = CropImage.getActivityResult(data);
             imageUri = result.getUri();

             profileImageView1.setImageURI(imageUri);
         }
         else
         {
             //Toast.makeText(this, "Error, Try Again.", Toast.LENGTH_SHORT).show();

//            startActivity(new Intent(ReceptionProfileActivity.this, ReceptionProfileActivity.class));
//            finish();
         }
     }
     private void userInfoSaved()
     {
         String name = name1.getEditText().getText().toString();
         String username = username1.getEditText().getText().toString();
         String email = email1.getEditText().getText().toString();
         String phone = phone1.getEditText().getText().toString();

         String noWhiteSpaces = "(?=\\s+$)";
         String emailpattern = "[a-zA=z0-9._-]+@[a-z]+\\.+[a-z]+";
         if(name.isEmpty()){
             name1.setError("Field can't be empty");
         }
         else if(username.isEmpty()){
             username1.setError("Field can't be empty");
         }
         else if(phone.isEmpty()){
             phone1.setError("Field can't be empty");
         }
         else if (username.matches(noWhiteSpaces)){
             username1.setError("White spaces are not allowed");
         }
         else if (username.length() > 15){
             username1.setError("Username is too long");
         }
         else if(phone.length() > 10 && phone.length() < 10){
             phone1.setError("Phone number is not valid");
         }
         else if (!email.matches(emailpattern)){
             email1.setError("Invalid email");
         }
         else if(checker.equals("clicked"))
         {
             uploadImage();
         }
     }
     private void uploadImage()
     {
         progressBar.setVisibility(View.VISIBLE);

         if (imageUri != null)
         {
             final StorageReference fileRef = storageProfilePrictureRef
                     .child(currentUserID + ".jpg");

             uploadTask = fileRef.putFile(imageUri);

             uploadTask.continueWithTask(new Continuation() {
                 @Override
                 public Object then(Task task) throws Exception
                 {
                     if (!task.isSuccessful())
                     {
                         throw task.getException();
                     }

                     return fileRef.getDownloadUrl();
                 }
             })
                     .addOnCompleteListener(new OnCompleteListener<Uri>() {
                         @Override
                         public void onComplete(Task<Uri> task)
                         {
                             if (task.isSuccessful())
                             {
                                 Uri downloadUrl = task.getResult();
                                 myUrl = downloadUrl.toString();

                                 DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Civil");

                                 HashMap<String, Object> userMap = new HashMap<>();
                                 userMap.put("name", name1.getEditText().getText().toString());
                                 userMap.put("username", username1.getEditText().getText().toString());
                                 userMap.put("email", email1.getEditText().getText().toString());
                                 userMap.put("phone", phone1.getEditText().getText().toString());
                                 userMap. put("image", myUrl);
                                 ref.child(currentUserID).updateChildren(userMap);
                                 UsersRef1.updateChildren(userMap);

                                 //progressDialog.dismiss();
                                 progressBar.setVisibility(View.INVISIBLE);

                                 //startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                                 Toast.makeText(ProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                                 finish();
                             }
                             else
                             {
                                 //progressDialog.dismiss();
                                 progressBar.setVisibility(View.INVISIBLE);
                                 Toast.makeText(ProfileActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                             }
                         }
                     });
         }
         else
         {
             Toast.makeText(this, "Select profile image", Toast.LENGTH_SHORT).show();
         }
     }

     private void userInfoDisplay(final CircleImageView profileImageView1, final TextInputLayout name1, final TextInputLayout username1, final TextInputLayout email1, final TextInputLayout phone1) {
         DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Civil").child(currentUserID);
         DatabaseReference CountRef = FirebaseDatabase.getInstance().getReference("Individual Reports");

         CountRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot snapshot) {
                 if(snapshot.exists()){
                     String count = String.valueOf(snapshot.getChildrenCount());
                     totalPotholeReported.setText(count);
                 }

             }

             @Override
             public void onCancelled(DatabaseError error) {

             }
         });

         UsersRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot)
             {
                 if (dataSnapshot.exists())
                 {
                     if (dataSnapshot.child("image").exists())
                     {
//                        String image = String.valueOf(dataSnapshot.child("image").getValue());
//                        String name = String.valueOf(dataSnapshot.child("name").getValue());
//                        String username = String.valueOf(dataSnapshot.child("username").getValue());
//                        String email = String.valueOf(dataSnapshot.child("email").getValue());
//                        String phone = String.valueOf(dataSnapshot.child("phone").getValue());
//
//
//                        Picasso.get().load(image).into(profileImageView1);
//                        name1.getEditText().setText(name);
//                        username1.getEditText().setText(username);
//                        email1.getEditText().setText(email);
//                        phone1.getEditText().setText(phone);
                     }
                     String image = String.valueOf(dataSnapshot.child("image").getValue());
                     String name = String.valueOf(dataSnapshot.child("name").getValue());
                     String username = String.valueOf(dataSnapshot.child("username").getValue());
                     String email = String.valueOf(dataSnapshot.child("email").getValue());
                     String phone = String.valueOf(dataSnapshot.child("phone").getValue());


                     Picasso.get().load(image).into(profileImageView1);
                     name1.getEditText().setText(name);
                     username1.getEditText().setText(username);
                     email1.getEditText().setText(email);
                     phone1.getEditText().setText(phone);
                 }
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });
     }
 }