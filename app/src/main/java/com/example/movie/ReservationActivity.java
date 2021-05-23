package com.example.movie;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;

public class ReservationActivity extends AppCompatActivity {

    private static String TAG = "예약";
    private static int SELECT_SEAT = 1;

    // 파이어베이스
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    DatabaseReference ref;
    ArrayList<String> dateList;

    // 극장(임시, 강동 메가박스)
    private String theater = "강동 메가박스";

    // 날짜선택
    private MaterialDatePicker<Long> datePicker;
    private TextView selectedDateText;
    private String today;
    private int firebaseTodayIndex = 0;
    private int firebaseSelectedIndex = 0;


    /* 선택된 정보들 */
    // 날짜
    private int selectedYear;
    private int selectedMonth;
    private int selectedDate;

    // 상영 시간
    private ArrayList<String> times = new ArrayList<>();
    private int selectedTimePosition = 0;

    // 인원
    private int numOfAdult;
    private int numOfChild;

    // 좌석
    private HashSet<Integer> selectedSeatList;
    
    // 가격(영수증)
    private TextView receiptCalculate;
    private TextView receiptResult;
    View border;    // 경계선
    private int paymentAmount = 0;  // 결제액

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // 영화 정보
        int id = getIntent().getIntExtra("id", 0);
        String name = getIntent().getStringExtra("name");
        String director = getIntent().getStringExtra("director");
        String content = getIntent().getStringExtra("content");
        String imageName = getIntent().getStringExtra("imageName");

        getSupportActionBar().setTitle(name);

        // 영화 예약 관련 정보 받아오기
        ref = database.child("theater").child("0").child("movie").child(String.valueOf(id));

