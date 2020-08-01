package com.example.civilagency;


import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class DetailsPothole extends AppCompatActivity {

    public static final String EXTRA_URL = "image url";
    public static final String EXTRA_POTHOLE_TYPE = "pothole type";
    public static final String EXTRA_LANDMARK = "landmark";
    public static final String EXTRA_ADDRESS = "address";
    public static final String EXTRA_DIMENSION = "dimension";
    public static final String EXTRA_COMMENT = "comment";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_TIMEKEY = "timeKey";
    public static final String EXTRA_USERID = "userId";
    public static final String EXTRA_STATUS = "status";

    Button button_update_pothole_status;

    Uri mImageUri;
    String currentPhotoPath;

    RelativeLayout layout_proof;

    private static final int PICK_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;

    private StorageTask mUploadTask;

    private EditText comment_EditText;

    ProgressBar mPrpgressBar;
    Button back_btn;
    private DatabaseReference mDatabaseRef1,mDatabaseRef2;
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    String currentUserID;
    private List<Upload> mUploads;
    SeekBar report_status_seekbar;
    ImageView proof_view_pothole_image_view;

    TextView text_view_pothole_status;

    public enum Progress {
        Reported,
        Processing,
        Midway,
        Completed
    }

    int progress_of_pothole = 0;

    private TextView button_remove_image;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_pothole);

        Toolbar my_toolbar = (Toolbar) findViewById(R.id.action_bar);
        my_toolbar.setTitle("");
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



     //   mPrpgressBar = findViewById(R.id.Upload_proof_ProgressBar);

        comment_EditText = findViewById(R.id.proof_view_potholes_comments_textview);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance();
     //   mDatabaseRef = FirebaseDatabase.getInstance().getReference("Reports");
        back_btn =findViewById(R.id.p_button_back);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailsPothole.this,HomeActivity.class);
                startActivity(intent);
            }
        });
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra(EXTRA_URL);
        String potholeType = intent.getStringExtra(EXTRA_POTHOLE_TYPE);
        String landmark = intent.getStringExtra(EXTRA_LANDMARK);
        String address = intent.getStringExtra(EXTRA_ADDRESS);
        String dimension = intent.getStringExtra(EXTRA_DIMENSION);
        String comment = intent.getStringExtra(EXTRA_COMMENT);
        String phone = intent.getStringExtra(EXTRA_PHONE);
        String status = intent.getStringExtra(EXTRA_STATUS);
        String timeKey = intent.getStringExtra(EXTRA_TIMEKEY);
        String userId = intent.getStringExtra(EXTRA_USERID);

        ImageView imageView = findViewById(R.id.pothole_image_view);
        TextView pothole_type_textView = findViewById(R.id.pothole_type_textView);
        TextView landmark_textView = findViewById(R.id.pothole_landmark_textview);
        TextView address_textView = findViewById(R.id.pothole_address_textView);
        TextView dimension_textView = findViewById(R.id.pothole_dimension_textview);
        final TextView comment_textView = findViewById(R.id.potholes_comments_textview);
        TextView phone_textView = findViewById(R.id.potholes_phonenumber);
        final TextView mTimeKey = findViewById(R.id.potholes_timekey);
        final TextView muId = findViewById(R.id.potholes_uid);

        Picasso.get().load(imageUrl).fit().into(imageView);
        pothole_type_textView.setText(potholeType);
        landmark_textView.setText(landmark);
        address_textView.setText(address);
        dimension_textView.setText(dimension);
        comment_textView.setText(comment);
        phone_textView.setText(phone);
        mTimeKey.setText(timeKey);
        muId.setText(userId);

        mDatabaseRef2 = FirebaseDatabase.getInstance().getReference().child("Reports").child(timeKey);
        mDatabaseRef1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Citizens").child(userId).child("potholeReports").child(timeKey);
        mStorageRef = FirebaseStorage.getInstance().getReference("Reported Potholes");

        final SeekBar severity_seekBar = findViewById(R.id.seekbar_bar_pothole);
        severity_seekBar.getThumb().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        text_view_pothole_status = findViewById(R.id.text_view_pothole_status);

        switch (status == null ? "" : status) {
            case "Completed":
                severity_seekBar.setProgress(4);
                severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#4caf50"), PorterDuff.Mode.SRC_IN);
                break;
            case "Midway":
                severity_seekBar.setProgress(3);
                severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#ffeb3b"), PorterDuff.Mode.SRC_IN);
                break;
            case "Processing":
                severity_seekBar.setProgress(2);
                severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#ff9800"), PorterDuff.Mode.SRC_IN);
                break;
            default:
                severity_seekBar.setProgress(1);
                severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#f44336"), PorterDuff.Mode.SRC_IN);
                break;
        }
        text_view_pothole_status.setText(status);

        severity_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                button_update_pothole_status = findViewById(R.id.button_update_pothole_status);
                button_update_pothole_status.setVisibility(View.VISIBLE);
                button_update_pothole_status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        button_update_pothole_status.setVisibility(View.GONE);
                        if (progress_of_pothole == 4) {
                            layout_proof = findViewById(R.id.layout_proof);
                            layout_proof.setVisibility(View.VISIBLE);

                            button_remove_image = findViewById(R.id.proof_view_pothole_remove_image_button);
                            CardView image_layout = findViewById(R.id.pothole_image_layout);
                            proof_view_pothole_image_view = findViewById(R.id.proof_view_pothole_image_view);

                            image_layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final CharSequence[] items = {"Open Camera", "Upload from Gallery"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsPothole.this);
                                    builder.setItems(items, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (items[i].equals("Open Camera")) {
                                                askCameraPermissions();
                                                update_imageView_layout(true);
                                            } else if (items[i].equals("Upload from Gallery")) {
                                                OpenImageFileChooser();
                                                update_imageView_layout(true);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });

                            findViewById(R.id.proof_view_button_continue).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (mUploadTask!= null && mUploadTask.isInProgress()){
                                        {
                                            Toast.makeText(DetailsPothole.this, "Upload in Process", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else {
                                        uploadFile();

                                    }
                            }
                        });

                            findViewById(R.id.proof_view_button_cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    layout_proof.setVisibility(View.GONE);
                                }
                            });
                        }
                        else {
                           // DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Reports").child(timeKey);
                      //      DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Citizens").child(userId).child("potholeReports").child(timeKey);
                            //                                String mStatus = text_view_pothole_status.getText().toString();
                            //                                Upload1 upload = new Upload1(mStatus);
                            //                                ref.push().setValue(upload);


                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("status", text_view_pothole_status.getText().toString());
                            mDatabaseRef2.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(DetailsPothole.this, "Updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                            mDatabaseRef1.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(DetailsPothole.this, "Updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }

            private void initiate_remove_image_button(){
                button_remove_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: Delete image from the view when this button is clicked.
                        update_imageView_layout(false);
                    }
                });
            }

            private void update_imageView_layout(Boolean isImageLoaded){
                LinearLayout hint_view = findViewById(R.id.proof_view_upload_image_hint_view);
                if (isImageLoaded){
                    hint_view.setVisibility(View.GONE);
                    button_remove_image.setVisibility(View.VISIBLE);
                    initiate_remove_image_button();
                }
                else{
                    hint_view.setVisibility(View.VISIBLE);
                    button_remove_image.setVisibility(View.GONE);
                    proof_view_pothole_image_view.setImageResource(0);
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress){
                    case 0:
                    case 1:
                        text_view_pothole_status.setText(Progress.Reported.toString());
                        severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#f44336"), PorterDuff.Mode.SRC_IN);
                        break;
                    case 2:
                        text_view_pothole_status.setText(Progress.Processing.toString());
                        severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#ff9800"), PorterDuff.Mode.SRC_IN);
                        break;
                    case 3:
                        text_view_pothole_status.setText(Progress.Midway.toString());
                        severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#ffeb3b"), PorterDuff.Mode.SRC_IN);
                        break;
                    case 4:
                        text_view_pothole_status.setText(Progress.Completed.toString());
                        severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#4caf50"), PorterDuff.Mode.SRC_IN);
                        break;
                    default:
                        break;
                }
                progress_of_pothole = progress;
            }
        });
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        else
        {
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERM_CODE){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }
            else{
                Toast.makeText(this, "Camera Permission needed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void OpenImageFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST  && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).fit().into(proof_view_pothole_image_view);
        }     else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            File f = new File(currentPhotoPath);
            proof_view_pothole_image_view.setImageURI(Uri.fromFile(f));
            mImageUri = Uri.fromFile(f);
        }

//        switch (requestCode){
//
//            case 10:
//                if (resultCode == RESULT_OK && data != null) {
//                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    inputMessage.getEditText().setText(result.get(0));
//                }
//                break;
        }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadFile(){
      //  mPrpgressBar.setVisibility(View.VISIBLE);
        if (mImageUri != null){
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    HashMap<String, Object> userMap = new HashMap<>();
                                    userMap.put("proof image", uri.toString());
                                    userMap.put("status", text_view_pothole_status.getText().toString());
                                    userMap.put("Civil Agent Comment",comment_EditText.getText().toString());

                                    mDatabaseRef2.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                        }
                                    });
                                    mDatabaseRef1.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(DetailsPothole.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                          //  mPrpgressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                    layout_proof.setVisibility(View.GONE);

                                }
                            });
                        }
                    });
        }
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

//    private void onDeleteClick(int position) {
//        Upload selectedItem = mUploads.get(position);
//        final String selectedKey = selectedItem.getKey();
//        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
//        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                mDatabaseRef.child(selectedKey).removeValue();
//                Toast.makeText(DetailsPothole.this, "Item Deleted", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}