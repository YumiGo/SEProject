package com.example.movie.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import com.example.movie.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//영화는 뷰페이저2 레이아웃으로 좌우 스크롤
public class HomeFragment extends Fragment {
    private final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyyMMdd");
    String[] movie = new String[10];
    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    requestAPI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        return root;
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

        Map<String, String> paramMap = new HashMap<String, String>();
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
                movie[i] = (String) boxOffice.get("movieNm");
                System.out.printf(movie[i] + '\n');
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}