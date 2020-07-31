package com.example.civilagency;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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

    Button button_update_pothole_status;

    Button back_btn;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;
    String currentUserID;
    private List<Upload> mUploads;
    SeekBar report_status_seekbar;
    ImageView proof_view_pothole_image_view;

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

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Reports");
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
        final String timeKey = intent.getStringExtra(EXTRA_TIMEKEY);

        ImageView imageView = findViewById(R.id.pothole_image_view);
        TextView pothole_type_textView = findViewById(R.id.pothole_type_textView);
        TextView landmark_textView = findViewById(R.id.pothole_landmark_textview);
        TextView address_textView = findViewById(R.id.pothole_address_textView);
        TextView dimension_textView = findViewById(R.id.pothole_dimension_textview);
        TextView comment_textView = findViewById(R.id.potholes_comments_textview);
        TextView phone_textView = findViewById(R.id.potholes_phonenumber);
        final TextView mTimeKey = findViewById(R.id.potholes_timekey);

        Picasso.get().load(imageUrl).fit().into(imageView);
        pothole_type_textView.setText(potholeType);
        landmark_textView.setText(landmark);
        address_textView.setText(address);
        dimension_textView.setText(dimension);
        comment_textView.setText(comment);
        phone_textView.setText(phone);
        mTimeKey.setText(timeKey);

        final SeekBar severity_seekBar = findViewById(R.id.seekbar_bar_pothole);
        severity_seekBar.getThumb().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
        final TextView text_view_pothole_status = findViewById(R.id.text_view_pothole_status);

//        progress_of_pothole = TODO: Read from DB and uncomment this.

        switch (progress_of_pothole){
            case 0:
                break;
            case 1:
                text_view_pothole_status.setText(Progress.Reported.toString());
                severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#f44336"), PorterDuff.Mode.SRC_IN);
                severity_seekBar.setProgress(1);
                break;
            case 2:
                text_view_pothole_status.setText(Progress.Processing.toString());
                severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#ff9800"), PorterDuff.Mode.SRC_IN);
                severity_seekBar.setProgress(2);
                break;
            case 3:
                text_view_pothole_status.setText(Progress.Midway.toString());
                severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#ffeb3b"), PorterDuff.Mode.SRC_IN);
                severity_seekBar.setProgress(3);
                break;
            case 4:
                text_view_pothole_status.setText(Progress.Completed.toString());
                severity_seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#4caf50"), PorterDuff.Mode.SRC_IN);
                severity_seekBar.setProgress(4);
                break;
            default:
                break;
        }

        severity_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                button_update_pothole_status = findViewById(R.id.button_update_pothole_status);
                button_update_pothole_status.setVisibility(View.VISIBLE);
                button_update_pothole_status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        button_update_pothole_status.setVisibility(View.GONE);
                        final RelativeLayout layout_proof = findViewById(R.id.layout_proof);
                        layout_proof.setVisibility(View.VISIBLE);

                        button_remove_image = findViewById(R.id.proof_view_pothole_remove_image_button);
                        CardView image_layout = findViewById(R.id.pothole_image_layout);
                        proof_view_pothole_image_view = findViewById(R.id.proof_view_pothole_image_view);

                        image_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final CharSequence[] items ={"Open Camera","Upload from Gallery"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsPothole.this);
                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (items[i].equals("Open Camera")){
                                            //TODO: Complete this function
                                            update_imageView_layout(true);
                                        }
                                        else if (items[i].equals("Upload from Gallery")){
                                            //TODO: Complete this function
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
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Reports").child(timeKey);
                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("users").child("Citizens").child(timeKey);
//                                String mStatus = text_view_pothole_status.getText().toString();
//                                Upload1 upload = new Upload1(mStatus);
//                                ref.push().setValue(upload);


                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("status", text_view_pothole_status.getText().toString());
                                ref.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(DetailsPothole.this, "Updated", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                layout_proof.setVisibility(View.GONE);
                            }
                        });

                        findViewById(R.id.proof_view_button_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                layout_proof.setVisibility(View.GONE);
                            }
                        });
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