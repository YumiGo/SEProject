package com.example.movie.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.movie.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
//영화는 뷰페이저2 레이아웃으로 좌우 스크롤
//상영 시간대는 임의로 만들어야 할듯 (9시 12시 15시 18시 21시 정도..)
public class HomeFragment extends Fragment {
    /*영화진흥위원회*/
    String kobisURL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
    //박스오피스 순위 영화 제목, 상영관 받아오기
    String key = "e95e47ae1583b95618b9eaa7168f7449";//영화진흥위원회 키
    String rank;//박스오피스 순위
    String targetDt = "20210520";//yyyymmdd 형식
    String movieNm;//영화 제목(국문)

    /*네이버 검색*/
    String query;
    String naverURL;//이미지 받아오기
    String clientId = "z3dZAySkBm3v1rjhLZox";//네이버 API
    String clientSecret = "fNAlOwjEp2"; //네이버 API
    int display = 1;//네이버 검색 출력 건수
    int start = 1;//네이버 검색 시작 위치
    String image;//영화 이미지 url

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        //영화 API - 검색한 영화 제목, 이미지 url 가져오기



        Thread thread = new Thread(new Runnable() {
            URL url = null;
            @Override
            public void run() {
                try  {
                    query = URLEncoder.encode("분노의 질주", "UTF-8");
                    naverURL = "https://openapi.naver.com/v1/search/movie.json?query=" + query;
                    url = new URL(naverURL);
                    Map<String, String> requestHeaders = new HashMap<>();
                    requestHeaders.put("X-Naver-Client-Id", clientId);
                    requestHeaders.put("X-Naver-Client-Secret", clientSecret);
                    String responseBody = get(naverURL,requestHeaders);
                    parseData(responseBody);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                }
            });
        thread.start();
        return root;
        }



    private static String get(String apiUrl, Map<String, String> requestHeaders){
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }

        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }

    private static void parseData(String responseBody) {
        String title;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(responseBody.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                title = item.getString("title");
                System.out.println("TITLE : " + title);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}