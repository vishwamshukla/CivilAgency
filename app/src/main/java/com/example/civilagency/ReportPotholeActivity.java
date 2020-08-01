package com.example.civilagency;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.ibm.cloud.sdk.core.http.Response;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.SessionResponse;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReportPotholeActivity extends AppCompatActivity {

    private ArrayList messageArrayList;
    private TextInputLayout inputMessage;
    private ImageButton btnSend;
    private ImageView btnRecord;
    StreamPlayer streamPlayer = new StreamPlayer();
    private boolean initialRequest;
    private boolean permissionToRecordAccepted = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String TAG = "MainActivity";
    private static final int RECORD_REQUEST_CODE = 101;
    private boolean listening = false;
    private MicrophoneInputStream capture;
    private Context mContext;
    private MicrophoneHelper microphoneHelper;

    private Assistant watsonAssistant;
    private Response<SessionResponse> watsonAssistantSession;
    private SpeechToText speechService;
    private TextToSpeech textToSpeech;



    private static final int PICK_VIDEO_REQUEST=1;
    private static final int PICK_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;

    private ImageView backButton;
    private Button mButtonUpload, videoUploadnew;
    private TextInputLayout mEditTextPothole_Type, mEditTextAddress, mEditTextLandmark, mEditTextDimensions, mEditTextComments, phoneNumber;
    private ImageView mImageView;
    private TextView button_remove_image;
    Integer REQUEST_CAMERA = 0;
    private VideoView mVideoView;
    String currentPhotoPath;

    // MediaController mediaController;

    private ProgressBar mProgressBar;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef,mDatabaseRef1;

    private StorageTask mUploadTask;

    private FirebaseAuth mAuth;
    String currentUserID;
    TextView severity_textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_pothole);

        inputMessage = findViewById(R.id.potholes_comments_textview);
        btnRecord = findViewById(R.id.btn_record);
//        String customFont = "Montserrat-Regular.ttf";
//        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
//        inputMessage.setTypeface(typeface);

        microphoneHelper = new MicrophoneHelper(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        } else {
            Log.i(TAG, "Permission to record was already granted");
        }

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSpeechInput(v);
            }
        });

        //createServices();



