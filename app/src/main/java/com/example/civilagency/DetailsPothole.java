package com.example.civilagency;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;


public class DetailsPothole extends AppCompatActivity {

    public static final String EXTRA_URL = "image url";
    public static final String EXTRA_POTHOLE_TYPE = "pothole type";
    public static final String EXTRA_LANDMARK = "landmark";
    public static final String EXTRA_ADDRESS = "address";
    public static final String EXTRA_DIMENSION = "dimension";
    public static final String EXTRA_COMMENT = "comment";

    Button back_btn;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private FirebaseAuth mAuth;
    String currentUserID;
    private List<Upload> mUploads;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_pothole);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child("Citizens").child(currentUserID).child("potholeReports");
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

        ImageView imageView = findViewById(R.id.pothole_image_view);
        TextView pothole_type_textView = findViewById(R.id.pothole_type_textView);
        TextView landmark_textView = findViewById(R.id.pothole_landmark_textview);
        TextView address_textView = findViewById(R.id.pothole_address_textView);
        TextView dimension_textView = findViewById(R.id.pothole_dimension_textview);
        TextView comment_textView = findViewById(R.id.potholes_comments_textview);

        Picasso.get().load(imageUrl).fit().into(imageView);
        pothole_type_textView.setText(potholeType);
        landmark_textView.setText(landmark);
        address_textView.setText(address);
        dimension_textView.setText(dimension);
        comment_textView.setText(comment);


    }

    private void onDeleteClick(int position) {
        Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();
        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(DetailsPothole.this, "Item Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}