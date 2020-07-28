package com.example.electedperson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle nToggle;
    NavigationView navigationView;

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
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
//        switch (item.getItemId()) {
//            case R.id.logout:
//                FirebaseAuth.getInstance().signOut();
//                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
//                break;
//            case R.id.about_us:
//                startActivity(new Intent(HomeActivity.this, AboutUs.class));
//                break;
//            case R.id.help:
//                Intent Getintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vishwamshukla.intelaedu.com/"));
//                startActivity(Getintent);
//                break;
//            case R.id.chats:
//                startActivity(new Intent(HomeActivity.this, ChatsActivity.class));
//                break;
//        }
        return false;
    }
}