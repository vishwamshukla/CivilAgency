package com.example.civilagency;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener,NavigationView.OnNavigationItemSelectedListener {

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

    private String Lang;
    private ActionBarDrawerToggle nToggle;
    NavigationView navigationView;

    private RecyclerView mRecyclerView;
    private ImageAdapter mImageAdapter;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference UserRef;

    private ProgressBar mProgressCircle;

    private ValueEventListener mDBListener;
    private List<Upload> mUploads;

    private FirebaseAuth mAuth;
    String currentUserID;

    LinearLayoutManager mlinearLayoutManager;


    private CircleImageView profileImage;
    private TextView nametextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        @SuppressLint("WrongViewCast") Toolbar my_toolbar = findViewById(R.id.actionBar);
        my_toolbar.setTitle("");
        setSupportActionBar(my_toolbar);

        DrawerLayout nDrawerLayout = findViewById(R.id.navigationMenu);
        nToggle = new ActionBarDrawerToggle(this, nDrawerLayout, R.string.open, R.string.close);

        nDrawerLayout.addDrawerListener(nToggle);
        nToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        mlinearLayoutManager = new LinearLayoutManager(this);
//        mlinearLayoutManager.setReverseLayout(true);
//        mlinearLayoutManager.setStackFromEnd(true);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mlinearLayoutManager);

        mProgressCircle = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();
        mImageAdapter = new ImageAdapter(HomeActivity.this,mUploads);
        mRecyclerView.setAdapter(mImageAdapter);
        mImageAdapter.setOnItemClickListener(HomeActivity.this);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        Log.i("image", String.valueOf(mAuth.getCurrentUser().getPhotoUrl()));
        Log.i("name", String.valueOf(mAuth.getCurrentUser().getDisplayName()));

        View headerView = navigationView.getHeaderView(0);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
        });
        final TextView userNameTextView = headerView.findViewById(R.id.name_textView);
        final CircleImageView profileImageView = headerView.findViewById(R.id.nav_header_profile_imageView);

        UserRef = FirebaseDatabase.getInstance().getReference("Users").child("Civil").child(currentUserID);

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("image").exists())
                    {
                        String image = String.valueOf(dataSnapshot.child("image").getValue());
                        //String name = String.valueOf(dataSnapshot.child("name").getValue());

                        Picasso.get().load(image).into(profileImageView);
                        //userNameTextView.setText(name);

                    }
                    String name = String.valueOf(dataSnapshot.child("name").getValue());
                    userNameTextView.setText(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStorage = FirebaseStorage.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Reports");

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                mUploads.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()){
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }

                mImageAdapter.notifyDataSetChanged();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });

        findViewById(R.id.report_pothole_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, ReportPotholeActivity.class));
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (nToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String languageToLoad = "en";
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                break;
//            case R.id.about_us:
//                startActivity(new Intent(HomeActivity.this, AboutUs.class));
//                break;
            case R.id.help:
                Intent Getintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vishwamshukla.intelaedu.com/"));
                startActivity(Getintent);
                break;
            case R.id.map_card_view:
                startActivity(new Intent(HomeActivity.this, MapActivity.class));
                break;
//            case R.id.chats:
//                startActivity(new Intent(HomeActivity.this, ChatsActivity.class));
//                break;
            //case R.id.hi:
               // Lang=("hi-rIN");
                //break;
            //case R.id.gu:
              //  Lang=("gu-rIN");
                //break;

            //case R.id.ta:
            //  Lang=("ta-rIN");
                //break;

   //         case R.id.pa:
 //               Lang=("pa-rIN");
  //              break;

    //        case R.id.mr:
      //          Lang=("mr-rIN");
        //        break;
          //  default:
            //    break;
        }
        Locale locale = new Locale(Lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        return false;
    }


    public void onItemClick(int position) {

        Intent detailIntent = new Intent(this,DetailsPothole.class);
        Upload clickeditem = mUploads.get(position);

        detailIntent.putExtra(EXTRA_URL, clickeditem.getImageUrl());
        detailIntent.putExtra(EXTRA_POTHOLE_TYPE, clickeditem.getmPotholeType());
        detailIntent.putExtra(EXTRA_ADDRESS, clickeditem.getmAddress());
        detailIntent.putExtra(EXTRA_LANDMARK, clickeditem.getmLandmark());
        detailIntent.putExtra(EXTRA_DIMENSION, clickeditem.getmDimension());
        detailIntent.putExtra(EXTRA_COMMENT, clickeditem.getmComment());
        detailIntent.putExtra(EXTRA_PHONE, clickeditem.getmPhone());
        detailIntent.putExtra(EXTRA_TIMEKEY, clickeditem.getmTimeKey());
        detailIntent.putExtra(EXTRA_USERID, clickeditem.getmUserId());
        detailIntent.putExtra(EXTRA_STATUS, clickeditem.getStatus());

        startActivity(detailIntent);

    }
    public void onDeleteClick(int position) {
        Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(selectedKey).removeValue();
                Toast.makeText(HomeActivity.this, "Item Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy(){
            super.onDestroy();
            mDatabaseRef.removeEventListener(mDBListener);
        }

        @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        finish();
    }
}
