package com.example.movie.ui.settings;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movie.R;

public class VersionInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_version_info);
        getSupportActionBar().setTitle("Version Info");
    }

}