        // TODO 날짜 정보 받아오기
        dateList = new ArrayList<>();
        ref.child("date").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DataSnapshot child: task.getResult().getChildren())
                    dateList.add(child.getValue(String.class));
                Log.d(TAG, "날짜 정보: " +dateList.toString());
            }
        });
        // TODO 날짜 텍스트 뷰 초기화
        initDateText();


        // 극장 선택
        TextView theaterText = findViewById(R.id.theaterText);
        theaterText.setText(theater);

        // 인원 선택
        Spinner adultSpinner = findViewById(R.id.adultSpinner);
        initSpinner(adultSpinner, true);
        Spinner childSpinner = findViewById(R.id.childSpinner);
        initSpinner(childSpinner, false);

        // 결제 텍스트
        receiptCalculate = findViewById(R.id.receiptCalculate);
        receiptResult = findViewById(R.id.receiptResult);
        receiptResult.setText("총 " + paymentAmount + "원");

        border = findViewById(R.id.border);
        border.setBackgroundColor(receiptCalculate.getCurrentTextColor());
    }

    
    // 상영시간표 초기화
    private void initTimeTable() {
        final int[] selectedItem = new int[1];

        ListView timeTable = findViewById(R.id.timeTable);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, times) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = View.inflate(getApplicationContext(), android.R.layout.simple_list_item_1, null);

                if (position == selectedItem[0])
                    view.setBackgroundColor(Color.LTGRAY);

                TextView textView = view.findViewById(android.R.id.text1);
                textView.setText(times.get(position));

                return view;
            }
        };

        timeTable.setAdapter(adapter);
        timeTable.setOnItemClickListener((parent, view, position, id) -> {
            selectedItem[0] = position;
            selectedTimePosition = position;
            adapter.notifyDataSetChanged();
        });
        
        // TODO 타임 테이블 받아오기
        DatabaseReference timeTableRef = ref.child("time").child(String.valueOf(firebaseSelectedIndex)).child("time table");
        timeTableRef.get().addOnCompleteListener(task -> {

            times.clear();
            if (task.isSuccessful()) {
                for (DataSnapshot child: task.getResult().getChildren()) {
                    times.add(child.getValue(String.class));
                }
            }
            Log.d(TAG, "타임 테이블: " + times.toString());
            adapter.notifyDataSetChanged();

        });
    }

    // 스피너 초기화
    private void initSpinner(Spinner spinner, boolean isAdult) {
        Integer[] numOfPeople = { 0, 1, 2, 3, 4 };
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, numOfPeople);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 좌석이 선택된 후에 인원수를 바꾸면 좌석 초기화
                if (selectedSeatList != null) {
                    selectedSeatList = null;
                    TextView selectedSeatText = findViewById(R.id.selectedSeatText);
                    selectedSeatText.setText("-");
                }
                if (isAdult)
                    numOfAdult = numOfPeople[position];
                else
                    numOfChild = numOfPeople[position];

                // 경계선
                if (numOfAdult + numOfChild == 0)
                    border.setVisibility(View.GONE);
                else
                    border.setVisibility(View.VISIBLE);

                // 가격 계산
                StringBuffer buffer = new StringBuffer();
                if (numOfAdult != 0)
                    buffer.append("성인 ").append(numOfAdult).append("명 * 8000원");
                if (numOfChild != 0) {
                    if (numOfAdult != 0)
                        buffer.append("\n청소년 ").append(numOfChild).append("명 * 6000원");
                    else
                        buffer.append("청소년 ").append(numOfChild).append("명 * 6000원");
                }

                receiptCalculate.setText(buffer.toString());

                // 가격 결과
                paymentAmount = numOfAdult * 8000 + numOfChild * 6000;
                receiptResult.setText("총 " + paymentAmount + "원");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // 날짜 텍스트 뷰 초기화
    private void initDateText() {
        if (selectedDateText == null)
            selectedDateText = findViewById(R.id.selectedDateText);

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedDate = calendar.get(Calendar.DATE);

        today = selectedYear + "년 " + selectedMonth + "월 " + selectedDate + "일";

        for (int i = 0; i < dateList.size(); i++) {
            if (today.equals(dateList.get(i))) {
                firebaseTodayIndex = i;
                break;
            }
        }

        for (int i = firebaseTodayIndex - 1; i > 0; i--) {
            dateList.remove(i);
        }

        selectedDateText.setText(today);
    }

    // 좌석 선택 액티비티 실행
    public void selectSeat(View v) {
        if (numOfAdult + numOfChild == 0) {
            Toast.makeText(this, "좌석에 앉을 인원을 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, SeatActivity.class);
        intent.putExtra("numOfPeople", numOfAdult + numOfChild);
        if (selectedSeatList != null) {
            intent.putExtra("selectedSeatList", selectedSeatList);
        }
        else {
            intent.putExtra("selectedSeatList", new HashSet<>());
        }
        startActivityForResult(intent, SELECT_SEAT);
    }

    // 날짜선택 다이얼로그
    public void showDatePicker(View v) {
        if (datePicker == null) {
            datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("날짜선택")
                    .setCalendarConstraints(limitRange().build())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selection);
                selectedYear = calendar.get(Calendar.YEAR);
                selectedMonth = calendar.get(Calendar.MONTH) + 1;
                selectedDate = calendar.get(Calendar.DATE);

                String time = selectedYear + "년 " + selectedMonth + "월 " + selectedDate + "일";

                selectedDateText.setText(time);

                // 선택된 날짜 인덱스 찾기
                for (int i = 0; i < dateList.size(); i++) {
                    if (time.equals(dateList.get(i))) {
                        firebaseSelectedIndex = i;
                        break;
                    }
                }

                // TODO 상영시간표 초기화
                initTimeTable();
            });
        }
        datePicker.show(getSupportFragmentManager(), TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_SEAT && data != null) {

            selectedSeatList = (HashSet<Integer>) data.getSerializableExtra("selectedSeatList");
            ArrayList<Integer> temp = new ArrayList<>(selectedSeatList);
            Collections.sort(temp);

            StringBuffer buffer = new StringBuffer();
            int count = 0;
            for (int seat : temp) {
                buffer.append(seat);
                buffer.append("번");
                if (count != temp.size() - 1)
                    buffer.append(", ");
                count++;
            }
            String selectedSeatStr = buffer.toString();
            if (selectedSeatStr.equals(""))
                selectedSeatStr = "-";

            TextView selectedSeatText = findViewById(R.id.selectedSeatText);
            selectedSeatText.setText(selectedSeatStr);
        }
    }

    // 뒤로가기 버튼 누르면 취소 취급
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
        alBuilder.setMessage("영화 예매를 취소하시겠습니까?");

        alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alBuilder.setTitle("예매 취소");
        alBuilder.show();
    }

    private CalendarConstraints.Builder limitRange() {
        CalendarConstraints.Builder builder = new CalendarConstraints.Builder();
        Calendar start = GregorianCalendar.getInstance();
        Calendar end = GregorianCalendar.getInstance();

        int year = start.get(Calendar.YEAR);
        int month = start.get(Calendar.MONTH);
        int date = start.get(Calendar.DATE) - 1;
        start.set(year, month, date);
        end.set(year, month, date);
        end.add(Calendar.DATE, 14);

        long minDate = start.getTimeInMillis();
        long maxDate = end.getTimeInMillis();

        builder.setStart(minDate);
        builder.setEnd(maxDate);
        builder.setValidator(new RangeValidator(minDate, maxDate));

        return builder;
    }


    private class RangeValidator implements CalendarConstraints.DateValidator {

        private long minDate;
        private long maxDate;

        private Parcelable.Creator<RangeValidator> CREATOR = new Parcelable.Creator<RangeValidator>() {
            @Override
            public RangeValidator createFromParcel(Parcel source) {
                return new RangeValidator(source);
            }
            @Override
            public RangeValidator[] newArray(int size) {
                return new RangeValidator[size];
            }
        };

        public RangeValidator(long minDate, long maxDate) {
            this.minDate = minDate;
            this.maxDate = maxDate;
        }

        public RangeValidator(Parcel parcel) {
            this(parcel.readLong(), parcel.readLong());
        }

        @Override
        public boolean isValid(long date) {
            return !(minDate > date || maxDate < date);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }


}