//        videoUploadnew = findViewById(R.id.video_view_button);
//        videoUploadnew.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                uploadFile1();
//                startActivity(new Intent(ReportPotholeActivity.this, UploadVideoActivity.class));
//            }
//        });



        mProgressBar=findViewById(R.id.progress_bar);
        button_remove_image = findViewById(R.id.pothole_remove_image_button);

        mButtonUpload = findViewById(R.id.p_button_continue);
        mImageView = findViewById(R.id.pothole_image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mEditTextPothole_Type = findViewById(R.id.pothole_type_textView);
        mEditTextAddress = findViewById(R.id.pothole_address_textView);
        // mEditTextComments = findViewById(R.id.potholes_comments_textview);
        mEditTextDimensions = findViewById(R.id.pothole_dimension_textview);
        mEditTextLandmark = findViewById(R.id.pothole_landmark_textview);
        phoneNumber = findViewById(R.id.pothole_phone_textview);
        mAuth = FirebaseAuth.getInstance();

        currentUserID = mAuth.getCurrentUser().getUid();

        mStorageRef = FirebaseStorage.getInstance().getReference("Reported Potholes");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child("Civil").child(currentUserID).child("potholeReports");
        mDatabaseRef1 = FirebaseDatabase.getInstance().getReference("Reports");

        userInfoDisplay(phoneNumber);

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUploadTask!= null && mUploadTask.isInProgress()){
                    {
                        Toast.makeText(ReportPotholeActivity.this, "Upload in Process", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    uploadFile();

                }
            }
        });

        CardView image_layout = findViewById(R.id.pothole_image_layout);
        image_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] items ={"Open Camera","Upload from Gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ReportPotholeActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (items[i].equals("Open Camera")){
                            askCameraPermissions();
                            update_imageView_layout(true);
                        }
                        else if (items[i].equals("Upload from Gallery")){
                            OpenImageFileChooser();
                            update_imageView_layout(true);
                        }
                    }
                });
                builder.show();
            }
        });

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportPotholeActivity.this.finish();
            }
        });

        severity_textView = findViewById(R.id.severity_textView);
        final SeekBar severity_seekBar = findViewById(R.id.pothole_severity_seekBar);
        severity_seekBar.setProgress(1);
        severity_textView.setText(String.valueOf(1));
        severity_seekBar.getThumb().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        severity_seekBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
        severity_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(getApplicationContext(), seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                severity_textView.setText(String.valueOf(progress));
                switch (progress){
                    case 0:
                        severity_seekBar.setProgress(1);
                        break;
                    case 1:
                        severity_seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.severity_level1), PorterDuff.Mode.SRC_IN);
                        break;
                    case 2:
                        severity_seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.severity_level2), PorterDuff.Mode.SRC_IN);
                        break;
                    case 3:
                        severity_seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.severity_level3), PorterDuff.Mode.SRC_IN);
                        break;
                    case 4:
                        severity_seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.severity_level4), PorterDuff.Mode.SRC_IN);
                        break;
                    case 5:
                        severity_seekBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.severity_level5), PorterDuff.Mode.SRC_IN);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void SelectVideo(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_VIDEO_REQUEST);
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
        LinearLayout hint_view = findViewById(R.id.upload_image_hint_view);
        if (isImageLoaded){
            hint_view.setVisibility(View.GONE);
            button_remove_image.setVisibility(View.VISIBLE);
            initiate_remove_image_button();
        }
        else{
            hint_view.setVisibility(View.VISIBLE);
            button_remove_image.setVisibility(View.GONE);
            mImageView.setImageResource(0);
        }
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else
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
            Picasso.get().load(mImageUri).fit().into(mImageView);
        }     else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            File f = new File(currentPhotoPath);
            mImageView.setImageURI(Uri.fromFile(f));
            mImageUri = Uri.fromFile(f);
        }

        switch (requestCode){

            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputMessage.getEditText().setText(result.get(0));
                }
                break;
        }


    }


        /*if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            mVideoUri = data.getData();

            mVideoView.setVideoURI(mVideoUri);
        }*/

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
        if (mImageUri != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressBar.setProgress(0);
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    mProgressBar.setVisibility(View.INVISIBLE);

                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM");
                                    Date date = new Date();

                                    DateFormat datefull = new SimpleDateFormat("dd/MM/yyyy");
                                    Date date1 = new Date();
                                    DateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
                                    Date time = new Date();
                                    String mPotholeType = mEditTextPothole_Type.getEditText().getText().toString();
                                    String mAddress = mEditTextAddress.getEditText().getText().toString();
                                    String mLandmark = mEditTextLandmark.getEditText().getText().toString();
                                    String mDimension = mEditTextDimensions.getEditText().getText().toString().trim();
                                    String mComment = inputMessage.getEditText().getText().toString();
                                    String mDate = dateFormat.format(date).toString();
                                    String mDateFull = datefull.format(date1).toString();
                                    String mTime = timeformat.format(time).toString();
                                    String mSeverity = severity_textView.getText().toString();
                                    String mName = mAuth.getCurrentUser().getDisplayName().toString();
                                    String mEmail = mAuth.getCurrentUser().getEmail().toString();
                                    String mUserId = mAuth.getCurrentUser().getUid().toString();
                                    String mPhone = phoneNumber.getEditText().getText().toString();
                                    String mStatus = "Reported";
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM");
                                    String saveCurrentDate = currentDate.format(calendar.getTime());


                                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                                    String saveCurrentTime = currentTime.format(calendar.getTime());

                                    String mTimeKey = mTime + "-"+saveCurrentDate;

                                    Upload upload = new Upload(uri.toString(), mPotholeType, mAddress, mLandmark, mDimension, mComment, mDate, mDateFull, mTime, mSeverity,  mName, mEmail, mPhone, mUserId, mTimeKey, mStatus);
                                    String uploadId = mDatabaseRef.push().getKey();
                                    assert uploadId != null;
                                    mDatabaseRef.child(mTimeKey).setValue(upload);
                                    mDatabaseRef1.child(mTimeKey).setValue(upload);


                                    Toast.makeText(ReportPotholeActivity.this, "Thank you for reporting!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(ReportPotholeActivity.this, HomeActivity.class));

                                }
                            });
                            /*Toast.makeText(Image_video_upload.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                               Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                               while (!urlTask.isSuccessful()) ;
                               Uri downloadUrl = urlTask.getResult();
                               Upload upload = new Upload(mEditTextFilename.getText().toString().trim(),downloadUrl.toString());
                               String uploadId = mDatabaseRef.push().getKey();
                               mDatabaseRef.child(uploadId).setValue(upload);*/

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ReportPotholeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        }

       /* if (mVideoUri != null){
            StorageReference reference = mStorageRefVideo.child(System.currentTimeMillis()+"."+getfileExt(mVideoUri));

            reference.putFile(mVideoUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(),"Video Successfully Uploaded",Toast.LENGTH_SHORT).show();
                            Upload upload = new Upload(mEditTextFilename.getText().toString().trim(),
                                    taskSnapshot.getUploadSessionUri().toString());
                            String uploadId = mDatabaseRefVideo.push().getKey();
                            mDatabaseRefVideo.child(uploadId).setValue(upload);
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }*/

        else {
            Toast.makeText(this,"No File Selected",Toast.LENGTH_SHORT).show();
        }
    }
