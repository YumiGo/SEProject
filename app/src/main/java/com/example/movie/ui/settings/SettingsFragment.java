package com.example.movie.ui.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.movie.MainActivity;
import com.example.movie.R;
import com.example.movie.SplashActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    ListView listView;
    MainActivity MA = (MainActivity)MainActivity.Main_Activity; //메인 액티비티
    @Override
    public void onStart() {
        super.onStart();

        listView=(ListView)this.getView().findViewById(R.id.listView);

        ArrayList<String> items=new ArrayList<>();
        items.add("제작자");
        items.add("버전정보");
        items.add("오류신고");
        items.add("로그아웃");

        CustomAdapter adapter=new CustomAdapter(getActivity(), 0, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;
                if(position==0){
                    //intent=new Intent(getActivity(), AlarmActivity.class);
                    //startActivity(intent);
                }
                else if(position==1){
                    //intent=new Intent(getActivity(), SettingActivity.class);
                    //startActivity(intent);
                }
                else if(position==2){
                    //intent=new Intent(getActivity(), SettingActivity.class);
                    //startActivity(intent);
                }
                else if(position==3){
                    AlertDialog.Builder alBuilder = new AlertDialog.Builder(getActivity());
                    alBuilder.setMessage("로그아웃 하시겠습니까?");

                    // "예" 버튼을 누르면 실행되는 리스너
                    alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            FirebaseAuth.getInstance().signOut();//로그아웃
                            Intent intent = new Intent(getContext(), SplashActivity.class);
                            startActivity(intent);
                            getActivity().finish();

                        }
                    });
                    // "아니오" 버튼을 누르면 실행되는 리스너
                    alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return; // 아무런 작업도 하지 않고 돌아간다
                        }
                    });
                    alBuilder.setTitle("로그아웃");
                    alBuilder.show(); // AlertDialog.Bulider로 만든 AlertDialog를 보여준다.
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    private class CustomAdapter extends ArrayAdapter<String> {
        private ArrayList<String> items;

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
            super(context, textViewResourceId, objects);
            this.items = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = LayoutInflater.from(parent.getContext());
                view = vi.inflate(R.layout.fragment_more_listview_item, null);
            }

            ImageView imageView = (ImageView)view.findViewById(R.id.iv_photo);

            if("로그아웃".equals(items.get(position)))
                imageView.setImageResource(R.drawable.ic_baseline_login_24);
            else if("제작자".equals(items.get(position)))
                imageView.setImageResource(R.drawable.ic_baseline_tag_faces_24);
            else if("버전 정보".equals(items.get(position)))
                imageView.setImageResource(R.drawable.ic_baseline_description_24);
            else if("오류 신고".equals(items.get(position)))
                imageView.setImageResource(R.drawable.ic_baseline_warning_24);

            TextView textView = (TextView)view.findViewById(R.id.textView);
            textView.setText(items.get(position));
            return view;
        }
    }

}