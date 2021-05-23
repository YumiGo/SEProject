package com.example.movie.ui.mypage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movie.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyPageFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_page, container, false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user.getEmail();
        TextView user_email = root.findViewById(R.id.user_email);
        user_email.setText(email);

        RecyclerView recyclerView = (RecyclerView)root.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        MyPageAdapter adapter = new MyPageAdapter();
        /*샘플데이터 - 예매한 영화 목록*/
        adapter.addItem(new MyPageMovie("미나리", "2021.05.26", "10:20", "CGV 판교","2", "439320802583380"));
        adapter.addItem(new MyPageMovie("분노의 질주: 더 얼티메이트", "2021.05.29", "17:30", "강동 메가박스","1", "304923094029480"));
        recyclerView.setAdapter(adapter);
        /*샘플데이터 - 관람한 영화 목록*/
        adapter.addItem(new MyPageMovie("귀멸의 칼날", "2021.04.21", "11:35", "CGV 가든파이브","1", "24342344380"));
        adapter.addItem(new MyPageMovie("도라에몽: 스탠바이미 2", "2021.04.20", "10:30", "강동 메가박스","2", "1232356680"));
        adapter.addItem(new MyPageMovie("비와 당신의 이야기", "2021.04.18", "17:30", "강동 메가박스","1", "23646029480"));
        return root;
    }
}