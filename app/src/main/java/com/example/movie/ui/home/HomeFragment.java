package com.example.movie.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.movie.R;
import com.example.movie.data.Movie;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//영화는 뷰페이저2 레이아웃으로 좌우 스크롤
public class HomeFragment extends Fragment {
    
    private final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyyMMdd");
    String[] movie = new String[10];

    // 디버깅용
    private static String TAG = "home_fragment";
    
    // 영화 뷰
    private static ArrayList<Movie> mList;
    private static MovieAdapter mAdapter;
    private RecyclerView movieViewList;

    private static int width;
    
    // 파이어베이스
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private StorageReference storage = FirebaseStorage.getInstance().getReference();
    
    // 영화 데이터, 이미지 중복 다운로드 방지용
    private static boolean isLoaded = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // 영화 포스터 스크롤 뷰 구현
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false);

        movieViewList = root.findViewById(R.id.movieViewList);
        movieViewList.setLayoutManager(manager);
        movieViewList.getLayoutManager().scrollToPosition(Integer.MAX_VALUE / 2 + 7);

        // 영화 포스터에 따라 홈 아래쪽의 영화 정보 변경
        TextSwitcher nameSwitcher = root.findViewById(R.id.nameSwitcher);
        nameSwitcher.setInAnimation(getContext(), android.R.anim.slide_in_left);
        nameSwitcher.setOutAnimation(getContext(), android.R.anim.slide_out_right);

        TextSwitcher directorSwitcher = root.findViewById(R.id.directorSwitcher);
        directorSwitcher.setInAnimation(getContext(), android.R.anim.slide_in_left);
        directorSwitcher.setOutAnimation(getContext(), android.R.anim.slide_out_right);

        // 현재 보이는 포스터에 따라 영화 정보 텍스트 변경
        movieViewList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = ((LinearLayoutManager) movieViewList.getLayoutManager()).findFirstCompletelyVisibleItemPosition() % 10;
                if (position >= 0 && position < mList.size()) {
                    Movie movie = mList.get(position);
                    TextView tv = (TextView) nameSwitcher.getCurrentView();
                    if (!tv.getText().toString().equals(movie.getName())) {
                        nameSwitcher.setText(movie.getName());
                        directorSwitcher.setText(movie.getDirector());
                    }
                }
            }
        });


        // 영화 데이터 중복 다운 방지
        if (!isLoaded) {
            isLoaded = true;

            mList = new ArrayList<>();
            mAdapter = new MovieAdapter(getActivity().getApplicationContext(), mList);
            
            // 영화 정보 받아오기
            database.child("movie").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int movieCounter = 0;

                    for (DataSnapshot child : task.getResult().getChildren()) {
                        Movie movie = child.getValue(Movie.class);
                        Log.d(TAG, "영화 데이터 불러오기: " + movie.toString());

                        /* 주의 건들지 말 것 (데이터베이스 초기화) */
                        //initDatabase(movieCounter++);

                        if (movie.getId() == 0) {
                            nameSwitcher.setText(movie.getName());
                            directorSwitcher.setText(movie.getDirector());
                        }

                        // 파이어베이스 데이터 절약용 이미지 캐싱
                        String cachePath = getContext().getCacheDir().getPath() + "/" + movie.getImageName();
                        File cacheFile = new File(cachePath);
                        if (cacheFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(cachePath);
                            movie.setImage(bitmap);
                            mList.add(movie);
                            mAdapter.notifyDataSetChanged();
                            Log.d(TAG, "이미지 캐싱: " + movie.getImageName());
                            continue;
                        }

                        // 파이어베이스에서 영화 포스터 받아오기
                        final long TWO_MEGABYTE = 1024 * 1024 * 12;
                        StorageReference ref = storage.child("movie image/" + movie.getImageName());
                        ref.getBytes(TWO_MEGABYTE).addOnSuccessListener(bytes -> {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            movie.setImage(bitmap);
                            mList.add(movie);
                            mAdapter.notifyDataSetChanged();
                            // 이미지 캐싱
                            storeImage(bitmap, movie.getImageName());
                            Log.d(TAG, "이미지 다운: " + movie.getImageName());
                        });
                    }
                    autoScroll();
                }
            });
        }
        else
            autoScroll();

        movieViewList.setAdapter(mAdapter);

        return root;
    }

    // 데이터베이스 완전 초기화
    private void initDatabase(int count) {
        // DB 임시 초기화                                      (강동 메가박스)           (분노의 질주)
        DatabaseReference movieRef = database.child("theater").child("0").child("movie").child(String.valueOf(count));

        // 날짜 초기화
        int dateCounter;
        for (dateCounter = 0; dateCounter < 14; dateCounter++) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, dateCounter);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int date = calendar.get(Calendar.DATE);

            String today = year + "년 " + month + "월 " + date + "일";

            movieRef.child("date").child(String.valueOf(dateCounter)).setValue(today);
        }

        // 타임 테이블 초기화
        for (int i = 0; i < dateCounter; i++) {
            DatabaseReference timeTableRef = movieRef.child("time").child(String.valueOf(i)).child("time table");

            int numOfTimeTable = ThreadLocalRandom.current().nextInt(1, 4 + 1);

            for (int j = 0; j < numOfTimeTable; j++) {
                StringBuffer buffer = new StringBuffer();

                if (j < numOfTimeTable / 2) {
                    buffer.append("오전 ");
                    int hour = ThreadLocalRandom.current().nextInt(7, 11 + 1);
                    buffer.append(hour).append("시");

                    int minutes = ThreadLocalRandom.current().nextInt(0, 11 + 1) * 5;
                    if (minutes != 0)
                        buffer.append(" ").append(minutes).append("분");
                }
                else {
                    buffer.append("오후 ");
                    int hour = ThreadLocalRandom.current().nextInt(1, 7 + 1);
                    buffer.append(hour).append("시");

                    int minutes = ThreadLocalRandom.current().nextInt(0, 11 + 1) * 5;
                    if (minutes != 0)
                        buffer.append(" ").append(minutes).append("분");
                }
                timeTableRef.child(String.valueOf(j)).setValue(buffer.toString());


                // 예약 초기화
                DatabaseReference resvRef = movieRef.child("time").child(String.valueOf(i)).child("reservation").child(String.valueOf(j));
                resvRef.child("reserved").child("0").setValue(0);
                resvRef.child("reserving").child("0").setValue(0);
                resvRef.child("the number of seats reserved").setValue(0);
                resvRef.child("the number of seats reserving").setValue(0);
            }
        }
    }




    // 영화 포스터 자동으로 스크롤 해주기
    public void autoScroll() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                movieViewList.scrollBy(1, 0);
                handler.postDelayed(this, 0);
            }
        };
        handler.postDelayed(runnable, 0);
    }

    // 다운받은 이미지를 내부 저장소에 따로 저장(캐싱)
    private void storeImage(Bitmap image, String imageName) {
        String path = getContext().getCacheDir().getPath() + "/" + imageName;
        File pictureFile = new File(path);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public String makeQueryString(Map<String, String> paramMap) {
        final StringBuilder sb = new StringBuilder();

        paramMap.entrySet().forEach(( entry )->{
            if( sb.length() > 0 ) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        });

        return sb.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void requestAPI() {
        // 하루전 날짜
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);

        Map<String, String> paramMap = new HashMap<>();
        String AUTH_KEY = "e95e47ae1583b95618b9eaa7168f7449";
        paramMap.put("key"          , AUTH_KEY);
        paramMap.put("targetDt"     , DATE_FMT.format(cal.getTime()));
        paramMap.put("itemPerPage"  , "10");

        try {
            String REQUEST_URL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
            URL requestURL = new URL(REQUEST_URL +"?"+makeQueryString(paramMap));
            HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();

            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // 응답(Response) 구조 작성
            //   - Stream -> JSONObject
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String readline = null;
            StringBuffer response = new StringBuffer();
            while ((readline = br.readLine()) != null) {
                response.append(readline);
            }

            // JSON 객체로  변환
            JSONObject responseBody = new JSONObject(response.toString());

            // 데이터 추출
            JSONObject boxOfficeResult = responseBody.getJSONObject("boxOfficeResult");

            // 박스오피스 목록 출력
            JSONArray dailyBoxOfficeList = boxOfficeResult.getJSONArray("dailyBoxOfficeList");
            for (int i = 0; i < dailyBoxOfficeList.length(); i++){
                JSONObject boxOffice = dailyBoxOfficeList.getJSONObject(i);
                movie[i] = (String) boxOffice.get("rnum");

                HashMap<String, Object> map = new HashMap<>();
                map.put("id", i);
                map.put("name", boxOffice.get("movieNm"));
                map.put("content", "");
                map.put("director", "");
                map.put("imageName", i + ".jpg");

                //System.out.printf(movie[i] + '\n');
                System.out.println(boxOffice);

                //database.child("movie").child(String.valueOf(i)).setValue(map);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}