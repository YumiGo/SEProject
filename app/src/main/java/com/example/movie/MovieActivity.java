package com.example.movie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class MovieActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        
        // 영화 정보
        int id = getIntent().getIntExtra("id", 0);
        String name = getIntent().getStringExtra("name");
        String director = getIntent().getStringExtra("director");
        String content = getIntent().getStringExtra("content");
        String imageName = getIntent().getStringExtra("imageName");

        getSupportActionBar().setTitle(name);

        // 영화 포스터 캐시에서 가져오기
        String cachePath = getApplicationContext().getCacheDir().getPath() + "/" + imageName;
        File cacheFile = new File(cachePath);
        if (cacheFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(cachePath);

            ImageView movieImageView = findViewById(R.id.movieImageView);
            movieImageView.setImageBitmap(bitmap);
        }

        TextView movieName = findViewById(R.id.movieName);
        movieName.setText(name);

        TextView movieDirector = findViewById(R.id.movieDirector);
        movieDirector.setText(director);

        TextView movieContent = findViewById(R.id.movieContent);
        movieContent.setText(content);

        // 영화 예약 엑티비티
        Button reservationButton = findViewById(R.id.reservationButton);
        reservationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReservationActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                intent.putExtra("director", director);
                intent.putExtra("content", content);
                intent.putExtra("imageName", imageName);
                startActivity(intent);
            }
        });
    }
}
