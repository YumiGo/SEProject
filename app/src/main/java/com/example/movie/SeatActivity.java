package com.example.movie;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SeatActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "SeatActivity";
    private int numOfPeople;    // 인원 수
    private HashSet<Integer> selectedSeatList;  // 내가 예약하려는 좌석
    private Button confirmSeatButton;   // 확인 버튼
    private ArrayList<Integer> reservedSeat;    // 이미 예약된 좌석
    private ArrayList<Integer> reservingSeat;   // 예약중인 좌석

    ViewGroup layout;

    String seats = "_AAAAAAAAAAAAAAA_/"
            + "_________________/"
            + "AA__AAAAAAAAA__AA/"
            + "AA__AAAAAAAAA__AA/"
            + "AA__AAAAAAAAA__AA/"
            + "AA__AAAAAAAAA__AA/"
            + "AA__AAAA_AAAA__AA/"
            + "AA__AAAA_AAAA__AA/"
            + "AA__AAAA_AAAA__AA/"
            + "AA__AAAA_AAAA__AA/"
            + "_________________/"
            + "AA_AAAAAAAAAAA_AA/"
            + "AA_AAAAAAAAAAA_AA/"
            + "AA_AAAAAAAAAAA_AA/"
            + "AA_AAAAAAAAAAA_AA/"
            + "_________________/";

            /*
            "_UUUUUUAAAAARRRR_/"
            + "_________________/"
            + "UU__AAAARRRRR__RR/"
            + "UU__UUUAAAAAA__AA/"
            + "AA__AAAAAAAAA__AA/"
            + "AA__AARUUUURR__AA/"
            + "UU__UUUA_RRRR__AA/"
            + "AA__AAAA_RRAA__UU/"
            + "AA__AARR_UUUU__RR/"
            + "AA__UUAA_UURR__RR/"
            + "_________________/"
            + "UU_AAAAAAAUUUU_RR/"
            + "RR_AAAAAAAAAAA_AA/"
            + "AA_UUAAAAAUUUU_AA/"
            + "AA_AAAAAAUUUUU_AA/"
            + "_________________/";

             */


    List<TextView> seatViewList = new ArrayList<>();
    int seatSize = 100;
    int seatGaping = 10;

    int STATUS_AVAILABLE = 1;
    int STATUS_BOOKED = 2;
    int STATUS_RESERVED = 3;
    String selectedIds = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat);

        numOfPeople = getIntent().getIntExtra("numOfPeople", 0);
        selectedSeatList = (HashSet<Integer>) getIntent().getSerializableExtra("selectedSeatList");
        reservedSeat = (ArrayList<Integer>) getIntent().getSerializableExtra("reservedSeat");
        reservingSeat = (ArrayList<Integer>) getIntent().getSerializableExtra("reservingSeat");

        getSupportActionBar().setTitle("좌석선택");
        confirmSeatButton = findViewById(R.id.confirmSeatButton);


        layout = findViewById(R.id.layoutSeat);

        seats = "/" + seats;

        LinearLayout layoutSeat = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutSeat.setOrientation(LinearLayout.VERTICAL);
        layoutSeat.setLayoutParams(params);
        layoutSeat.setPadding(8 * seatGaping, 8 * seatGaping, 8 * seatGaping, 8 * seatGaping);
        layout.addView(layoutSeat);

        LinearLayout layout = null;

        int count = 0;

        for (int index = 0; index < seats.length(); index++) {
            if (seats.charAt(index) == '/') {
                layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layoutSeat.addView(layout);
            }
            else if (seats.charAt(index) == 'A') {
                count++;
                TextView view = new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setPadding(0, 0, 0, 2 * seatGaping);
                view.setId(count);
                view.setGravity(Gravity.CENTER);
                view.setText(count + "");
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);

                // 예약된 좌석
                if (reservedSeat.contains(count)) {
                    view.setBackgroundResource(R.drawable.ic_seats_booked);
                    view.setTextColor(Color.WHITE);
                    view.setTag(STATUS_BOOKED);
                }
                else {  // 가능한 좌석
                    view.setTextColor(Color.BLACK);
                    view.setTag(STATUS_AVAILABLE);
                    // 전에 이미 선택해놓은 좌석 표시
                    if(selectedSeatList.contains(count)) {
                        Log.d("선택", String.valueOf(count));
                        selectedIds = selectedIds + view.getId() + ",";
                        view.setBackgroundResource(R.drawable.ic_seats_selected);
                        numOfPeople--;
                    }
                    else
                        view.setBackgroundResource(R.drawable.ic_seats_book);
                }
                layout.addView(view);
                seatViewList.add(view);
                view.setOnClickListener(this);

            } else if (seats.charAt(index) == '_') {
                TextView view = new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setBackgroundColor(Color.TRANSPARENT);
                view.setText("");
                layout.addView(view);
            }
            /*
            else if (seats.charAt(index) == 'U') {  // 예약된 좌석
                count++;
                TextView view = new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setPadding(0, 0, 0, 2 * seatGaping);
                view.setId(count);
                view.setGravity(Gravity.CENTER);
                view.setBackgroundResource(R.drawable.ic_seats_booked);
                view.setText(count + "");
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
                view.setTextColor(Color.WHITE);
                view.setTag(STATUS_BOOKED);
                layout.addView(view);
                seatViewList.add(view);
                view.setOnClickListener(this);
            } else if (seats.charAt(index) == 'A') {    // 가능한 좌석
                count++;
                TextView view = new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setPadding(0, 0, 0, 2 * seatGaping);
                view.setId(count);
                view.setGravity(Gravity.CENTER);
                view.setBackgroundResource(R.drawable.ic_seats_book);
                view.setText(count + "");
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
                view.setTextColor(Color.BLACK);
                view.setTag(STATUS_AVAILABLE);
                layout.addView(view);
                seatViewList.add(view);
                view.setOnClickListener(this);
                // 전에 이미 선택해놓은 좌석 표시
                if(selectedSeatList.contains(count)) {
                    Log.d("선택", String.valueOf(count));
                    selectedIds = selectedIds + view.getId() + ",";
                    view.setBackgroundResource(R.drawable.ic_seats_selected);
                    numOfPeople--;
                }
            } else if (seats.charAt(index) == 'R') {    // 예약중인 좌석
                count++;
                TextView view = new TextView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(seatSize, seatSize);
                layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                view.setLayoutParams(layoutParams);
                view.setPadding(0, 0, 0, 2 * seatGaping);
                view.setId(count);
                view.setGravity(Gravity.CENTER);
                view.setBackgroundResource(R.drawable.ic_seats_reserved);
                view.setText(count + "");
                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
                view.setTextColor(Color.WHITE);
                view.setTag(STATUS_RESERVED);
                layout.addView(view);
                seatViewList.add(view);
                view.setOnClickListener(this);
            }
            */
        }


        if (numOfPeople == 0)
            confirmSeatButton.setText("선택 완료");
        else
            confirmSeatButton.setText("선택가능한 인원: " + numOfPeople);
    }

    @Override
    public void onClick(View view) {
        if ((int) view.getTag() == STATUS_AVAILABLE) {
            if (selectedIds.contains(view.getId() + ",")) {
                selectedIds = selectedIds.replace(+view.getId() + ",", "");
                view.setBackgroundResource(R.drawable.ic_seats_book);
                numOfPeople++;
                selectedSeatList.remove(view.getId());
                if (numOfPeople == 0)
                    confirmSeatButton.setText("선택 완료");
                else
                    confirmSeatButton.setText("선택가능한 인원: " + numOfPeople);
            } else if (numOfPeople <= 0) {
                Toast.makeText(this, "좌석이 이미 인원수만큼 선택되었습니다", Toast.LENGTH_SHORT).show();
            } else {
                selectedIds = selectedIds + view.getId() + ",";
                view.setBackgroundResource(R.drawable.ic_seats_selected);
                numOfPeople--;
                selectedSeatList.add(view.getId());
                if (numOfPeople == 0)
                    confirmSeatButton.setText("선택 완료");
                else
                    confirmSeatButton.setText("선택가능한 인원: " + numOfPeople);
            }
        } else if ((int) view.getTag() == STATUS_BOOKED) {
            Toast.makeText(this, "Seat " + view.getId() + " is Booked", Toast.LENGTH_SHORT).show();
        } else if ((int) view.getTag() == STATUS_RESERVED) {
            Toast.makeText(this, "Seat " + view.getId() + " is Reserved", Toast.LENGTH_SHORT).show();
        }
    }

    // 선택된 좌석 데이터베이스에 등록, ReservationActivity에 넘겨주기
    public void confirmSeat(View v) {
        if (numOfPeople != 0) {
            Toast.makeText(this, "인원수만큼 좌석을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("selectedSeatList", selectedSeatList);
        setResult(1, intent);
        finish();
    }

    // 뒤로가기 버튼 누르면 취소 취급
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
        alBuilder.setMessage("이미 선택된 좌석도 취소됩니다.\n취소하시겠습니까?");

        alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "좌석 선택이 취소되었습니다", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("selectedSeatList", new HashSet<>());
                setResult(1, intent);
                finish();
            }
        });
        alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alBuilder.setTitle("좌석선택 취소");
        alBuilder.show();
    }
}