//    private void uploadFile1(){
//        if (mImageUri != null) {
//            mProgressBar.setVisibility(View.VISIBLE);
//            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
//                    + "." + getFileExtension(mImageUri));
//            mUploadTask = fileReference.putFile(mImageUri)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            mProgressBar.setProgress(0);
//                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(final Uri uri) {
//                                    mProgressBar.setVisibility(View.INVISIBLE);
//
//                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM");
//                                    Date date = new Date();
//
//                                    DateFormat datefull = new SimpleDateFormat("dd/MM/yyyy");
//                                    Date date1 = new Date();
//                                    DateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
//                                    Date time = new Date();
//                                    String mPotholeType = mEditTextPothole_Type.getEditText().getText().toString();
//                                    String mAddress = mEditTextAddress.getEditText().getText().toString();
//                                    String mLandmark = mEditTextLandmark.getEditText().getText().toString();
//                                    String mDimension = mEditTextDimensions.getEditText().getText().toString().trim();
//                                    String mComment = mEditTextComments.getEditText().getText().toString();
//                                    String mDate = dateFormat.format(date).toString();
//                                    String mDateFull = datefull.format(date1).toString();
//                                    String mTime = timeformat.format(time).toString();
//                                    String mSeverity = severity_textView.getText().toString();
//                                    String mName = mAuth.getCurrentUser().getDisplayName().toString();
//                                    String mEmail = mAuth.getCurrentUser().getEmail().toString();
//                                    String mPhone = mAuth.getCurrentUser().getPhoneNumber().toString();
//                                    String mUserId = mAuth.getCurrentUser().getPhoneNumber().toString();
//
////                                    mDatabaseRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
////                                        @Override
////                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
////                                            if(snapshot.exists()){
////                                                if (snapshot.child("videourl").exists()){
////                                                    String mVideo = String.valueOf(snapshot.child("videourl").getValue());
////                                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM");
////                                                    Date date = new Date();
////
////                                                    DateFormat datefull = new SimpleDateFormat("dd/MM/yyyy");
////                                                    Date date1 = new Date();
////                                                    DateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
////                                                    Date time = new Date();
////                                                    String mPotholeType = mEditTextPothole_Type.getEditText().getText().toString();
////                                                    String mAddress = mEditTextAddress.getEditText().getText().toString();
////                                                    String mLandmark = mEditTextLandmark.getEditText().getText().toString();
////                                                    String mDimension = mEditTextDimensions.getEditText().getText().toString().trim();
////                                                    String mComment = mEditTextComments.getEditText().getText().toString();
////                                                    String mDate = dateFormat.format(date).toString();
////                                                    String mDateFull = datefull.format(date1).toString();
////                                                    String mTime = timeformat.format(time).toString();
////
////                                                   // DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Citizens");
////
////                                                    HashMap<String, Object> userMap = new HashMap<>();
////                                                    userMap.put("ImageUrl", uri.toString());
////                                                    userMap.put("mPotholeType", mPotholeType);
////                                                    userMap.put("mAddress", mAddress);
////                                                    userMap.put("mLandmark", mLandmark);
////                                                    userMap.put("mDimension", mDimension);
////                                                    userMap.put("mComment", mComment);
////                                                    userMap.put("mDate", mDate);
////                                                    userMap.put("mDateFull", mDateFull);
////                                                    userMap.put("mTime", mTime);
////                                                    userMap.put("mVideo", mVideo);
////
////                                                    mDatabaseRef.child(currentUserID).updateChildren(userMap);
////
////                                                }
////                                            }
////                                        }
////
////                                        @Override
////                                        public void onCancelled(@NonNull DatabaseError error) {
////
////                                        }
////                                    });
//
//                                    Upload upload = new Upload(uri.toString(), mPotholeType, mAddress, mLandmark, mDimension, mComment, mDate, mDateFull, mTime, mSeverity, mName, mEmail, mPhone, mUserId);
//                                    String uploadId = mDatabaseRef.push().getKey();
//                                    assert uploadId != null;
//                                    mDatabaseRef.child(uploadId).setValue(upload);
//
//                                }
//                            });
//                            /*Toast.makeText(Image_video_upload.this, "Upload Successful", Toast.LENGTH_SHORT).show();
//                               Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
//                               while (!urlTask.isSuccessful()) ;
//                               Uri downloadUrl = urlTask.getResult();
//                               Upload upload = new Upload(mEditTextFilename.getText().toString().trim(),downloadUrl.toString());
//                               String uploadId = mDatabaseRef.push().getKey();
//                               mDatabaseRef.child(uploadId).setValue(upload);*/
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(ReportPotholeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                            mProgressBar.setProgress((int) progress);
//                        }
//                    });
//        }
//
//       /* if (mVideoUri != null){
//            StorageReference reference = mStorageRefVideo.child(System.currentTimeMillis()+"."+getfileExt(mVideoUri));
//
//            reference.putFile(mVideoUri)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            mProgressBar.setVisibility(View.INVISIBLE);
//                            Toast.makeText(getApplicationContext(),"Video Successfully Uploaded",Toast.LENGTH_SHORT).show();
//                            Upload upload = new Upload(mEditTextFilename.getText().toString().trim(),
//                                    taskSnapshot.getUploadSessionUri().toString());
//                            String uploadId = mDatabaseRefVideo.push().getKey();
//                            mDatabaseRefVideo.child(uploadId).setValue(upload);
//                        }
//                    })
//
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
//                        }
//                    });
//        }*/
//
//        else {
//            Toast.makeText(this,"No File Selected",Toast.LENGTH_SHORT).show();
//        }
//    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MicrophoneHelper.REQUEST_PERMISSION);
    }
    //Record a message via Watson Speech to Text
    private void recordMessage() {
        if (listening != true) {
            capture = microphoneHelper.getInputStream(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        speechService.recognizeUsingWebSocket(getRecognizeOptions(capture), new MicrophoneRecognizeDelegate());
                    } catch (Exception e) {
                        showError(e);
                    }
                }
            }).start();
            listening = true;
            Toast.makeText(ReportPotholeActivity.this, "Listening....Click to Stop", Toast.LENGTH_LONG).show();

        } else {
            try {
                microphoneHelper.closeInputStream();
                listening = false;
                Toast.makeText(ReportPotholeActivity.this, "Stopped Listening....Click to Start", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // Check for network connections
        if (isConnected) {
            return true;
        } else {
            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    //Private Methods - Speech to Text
    private RecognizeOptions getRecognizeOptions(InputStream audio) {
        return new RecognizeOptions.Builder()
                .audio(audio)
                .contentType(ContentType.OPUS.toString())
                .model("en-US_BroadbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
    }

    private void showMicText(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                inputMessage.getEditText().setText(text);
            }
        });
    }

    private void enableMicButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnRecord.setEnabled(true);
            }
        });
    }

    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ReportPotholeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    //Watson Speech to Text Methods.
    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback {
        @Override
        public void onTranscription(SpeechRecognitionResults speechResults) {
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                showMicText(text);
            }
        }

        @Override
        public void onError(Exception e) {
            showError(e);
            enableMicButton();
        }

        @Override
        public void onDisconnected() {
            enableMicButton();
        }

    }

    public void getSpeechInput(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    private void userInfoDisplay(final TextInputLayout phoneNumber) {
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Elected").child(currentUserID);

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String phone = String.valueOf(dataSnapshot.child("phone").getValue());
                    phoneNumber.getEditText().setText(phone);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}