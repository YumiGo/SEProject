package com.example.movie.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.movie.R;
import java.util.ArrayList;

public class DeveloperActivity extends AppCompatActivity {
    ListView listView;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_developer);
        getSupportActionBar().setTitle("Developers");
        listView=(ListView)this.findViewById(R.id.listView);

        ArrayList<String> items=new ArrayList<>();
        items.add("안정우(zzxx9633@gmail.com)");
        items.add("이지헌(wlgjsdl0@gmail.com)");
        items.add("유승준(dragon041414@gmail.com)");
        items.add("고유미(kumi3994@gmail.com)");

        CustomAdapter adapter=new CustomAdapter(this, 0, items);
        listView.setAdapter(adapter);
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
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.fragment_more_listview_item, null);
            }

            ImageView imageView = (ImageView)view.findViewById(R.id.iv_photo);

            if("안정우(zzxx9633@gmail.com)".equals(items.get(position)))
                imageView.setImageResource(R.drawable.round_star_24);
            else if("이지헌(wlgjsdl0@gmail.com)".equals(items.get(position)))
                imageView.setImageResource(R.drawable.round_star_24);
            else if("유승준(dragon041414@gmail.com)".equals(items.get(position)))
                imageView.setImageResource(R.drawable.round_star_24);
            else if("고유미(kumi3994@gmail.com)".equals(items.get(position)))
                imageView.setImageResource(R.drawable.round_star_24);

            TextView textView = (TextView)view.findViewById(R.id.textView);
            textView.setText(items.get(position));

            return view;
        }
    }

}
