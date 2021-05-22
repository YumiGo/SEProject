package com.example.movie.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.movie.R;


public class BugReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_report);
        getSupportActionBar().setTitle("Bug Report");
        Button btn_send = (Button)findViewById(R.id.btn_send);
        btn_send.setOnClickListener(new TextView.OnClickListener() {
            public void onClick(View view) {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                String[] address = {"kumi1224@gachon.ac.kr"};
                email.putExtra(Intent.EXTRA_EMAIL, address);
                startActivity(email);
            }
        });
    }

}
