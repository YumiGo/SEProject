package com.example.movie.ui.mypage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movie.R;
import com.example.movie.ui.home.MovieAdapter;

import java.util.ArrayList;

public class MyPageAdapter extends RecyclerView.Adapter<MyPageAdapter.ViewHolder>{
    ArrayList<MyPageMovie> items = new ArrayList<MyPageMovie>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.mypage_item, viewGroup, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        MyPageMovie item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        TextView textView2;
        TextView textView3;
        TextView textView4;
        TextView textView5;
        TextView textView6;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = (TextView)itemView.findViewById(R.id.textView);
            textView2 = (TextView)itemView.findViewById(R.id.textView2);
            textView3 = (TextView)itemView.findViewById(R.id.textView3);
            textView4 = (TextView)itemView.findViewById(R.id.textView4);
            textView5 = (TextView)itemView.findViewById(R.id.textView5);
            textView6 = (TextView)itemView.findViewById(R.id.textView6);
        }
        public void setItem(MyPageMovie item){
            textView.setText(item.getName());
            textView2.setText(item.getDate());
            textView3.setText(item.getTime());
            textView4.setText(item.getTheater());
            textView5.setText(item.getPerson_num());
            textView6.setText(item.getReservation_num());
        }
    }

    public void addItem(MyPageMovie item){
        items.add(item);
    }
    public void setItems(ArrayList<MyPageMovie> items){
        this.items = items;
    }

    public MyPageMovie getItem(int position){
        return items.get(position);
    }

    public void setItem(int position, MyPageMovie item){
        items.set(position, item);
    }
}
